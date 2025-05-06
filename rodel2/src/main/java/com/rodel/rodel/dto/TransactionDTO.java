package com.rodel.rodel.dto;

import java.io.Serializable;

public class TransactionDTO implements Serializable{
    public String accountId;
    public String to;
    public double amount;
    public String dataSigned;
    public String publicKey;
    public String timeStamp;

    public TransactionDTO() {}
}
