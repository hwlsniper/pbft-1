package com.primeledger.higgs.pbft.server.worker;

import com.primeledger.higgs.pbft.common.message.BaseMessage;
import com.primeledger.higgs.pbft.server.main.ServerViewController;
import com.primeledger.higgs.pbft.server.communication.MessageBroadcaster;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author hanson
 * @Date 2018/4/25
 * @Description:
 */

public class SendThread extends Thread {

    private BlockingQueue<BaseMessage> outQueue = null;
    private ServerViewController controller = null;
    private MessageBroadcaster broadcaster = null;
    private boolean doWork = true;

    public SendThread(BlockingQueue<BaseMessage> outQueue,ServerViewController controller,MessageBroadcaster broadcaster){
        super("consensus-message-sender");
        this.outQueue = outQueue;
        this.controller = controller;
        this.broadcaster = broadcaster;
    }

    @Override
    public void run(){
        while (doWork){
            try {
                BaseMessage message = outQueue.poll(100, TimeUnit.MILLISECONDS);
                if( message == null){
                   continue;
                }
                broadcaster.boadcastToServer(message);
                message = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
                continue;
            }
        }
    }
}
