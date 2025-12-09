package com.eparapheur.core.features;

import java.io.Serializable;

public class ApiResponse<T> implements Serializable {

    private int status_code;
    private String status_message;
    private T data;

    public ApiResponse() {
    }

    public ApiResponse(int status_code, String status_message, T data) {
        this.status_code = status_code;
        this.status_message = status_message;
        this.data = data;
    }

    public int getStatus_code() {
        return status_code;
    }

    public void setStatus_code(int status_code) {
        this.status_code = status_code;
    }

    public String getStatus_message() {
        return status_message;
    }

    public void setStatus_message(String status_message) {
        this.status_message = status_message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String toJson() {
        return "{status_code: " + status_code + ", status_message: " + status_message + ", data: " + data + "}";
    }

}
