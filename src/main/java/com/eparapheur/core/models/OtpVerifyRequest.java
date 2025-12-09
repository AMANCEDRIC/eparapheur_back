package com.eparapheur.core.models;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class OtpVerifyRequest {
    
    @NotNull(message = "Le token est obligatoire")
    private String token;
    
    @NotNull(message = "Le code OTP est obligatoire")
    @Size(min = 6, max = 6, message = "Le code OTP doit contenir 6 chiffres")
    private String otp;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
}

