package com.rodel.rodel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import bftsmart.tom.ServiceProxy;

public class LedgerClientBFT {

    private final ServiceProxy proxy;

    public LedgerClientBFT(int clientId) {
        this.proxy = new ServiceProxy(clientId);
    }

    public Object sendOperation(Object operation) {
        try {
            //Serializing operation to byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(operation);
            oos.flush();
            byte[] request = bos.toByteArray();

            byte[] reply = proxy.invokeOrdered(request);

            //Desserializing operation
            ByteArrayInputStream bis = new ByteArrayInputStream(reply);
            ObjectInputStream ois = new ObjectInputStream(bis);
            return ois.readObject();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void close() {
        proxy.close();
    }
}

