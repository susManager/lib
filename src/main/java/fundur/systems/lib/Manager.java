package fundur.systems.lib;

import fundur.systems.lib.local.LocalManager;
import fundur.systems.lib.server.ServerManager;
import org.json.JSONObject;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;
import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

public class Manager {
    private ExceptionHandler handler;
    private boolean io, net;
    private BaseManager manager;

    public Manager (ExceptionHandler handler) {
        this.handler = handler;
        updateState();
    }

    public void updateState() {
        testIO();
        testNet();
        net = false; // due to server being implemented later on
        manager = net ? new ServerManager() : new LocalManager();
    }

    public boolean testIO() {
        io = true;
        return true;
    }

    public boolean testNet() {
        try {
            URL url = new URL("https://example.org");
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            io = true;
            return true;
        } catch (MalformedURLException ignored) {}
          catch (IOException e) {
            handler.handleException(e);
        }
        net = false;
        return false;
    }

    //decryption starts here

    //TODO: salt le password
    public static SecretKey getKeyFromPwd(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(),new byte[] {1, 3, 3, 7, 4, 2, 0, 6, 9},65536, 256);
        return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
    }

    public static IvParameterSpec generateIV() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    public static String encrypt(String algorithm, String input, SecretKey key, IvParameterSpec iv)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException,
            InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        byte[] encrText = cipher.doFinal(input.getBytes());
        return Base64.getEncoder().encodeToString(encrText);
    }

    public static String decrypt(String algorithm, String cipherText, SecretKey key, IvParameterSpec iv) throws
            NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException,
            InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.DECRYPT_MODE, key, iv);
        byte[] decrText = cipher.doFinal(Base64.getDecoder().decode(cipherText));
        return new String(decrText);
    }

    public JSONObject getJSONObject(String password) {
        String raw = manager.getRaw();

        return new JSONObject(manager.getRaw());

    }

    public static void main(String[] args) {
        String test = "you are gay";
        System.out.println(test);
        try {
            SecretKey key = getKeyFromPwd("iHaveAids69");
            IvParameterSpec iv = generateIV();
            String algorithm = "AES/CBC/PKCS5Padding";
            String cipherText = encrypt(algorithm, test, key, iv);
            System.out.println(cipherText);
            String decrText = decrypt(algorithm, cipherText, key, iv);
            System.out.println(decrText);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
