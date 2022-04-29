package fundur.systems.lib;

import fundur.systems.lib.local.LocalManager;
import fundur.systems.lib.server.ServerManager;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;

public class Manager {
    private ExceptionHandler handler;
    private boolean io, net;
    private BaseManager manager;
    private String filepath;

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
    public static SecretKey getKeyFromPwd(String password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(),salt,65536, 256);
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
        String s = new String(Base64.getDecoder().decode(cipherText));
        int i = 0;
        byte[] decrText = cipher.doFinal(Base64.getDecoder().decode(cipherText));
        return new String(decrText);
    }

    public JSONObject getJSONObject(String password) throws Exception {
        try {
            String file = loadFile("testFile");
            JSONObject json = new JSONObject(file.split("<startF>")[0]);
            byte[] iv = serer(json.getJSONArray("iv"));
            byte[] salt = serer(json.getJSONArray("salt"));
            String algo = json.getString("algo");
            String content = file.split("<startF>")[1];
            return new JSONObject(decrypt(algo, content, getKeyFromPwd("iHaveAids69", salt), new IvParameterSpec(iv)));
        } catch (Exception e) {
            handler.handleException(e);
            throw e; //weil hier das error handling echt doppelt sin macht
        }
    }

    public static String loadFile(String path) throws FileNotFoundException {
        File f = new File(path);
        Scanner scanner = new Scanner(f);
        StringBuilder res = new StringBuilder();
        scanner.forEachRemaining(res::append);
        return res.toString();
    }

    public static byte[] serer(JSONArray arr) {
        List<Byte> byteList = new ArrayList<>();
        int temp;
        for (Object o : arr) {
            temp = (Integer) o;
            byteList.add((byte) temp);
        }
        byte[] res = new byte[byteList.size()];
        for (int i = 0; i < byteList.size(); i++) {
            res[i] = byteList.get(i);
        }
        return res;
    }

    public static void main(String[] args) {
        try {
            String file = loadFile("testFile");
            JSONObject json = new JSONObject(file.split("<startF>")[0]);
            byte[] iv = serer(json.getJSONArray("iv"));
            byte[] salt = serer(json.getJSONArray("salt"));
            String algo = json.getString("algo");
            String content = file.split("<startF>")[1];
            System.out.println("le content: " + decrypt(algo, content, getKeyFromPwd("iHaveAids69", salt), new IvParameterSpec(iv)));
        } catch (Exception e) {
            e.printStackTrace();
        }




        String test = "{\"name\": \"fridolin\"}";
        System.out.println(test);
        try {
            SecretKey key = getKeyFromPwd("iHaveAids69", new byte[] {1, 3, 3, 7, 4, 2, 0, 6, 9});
            IvParameterSpec iv = generateIV();
            System.out.println(Arrays.toString(iv.getIV()));
            String algorithm = "AES/CBC/PKCS5Padding";
            String cipherText = encrypt(algorithm, test, key, iv);
            System.out.println(cipherText);
            String decrText = decrypt(algorithm, cipherText, key, iv);
            System.out.println(decrText);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(System.getProperty("user.dir"));
    }
}
