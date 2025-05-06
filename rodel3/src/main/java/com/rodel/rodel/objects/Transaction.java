package com.rodel.rodel.objects;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Transaction {

    //Transaction Id creation - Creates and id based on incremention
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String fromUser;
    private String toUser;
    private double amount;
    private LocalDateTime timeStamp;

    public Transaction () {}

    public Transaction( String fromUser, String toUser, double amount){
        this.fromUser       = fromUser;
        this.toUser         = toUser;
        this.amount     = amount;
        this.timeStamp  = LocalDateTime.now();
    }


    public Long getId() { 
        return id; 
    }

    public String getFrom() {
        return fromUser;
    }

    public void setFrom(String fromUser) {
        this.fromUser = fromUser;
    }
    
    public String getTo() {
        return toUser;
    }

    public void setTo(String toUser) {
        this.toUser = toUser;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDateTime getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(LocalDateTime timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getTrasanctionInfo() {
        return String.format("Transaction made from %s to %s of %.2f$", fromUser, toUser, amount);
    }

}
