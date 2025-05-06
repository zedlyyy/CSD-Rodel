package com.rodel.rodel;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rodel.rodel.dto.CreateAccountDTO;
import com.rodel.rodel.dto.LoadMoneyDTO;
import com.rodel.rodel.dto.TransactionDTO;
import com.rodel.rodel.dto.GetBalanceDTO;
import com.rodel.rodel.dto.GetExtractDTO;
import com.rodel.rodel.dto.GetGlobalLedgerValueDTO;
import com.rodel.rodel.dto.GetLedgerDTO;
import com.rodel.rodel.dto.GetTotalValueDTO;
import com.rodel.rodel.objects.SignedResponse;
import com.rodel.rodel.objects.Transaction;
import com.rodel.rodel.signverification.CreateAccountVerification;
import com.rodel.rodel.signverification.GetBalanceVerification;
import com.rodel.rodel.signverification.GetExtractVerification;
import com.rodel.rodel.signverification.GetGlobalLedgerValueVerification;
import com.rodel.rodel.signverification.GetLedgerVerification;
import com.rodel.rodel.signverification.GetTotalValueVerification;
import com.rodel.rodel.signverification.LoadMoneyVerification;
import com.rodel.rodel.signverification.TransactionVerification;


@RestController
@RequestMapping("/endpoints")
public class LedgerEndpoints {
    
    //@Autowired
    //private LedgerLogic ledgerLogic;
    
    @Autowired
    private TransactionVerification trasactionVerifier;

    @Autowired
    private CreateAccountVerification createAccountVerifier;

    @Autowired
    private LoadMoneyVerification loadMoneyVerifier;

    @Autowired
    private GetBalanceVerification balanceVerifier;

    @Autowired
    private GetTotalValueVerification totalvalueVerifier;

    @Autowired
    private GetExtractVerification extractVerifier;

    @Autowired
    private GetGlobalLedgerValueVerification globalLedgerVerifier;

    @Autowired
    private GetLedgerVerification getLedgerVerifier;

    private final LedgerClientBFT bftClient = new LedgerClientBFT(0);

    @PostMapping("/createaccount")
    public ResponseEntity<String> createAccount(@RequestBody CreateAccountDTO createAccountDTO) throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException {        
        if (createAccountVerifier.isValid(createAccountDTO)){
            Object obj = bftClient.sendOperation(createAccountDTO);
            boolean success = (Boolean) obj;
            if(success){
                return ResponseEntity.ok("Operation 'CreateAccount' successeful!"); 
            }
        }

        return ResponseEntity.badRequest().body("Account already exists!");
    }

    @PostMapping("/loadmoney")
    public ResponseEntity<String> loadMoney(@RequestBody LoadMoneyDTO loadMoneyDTO) throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException {
        if (loadMoneyVerifier.isValid(loadMoneyDTO)){
            Object obj = bftClient.sendOperation(loadMoneyDTO);
            boolean success = (Boolean) obj;
            if(success){
                return ResponseEntity.ok("Operation 'loadMoney' successeful!");
            }
        }
        return ResponseEntity.badRequest().body("Account does not exist!");
    }

    @PostMapping("/sendtransaction")
    public ResponseEntity<String> sendTransaction(@RequestBody TransactionDTO transaction) throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException {
        if(trasactionVerifier.isValid(transaction)){
            Object obj = bftClient.sendOperation(transaction);
            boolean success = (Boolean) obj;
            if(success){
                return ResponseEntity.ok("Operation 'sendTranscation' successeful!");
            }
        }
        return ResponseEntity.badRequest().body("Error in transaction!");
    }

    @GetMapping("/getbalance")
    public ResponseEntity<?> getBalance(@RequestParam String accountId, String dataSigned, String publicKey, String timeStamp) throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException {
        if(!balanceVerifier.isValid(accountId, dataSigned, publicKey, timeStamp)){
            return ResponseEntity.badRequest().body("Error in transaction!");
        }

        GetBalanceDTO getBalanceDTO = new GetBalanceDTO(accountId);
        //SignedResponse signedResponse = ledgerLogic.getBalance(accountId);
        Object obj = bftClient.sendOperation(getBalanceDTO);

        JSONObject json = new JSONObject(obj.toString());

        return ResponseEntity.ok(json.toMap());
    }

    @GetMapping("/getextract")
    public ResponseEntity<?> getExtract(@RequestParam String accountId, String dataSigned, String publicKey, String timeStamp) throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException {

        if (!extractVerifier.isValid(accountId, dataSigned, publicKey, timeStamp)){

            return ResponseEntity.badRequest().body("Error in transaction!");
        }

        GetExtractDTO getExtractDTO = new GetExtractDTO(accountId);
        //SignedResponse signedResponse = ledgerLogic.getExtract(accountId);
        Object obj = bftClient.sendOperation(getExtractDTO);

        JSONObject json = new JSONObject(obj.toString());

        return ResponseEntity.ok(json.toMap());
    }

    @GetMapping("/gettotalvalue")
    public ResponseEntity<?> getTotalValue(@RequestParam List<String> accountsId, String accountId, String dataSigned, String publicKey, String timeStamp) throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException {
        if(!totalvalueVerifier.isValid(accountsId, accountId, dataSigned, publicKey, timeStamp)){
            return ResponseEntity.badRequest().body("Error in transaction!");
        } 

        GetTotalValueDTO getTotalValueDTO = new GetTotalValueDTO(accountsId);
        //SignedResponse signedResponse = ledgerLogic.getTotalValue(accountsId);
        Object obj = bftClient.sendOperation(getTotalValueDTO);

        JSONObject json = new JSONObject(obj.toString());

        return ResponseEntity.ok(json.toMap());
    }

    @GetMapping("/getgloballedgervalue")
    public ResponseEntity<?> getGlobalLedgerValue(@RequestParam String accountId, String dataSigned, String publicKey, String timeStamp) throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException {
        if(!globalLedgerVerifier.isValid(accountId, dataSigned, publicKey, timeStamp)){
            return ResponseEntity.badRequest().body("Error in transaction!");
        }

        GetGlobalLedgerValueDTO getGlobalLedgerValueDTO = new GetGlobalLedgerValueDTO(accountId);
        //SignedResponse signedResponse = ledgerLogic.getGlobalLedgerValue();
        Object obj = bftClient.sendOperation(getGlobalLedgerValueDTO);

        JSONObject json = new JSONObject(obj.toString());

        return ResponseEntity.ok(json.toMap());
    }

    @GetMapping("/getledger")
    public ResponseEntity<?> getLedger(@RequestParam String accountId, String dataSigned, String publicKey, String timeStamp) throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException {
        if(!getLedgerVerifier.isValid(accountId, dataSigned, publicKey, timeStamp)){
            return ResponseEntity.badRequest().body("Error in transaction!");
        }
       
        GetLedgerDTO getLedgerDTO = new GetLedgerDTO(accountId);
        //SignedResponse signedResponse = ledgerLogic.getLedger();
        Object obj = bftClient.sendOperation(getLedgerDTO);

        JSONObject json = new JSONObject(obj.toString());

        return ResponseEntity.ok(json.toMap());
    }

}









