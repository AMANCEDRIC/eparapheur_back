package com.eparapheur.core.models;

import jakarta.validation.constraints.NotNull;

public class AuthRequest {
    @NotNull
    public String login;
    @NotNull
    public String password;

    public Boolean rememberMe;

    public @NotNull String getLogin() {
        return login;
    }

    public void setLogin(@NotNull String login) {
        this.login = login;
    }

    public @NotNull String getPassword() {
        return password;
    }

    public void setPassword(@NotNull String password) {
        this.password = password;
    }

    public Boolean getRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(Boolean rememberMe) {
        this.rememberMe = rememberMe;
    }
}
