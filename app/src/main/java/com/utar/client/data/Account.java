package com.utar.client.data;

public class Account{
    public static final String FIX_CLIENT = "client";

    private String name;
    private String email;
    private String balance;
    private String pin;
    private String role;

    private String password;


    public Account(){

    }

    public Account(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.balance = "0.00";
        this.role = FIX_CLIENT;
    }

    public String getPassword(){return password;}
    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getBalance() {
        return balance;
    }

    public String getPin(){
        if(pin == null){
            return "";
        }
        return pin;
    }

    public String getRole(){
        return this.role;
    }
}