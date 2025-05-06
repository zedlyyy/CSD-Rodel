package com.rodel.rodel.signverification;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.springframework.stereotype.Service;

import com.rodel.rodel.dto.TransactionDTO;

@Service
public class TransactionVerification {

    public boolean isValid(TransactionDTO transaction) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException{
        
        String dataSignedToValidate = transaction.accountId + transaction.to + transaction.amount + transaction.timeStamp;

        byte[] publicKeyBytes = Base64.getDecoder().decode(transaction.publicKey);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        PublicKey publicKey = keyFactory.generatePublic(keySpec);
        
        Signature sig = Signature.getInstance("SHA256withECDSA");
        sig.initVerify(publicKey);
        sig.update(dataSignedToValidate.getBytes());

        byte[] dataSignedBytes = Base64.getDecoder().decode(transaction.dataSigned);
        return sig.verify(dataSignedBytes);
    }
}
