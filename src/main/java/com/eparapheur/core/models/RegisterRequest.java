package com.eparapheur.core.models;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public class RegisterRequest {
    @NotNull
    public String firstName;

    @NotNull
    public String lastName;

    @NotNull
    @Email
    public String email;

    public String phone;

    @NotNull
    public String password;

    public String gender; // "M" / "F" / autre

    public Long profileId;
    public @NotNull String getFirstName() {
        return firstName;
    }
    public void setFirstName(@NotNull String firstName) {
        this.firstName = firstName;
    }
    public @NotNull String getLastName() {
        return lastName;
    }
    public void setLastName(@NotNull String lastName) {
        this.lastName = lastName;}
    public @NotNull String getEmail() {
        return email;
    }
    public void setEmail(@NotNull String email) {
        this.email = email;
    }
    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
    public @NotNull String getPassword() {
        return password;
    }
    public void setPassword(@NotNull String password) {
        this.password = password;
    }


    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Long getProfileId() {
        return profileId;
    }

    public void setProfileId(Long profileId) {
        this.profileId = profileId;
    }
}
