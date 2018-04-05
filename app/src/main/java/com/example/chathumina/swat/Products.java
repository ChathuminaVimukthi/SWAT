package com.example.chathumina.swat;

public class Products {
    private String prodId;
    private String prodName;
    private String currentUser;

    public Products() {
    }

    public Products(String prodName, String currentUser) {
        this.prodName = prodName;
        this.currentUser = currentUser;
    }

    public Products(String prodId, String prodName, String currentUser) {
        this.prodId = prodId;
        this.prodName = prodName;
        this.currentUser = currentUser;
    }

    public String getProdId() {
        return prodId;
    }

    public String getProdName() {
        return prodName;
    }

    public String getCurrentUser() {
        return currentUser;
    }
}
