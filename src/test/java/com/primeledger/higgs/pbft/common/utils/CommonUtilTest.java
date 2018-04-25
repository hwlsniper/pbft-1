package com.primeledger.higgs.pbft.common.utils;

import org.testng.Assert;
import org.testng.annotations.Test;


public class CommonUtilTest {

    @Test
    public void testIntToBytes() throws Exception {
        Assert.assertEquals(CommonUtil.intToBytes(1), new byte[]{1, 0, 0, 0});
    }

    @Test
    public void testBytesToInt() throws Exception {
        Assert.assertEquals(CommonUtil.bytesToInt(new byte[]{1, 0, 0, 0}), 1);
    }
}