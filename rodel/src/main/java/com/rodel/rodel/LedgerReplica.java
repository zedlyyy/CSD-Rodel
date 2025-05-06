package com.rodel.rodel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;

import com.rodel.rodel.dto.CreateAccountDTO;
import com.rodel.rodel.dto.GetBalanceDTO;
import com.rodel.rodel.dto.GetExtractDTO;
import com.rodel.rodel.dto.GetGlobalLedgerValueDTO;
import com.rodel.rodel.dto.GetLedgerDTO;
import com.rodel.rodel.dto.GetTotalValueDTO;
import com.rodel.rodel.dto.LoadMoneyDTO;
import com.rodel.rodel.dto.TransactionDTO;
import com.rodel.rodel.util.SpringContext;

import bftsmart.tom.MessageContext;
import bftsmart.tom.server.SingleExecutable;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;
import bftsmart.tom.ServiceReplica;

public class LedgerReplica extends DefaultSingleRecoverable  {

    private LedgerLogic ledgerLogic;

    public LedgerReplica() {
            this.ledgerLogic = SpringContext.getBean(LedgerLogic.class);
            new ServiceReplica(0, this, this);
    }

    @Override
    public byte[] appExecuteOrdered(byte[] command, MessageContext msgCtx) {
        try {
            // Desserializing operation
            ByteArrayInputStream bais = new ByteArrayInputStream(command);
            ObjectInputStream ois = new ObjectInputStream(bais);
            Object operation = ois.readObject();

            Object result = null;

            if (operation instanceof CreateAccountDTO dto) {
                result = ledgerLogic.createAccount(dto.accountId); 
            } else if (operation instanceof GetBalanceDTO dto) {
                result = ledgerLogic.getBalance(dto.accountId); 
            } else if (operation instanceof GetExtractDTO dto) {
                result = ledgerLogic.getExtract(dto.accountId); 
            } else if (operation instanceof GetGlobalLedgerValueDTO dto) {
                result = ledgerLogic.getGlobalLedgerValue(); 
            } else if (operation instanceof GetLedgerDTO dto) {
                result = ledgerLogic.getLedger(); 
            } else if (operation instanceof GetTotalValueDTO dto) {
                result = ledgerLogic.getTotalValue(dto.accountsId); 
            } else if (operation instanceof LoadMoneyDTO dto) {
                result = ledgerLogic.loadMoney(dto.accountId, dto.amount); 
            } else if (operation instanceof TransactionDTO dto) {
                result = ledgerLogic.sendTransaction(dto.accountId, dto.to, dto.amount); 
            } else {
                System.err.println("Unknown operation!!");
                result = "Invalid operation!!";
            }

            // Serializing the response
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(result);
            oos.flush();

            return baos.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
            return "Error".getBytes();
        }
    }

    public static void main(String[] args) {
        if ( args.length != 1 ){
            System.out.println("DO: java LedgerReplica [id]");
            System.exit(0);
        }

        int replicaId = Integer.parseInt(args[0]);
        //new ServiceReplica(replicaId, new LedgerReplica(), new LedgerReplica() );
    }

    @Override
    public void installSnapshot(byte[] state) {
        System.out.println("Not implemented");
    }

    @Override
    public byte[] getSnapshot() {
        return new byte[0];
    }

    @Override
    public byte[] appExecuteUnordered(byte[] command, MessageContext msgCtx) {
        return new byte[0];
    }

}