package com.exampledemo.parsaniahardik.registerloginsession;

import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class KCrypt {
    public static String encrypt(String key, String clear) throws Exception {

        Log.d("keyStore", key);
        if (key.startsWith("base64:")) {
            key = key.substring(7);
        }
        Log.d("Get base 64 key", key);
        byte[] keyDecode = Base64.decode(key, Base64.DEFAULT);
        Log.d("Key byte", keyDecode.toString());

//        Base64 base64 = new Base64();
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec skeySpec = new SecretKeySpec(keyDecode, "AES");
        byte[] iv = new byte[16];
        new Random().nextBytes(iv);
//        Cipher cipher = Cipher.getInstance("AES");
        IvParameterSpec initVector = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, initVector);
        byte[] encrypted = cipher.doFinal(clear.getBytes());
        JSONObject o1 = new JSONObject();
        String ivEncode = Base64.encodeToString(iv, Base64.DEFAULT);
        String valueEncode = Base64.encodeToString(encrypted, Base64.DEFAULT);
        o1.put("iv", ivEncode.trim().replaceAll("\n", ""));
        o1.put("value", valueEncode.trim().replaceAll("\n", ""));
        o1.put("mac", hashMac(ivEncode.trim().replaceAll("\n", ""), valueEncode.trim().replaceAll("\n", ""), keyDecode));
        Log.d("o1", o1.toString());
        return Base64.encodeToString(o1.toString().getBytes(), Base64.DEFAULT).replaceAll("\n", "");
//        return encrypted;
    }

    public static String decrypt(String key, String encrypted) throws Exception {
        if (key.startsWith("base64:")) {
            key = key.substring(7);
        }
        Log.d("Decode get base 64 key", key);
        byte[] keyDecode = Base64.decode(key, Base64.DEFAULT);
        Log.d("Decode key byte", keyDecode.toString());

        byte[] payloadByte = Base64.decode(encrypted, Base64.DEFAULT);
        String payload = new String(payloadByte);
        Log.d("payload", payload);
        JSONObject o = new JSONObject(payload);
        Log.d("object payload", o.toString());
        String  encryptedString = o.getString("value");
//        byte[] encryptedByte = encryptedString.getBytes();

//        iv = base64_decode($payload['iv']);
        String ivString = o.getString("iv");
        byte[] iv = Base64.decode(ivString, Base64.DEFAULT);

        byte[] encryptedNyte = Base64.decode(encryptedString, Base64.DEFAULT);
        SecretKeySpec skeySpec = new SecretKeySpec(keyDecode, "AES");
        IvParameterSpec initVector = new IvParameterSpec(iv);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, initVector);
        byte[] decrypted = cipher.doFinal(encryptedNyte);
        Log.d("decryptedByte", decrypted.toString());
//        byte[] decrypted = Base64.decode(decryptedByte, Base64.DEFAULT);
//        return decrypted;
        String s = new String(decrypted);
        return s;
    }

    private  static String hashMac(String iv, String value, byte[] key) throws Exception {
        String hmac = iv + value;
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(key, "HmacSHA256");
        sha256_HMAC.init(secret_key);
        byte[] rawHmac = sha256_HMAC.doFinal(hmac.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : rawHmac) {
            sb.append(String.format("%02x", b));
        }
        String hashMacString = sb.toString();
        Log.d("hashMacString", hashMacString);
        return hashMacString;
    }
}
