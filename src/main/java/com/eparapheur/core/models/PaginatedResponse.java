package com.eparapheur.core.models;

import java.io.Serializable;
import java.util.List;

/**
 * DTO pour les réponses paginées au format demandé
 */
public class PaginatedResponse<T> implements Serializable {
    private int statusCode;
    private String statusMessage;
    private PaginatedData<T> data;

    public PaginatedResponse() {
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public PaginatedData<T> getData() {
        return data;
    }

    public void setData(PaginatedData<T> data) {
        this.data = data;
    }

    /**
     * Classe interne pour la structure data
     */
    public static class PaginatedData<T> implements Serializable {
        private Long total;
        private int pageSize;
        private int page;
        private List<T> items;

        public PaginatedData() {
        }

        public Long getTotal() {
            return total;
        }

        public void setTotal(Long total) {
            this.total = total;
        }

        public int getPageSize() {
            return pageSize;
        }

        public void setPageSize(int pageSize) {
            this.pageSize = pageSize;
        }

        public int getPage() {
            return page;
        }

        public void setPage(int page) {
            this.page = page;
        }

        public List<T> getItems() {
            return items;
        }

        public void setItems(List<T> items) {
            this.items = items;
        }
    }
}

