package bgu.spl.net.impl.BGRSServer;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.impl.BGRSServer.PassiveObjects.Message;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MessageEncoderDecoderimp implements MessageEncoderDecoder<Message> {
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
            if (nextByte == '\0' | !(pattern.get(OP_CODE).second))
                numOfZero--;
            if (numOfZero == 0 && pattern.get(OP_CODE).second)
                return new Message(OP_CODE, new String(bytes, 0, size, StandardCharsets.UTF_8).split(" "));
            else if(numOfZero ==0)
                return new Message(OP_CODE, bytesToShort(bytes));
            bytes[size] = nextByte;
            size++;
            if (size==bytes.length)
                bytes = Arrays.copyOf(bytes, size * 2);
        }
        return null;
    }

    @Override
    public byte[] encode(Message message) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        byte[] OP_CODE = shortToBytes(message.getOP_CODE());
        byte[] COMMAND = shortToBytes(message.getAdditionalProps());
        bytes.write(OP_CODE[0]);
        bytes.write(OP_CODE[1]);
        bytes.write(COMMAND[0]);
        bytes.write(COMMAND[1]);
        String[] optional = message.getProperties();
        if (optional.length!=0) {
            for (int i=0;i<optional.length;i++) {
                byte[] encodedMessage = optional[i].getBytes(StandardCharsets.UTF_8);
                for (int j=0; j < encodedMessage.length;j++){
                    bytes.write(encodedMessage[j]);
                }
            }
        }
        if (message.getOP_CODE()==12)
            bytes.write('\0');
        return bytes.toByteArray();
    }

    private short bytesToShort(byte[] byteArray){
        short result = (short) ((byteArray[0] & 0xff) << 8);
        result += (short) (byteArray[1] & 0xff);
        return result;
    }

    private byte[] shortToBytes(short num){
        byte[] bytesArray = new byte[2];
        bytesArray[0] = (byte) ((num>>8)&0xFF);
        bytesArray[1] = (byte) (num&0xFF);
        return bytesArray;
    }

    private class Pair<K,V> {
        private K first;
        private V second;

        public Pair(K _first, V _second){
            first = _first;
            second = _second;
        }
    }
}
