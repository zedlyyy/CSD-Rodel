package com.rodel.rodel.dto;

import java.io.Serializable;

public class GetGlobalLedgerValueDTO implements Serializable {
    public String accountId;


    public GetGlobalLedgerValueDTO() {}
    public GetGlobalLedgerValueDTO(String accountId) {
        this.accountId = accountId;

    }

    public String getAccountsId(){
        return this.accountId;
    }
}
