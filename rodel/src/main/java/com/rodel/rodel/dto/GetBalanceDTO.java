package com.rodel.rodel.dto;

import java.io.Serializable;

public class GetBalanceDTO implements Serializable{
    public String accountId;

    public GetBalanceDTO() {}
    public GetBalanceDTO(String accountId) {
        this.accountId = accountId;
    }

    public String getAccountsId(){
        return this.accountId;
    }

}
