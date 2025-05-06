package com.rodel.rodel.dto;

import java.io.Serializable;

public class CreateAccountDTO implements Serializable {

    public String accountId;
    public String dataSigned;
    public String publicKey;
    public String timeStamp;

    public CreateAccountDTO() {}
}
