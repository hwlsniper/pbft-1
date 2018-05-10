package com.primeledger.higgs.pbft.common.message;

import io.netty.buffer.ByteBuf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * @author hanson
 * @Date 2018/4/25
 * @Description:
 */

public class RequestMessage extends BaseMessage {

    /**
     * client time stamp
     */
    private long timeaStamp;

    /**
     * client operation need to do
     */
    private byte[] operation;


    public long getTimeaStamp() {
        return timeaStamp;
    }

    public void setTimeaStamp(long timeaStamp) {
        this.timeaStamp = timeaStamp;
    }

    public byte[] getOperation() {
        return operation;
    }

    public void setOperation(byte[] operation) {
        this.operation = operation;
    }


//    public byte[] getSignature() {
//        return signature;
//    }
//
//    public void setSignature(byte[] signature) {
//        this.signature = signature;
//    }

    @Override
    public void read(ByteBuf byteBuf){
        super.read(byteBuf);
        timeaStamp = byteBuf.readLong();

        int opLength = byteBuf.readInt();
        int signLength = byteBuf.readInt();

        operation = new byte[opLength];
        byteBuf.readBytes(operation);

        signature = new byte[signLength];
        byteBuf.readBytes(signature);
    }

    @Override
    public void write(ByteBuf byteBuf) throws IOException {
//        super.write(byteBuf);
//        byteBuf.writeLong(timeaStamp);
//
//        byteBuf.writeInt(operation.length);
//        byteBuf.writeInt(signature.length);
//
//        byteBuf.writeBytes(operation);
//        byteBuf.writeBytes(signature);
        byte[] data = getData();
        byteBuf.writeInt(data.length);
        byteBuf.writeBytes(data);

    }

    public byte[] getData() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        super.write(dos);
        dos.writeLong(timeaStamp);

        dos.writeInt(operation.length);
        dos.writeInt(signature.length);

        dos.write(operation);
        dos.write(signature);

        byte[] data = bos.toByteArray();
        return data;
    }

    @Override
    public byte[] getSerializeMessage() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);

        try {
            super.write(dos);
            dos.writeInt(operation.length);
            dos.write(operation);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }


        return bos.toByteArray();
    }

}
