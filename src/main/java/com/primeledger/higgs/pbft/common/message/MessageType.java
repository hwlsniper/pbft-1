package com.primeledger.higgs.pbft.common.message;


/**
 * @author hanson
 * @Date 2018/4/25
 * @Description:
 */

public enum MessageType {
    REQUEST,  // client request
    PRE_PREPARE , //
    PREPARE,
    COMMIT,
    REPLY,
    CONNECT,
    VIEW_CHANGE,
    NEW_VIEW,
    ASK_CHECKPOINT,
    BACK_CHECKPOINT,
    ASK_SYN_LOG,
    BACK_SYC_LOG
}
