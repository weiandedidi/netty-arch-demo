package com.qidi.nettyme.demos.mqtt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.Cipher;
import java.io.File;
import java.io.FileInputStream;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * @author maqidi
 * @version 1.0
 * @create 2024-11-14 19:14
 */
@Component
public class SecurityUtil {
    @Value("${mqtt.security.private-key}")
    private String privateKeyPath;

    @Value("${mqtt.security.public-key}")
    private String publicKeyPath;

    private PrivateKey privateKey;
    private PublicKey publicKey;


    @PostConstruct
    public void init() throws Exception {
        // 加载密钥对
        // 实际项目中应该从配置的路径读取密钥文件
//        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
//        generator.initialize(2048);
//        KeyPair pair = generator.generateKeyPair();
//        privateKey = pair.getPrivate();
//        publicKey = pair.getPublic();
        this.privateKey = loadPrivateKey(privateKeyPath);
        this.publicKey = loadPublicKey(publicKeyPath);
    }

    /**
     * 加载公钥：public_key.pem
     *
     * @param filepath
     * @return
     * @throws Exception
     */
    public static PublicKey loadPublicKey(String filepath) throws Exception {
        FileInputStream fis = new FileInputStream(new File(filepath));
        byte[] encodedKey = new byte[fis.available()];
        fis.read(encodedKey);
        fis.close();

        X509EncodedKeySpec spec = new X509EncodedKeySpec(encodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(spec);
    }


    /**
     * 加载私钥
     * private_key.pem
     */
    public static PrivateKey loadPrivateKey(String filepath) throws Exception {
        FileInputStream fis = new FileInputStream(new File(filepath));
        byte[] encodedKey = new byte[fis.available()];
        fis.read(encodedKey);
        fis.close();

        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(encodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(spec);
    }

    /**
     * 加密
     *
     * @param data
     * @return
     * @throws Exception
     */
    public byte[] encrypt(byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(data);
    }

    /**
     * 解密
     *
     * @param data
     * @return
     * @throws Exception
     */
    public byte[] decrypt(byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(data);
    }
}
