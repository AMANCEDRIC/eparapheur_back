# Integration Angular - Signature (etapes 5 et 6)

Ce document explique comment integrer dans le front Angular:
- la preparation des elements de signature (visuel + certificat)
- l'execution de la signature avec OTP

Il est base sur les endpoints backend existants.

---

## 1. Prerequis fonctionnels

- L'utilisateur est authentifie et son `accountId` est connu par le front.
- Le front dispose du `participantId` et `documentId` a signer (recuperes depuis le workflow programme/etapes).
- Le backend est joignable via `environment.apiUrl`.

---

## 2. Endpoints utilises

### 2.1 Visuels de signature

- `POST /signature-visuals/upload`
- `GET /signature-visuals/account/{accountId}`

### 2.2 Certificats

- `POST /certificates/upload`
- `GET /certificates/account/{accountId}`

### 2.3 OTP de signature

- `POST /accounts/otp/send/EMAIL/SIGNATURE`

### 2.4 Execution signature

- `POST /signatures/execute`

---

## 3. Contrats TypeScript recommandes

```ts
export interface ApiResponse<T> {
  status_code: number;
  status_message: string;
  data: T;
}

export interface SignatureVisual {
  id: number;
  idAccount: number;
  label: string;
  visualType: 'drawn' | 'uploaded' | 'typed';
  visualPath?: string;
  active: boolean;
  default: boolean;
}

export interface UserCertificate {
  id: number;
  idAccount: number;
  // selon l'entite backend, d'autres champs peuvent exister
}

export interface ExecuteSignaturePayload {
  participantId: number;
  documentId: number;
  otp: string;
  visualId?: number;
  certificateId?: number;
  x?: number;
  y?: number;
  page?: number;
}
```

---

## 4. Service Angular (HttpClient)

Creer `src/app/core/api/signature-api.service.ts`.

```ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from 'src/environments/environment';
import { ApiResponse, ExecuteSignaturePayload, SignatureVisual, UserCertificate } from './signature.models';

@Injectable({ providedIn: 'root' })
export class SignatureApiService {
  private readonly baseUrl = environment.apiUrl;

  constructor(private readonly http: HttpClient) {}

  uploadVisual(payload: {
    accountId: number;
    image: string; // base64 sans prefixe data:image/...;base64,
    label?: string;
    type?: 'drawn' | 'uploaded' | 'typed';
  }) {
    return this.http.post<ApiResponse<SignatureVisual>>(
      `${this.baseUrl}/signature-visuals/upload`,
      payload
    );
  }

  listVisuals(accountId: number) {
    return this.http.get<ApiResponse<SignatureVisual[]>>(
      `${this.baseUrl}/signature-visuals/account/${accountId}`
    );
  }

  uploadCertificate(payload: { accountId: number; certificatePem: string }) {
    return this.http.post<ApiResponse<UserCertificate>>(
      `${this.baseUrl}/certificates/upload`,
      payload
    );
  }

  listCertificates(accountId: number) {
    return this.http.get<ApiResponse<UserCertificate[]>>(
      `${this.baseUrl}/certificates/account/${accountId}`
    );
  }

  sendSignatureOtp(email: string) {
    return this.http.post<ApiResponse<null>>(
      `${this.baseUrl}/accounts/otp/send/EMAIL/SIGNATURE`,
      { email }
    );
  }

  executeSignature(payload: ExecuteSignaturePayload) {
    return this.http.post<ApiResponse<any>>(
      `${this.baseUrl}/signatures/execute`,
      payload
    );
  }
}
```

---

## 5. Preparation des donnees (Base64 image, PEM certificat)

### 5.1 Convertir une image en Base64

```ts
export function fileToBase64(file: File): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => {
      const result = String(reader.result ?? '');
      // retire le prefixe "data:image/png;base64,"
      const base64 = result.includes(',') ? result.split(',')[1] : result;
      resolve(base64);
    };
    reader.onerror = reject;
    reader.readAsDataURL(file);
  });
}
```

### 5.2 Lire un certificat PEM

```ts
export function readTextFile(file: File): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => resolve(String(reader.result ?? ''));
    reader.onerror = reject;
    reader.readAsText(file, 'utf-8');
  });
}
```

---

## 6. Ecran Angular recommande (Reactive Forms)

Objectif: un composant unique "Signer un document" avec 3 zones:
- selection/upload visuel
- selection/upload certificat
- OTP + position + validation signature

### 6.1 Structure de formulaire

```ts
import { FormBuilder, Validators } from '@angular/forms';

form = this.fb.group({
  visualId: [null as number | null],
  certificateId: [null as number | null],
  otp: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(6)]],
  page: [1, [Validators.required, Validators.min(1)]],
  x: [100, [Validators.required, Validators.min(0)]],
  y: [100, [Validators.required, Validators.min(0)]],
});

constructor(private fb: FormBuilder) {}
```

### 6.2 Flux de chargement initial

Au `ngOnInit`:
- charger les visuels utilisateur
- charger les certificats utilisateur
- preselectionner le visuel/certificat par defaut si besoin

---

## 7. Coordination PDF (x, y, page)

Le backend attend:
- `page` commence a 1
- `x`, `y` en coordonnees PDF

### 7.1 Recommandation UI

- Afficher un preview PDF (ex: `ngx-extended-pdf-viewer` ou `pdfjs-dist`).
- Laisser l'utilisateur cliquer pour positionner la signature.
- Convertir coordonnees ecran -> coordonnees PDF.

### 7.2 Conversion simplifiee (a adapter au viewer choisi)

