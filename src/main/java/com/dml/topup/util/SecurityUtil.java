package com.dml.topup.util;

import com.dml.topup.config.Constants;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.KeySpec;
import java.security.spec.RSAPublicKeySpec;

/**
 * Class for create value "AuthValue" encryption public key
 *
 * @author i.sadeghi
 */
@Component
@PropertySource("classpath:topup.properties")
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class SecurityUtil {

    public static String authValue = null;
    public static String userAuthorization = null;
    private static final String UNICODE_FORMAT = Constants.UNICODE_FORMAT;
    public static final String DESEDE_ENCRYPTION_SCHEME = Constants.DESEDE_ENCRYPTION_SCHEME;
    private KeySpec ks;
    private SecretKeyFactory skf;
    private Cipher cipher;
    byte[] arrayBytes;
    private String myEncryptionKey;
    private String myEncryptionScheme;
    SecretKey key;


    @Autowired
    private Environment env;

    @Autowired
    public SecurityUtil(Environment env) throws Exception {
        this.env = env;
        myEncryptionKey = getEnv().getProperty(Constants.PARSIAN_KEY);
        myEncryptionScheme = DESEDE_ENCRYPTION_SCHEME;
        arrayBytes = myEncryptionKey.getBytes(UNICODE_FORMAT);
        ks = new DESedeKeySpec(arrayBytes);
        skf = SecretKeyFactory.getInstance(myEncryptionScheme);
        cipher = Cipher.getInstance(myEncryptionScheme);
        key = skf.generateSecret(ks);
    }

    public SecurityUtil() {
    }

    private Environment getEnv() {
        return env;
    }

    public void addAuthValue() {
        authValue = encrypt(String.format("%s|%s", getEnv().getProperty(Constants.SETAREYEK_USERNAME), getEnv().getProperty(Constants.SETAREYEK_PASSWORD)), createPublicKey());
        userAuthorization = encode(String.format("%s:%s", getEnv().getProperty(Constants.PARSIAN_AUTHENTICATION_USERNAME), getEnv().getProperty(Constants.PARSIAN_AUTHENTICATION_PASSWORD)));
    }

    public String encode(String authorization) {
        return Base64.encodeBase64String(StringUtils.getBytesUtf8(authorization));
    }

    private String encrypt(String message, PublicKey publicKey) {
        String encrypt = null;
        try {
            Cipher encryptCipher = Cipher.getInstance("RSA");
            encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);

            byte[] cipherText = encryptCipher.doFinal(message.getBytes(StandardCharsets.UTF_8));

            encrypt = java.util.Base64.getEncoder().encodeToString(cipherText);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encrypt;
    }

    private PublicKey createPublicKey() {
        return createPublicKey(getEnv().getProperty(Constants.SETAREYEK_MODULUS), getEnv().getProperty(Constants.SETAREYEK_EXPONENT));
    }

    private PublicKey createPublicKey(String modulus, String exponents) {
        PublicKey pubKey = null;
        try {
            byte[] expBytes = java.util.Base64.getDecoder().decode(exponents.trim());
            byte[] modBytes = java.util.Base64.getDecoder().decode(modulus.trim());

            BigInteger modules = new BigInteger(1, modBytes);
            BigInteger exponent = new BigInteger(1, expBytes);

            KeyFactory factory = KeyFactory.getInstance(getEnv().getProperty(Constants.ALGORITHM_RSA));
            RSAPublicKeySpec pubSpec = new RSAPublicKeySpec(modules, exponent);
            pubKey = factory.generatePublic(pubSpec);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pubKey;
    }

//    public String encrypt(String unencryptedString) {
//        String encryptedString = null;
//        try {
//            cipher.init(Cipher.ENCRYPT_MODE, key);
//            byte[] plainText = unencryptedString.getBytes(UNICODE_FORMAT);
//            byte[] encryptedText = cipher.doFinal(plainText);
//            encryptedString = new String(org.apache.commons.codec.binary.Base64.encodeBase64(encryptedText));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return encryptedString;
//    }

//    public String decrypt(String encryptedString) {
//        String decryptedText = null;
//        try {
//            cipher.init(Cipher.DECRYPT_MODE, key);
//            byte[] encryptedText = org.apache.commons.codec.binary.Base64.decodeBase64(encryptedString);
//            byte[] plainText = cipher.doFinal(encryptedText);
//            decryptedText = new String(plainText);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return decryptedText;
//    }
}
