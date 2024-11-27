package com.qidi.nettyme.demos.util;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMWriter;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.x509.X509V3CertificateGenerator;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.Date;

/**
 * 公钥和私钥的钥匙对加工成PEM格式数据
 *
 * @author maqidi
 * @version 1.0
 * @create 2024-11-27 16:02
 */
@Slf4j
public class KeyPairToPEMUtil {

    /**
     * 将私钥保存为 PEM 格式的文件
     *
     * @param privateKey         需要保存的私钥
     * @param privateKeyFilePath 私钥存储文件路径
     * @throws IOException 如果写入文件时发生错误
     */
    public static void savePrivateKeyToPEM(PrivateKey privateKey, String privateKeyFilePath) throws IOException {
        try (FileOutputStream privateKeyFile = new FileOutputStream(privateKeyFilePath);
             JcaPEMWriter privateKeyWriter = new JcaPEMWriter(new java.io.OutputStreamWriter(privateKeyFile))) {
            //PKCS#8 格式的私钥，文件的开头表示 -----BEGIN PRIVATE KEY-----
            PemObject pemObject = new PemObject("PRIVATE KEY", privateKey.getEncoded());
            privateKeyWriter.writeObject(pemObject);
        }

        log.info("Private key has been successfully saved to {}", privateKeyFilePath);
    }


    public static X509Certificate generateSelfSignedCertificate(KeyPair keyPair) throws Exception {
        // 创建自签名证书生成器
        X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();

        // 设置证书的主题和颁发者
        certGen.setSubjectDN(new X509Principal("CN=Test Certificate"));
        certGen.setIssuerDN(new X509Principal("CN=Test Certificate"));
        certGen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis())); // 设置证书序列号
        certGen.setNotBefore(new Date(System.currentTimeMillis())); // 设置有效期开始时间
        certGen.setNotAfter(new Date(System.currentTimeMillis() + 365 * 24 * 60 * 60 * 1000L)); // 设置有效期1年
        certGen.setPublicKey(keyPair.getPublic()); // 设置公钥
        certGen.setSignatureAlgorithm("SHA256WithRSA"); // 设置签名算法

        // 生成证书
        return certGen.generate(keyPair.getPrivate(), "BC");
    }

    public static void saveCertificateX509(X509Certificate certificate, String filePath) throws Exception {
        PEMWriter pemWriter = new PEMWriter(new FileWriter(filePath));
        pemWriter.writeObject(certificate);
        pemWriter.close();
        System.out.println("X.509证书已保存到: " + filePath);
    }

    /**
     * 生成公钥和私钥的 pem文件，原有
     * <p>
     * openssl生成的私钥，默认pkcs1格式，如下
     * PKCS8私钥文件是以-----BEGIN PRIVATE KEY-----开头
     * PKCS1私钥文件是以-----BEGIN RSA PRIVATE KEY-----开头
     *
     * @param args
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public static void main(String[] args) throws Exception {
        // 注册 BouncyCastle 提供程序
        Security.addProvider(new BouncyCastleProvider());

        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair pair = generator.generateKeyPair();
        PrivateKey privateK = pair.getPrivate();
        PublicKey publicKey = pair.getPublic();
        // 生成自签名证书
        KeyPair keyPair = new KeyPair(publicKey, privateK);
        X509Certificate certificate = generateSelfSignedCertificate(keyPair);

        // 保存证书为X.509格式,证书中仅包含公钥
        saveCertificateX509(certificate, "src/main/resources/certificate.pem");
        savePrivateKeyToPEM(privateK, "src/main/resources/private_key.pem");
    }
}
