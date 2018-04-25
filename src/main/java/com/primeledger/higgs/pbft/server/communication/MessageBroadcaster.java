package com.primeledger.higgs.pbft.server.communication;

import com.primeledger.higgs.pbft.common.message.BaseMessage;

/**
 * @author hanson
 * @Date 2018/4/25
 * @Description:
 */
public interface MessageBroadcaster {
    void boadcastToServer(BaseMessage message);

    void send(int id,BaseMessage message);

    void boadcastToServer(byte[] message);


    void sendToClient(int clientId,BaseMessage message);
}
