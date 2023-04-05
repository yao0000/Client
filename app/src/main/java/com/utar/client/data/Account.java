package com.utar.client.data;

public class Account{
    private String name;
    private String email;
    private String balance;


    public Account(){

    }

    public Account(String name, String email) {
        this.name = name;
        this.email = email;
        this.balance = "0.00";

    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getBalance() {
        return balance;
    }
}