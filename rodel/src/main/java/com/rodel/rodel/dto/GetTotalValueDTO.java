package com.rodel.rodel.dto;

import java.util.List;
import java.io.Serializable;

public class GetTotalValueDTO implements Serializable {
    public List<String> accountsId;

    public GetTotalValueDTO() {}

    public GetTotalValueDTO(List<String> accountsId) {
        this.accountsId = accountsId;
    }

    public List<String> getAccountsId(){
        return this.accountsId;
    }
}
