package com.example.doni.pos;


import android.util.Log;

import java.math.BigInteger;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by IDSL on 2016/12/6.
 */

public class pProtocol {

    //var

    private BigInteger prime;

    public final String Sk;
    public final String IDp;
    public final String IDc;
    public final String N;
    public final String T;

    public pProtocol(final String Sk, final String N, final String T, final String IDc, final String IDp){
        this.Sk = Sk;
        this.IDp = IDp;
        this.IDc = IDc;
        this.N = N;
        this.T = T;
    }



    public static byte[] xor(final byte[] input, final byte[] secret) {
        final byte[] output = new byte[input.length];
        if (secret.length == 0) {
            throw new IllegalArgumentException("empty security key");
        }
        int spos = 0;
        for (int pos = 0; pos < input.length; ++pos) {
            output[pos] = (byte) (input[pos] ^ secret[spos]);
            ++spos;
            if (spos >= secret.length) {
                spos = 0;
            }
        }
        return output;
    }
    public static String hmacSha1(String key, String value) {
        try {
            // Get an hmac_sha1 key from the raw key bytes
            byte[] keyBytes = key.getBytes();
            SecretKeySpec signingKey = new SecretKeySpec(keyBytes, "HmacSHA1");

            // Get an hmac_sha1 Mac instance and initialize with the signing key
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signingKey);

            // Compute the hmac on input data bytes
            byte[] rawHmac = mac.doFinal(value.getBytes());

            // Convert raw bytes to Hex
            //byte[] hexBytes = new Hex().encode(rawHmac);

            //  Covert array of Hex bytes to a String
            return new String(rawHmac, "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] encrypt(byte[] raw, byte[] clear) {
        try {
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            byte[] encrypted = cipher.doFinal(clear);
            return encrypted;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public static byte[] decrypt(byte[] raw, byte[] encrypted) {
        try {
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            byte[] decrypted = cipher.doFinal(encrypted);
            return decrypted;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


}
