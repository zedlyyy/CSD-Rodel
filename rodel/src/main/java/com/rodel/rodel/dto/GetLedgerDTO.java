package com.rodel.rodel.dto;

import java.io.Serializable;

public class GetLedgerDTO implements Serializable{
    public String accountId;


    public GetLedgerDTO() {}
    public GetLedgerDTO(String accountId) {
        this.accountId = accountId;
    }

    public String getAccountsId(){
        return this.accountId;
    }

}
