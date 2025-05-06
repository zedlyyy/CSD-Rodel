package com.rodel.rodel.objects;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

//Basic Account
@Entity
public class Account {

    @Id
    private String clientId;
    private double balance;

    public Account () {}    
    
    public Account(String clientId){
        this.clientId   = clientId;
        this.balance    = 0.0;
    }

    public void setId(String id) {
        this.clientId = id;
    }
    
    public String getId(){
        return clientId;
    }

    public double getBalance(){
        return balance;
    }

    public void changeBalance(double balance) {
        this.balance = balance;
    }



}
