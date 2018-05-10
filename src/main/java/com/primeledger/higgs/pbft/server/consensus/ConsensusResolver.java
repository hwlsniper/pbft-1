package com.primeledger.higgs.pbft.server.consensus;

import com.primeledger.higgs.pbft.common.message.BaseMessage;
import com.primeledger.higgs.pbft.common.message.ConsensusMessage;
import com.primeledger.higgs.pbft.common.message.MessageType;
import com.primeledger.higgs.pbft.common.message.StateLog;
import com.primeledger.higgs.pbft.common.utils.MessageUtils;
import com.primeledger.higgs.pbft.server.communication.MessageBroadcaster;
import com.primeledger.higgs.pbft.server.main.ServerViewController;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author hanson
 * @Date 2018/4/25
 * @Description:
 */
public class ConsensusResolver extends Thread {

    private BlockingQueue<BaseMessage> inQueue;

    private BlockingQueue<StateLog> stateQueu = null;

    private MessageBroadcaster messageBroadcaster = null;

    private ServerViewController controller = null;

    private ConsensusManager consensusManager = null;


    public ConsensusResolver(ConsensusManager consensusManager, ServerViewController controller, BlockingQueue<BaseMessage> inQueue, MessageBroadcaster messageBroadcaster, BlockingQueue<StateLog> stateQueu) {
        super("process-consensus");
        this.inQueue = inQueue;
        this.messageBroadcaster = messageBroadcaster;
        this.controller = controller;
        this.consensusManager = consensusManager;
        this.stateQueu = stateQueu;
    }

    @Override
    public void run() {
        while (true) {
            ConsensusMessage message = null;
            try {
                message = (ConsensusMessage) inQueue.poll(100, TimeUnit.MICROSECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
                continue;
            }
            if (message == null) {
                continue;
            }

            boolean verify = MessageUtils.verifySignature(controller.getPublicKey(message.getSender()), message.getSerializeMessage(), message.getSignature());
            if (!verify) {
                System.out.println("invalid signature from server:" + message.getSender());
                continue;
            }
            System.out.println("receive message " + message.getType() + " from " + message.getSender() + " height " + message.getCp());
            EPoch ePoch = consensusManager.getEPoch(message.getClientId(), message.getTimeStamp(), false);
            int cnt = 0;
            while (ePoch == null) {
                System.out.println("ePoch has not been created,waiting to be created!timestamp:" + message.getTimeStamp());
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ePoch = consensusManager.getEPoch(message.getClientId(), message.getTimeStamp(), false);
                cnt++;
                if (cnt > 5) {
                    break;
                }
                continue;
            }

            if (cnt > 5) {
                continue;
            }
            switch (message.getType()) {

                case PRE_PREPARE:
                    dealPrePrepare(message, ePoch);

                    break;
                case PREPARE:
                    dealPrepare(message, ePoch);
                    break;
                case COMMIT:
                    dealCommit(message, ePoch);
                    break;
                default:
                    System.out.println("INVALID MESSAGE TYPE!");
                    break;
            }
        }
    }

    /**
     * pre-prepare stage in pbft
     *
     * @param message
     */
    public void dealPrePrepare(BaseMessage message, EPoch ePoch) {
        ConsensusMessage consensus = (ConsensusMessage) message;
        if (consensus.getSender() != controller.getCurrentLeader()) {
            return;
        }

        ePoch.addPrepareDigest(consensus.getDigest(), consensus.getSender());

        ConsensusMessage myConsensus = new ConsensusMessage();
        myConsensus.setDigest(consensus.getDigest());
        myConsensus.setView(controller.getCurrentView());
        myConsensus.setRequest(consensus.getRequest());
        myConsensus.setType(MessageType.PREPARE);
        myConsensus.setClientId(consensus.getClientId());
        myConsensus.setTimeStamp(consensus.getTimeStamp());
        myConsensus.setSender(controller.getMyId());
        myConsensus.setCp(ePoch.getCheckPoint());
        byte[] serial = myConsensus.getSerializeMessage();
        byte[] signature = MessageUtils.signMessage(controller.getPrivateKey(), serial);
        myConsensus.setSignature(signature);

        messageBroadcaster.boadcastToServer(myConsensus);
    }

    /**
     * prepare stage in pbft
     *
     * @param message
     */
    public void dealPrepare(BaseMessage message, EPoch ePoch) {
        ConsensusMessage consensus = (ConsensusMessage) message;
//        EPoch ePoch = consensusManager.getEPoch(consensus.getClientId(), consensus.getTimeStamp(), false);
//        int cnt = 0;

        ePoch.addPrepareDigest(consensus.getDigest(), consensus.getSender());
        ePoch.setLastProcessTime(System.currentTimeMillis());

        if (!ePoch.isPrepare() && ePoch.countPrepare() >= controller.getPrepareQuarum()) {
            ConsensusMessage myConsensus = new ConsensusMessage();
            myConsensus.setSender(controller.getMyId());
            myConsensus.setTimeStamp(consensus.getTimeStamp());
            myConsensus.setRequest(consensus.getRequest());
            myConsensus.setClientId(consensus.getClientId());
            myConsensus.setType(MessageType.COMMIT);
            myConsensus.setSequnce(consensus.getSequnce());
            myConsensus.setView(controller.getCurrentView());
            myConsensus.setDigest(ePoch.getMyDigest());
            myConsensus.setCp(ePoch.getCheckPoint());
            ePoch.addCp(ePoch.getCheckPoint(), controller.getMyId());
            byte[] serial = myConsensus.getSerializeMessage();
            byte[] signature = MessageUtils.signMessage(controller.getPrivateKey(), serial);
            myConsensus.setSignature(signature);

            messageBroadcaster.boadcastToServer(myConsensus);
            System.out.println("send commit message to other node!height:" + myConsensus.getCp());
            ePoch.setPrepare(true);
        }

    }

    /**
     * commit stage in pbft
     *
     * @param message
     */
    public void dealCommit(BaseMessage message, EPoch ePoch) {
        ConsensusMessage consensus = (ConsensusMessage) message;
        ePoch.addCommitDigest(consensus.getDigest(), consensus.getSender());
        ePoch.setLastProcessTime(System.currentTimeMillis());
        ePoch.addCp(consensus.getCp(), consensus.getSender());
        if (!ePoch.isCommit() && ePoch.countCommit() >= controller.getCommitQuarum() && ePoch.getMaxSameCp() >= controller.getCommitQuarum()) {
            ePoch.setCommit(true);
            byte[] request = ePoch.getRequest();

            try {
                ePoch.getMaxSameCp();
                StateLog state = new StateLog();
                state.setCp(ePoch.getConsensusCp());
                state.setOperation(request);

//                if (controller.getHighCp() < ePoch.getConsensusCp()) {
//                    controller.setHighCp(ePoch.getConsensusCp());
//                }
//                controller.incHighCp();
                stateQueu.offer(state);
                Object obj = MessageUtils.byteToObj(request);
                System.out.println("end.consensus content " + obj + " high check point:" + controller.getHighCp() + " current consensus cp:" + ePoch.getConsensusCp());
                controller.setHaveMsgProcess(false);
                controller.notifyLastConsensusFinish();

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }


        }
    }
}
