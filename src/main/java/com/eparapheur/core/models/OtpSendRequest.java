package com.eparapheur.core.models;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class OtpSendRequest {
    @NotBlank(message = "L'email est requis")
    @Email(message = "L'email doit être valide")
    private String email;
    
    private String detail;
    
    private String phone;
    
    // Getters/Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}

