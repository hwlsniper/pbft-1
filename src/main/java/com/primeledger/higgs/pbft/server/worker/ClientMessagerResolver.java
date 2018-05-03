package com.primeledger.higgs.pbft.server.worker;

import com.primeledger.higgs.pbft.common.message.BaseMessage;
import com.primeledger.higgs.pbft.common.message.ConsensusMessage;
import com.primeledger.higgs.pbft.common.message.MessageType;
import com.primeledger.higgs.pbft.common.message.RequestMessage;
import com.primeledger.higgs.pbft.common.utils.MessageUtils;
import com.primeledger.higgs.pbft.server.consensus.ConsensusManager;
import com.primeledger.higgs.pbft.server.consensus.EPoch;
import com.primeledger.higgs.pbft.server.main.ServerViewController;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author hanson
 * @Date 2018/4/25
 * @Description:
 */
public class ClientMessagerResolver extends Thread {

    private BlockingQueue<BaseMessage> outQueue = null;

    private BlockingQueue<BaseMessage> requestQueue = null;

    private ReentrantLock messageLock = new ReentrantLock();

    private Condition canPrepare = messageLock.newCondition();

    private boolean doWork = true;

    private ServerViewController controller = null;

    private ConsensusManager consensusManager = null;

    public ClientMessagerResolver(ConsensusManager consensusManager, BlockingQueue<BaseMessage> outQueue, BlockingQueue<BaseMessage> requestQueue, ServerViewController controller) {
        super("start-consensus");

        this.outQueue = outQueue;
        this.requestQueue = requestQueue;
        this.controller = controller;
        this.consensusManager = consensusManager;
        controller.setCanPrepare(messageLock, canPrepare);
    }

    @Override
    public void run() {
        while (doWork) {
            try {
                BaseMessage message = requestQueue.poll(100, TimeUnit.MILLISECONDS);
                if (message == null) {
                    continue;
                }


                if (message.getType() == MessageType.REQUEST) {
                    RequestMessage request = (RequestMessage) message;

                    boolean verifySig = MessageUtils.verifySignature(controller.getPublicKey(message.getSender()), request.getSerializeMessage(), request.getSignature());
                    if (!verifySig) {
                        System.out.println("invalid signature from client:" + request.getSender());
                        return;
                    }
                    System.out.println("receive command " + MessageUtils.byteToObj(request.getOperation()) + "timestamp:" + request.getTimeaStamp());

                    EPoch ePoch = consensusManager.getEPoch(request.getSender(), request.getTimeaStamp(), true);

                    byte requestSeriaize[] = request.getSerializeMessage();
                    byte[] digest = MessageUtils.computeDigest(requestSeriaize);
                    ePoch.setClientId(request.getSender());
                    ePoch.setLastProcessTime(System.currentTimeMillis());
                    ePoch.setMyDigest(digest);
                    ePoch.addCommitDigest(digest, controller.getMyId());
                    ePoch.addPrepareDigest(digest, controller.getMyId());
                    ePoch.setRequest(request.getOperation());
                    ePoch.setView(controller.getCurrentView());

                    //am I the leader?
                    if (controller.amITheLeader()) {
                        while (controller.isHaveMsgProcess()) {
                            messageLock.lock();
                            canPrepare.await(3000L, TimeUnit.MILLISECONDS);
                            messageLock.unlock();
                        }
                        System.out.println("start consensus height:" + controller.getHighCp());
                        controller.setHaveMsgProcess(true);
                        ConsensusMessage consensusMessage = new ConsensusMessage();
                        consensusMessage.setSender(controller.getMyId());
                        consensusMessage.setType(MessageType.PRE_PREPARE);
                        consensusMessage.setView(controller.getCurrentView());
                        consensusMessage.setTimeStamp(request.getTimeaStamp());
                        consensusMessage.setClientId(request.getSender());

                        consensusMessage.setCp(controller.getHighCp());
                        consensusMessage.setRequest(requestSeriaize);

                        consensusMessage.setDigest(digest);

                        byte signature[] = MessageUtils.signMessage(controller.getPrivateKey(), consensusMessage.getSerializeMessage());
                        consensusMessage.setSignature(signature);

                        outQueue.offer(consensusMessage);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

}
