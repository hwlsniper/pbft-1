package com.primeledger.higgs.pbft.common.network.netty;

import com.primeledger.higgs.pbft.common.message.RequestMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @author hanson
 * @Date 2018/4/25
 * @Description:
 */

public class NettyMessageDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {

        if (byteBuf.readableBytes() < 4) {
            return;
        }

        int dataLength = byteBuf.getInt(byteBuf.readerIndex());

        if(byteBuf.readableBytes() < dataLength + 4){
            return;
        }

        byteBuf.skipBytes(4);

        RequestMessage request = new RequestMessage();
        request.read(byteBuf);

        list.add(request);
    }
}
