package bgu.spl.net.api;

import bgu.spl.net.PassiveObjects.Message;
import jdk.internal.net.http.common.Pair;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MessageEncoderDecoderimp implements MessageEncoderDecoder<Message>{
    private byte[] bytes = null; //start with 1k
    private int size = 0;
    private Short OP_CODE = null;
    private final ByteBuffer lengthBuffer = ByteBuffer.allocate(2);
    private int numOfZero;
    private final Map<Short, Pair<Integer,Boolean>> pattern;

    public MessageEncoderDecoderimp(){
        pattern = new HashMap<>();
        for (short i=1;i<=11;i++){
            if (i<4){
                pattern.put(i,new Pair(2,true));
            }
            else if(i==4 | i==11){
                pattern.put(i,new Pair(0,false));
            }
            else if(i==8){
                pattern.put(i,new Pair(1,true));
            }
            else
                pattern.put(i,new Pair<>(2,false));
        }
    }

    @Override
    public Message decodeNextByte(byte nextByte) {
        if (bytes == null) { //indicates that we are still reading the length
            lengthBuffer.put(nextByte);
            if (!lengthBuffer.hasRemaining()) { //we read 2 bytes and therefore can take the length
                lengthBuffer.flip();
                OP_CODE = lengthBuffer.getShort();
                if (OP_CODE == 4 | OP_CODE == 11)
                    return new Message(OP_CODE);
                boolean what = pattern.get(OP_CODE).second;
                if (!what) {
                    bytes = new byte[2];
                } else
                    bytes = new byte[1 << 10]; //starts with 1024
                numOfZero = pattern.get(OP_CODE).first;
                lengthBuffer.clear();
            }
        } else {
            if (nextByte == 0 | !(pattern.get(OP_CODE).second))
                numOfZero--;
            if (numOfZero == 0)
                return new Message(OP_CODE, new String(bytes, 0, size, StandardCharsets.UTF_8).split(" "));
            bytes[size] = nextByte;
            size++;
            if (size==bytes.length)
                bytes = Arrays.copyOf(bytes, size * 2);
        }
        return null;
    }

    @Override
    public byte[] encode(Message message) {
        return new byte[0];
    }

    private Message popMessage() {
        //notice that we explicitly requesting that the string will be decoded from UTF-8
        //this is not actually required as it is the default encoding in java.
        String str = new String(bytes, 0, size, StandardCharsets.UTF_8);
        String[] props = str.split(" ");
        Message result = new Message(OP_CODE, props);
        size = 0;
        return result;
    }
}
