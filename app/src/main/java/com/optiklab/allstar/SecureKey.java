package com.optiklab.allstar;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import java.security.KeyStore;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class SecureKey {
    private static final String ALIAS="Optik_AI_Key_1";
    private static SharedPreferences p(Context c){return c.getSharedPreferences("ai",Context.MODE_PRIVATE);}
    private static SecretKey key() throws Exception {KeyStore ks=KeyStore.getInstance("AndroidKeyStore");ks.load(null);if(ks.containsAlias(ALIAS))return ((KeyStore.SecretKeyEntry)ks.getEntry(ALIAS,null)).getSecretKey();
        KeyGenerator g=KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,"AndroidKeyStore");g.init(new KeyGenParameterSpec.Builder(ALIAS,KeyProperties.PURPOSE_ENCRYPT|KeyProperties.PURPOSE_DECRYPT).setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).setKeySize(256).build());return g.generateKey();}
    public static void put(Context c,String secret) throws Exception {Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(Cipher.ENCRYPT_MODE,key());String iv=Base64.encodeToString(cipher.getIV(),Base64.NO_WRAP),bytes=Base64.encodeToString(cipher.doFinal(secret.getBytes(java.nio.charset.StandardCharsets.UTF_8)),Base64.NO_WRAP);p(c).edit().putString("key_iv",iv).putString("key_data",bytes).apply();}
    public static String get(Context c) {try{String iv=p(c).getString("key_iv",null),v=p(c).getString("key_data",null);if(iv==null||v==null)return "";Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(Cipher.DECRYPT_MODE,key(),new GCMParameterSpec(128,Base64.decode(iv,Base64.NO_WRAP)));return new String(cipher.doFinal(Base64.decode(v,Base64.NO_WRAP)),java.nio.charset.StandardCharsets.UTF_8);}catch(Exception e){return "";}}
    public static String model(Context c){return p(c).getString("model","gemini-3.8-flash");}
    public static void setModel(Context c,String s){p(c).edit().putString("model",s).apply();}
}
