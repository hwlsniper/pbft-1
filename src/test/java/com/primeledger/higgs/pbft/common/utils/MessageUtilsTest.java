package com.primeledger.higgs.pbft.common.utils;

import org.junit.Assert;
import org.testng.annotations.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import static org.testng.Assert.*;

public class MessageUtilsTest {

    @Test
    public void testVerifySignature() throws Exception {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        keyPairGen.initialize(1024);
        //通过对象 KeyPairGenerator 获取对象KeyPair
        KeyPair keyPair = keyPairGen.generateKeyPair();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();


        byte[] signature = MessageUtils.signMessage(privateKey,"message".getBytes());

        Assert.assertTrue(MessageUtils.verifySignature(publicKey,"message".getBytes(),signature));
    }

    @Test
    public void testComputeDigest() throws Exception {
        Assert.assertNotNull(MessageUtils.computeDigest("123".getBytes()));

    }
}