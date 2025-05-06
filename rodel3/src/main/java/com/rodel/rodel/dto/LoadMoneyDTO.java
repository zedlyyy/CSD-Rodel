package com.rodel.rodel.dto;

import java.io.Serializable;

public class LoadMoneyDTO implements Serializable{
    public String accountId;
    public double amount;
    public String dataSigned;
    public String publicKey;
    public String timeStamp;

    public LoadMoneyDTO(){ }
}
