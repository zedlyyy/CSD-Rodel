package com.rodel.rodel.util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;

import org.springframework.stereotype.Component;

@Component
public class RestKeys {

    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    static Charset charset = StandardCharsets.UTF_8;

    public RestKeys() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("EC");

        ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256r1");
        keyGenerator.initialize(ecSpec);

        KeyPair pair = keyGenerator.generateKeyPair();

        this.privateKey = pair.getPrivate();
        this.publicKey = pair.getPublic();
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public String getPublicKeyBase64() {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    public Charset getCharset() {
        return charset;
    }
}
