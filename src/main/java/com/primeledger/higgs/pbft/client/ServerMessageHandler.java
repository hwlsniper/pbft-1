package com.primeledger.higgs.pbft.client;

import com.primeledger.higgs.pbft.common.message.BaseMessage;
import com.primeledger.higgs.pbft.common.message.MessageType;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;


/**
 * @author hanson
 * @Date 2018/4/25
 * @Description:
 */

public class ServerMessageHandler extends SimpleChannelInboundHandler<BaseMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, BaseMessage message) throws Exception {
        if(message.getType() == MessageType.REPLY){

        }
    }
}
