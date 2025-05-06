package com.rodel.rodel.dto;

import java.io.Serializable;

public class GetExtractDTO implements Serializable {
    public String accountId;


    public GetExtractDTO() {}
    public GetExtractDTO(String accountId) {
        this.accountId = accountId;
    }

    public String getAccountsId(){
        return this.accountId;
    }

}
