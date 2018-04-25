package com.primeledger.higgs.test;

import com.primeledger.higgs.pbft.common.Config;
import com.primeledger.higgs.pbft.common.network.connection.NodeInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.Test;


@SpringBootTest(classes = {Config.class, NodeInfo.class})
public class BechMarkTest {

    @Autowired
    private Config config;

    @Test
    public void testBenchMark() {

    }
}
