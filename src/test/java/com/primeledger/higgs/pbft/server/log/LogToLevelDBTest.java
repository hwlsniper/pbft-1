package com.primeledger.higgs.pbft.server.log;

import com.primeledger.higgs.pbft.common.message.StateLog;
import com.primeledger.higgs.pbft.common.utils.MessageUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.testng.annotations.Test;

public class LogToLevelDBTest {
    private static LogToLevelDB log = new LogToLevelDB(System.getProperty("user.dir") + "/leveldb_test");

    @AfterClass
    public void finish(){
        log.close();
    }
    @Test
    public void testGetStableCp() throws Exception {
//        LogToLevelDB log = new LogToLevelDB(System.getProperty("user.dir") + "/leveldb_test");
        log.putStableCp(8);
        int stableCp = log.getStableCp();
        Assert.assertEquals(stableCp, 8);
    }

    @Test
    public void testPutLog1() throws Exception {

        StateLog stateLog = new StateLog();
        stateLog.setCp(0);
        stateLog.setOperation(MessageUtils.objToBytes("1235454354"));
        log.putLog(stateLog);

        StateLog[] stateLogs = log.getStateLog(0, 0);
        Assert.assertEquals(stateLogs.length,1);
        Assert.assertEquals(stateLogs[0].getCp(),0);
        Assert.assertEquals(MessageUtils.byteToObj(stateLogs[0].getOperation()),"1235454354");

    }

    @Test
    public void testPutLog2(){
//        LogToLevelDB log = new LogToLevelDB(System.getProperty("user.dir") + "/leveldb_test");
        StateLog[] stateLogs = log.getStateLog(9,8);
        Assert.assertNull(stateLogs);
        log.close();
    }

    @Test
    public void testGetLowStableCp() throws Exception {
        log.setLowStableCp(100);
        Assert.assertEquals(log.getLowStableCp(),100);
    }


    @Test
    public void testDeleteLog() throws Exception {
        log.deleteLog(3,4);
    }

}