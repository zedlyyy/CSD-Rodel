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

import com.rodel.rodel.dto.LoadMoneyDTO;

@Service
public class LoadMoneyVerification {

    public boolean isValid(LoadMoneyDTO loadMoneyDTO) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException{
        
        String dataSignedToValidate = loadMoneyDTO.accountId + loadMoneyDTO.amount + loadMoneyDTO.timeStamp;

        byte[] publicKeyBytes = Base64.getDecoder().decode(loadMoneyDTO.publicKey);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        PublicKey publicKey = keyFactory.generatePublic(keySpec);
        
        Signature sig = Signature.getInstance("SHA256withECDSA");
        sig.initVerify(publicKey);
        sig.update(dataSignedToValidate.getBytes());

        byte[] dataSignedBytes = Base64.getDecoder().decode(loadMoneyDTO.dataSigned);
        return sig.verify(dataSignedBytes);
    }
}