```ts
interface PdfPoint {
  x: number;
  y: number;
}

function viewportToPdfPoint(
  clickX: number,
  clickY: number,
  viewportWidth: number,
  viewportHeight: number,
  pdfPageWidth: number,
  pdfPageHeight: number
): PdfPoint {
  const xRatio = pdfPageWidth / viewportWidth;
  const yRatio = pdfPageHeight / viewportHeight;

  // Y PDF est souvent inverse par rapport au DOM
  const x = clickX * xRatio;
  const y = (viewportHeight - clickY) * yRatio;

  return { x, y };
}
```

Important: valider les coordonnees reelles selon le composant PDF choisi.

---

## 8. Exemple de composant Angular (orchestration)

```ts
import { Component, Input, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { finalize } from 'rxjs/operators';
import { SignatureApiService } from './signature-api.service';

@Component({
  selector: 'app-sign-document',
  templateUrl: './sign-document.component.html',
})
export class SignDocumentComponent implements OnInit {
  @Input() accountId!: number;
  @Input() accountEmail!: string;
  @Input() participantId!: number;
  @Input() documentId!: number;

  loading = false;
  sendingOtp = false;
  visuals: any[] = [];
  certificates: any[] = [];
  errorMessage = '';
  successMessage = '';

  form = this.fb.group({
    visualId: [null as number | null],
    certificateId: [null as number | null],
    otp: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(6)]],
    page: [1, [Validators.required, Validators.min(1)]],
    x: [100, [Validators.required, Validators.min(0)]],
    y: [100, [Validators.required, Validators.min(0)]],
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly signatureApi: SignatureApiService
  ) {}

  ngOnInit(): void {
    this.refreshAssets();
  }

  refreshAssets(): void {
    this.signatureApi.listVisuals(this.accountId).subscribe({
      next: (res) => (this.visuals = res.data ?? []),
      error: () => (this.visuals = []),
    });

    this.signatureApi.listCertificates(this.accountId).subscribe({
      next: (res) => (this.certificates = res.data ?? []),
      error: () => (this.certificates = []),
    });
  }

  sendOtp(): void {
    this.errorMessage = '';
    this.successMessage = '';
    this.sendingOtp = true;

    this.signatureApi
      .sendSignatureOtp(this.accountEmail)
      .pipe(finalize(() => (this.sendingOtp = false)))
      .subscribe({
        next: (res) => (this.successMessage = res.status_message || 'OTP envoye.'),
        error: (err) => (this.errorMessage = err?.error?.status_message || 'Echec envoi OTP.'),
      });
  }

  submit(): void {
    this.errorMessage = '';
    this.successMessage = '';
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;
    const v = this.form.value;

    this.signatureApi
      .executeSignature({
        participantId: this.participantId,
        documentId: this.documentId,
        otp: String(v.otp),
        visualId: v.visualId ?? undefined,
        certificateId: v.certificateId ?? undefined,
        page: Number(v.page),
        x: Number(v.x),
        y: Number(v.y),
      })
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (res) => {
          this.successMessage = res.status_message || 'Signature effectuee.';
          // TODO: refresh statut participant/programme
        },
        error: (err) => {
          this.errorMessage = err?.error?.status_message || 'Echec de la signature.';
        },
      });
  }
}
```

---

## 9. Exemple de template HTML minimal

```html
<button type="button" (click)="sendOtp()" [disabled]="sendingOtp">
  {{ sendingOtp ? 'Envoi OTP...' : 'Recevoir OTP' }}
</button>

<form [formGroup]="form" (ngSubmit)="submit()">
  <label>Visuel</label>
  <select formControlName="visualId">
    <option [ngValue]="null">Aucun</option>
    <option *ngFor="let v of visuals" [ngValue]="v.id">{{ v.label }} ({{ v.visualType }})</option>
  </select>

  <label>Certificat</label>
  <select formControlName="certificateId">
    <option [ngValue]="null">Aucun</option>
    <option *ngFor="let c of certificates" [ngValue]="c.id">Certificat #{{ c.id }}</option>
  </select>

  <label>OTP</label>
  <input type="text" formControlName="otp" maxlength="6" />

  <label>Page</label>
  <input type="number" formControlName="page" min="1" />

  <label>X</label>
  <input type="number" formControlName="x" min="0" />

  <label>Y</label>
  <input type="number" formControlName="y" min="0" />

  <button type="submit" [disabled]="loading">
    {{ loading ? 'Signature...' : 'Signer le document' }}
  </button>
</form>

<p *ngIf="successMessage" style="color: green">{{ successMessage }}</p>
<p *ngIf="errorMessage" style="color: red">{{ errorMessage }}</p>
```

---

## 10. Gestion d'erreurs et UX

Mapper les statuts backend:
- `400`: champs invalides
- `401`: OTP invalide/expire
- `404`: participant/document/compte introuvable
- `500`: erreur interne

Bonnes pratiques UX:
- bouton OTP desactive pendant envoi
- timer de renvoi OTP (ex: 30 a 60 sec)
- validation stricte OTP (6 chiffres)
- messages d'erreur lisibles
- refresh automatique des statuts apres signature

---

## 11. Sequence front recommandee (resume)

1. Charger visuels et certificats utilisateur.
2. Permettre upload visuel/certificat si besoin.
3. Positionner signature sur PDF (x, y, page).
4. Envoyer OTP signature.
5. Saisir OTP et appeler `/signatures/execute`.
6. Afficher succes et rafraichir le workflow.

---

## 12. Check-list de recette front

- Upload visuel OK et listage OK.
- Upload certificat OK et listage OK.
- OTP SIGNATURE recu par email.
- Signature refusee si OTP faux/expire.
- Signature acceptee avec OTP valide.
- Statut participant passe a `COMPLETED`.
- Audit trace cote backend.

