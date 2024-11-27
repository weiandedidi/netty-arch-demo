package com.qidi.nettyme.demos.mqtt;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.Cipher;
import java.io.*;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

/**
 * @author maqidi
 * @version 1.0
 * @create 2024-11-14 19:14
 */
@Component
@Slf4j
public class SecurityUtil {

    private PrivateKey privateKey;
    private X509Certificate certificate;

    //需要显示注册这个类，才能加载证书
    static {
        // 注册 BouncyCastle 提供者
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }


    @PostConstruct
    public void init() throws Exception {
        // 加载密钥对
        // 实际项目中应该从配置的路径读取密钥文件
//        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
//        generator.initialize(2048);
//        KeyPair pair = generator.generateKeyPair();
//        privateKey = pair.getPrivate();
//        publicKey = pair.getPublic();
        this.privateKey = loadPrivateKey();
        this.certificate = loadCertificate();
    }

    /**
     * 加载证书
     *
     * @return
     * @throws Exception
     */
    public X509Certificate loadCertificate() throws Exception {
        Resource resource = new ClassPathResource("certificate.pem");
        try (Reader reader = new InputStreamReader(resource.getInputStream())) {
            PemReader pemReader = new PemReader(reader);

            PemObject pemObject = pemReader.readPemObject();
            X509CertificateHolder certificateHolder = new X509CertificateHolder(pemObject.getContent());
            // 转换为 Java X509Certificate
            JcaX509CertificateConverter converter = new JcaX509CertificateConverter().setProvider("BC");
            //解析出证书，返回公钥
            return converter.getCertificate(certificateHolder);
        }

    }


    /**
     * 从配置文件指定的路径加载私钥并返回 PrivateKey 对象。
     * 使用 BouncyCastle 从 PEM 文件加载私钥
     * pem的区别点
     * PKCS8私钥文件是以-----BEGIN PRIVATE KEY-----开头
     * PKCS1私钥文件是以-----BEGIN RSA PRIVATE KEY-----开头
     *
     * @return PrivateKey
     * @throws Exception 如果加载和解析私钥时发生错误
     */
    public PrivateKey loadPrivateKey() throws Exception {
        // 从 application.yaml 中读取的路径，加载文件
        Resource resource = new ClassPathResource("private_key.pem");
        try (Reader reader = new InputStreamReader(resource.getInputStream())) {
            PemReader pemReader = new PemReader(reader);

            PemObject pemObject = pemReader.readPemObject();
            byte[] keyBytes = pemObject.getContent();

            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(keySpec);
        }
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
        cipher.init(Cipher.ENCRYPT_MODE, getPublicKey());
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

    /**
     * 从证书获取公钥
     *
     * @return PublicKey
     * @throws Exception 公钥获取异常
     */
    public PublicKey getPublicKey() throws Exception {
        return certificate.getPublicKey();
    }

    /**
     * 使用公钥加密数据
     *
     * @param data 原始数据
     * @return Base64编码的加密数据
     * @throws Exception 加密异常
     */
    public String encrypt(String data) throws Exception {
        javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, getPublicKey());

        byte[] encryptedBytes = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    /**
     * 使用私钥解密数据
     *
     * @param encryptedData Base64编码的加密数据
     * @return 解密后的原始数据
     * @throws Exception 解密异常
     */
    public String decrypt(String encryptedData) throws Exception {
        javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(javax.crypto.Cipher.DECRYPT_MODE, privateKey);

        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
        return new String(decryptedBytes);
    }


    /**
     * 验证证书有效期
     *
     * @return 是否在有效期内
     * @throws Exception 证书验证异常
     */
    public boolean verifyCertificateValidity() throws Exception {
        try {
            certificate.checkValidity();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
