package com.rodel.rodel;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rodel.rodel.database.CrudRepositoryAccounts;
import com.rodel.rodel.database.CrudRepositoryTransactions;
import com.rodel.rodel.objects.Account;
import com.rodel.rodel.objects.Transaction;


@Service
public class LedgerLogic {


    @Autowired
    private CrudRepositoryAccounts userAccounts;

    @Autowired
    private CrudRepositoryTransactions userTransactions;


    public LedgerLogic() {}

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

    public String getBalance(String userId) {
        Optional<Account> userAccount = userAccounts.findById(userId);
        if (userAccount.isEmpty()) {
            return null;
        }

        double balance = 0.0;

        for(Transaction t : userTransactions.findAll()){

            if(t.getFrom() != null && t.getFrom().equals(userId)){
                balance -= t.getAmount();
            }
            if(t.getTo().equals(userId)) {
                balance += t.getAmount();
            }
        }

        return Double.toString(balance);
    }

    public String getExtract(String userId) {
        StringBuilder dataToSignBuilder = new StringBuilder();
        
        for(Transaction t : userTransactions.findAll()){
            if((t.getFrom() != null && t.getFrom().equals(userId)) || t.getTo().equals(userId)){

                dataToSignBuilder.append(t.getId()).append("|")
                .append(t.getFrom()).append("|")
                .append(t.getTo()).append("|")
                .append(t.getAmount()).append("|")
                .append(t.getTimeStamp()).append("\n");

            }
        }

        return dataToSignBuilder.toString();
    }

    public String getTotalValue(List<String> userIds) {
        double totalAmount = 0.0;
        
        for(String s : userIds){
            Optional<Account> userAccount = userAccounts.findById(s);
            if(userAccount.isPresent()){
                totalAmount += userAccount.get().getBalance();
            }
        }

        return Double.toString(totalAmount);
    }

    public String getGlobalLedgerValue() {
        double totalAmount = 0.0;

        for(Account a : userAccounts.findAll()){
            totalAmount += a.getBalance();
        }

        return Double.toString(totalAmount);
    }

    public String getLedger() {

        StringBuilder dataToSignBuilder = new StringBuilder();
        
        for(Transaction t : userTransactions.findAll()){

            dataToSignBuilder.append(t.getId()).append("|")
            .append(t.getFrom()).append("|")
            .append(t.getTo()).append("|")
            .append(t.getAmount()).append("|")
            .append(t.getTimeStamp()).append("\n");

        }

        return dataToSignBuilder.toString();
    }

}
