import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Scanner;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.*;
import org.json.*;


import objects.SignedResponse;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class Client {

    static Charset charset = StandardCharsets.UTF_8;

    public static void main(String[] args) throws Exception {

        //Function to be able to access the https domain 
        disableCertificateValidation();
        Scanner scanner = new Scanner(System.in);
         
        if(args.length < 3) {
            System.out.println("Error - Type in an email, secretKey and a operation");
            return;
        }

        String clientEmail = args[0];
        String secretKey = args[1];
        String operation = args[2];
        

        /*String clientEmail = "email";
        String secretKey = "sporting";
        String operation = "loadmoney";
        */

        // Public/Private Keys creation
        KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("EC");

        ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256r1");
        keyGenerator.initialize(ecSpec);

        KeyPair clientKeyPair = keyGenerator.generateKeyPair();

        PrivateKey privateKey = clientKeyPair.getPrivate();
        PublicKey publicKey = clientKeyPair.getPublic();

        //Strings of each key
        String privateKeyBase64 = Base64.getEncoder().encodeToString(privateKey.getEncoded());
        String publicKeyBase64 = Base64.getEncoder().encodeToString(publicKey.getEncoded());

        String timestamp = Instant.now().toString();

        //UserId generation
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] emailHash = digest.digest(clientEmail.getBytes());
        String emailHashBase64 = Base64.getEncoder().encodeToString(emailHash);
        String userId = emailHashBase64 + publicKeyBase64;

        //AccountId generation
        String accountId = userId + timestamp;

        //HMAC
        byte[] secretKeyBytes = secretKey.getBytes(charset);

        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKeyBytes, "HmacSHA256");
        mac.init(secretKeySpec);

        byte[] hmacBytes = mac.doFinal(accountId.getBytes(charset));

        String accountIdEncryptedBase64 = Base64.getEncoder().encodeToString(hmacBytes);
        createAccountCall(accountIdEncryptedBase64, privateKey, publicKeyBase64);
        System.out.println("Account created with ID: " + accountIdEncryptedBase64);

        while (true) {
            switch(operation){
            case "loadmoney":
                System.out.println("Please type in the amount to load into the account!");
                System.out.print(">");
                double amount = scanner.nextDouble();
                scanner.nextLine();
                loadMoneyCall(accountIdEncryptedBase64, amount, privateKey, publicKeyBase64);
            break;
            
            case "sendtransaction":
                System.out.println("Please type in the destination account and the amount!");
                System.out.print(">");
                String[] message = scanner.nextLine().split(" ");
                String destId = message[0];
                double amountTransaction = Double.parseDouble(message[1]);
                sendTransactionCall(accountIdEncryptedBase64, destId, amountTransaction, privateKey, publicKeyBase64); 
            break;
            
            case "getbalance":
                getBalanceCall(accountIdEncryptedBase64, privateKey, publicKeyBase64);
            break;
            
            case "getextract":
                getExtractCall(accountIdEncryptedBase64, privateKey, publicKeyBase64);
            break;
            
            case "gettotalvalue":
                System.out.println("Please type in the acconts to get the total value all split by a space");
                System.out.print(">");
                String[] accounts = scanner.nextLine().split(" ");
                getTotalValueCall(accounts,accountIdEncryptedBase64, privateKey, publicKeyBase64);
            break;

            case "getgloballedgervalue":
                getGlobalLedgerValue(accountIdEncryptedBase64, privateKey, publicKeyBase64);
            break;

            case "getledger":
                getLedger(accountIdEncryptedBase64, privateKey, publicKeyBase64);
            break;

            case "exit":
                System.out.println("Bye !!!");
                return;
            default:
                System.out.println("Wrong operation !");
            }
            System.out.println("Write another operation or type 'exit' to end the service.");
            System.out.print(">");
            operation   = scanner.nextLine();
            
        }
        
    }

    private static void getLedger(String accountIdEncryptedBase64, PrivateKey privateKey, String publicKeyBase64) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, URISyntaxException, IOException, InvalidKeySpecException {
        String timeStamp = Instant.now().toString();

        String dataExempleToSign = accountIdEncryptedBase64 + timeStamp;

        //Signature generation
        Signature signer = Signature.getInstance("SHA256withECDSA");
        signer.initSign(privateKey);
        signer.update(dataExempleToSign.getBytes(charset));
        byte[] dataSignedBytes = signer.sign();
        String dataSignedBytesBase64 = Base64.getEncoder().encodeToString(dataSignedBytes);

        String httpsQuery = String.format(
            "accountId=%s&dataSigned=%s&publicKey=%s&timeStamp=%s",
            URLEncoder.encode(accountIdEncryptedBase64, "UTF-8"),
            URLEncoder.encode(dataSignedBytesBase64, "UTF-8"),
            URLEncoder.encode(publicKeyBase64, "UTF-8"),
            URLEncoder.encode(timeStamp, "UTF-8")
        );

        URL url = new URI("https://localhost:8080/endpoints/getledger?" + httpsQuery).toURL();
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        int responseCode = con.getResponseCode();
        System.out.println("Server Response: " + responseCode);

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder ledgerResponse = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            ledgerResponse.append(inputLine);
        }
        in.close();

        //Sig verification
        JSONObject json = new JSONObject(ledgerResponse.toString());

        SignedResponse signedResponse = new SignedResponse(json.getString("message"),json.getString("signedMessage"), json.getString("publicKey"));

        byte[] publicKeyBytes = Base64.getDecoder().decode(signedResponse.getPublicKey());
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        PublicKey realPublicKey = keyFactory.generatePublic(keySpec);

        Signature sig = Signature.getInstance("SHA256withECDSA");
        sig.initVerify(realPublicKey);
        sig.update(signedResponse.getMessage().getBytes(StandardCharsets.UTF_8));

        byte[] dataResponseSignedBytes = Base64.getDecoder().decode(signedResponse.getSignedMessage());
        boolean isValid = sig.verify(dataResponseSignedBytes);

        if(isValid){
            System.out.println("Valid signature !!");
            System.out.println("////////////////////////////////");
            String[] singleExtract = signedResponse.getMessage().split("\n");
            for (String extract : singleExtract) {
                String[] content = extract.split("\\|");

                if (content.length == 5) {
                    String extractId = content[0];
                    String fromUser = content[1];
                    String toUser = content[2];
                    String amount = content[3];

                    System.out.println("Extract Id: " + extractId);
                    System.out.println("From: " + fromUser);
                    System.out.println("To: " + toUser);
                    System.out.println("Amount: " + amount);
                    System.out.println("////////////////////////////////");
                }
            }

        }else {
            System.out.println("Signature not valid!");
        }

    }

    private static void getGlobalLedgerValue(String accountIdEncryptedBase64, PrivateKey privateKey, String publicKeyBase64) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, URISyntaxException, IOException, InvalidKeySpecException {
        String timeStamp = Instant.now().toString();

        String dataExempleToSign = accountIdEncryptedBase64 + timeStamp;

        //Signature generation
        Signature signer = Signature.getInstance("SHA256withECDSA");
        signer.initSign(privateKey);
        signer.update(dataExempleToSign.getBytes(charset));
        byte[] dataSignedBytes = signer.sign();
        String dataSignedBytesBase64 = Base64.getEncoder().encodeToString(dataSignedBytes);

        String httpsQuery = String.format(
            "accountId=%s&dataSigned=%s&publicKey=%s&timeStamp=%s",
            URLEncoder.encode(accountIdEncryptedBase64, "UTF-8"),
            URLEncoder.encode(dataSignedBytesBase64, "UTF-8"),
            URLEncoder.encode(publicKeyBase64, "UTF-8"),
            URLEncoder.encode(timeStamp, "UTF-8")
        );

        URL url = new URI("https://localhost:8080/endpoints/getgloballedgervalue?" + httpsQuery).toURL();
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        int responseCode = con.getResponseCode();
        System.out.println("Server Response: " + responseCode);

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder ledgerResponse = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            ledgerResponse.append(inputLine);
        }
        in.close();


        //Sig verification
        JSONObject json = new JSONObject(ledgerResponse.toString());

        SignedResponse signedResponse = new SignedResponse(json.getString("message"),json.getString("signedMessage"), json.getString("publicKey"));

        byte[] publicKeyBytes = Base64.getDecoder().decode(signedResponse.getPublicKey());
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        PublicKey realPublicKey = keyFactory.generatePublic(keySpec);

        Signature sig = Signature.getInstance("SHA256withECDSA");
        sig.initVerify(realPublicKey);
        sig.update(signedResponse.getMessage().getBytes(StandardCharsets.UTF_8));

        byte[] dataResponseSignedBytes = Base64.getDecoder().decode(signedResponse.getSignedMessage());
        boolean isValid = sig.verify(dataResponseSignedBytes);

        if(isValid){
            System.out.println("Valid signature !!");
            System.out.println("Global value of the ledger: " + signedResponse.getMessage());
        }else {
            System.out.println("Signature not valid!");
        }

    }

    private static void getTotalValueCall(String[] accounts, String accountIdEncryptedBase64, PrivateKey privateKey, String publicKeyBase64) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, URISyntaxException, IOException, InvalidKeySpecException {
        String timeStamp = Instant.now().toString();

        String dataExempleToSign = accountIdEncryptedBase64 + String.join("", accounts) + timeStamp;

        //Signature generation
        Signature signer = Signature.getInstance("SHA256withECDSA");
        signer.initSign(privateKey);
        signer.update(dataExempleToSign.getBytes(charset));
        byte[] dataSignedBytes = signer.sign();
        String dataSignedBytesBase64 = Base64.getEncoder().encodeToString(dataSignedBytes);

        
        StringBuilder httpsQuery = new StringBuilder();
        
        for(String id : accounts){
            httpsQuery.append("accountsId=").append(URLEncoder.encode(id, "UTF-8")).append("&");
        }
        
        httpsQuery.append("accountId=").append(URLEncoder.encode(accountIdEncryptedBase64, "UTF-8")).append("&");
        httpsQuery.append("dataSigned=").append(URLEncoder.encode(dataSignedBytesBase64, "UTF-8")).append("&");
        httpsQuery.append("publicKey=").append(URLEncoder.encode(publicKeyBase64, "UTF-8")).append("&");
        httpsQuery.append("timeStamp=").append(URLEncoder.encode(timeStamp, "UTF-8"));

        URL url = new URI("https://localhost:8080/endpoints/gettotalvalue?" + httpsQuery).toURL();
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        int responseCode = con.getResponseCode();
        System.out.println("Server Response: " + responseCode);

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder ledgerResponse = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            ledgerResponse.append(inputLine);
        }
        in.close();

        //Sig verification
        JSONObject json = new JSONObject(ledgerResponse.toString());

        SignedResponse signedResponse = new SignedResponse(json.getString("message"),json.getString("signedMessage"), json.getString("publicKey"));

        byte[] publicKeyBytes = Base64.getDecoder().decode(signedResponse.getPublicKey());
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        PublicKey realPublicKey = keyFactory.generatePublic(keySpec);

        Signature sig = Signature.getInstance("SHA256withECDSA");
        sig.initVerify(realPublicKey);
        sig.update(signedResponse.getMessage().getBytes(StandardCharsets.UTF_8));

        byte[] dataResponseSignedBytes = Base64.getDecoder().decode(signedResponse.getSignedMessage());
        boolean isValid = sig.verify(dataResponseSignedBytes);

        if(isValid){
            System.out.println("Valid signature !!");
            System.out.println("Total balance on the accounts selected: " + signedResponse.getMessage());
        }else {
            System.out.println("Signature not valid!");
        }
    }

    private static void getExtractCall(String accountIdEncryptedBase64, PrivateKey privateKey, String publicKeyBase64) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, URISyntaxException, IOException, InvalidKeySpecException{
        String timeStamp = Instant.now().toString();

        String dataExempleToSign = accountIdEncryptedBase64 + timeStamp;

        //Signature generation
        Signature signer = Signature.getInstance("SHA256withECDSA");
        signer.initSign(privateKey);
        signer.update(dataExempleToSign.getBytes(charset));
        byte[] dataSignedBytes = signer.sign();
        String dataSignedBytesBase64 = Base64.getEncoder().encodeToString(dataSignedBytes);
      
        String httpsQuery = String.format(
            "accountId=%s&dataSigned=%s&publicKey=%s&timeStamp=%s",
            URLEncoder.encode(accountIdEncryptedBase64, "UTF-8"),
            URLEncoder.encode(dataSignedBytesBase64, "UTF-8"),
            URLEncoder.encode(publicKeyBase64, "UTF-8"),
            URLEncoder.encode(timeStamp, "UTF-8")
        );

        URL url = new URI("https://localhost:8080/endpoints/getextract?" + httpsQuery).toURL();
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        int responseCode = con.getResponseCode();
        System.out.println("Server Response: " + responseCode);

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder ledgerResponse = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            ledgerResponse.append(inputLine);
        }
        in.close();

        //Sig verification
        JSONObject json = new JSONObject(ledgerResponse.toString());

        SignedResponse signedResponse = new SignedResponse(json.getString("message"),json.getString("signedMessage"), json.getString("publicKey"));

        byte[] publicKeyBytes = Base64.getDecoder().decode(signedResponse.getPublicKey());
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        PublicKey realPublicKey = keyFactory.generatePublic(keySpec);

        Signature sig = Signature.getInstance("SHA256withECDSA");
        sig.initVerify(realPublicKey);
        sig.update(signedResponse.getMessage().getBytes(StandardCharsets.UTF_8));

        byte[] dataResponseSignedBytes = Base64.getDecoder().decode(signedResponse.getSignedMessage());
        boolean isValid = sig.verify(dataResponseSignedBytes);

        if(isValid){
            System.out.println("Valid signature !!");
            String[] singleExtract = signedResponse.getMessage().split("\n");
            for (String extract : singleExtract) {
                String[] content = extract.split("\\|");

                if (content.length == 5) {
                    String extractId = content[0];
                    String fromUser = content[1];
                    String toUser = content[2];
                    String amount = content[3];

                    System.out.println("Extract Id: " + extractId);
                    System.out.println("From: " + fromUser);
                    System.out.println("To: " + toUser);
                    System.out.println("Amount: " + amount);
                    System.out.println("////////////////////////////////");
                }
            }

        }else {
            System.out.println("Signature not valid!");
        }
    }

    private static void getBalanceCall(String accountIdEncryptedBase64, PrivateKey privateKey, String publicKeyBase64) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, URISyntaxException, IOException, InvalidKeySpecException {
        
        String timeStamp = Instant.now().toString();

        String dataExempleToSign = accountIdEncryptedBase64 + timeStamp;

        //Signature generation
        Signature signer = Signature.getInstance("SHA256withECDSA");
        signer.initSign(privateKey);
        signer.update(dataExempleToSign.getBytes(charset));
        byte[] dataSignedBytes = signer.sign();
        String dataSignedBytesBase64 = Base64.getEncoder().encodeToString(dataSignedBytes);

        String httpsQuery = String.format(
            "accountId=%s&dataSigned=%s&publicKey=%s&timeStamp=%s",
            URLEncoder.encode(accountIdEncryptedBase64, "UTF-8"),
            URLEncoder.encode(dataSignedBytesBase64, "UTF-8"),
            URLEncoder.encode(publicKeyBase64, "UTF-8"),
            URLEncoder.encode(timeStamp, "UTF-8")
        );

        URL url = new URI("https://localhost:8080/endpoints/getbalance?" + httpsQuery).toURL();
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        int responseCode = con.getResponseCode();
        System.out.println("Server Response: " + responseCode);

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder ledgerResponse = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            ledgerResponse.append(inputLine);
        }
        in.close();

        //Sig verification
        JSONObject json = new JSONObject(ledgerResponse.toString());

        SignedResponse signedResponse = new SignedResponse(json.getString("message"),json.getString("signedMessage"), json.getString("publicKey"));

        byte[] publicKeyBytes = Base64.getDecoder().decode(signedResponse.getPublicKey());
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        PublicKey realPublicKey = keyFactory.generatePublic(keySpec);

        Signature sig = Signature.getInstance("SHA256withECDSA");
        sig.initVerify(realPublicKey);
        sig.update(signedResponse.getMessage().getBytes(StandardCharsets.UTF_8));

        byte[] dataResponseSignedBytes = Base64.getDecoder().decode(signedResponse.getSignedMessage());
        boolean isValid = sig.verify(dataResponseSignedBytes);

        if(isValid){
            System.out.println("Valid signature !!");
            System.out.println("Balance on the account: " + signedResponse.getMessage());
        }else {
            System.out.println("Signature not valid!");
        }

    }

    private static void sendTransactionCall(String accountIdEncryptedBase64, String destId, double amountTransaction, PrivateKey privateKey, String publicKeyBase64) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, URISyntaxException, IOException {
        //Data encryption
        //Variavel consoante a operacao

        String timeStamp = Instant.now().toString();

        String to = destId;

        String dataExempleToSign = accountIdEncryptedBase64 + to + amountTransaction + timeStamp;

        //Signature generation
        Signature signer = Signature.getInstance("SHA256withECDSA");
        signer.initSign(privateKey);
        signer.update(dataExempleToSign.getBytes(charset));
        byte[] dataSignedBytes = signer.sign();
        String dataSignedBytesBase64 = Base64.getEncoder().encodeToString(dataSignedBytes);

        //JSON generation, HTTPS connection and package delivery
        //Variavel consoante a operacao

        JSONObject jo = new JSONObject();
        jo.put("accountId", accountIdEncryptedBase64);
        jo.put("to", to);
        jo.put("amount", amountTransaction);
        jo.put("dataSigned", dataSignedBytesBase64);
        jo.put("publicKey", publicKeyBase64);
        jo.put("timeStamp", timeStamp);


        //HTTPS request
        //Variavel consoante a operacao
        URL url = new URI("https://localhost:8080/endpoints/sendtransaction").toURL();

        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
        con.setDoOutput(true);

        try(OutputStream os = con.getOutputStream()) {
            byte[] input = jo.toString().getBytes(charset);
            os.write(input, 0, input.length);			
        }

        int responseCode = con.getResponseCode();
        System.out.println("Server Response: " + responseCode);

    }

    private static void loadMoneyCall(String accountIdEncryptedBase64, double amount, PrivateKey privateKey, String publicKeyBase64) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, URISyntaxException, IOException {
        String timeStamp = Instant.now().toString();

        String dataToSign = accountIdEncryptedBase64 + amount + timeStamp;

        //Signature generation
        Signature signer = Signature.getInstance("SHA256withECDSA");
        signer.initSign(privateKey);
        signer.update(dataToSign.getBytes(charset));
        byte[] dataSignedBytes = signer.sign();
        String dataSignedBytesBase64 = Base64.getEncoder().encodeToString(dataSignedBytes);

        //JSON generation, HTTPS connection and package delivery
        //Variavel consoante a operacao

        JSONObject jo = new JSONObject();
        jo.put("accountId", accountIdEncryptedBase64);
        jo.put("amount", amount);
        jo.put("dataSigned", dataSignedBytesBase64);
        jo.put("publicKey", publicKeyBase64);
        jo.put("timeStamp", timeStamp);


        //HTTPS request
        //Variavel consoante a operacao
        URL url = new URI("https://localhost:8080/endpoints/loadmoney").toURL();

        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
        con.setDoOutput(true);

        try(OutputStream os = con.getOutputStream()) {
            byte[] input = jo.toString().getBytes(charset);
            os.write(input, 0, input.length);			
        }

        int responseCode = con.getResponseCode();
        System.out.println("Server Response: " + responseCode);
    }

    private static void createAccountCall(String accountIdEncryptedBase64, PrivateKey privateKey, String publicKeyBase64) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, URISyntaxException, IOException {
        
        String timeStamp = Instant.now().toString();

        String dataToSign = accountIdEncryptedBase64 + timeStamp;

        //Signature generation
        Signature signer = Signature.getInstance("SHA256withECDSA");
        signer.initSign(privateKey);
        signer.update(dataToSign.getBytes(charset));
        byte[] dataSignedBytes = signer.sign();
        String dataSignedBytesBase64 = Base64.getEncoder().encodeToString(dataSignedBytes);

        //JSON generation, HTTPS connection and package delivery
        //Variavel consoante a operacao

        JSONObject jo = new JSONObject();
        jo.put("accountId", accountIdEncryptedBase64);
        jo.put("dataSigned", dataSignedBytesBase64);
        jo.put("publicKey", publicKeyBase64);
        jo.put("timeStamp", timeStamp);


        //HTTPS request
        //Variavel consoante a operacao
        URL url = new URI("https://localhost:8080/endpoints/createaccount").toURL();

        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
        con.setDoOutput(true);

        try(OutputStream os = con.getOutputStream()) {
            byte[] input = jo.toString().getBytes(charset);
            os.write(input, 0, input.length);			
        }

        int responseCode = con.getResponseCode();
        System.out.println("Server Response: " + responseCode);


    }




    public static void disableCertificateValidation() throws Exception {
        TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() { return null; }
                public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
                public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
            }
        };

        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        HostnameVerifier allHostsValid = (hostname, session) -> true;
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    }


}
