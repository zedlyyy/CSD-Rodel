package com.rodel.rodel;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rodel.rodel.database.CrudRepositoryAccounts;
import com.rodel.rodel.database.CrudRepositoryTransactions;
import com.rodel.rodel.objects.Account;
import com.rodel.rodel.objects.SignedResponse;
import com.rodel.rodel.objects.Transaction;




import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.ECGenParameterSpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Scanner;
import java.net.URL;
import java.net.URLEncoder;

@Service
public class LedgerLogic {

    private String info = "This is the ledger !";

    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private final String publicKeyBase64;
    static Charset charset = StandardCharsets.UTF_8;

    @Autowired
    private CrudRepositoryAccounts userAccounts;

    @Autowired
    private CrudRepositoryTransactions userTransactions;


    public LedgerLogic() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("EC");

        ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256r1");
        keyGenerator.initialize(ecSpec);

        KeyPair clientKeyPair = keyGenerator.generateKeyPair();

        this.privateKey = clientKeyPair.getPrivate();
        this.publicKey = clientKeyPair.getPublic();

        this.publicKeyBase64 = Base64.getEncoder().encodeToString(publicKey.getEncoded());


    }

    public boolean createAccount(String userId){
        if(userAccounts.existsById(userId)){
            return false;
        }
        Account newAccount = new Account(userId); 
        userAccounts.save(newAccount);

        Transaction newTransaction = new Transaction(null, userId, 0);
        userTransactions.save(newTransaction);

        return true;
    }

    public boolean loadMoney(String userId, double amount){
        Optional<Account> userAccount = userAccounts.findById(userId);
        if (userAccount.isEmpty()) {
            return false;
        }

        Account accountObj = userAccount.get();
        accountObj.changeBalance(accountObj.getBalance() + amount);
        userAccounts.save(accountObj);

        Transaction newTransaction = new Transaction(null, userId, amount);
        userTransactions.save(newTransaction);

        return true;
    }

    public boolean sendTransaction(String origId, String destId, double amount){
        Optional<Account> userAccountOrig = userAccounts.findById(origId);
        Optional<Account> userAccountDest = userAccounts.findById(destId);

        if(userAccountOrig.isEmpty() || userAccountDest.isEmpty() || userAccountOrig.get().getBalance() < amount ) {
            return false;
        }
        Account userAccountOrigObj = userAccountOrig.get();
        Account userAccountDestObj = userAccountDest.get();

        userAccountOrigObj.changeBalance(userAccountOrigObj.getBalance() - amount);
        userAccountDestObj.changeBalance(userAccountDestObj.getBalance() + amount);

        Transaction newTransaction = new Transaction(origId, destId, amount);
        userTransactions.save(newTransaction);

        userAccounts.save(userAccountOrigObj);
        userAccounts.save(userAccountDestObj);

        return true;
    }

    public SignedResponse getBalance(String userId) throws InvalidKeyException, NoSuchAlgorithmException, SignatureException{
        Optional<Account> userAccount = userAccounts.findById(userId);
        if (userAccount.isEmpty()) {
            return null;
        }

        String dataExempleToSign = Double.toString(userAccount.get().getBalance());
        Signature signer = Signature.getInstance("SHA256withECDSA");
        signer.initSign(this.privateKey);
        signer.update(dataExempleToSign.getBytes(charset));
        byte[] dataSignedBytes = signer.sign();
        String dataSignedBytesBase64 = Base64.getEncoder().encodeToString(dataSignedBytes);

        return new SignedResponse(dataExempleToSign, dataSignedBytesBase64, this.publicKeyBase64);
    }

    public SignedResponse getExtract(String userId) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException{
        //ArrayList<Transaction> filteredUsersTransactions = new ArrayList<>();
        StringBuilder dataExempleToSignBuilder = new StringBuilder();
        
        for(Transaction t : userTransactions.findAll()){
            if((t.getFrom() != null && t.getFrom().equals(userId)) || t.getTo().equals(userId)){
                //filteredUsersTransactions.add(t);

                dataExempleToSignBuilder.append(t.getId()).append("|")
                .append(t.getFrom()).append("|")
                .append(t.getTo()).append("|")
                .append(t.getAmount()).append("|")
                .append(t.getTimeStamp()).append("\n");

            }
        }

        String dataExempleToSign = dataExempleToSignBuilder.toString();
        Signature signer = Signature.getInstance("SHA256withECDSA");
        signer.initSign(this.privateKey);
        signer.update(dataExempleToSign.getBytes(charset));
        byte[] dataSignedBytes = signer.sign();
        String dataSignedBytesBase64 = Base64.getEncoder().encodeToString(dataSignedBytes);

        return new SignedResponse(dataExempleToSign, dataSignedBytesBase64, this.publicKeyBase64);
    }

    public SignedResponse getTotalValue(List<String> userIds) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        double totalAmount = 0.0;
        
        for(String s : userIds){
            Optional<Account> userAccount = userAccounts.findById(s);
            if(userAccount.isPresent()){
                totalAmount += userAccount.get().getBalance();
            }
        }

        String dataExempleToSign = Double.toString(totalAmount);
        Signature signer = Signature.getInstance("SHA256withECDSA");
        signer.initSign(this.privateKey);
        signer.update(dataExempleToSign.getBytes(charset));
        byte[] dataSignedBytes = signer.sign();
        String dataSignedBytesBase64 = Base64.getEncoder().encodeToString(dataSignedBytes);

        return new SignedResponse(dataExempleToSign, dataSignedBytesBase64, this.publicKeyBase64);
    }

    public SignedResponse getGlobalLedgerValue() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        double totalAmount = 0.0;

        for(Account a : userAccounts.findAll()){
            totalAmount += a.getBalance();
        }

        String dataExempleToSign = Double.toString(totalAmount);
        Signature signer = Signature.getInstance("SHA256withECDSA");
        signer.initSign(this.privateKey);
        signer.update(dataExempleToSign.getBytes(charset));
        byte[] dataSignedBytes = signer.sign();
        String dataSignedBytesBase64 = Base64.getEncoder().encodeToString(dataSignedBytes);

        return new SignedResponse(dataExempleToSign, dataSignedBytesBase64, this.publicKeyBase64);
    }

    public SignedResponse getLedger() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {

        StringBuilder dataExempleToSignBuilder = new StringBuilder();
        
        for(Transaction t : userTransactions.findAll()){

            dataExempleToSignBuilder.append(t.getId()).append("|")
            .append(t.getFrom()).append("|")
            .append(t.getTo()).append("|")
            .append(t.getAmount()).append("|")
            .append(t.getTimeStamp()).append("\n");

        }

        String dataExempleToSign = dataExempleToSignBuilder.toString();
        Signature signer = Signature.getInstance("SHA256withECDSA");
        signer.initSign(this.privateKey);
        signer.update(dataExempleToSign.getBytes(charset));
        byte[] dataSignedBytes = signer.sign();
        String dataSignedBytesBase64 = Base64.getEncoder().encodeToString(dataSignedBytes);

        return new SignedResponse(dataExempleToSign, dataSignedBytesBase64, this.publicKeyBase64);
    }

    public String getInfo() {
        return info;
    }


}
