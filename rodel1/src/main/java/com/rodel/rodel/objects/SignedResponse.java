package com.rodel.rodel.objects;

import java.io.Serializable;

public class SignedResponse implements Serializable{

    private String message;
    private String signedMessage;
    private String publicKey;
    
    public SignedResponse (String message, String signedMessage, String publicKey){
        this.message = message;
        this.signedMessage = signedMessage;
        this.publicKey = publicKey;
    }

    public void setMessage(String newMessage) {
        this.message = newMessage;
    }

    public String getMessage() {
        return this.message;
    }

    public void setSignedMessage(String newSignedMessage) {
        this.signedMessage = newSignedMessage;
    }

    public String getSignedMessage () {
        return this.signedMessage;
    }

    public void setPublicKey (String newPublicKey) {
        this.publicKey = newPublicKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

}
