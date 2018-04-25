package com.primeledger.higgs.pbft.common.network.netty;

import com.primeledger.higgs.pbft.common.message.RequestMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;


/**
 * @author hanson
 * @Date 2018/4/25
 * @Description:
 */

public class NettyMessageEncoder extends MessageToByteEncoder<RequestMessage>{
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RequestMessage message, ByteBuf byteBuf) throws Exception {
        message.write(byteBuf);
    }
}
