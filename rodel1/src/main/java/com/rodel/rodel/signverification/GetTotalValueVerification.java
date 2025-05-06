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
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class GetTotalValueVerification {
public boolean isValid(List<String> accountsId, String accountId, String dataSigned, String publicKey, String timeStamp) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException{
        
        String dataSignedToValidate = accountId + String.join("", accountsId) + timeStamp;

        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKey);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        PublicKey realPublicKey = keyFactory.generatePublic(keySpec);
        
        Signature sig = Signature.getInstance("SHA256withECDSA");
        sig.initVerify(realPublicKey);
        sig.update(dataSignedToValidate.getBytes());

        byte[] dataSignedBytes = Base64.getDecoder().decode(dataSigned);
        return sig.verify(dataSignedBytes);
    }
}
