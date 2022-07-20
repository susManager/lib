package fundur.systems.lib.sec;

import fundur.systems.lib.FileManager;
import fundur.systems.lib.Manager;
import fundur.systems.lib.NetManager;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.RSAKeyGenParameterSpec;
import java.util.ArrayList;
import java.util.Arrays;

public final class Security {
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
        return Encoder.encode(encrText);
    }

    public static String decrypt(String algorithm, String cipherText, SecretKey key, IvParameterSpec iv) throws
            NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException,
            InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.DECRYPT_MODE, key, iv);
        String s = new String(Encoder.decode(cipherText));
        int i = 0;
        byte[] decrText = cipher.doFinal(Encoder.decode(cipherText));
        return new String(decrText);
    }

    public static String hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] messageDigest = md.digest(input.getBytes());

            BigInteger no = new BigInteger(1, messageDigest);

            StringBuilder hashtext = new StringBuilder(no.toString(16));
            while (hashtext.length() < 32) {
                hashtext.insert(0, "0");
            }

            return hashtext.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * (Securely) Generate a new EncrState for use when registering
     * @return the newly generated EncrState
     */
    public static EncrState generateNewEncrstate() {
        return new EncrState(
                "AES/CBC/PKCS5Padding",
                "sussymanager(sus)".getBytes(),
                generateIV().getIV()
        );
    }

    public static String sign(String data, String pwd) throws GeneralSecurityException {
        KeyPair kp = getKeyPair(pwd);
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] msgHash = md.digest(data.getBytes());
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, kp.getPrivate());
        byte[] digitalSignature = cipher.doFinal(msgHash);
        return Encoder.encode(digitalSignature);
    }

    public static KeyPair getKeyPair(String password) throws GeneralSecurityException {
        byte[] seed = password.getBytes();

        SecureRandom rnd = SecureRandom.getInstance("SHA1PRNG");
        rnd.setSeed(seed);

        RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(2048, RSAKeyGenParameterSpec.F4);
        KeyPairGenerator pairGenerator = KeyPairGenerator.getInstance("RSA");
        pairGenerator.initialize(spec, rnd);

        return pairGenerator.generateKeyPair();
    }

    public static void main(String[] args) throws Exception {
        String encrypted = "this is encrypted text";
        String pwd = "password";
        KeyPair kp = getKeyPair(pwd);
        byte[] msgbytes = encrypted.getBytes();
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] msgHash = md.digest(msgbytes);
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, kp.getPrivate());
        byte[] digitalSignature = cipher.doFinal(msgHash);

        Cipher cipher2 = Cipher.getInstance("RSA");
        cipher2.init(Cipher.DECRYPT_MODE, kp.getPublic());
        byte[] decryptedMessageHash = cipher2.doFinal(digitalSignature);
        MessageDigest md1 = MessageDigest.getInstance("SHA-256");
        byte[] newMessageHash = md1.digest(msgbytes);
        System.out.println(Arrays.equals(decryptedMessageHash, newMessageHash));
    }
}
