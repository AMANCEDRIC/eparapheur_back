package com.eparapheur.core;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class Pagination <T> implements Serializable {
    private Long total;
    private int total_of_pages;
    private Long per_page;
    private String[] sorts;
    private String order;
    private int current_page;
    private int next_page;
    private int previous_page;
    private Map<String,String> filters;
    private List<T> items;

    public Pagination() {
    }

    public Pagination(Long total, int total_of_pages, Long per_page, int current_page, int next_page, int previous_page, List<T> items) {
        this.total = total;
        this.total_of_pages = total_of_pages;
        this.per_page = per_page;
        this.current_page = current_page;
        this.next_page = next_page;
        this.previous_page = previous_page;
        this.items = items;
    }

    public int getTotal_of_pages() {
        return total_of_pages;
    }

    public void setTotal_of_pages(int total_of_pages) {
        this.total_of_pages = total_of_pages;
    }

    public Long getPer_page() {
        return per_page;
    }

    public void setPer_page(Long per_page) {
        this.per_page = per_page;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public String[] getSorts() {
        return sorts;
    }

    public void setSorts(String[] sorts) {
        this.sorts = sorts;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public int getCurrent_page() {
        return current_page;
    }

    public void setCurrent_page(int current_page) {
        this.current_page = current_page;
    }

    public int getNext_page() {
        return next_page;
    }

    public void setNext_page(int next_page) {
        this.next_page = next_page;
    }

    public int getPrevious_page() {
        return previous_page;
    }

    public void setPrevious_page(int previous_page) {
        this.previous_page = previous_page;
    }

    public Map<String, String> getFilters() {
        return filters;
    }

    public void setFilters(Map<String, String> filters) {
        this.filters = filters;
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }
}
