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
    private byte[] bytes = null; //start null
    private int size = 0;
    private Short OP_CODE = null;
    private final ByteBuffer lengthBuffer = ByteBuffer.allocate(2);
    private int numOfZero;
    private final Map<Short, Pair<Integer,Boolean>> pattern;


    /**
     * initializes pattern to decide in which way to decode the message from the socket.
     * pattern<Short,Pair>: Short stands for the OP_CODE and the pair decides which way to act.
     * Pair<Integer,Boolean>: the Integer stands for how many bytes to count, and the
     *                          boolean stands for which bytes to count.
     *                          true for counting '\0'
     *                          false for counting all kinds of bytes
     */
    public MessageEncoderDecoderimp(){
        pattern = new HashMap<>();
        for (short i=1;i<=11;i++){
            if (i<4){
                pattern.put(i, new Pair<>(2, true));
            }
            else if(i==4 | i==11){
                pattern.put(i, new Pair<>(0, false));
            }
            else if(i==8){
                pattern.put(i, new Pair<>(1, true));
            }
            else
                pattern.put(i, new Pair<>(2, false));
        }
    }

    /**
     * if bytes==null it means we are still reading the first 2 bytes which is the OP_CODE and
     * calls continueDecodeOP assistant function
     *
     * else
     *      calls continuteDecodeMsg assistant function
     *
     * @param nextByte the next byte to consider for the currently decoded Message
     * @return if the whole message was received - returns Message
     *          else returns null
     */
    @Override
    public Message decodeNextByte(byte nextByte) {
        if (bytes == null)  //indicates that we are still reading the length
            return continueDecodeOP(nextByte);
        else
            return continueDecodeMsg(nextByte);
    }

    /**
     *
     * @param nextByte the next byte to consider for the currently decoded OP_CODE
     * @return
     */
    private Message continueDecodeOP(byte nextByte){
        lengthBuffer.put(nextByte);
        if (!lengthBuffer.hasRemaining()) { //we read 2 bytes and therefore can take the length
            lengthBuffer.flip();
            OP_CODE = lengthBuffer.getShort();
            if (OP_CODE == 4 | OP_CODE == 11) {  //all message has been received
                lengthBuffer.clear();
                return new Message(OP_CODE);
            }
            boolean what = pattern.get(OP_CODE).second;     //decides which bytes to read
            if (!what)  //only 2 bytes left in the message
                bytes = new byte[2];
            else        //we dont know how many left, so start with 1kb
                bytes = new byte[1 << 10];
            numOfZero = pattern.get(OP_CODE).first;     //how many to count
            lengthBuffer.clear();
        }
        return null;    //if it wasn't the last byte of the OP_CODE
    }

    /**
     * pattern.get(OP_CODE).second is true if we need to count '\0', so the Message is
     * built from Strings. send to decodeStringMsg assistant function
     *
     * else we need to count all the bytes, which means only 2 bytes left and
     * the Message is built from shorts. send to decodeShortMsg assistant function.
     *
     * @param nextByte the next byte to consider for the currently decoded Message
     * @return if the whole message was received - returns Message
     *         else returns null
     */
    private Message continueDecodeMsg(byte nextByte) {
        if (pattern.get(OP_CODE).second)
            return decodeStringMsg(nextByte);
        else
            return decodeShortMsg(nextByte);
    }

    /**
     *
     * @param nextByte the next byte to consider for the currently decoded Message
     * @return if the last '\0' byte was received - returns Message
     *         else returns null
     */
    private Message decodeStringMsg(byte nextByte){
        if (nextByte == '\0')
            numOfZero--;
        if (numOfZero == 0) {        //op codes 1-3, 8
            Message output = new Message(OP_CODE,new String(bytes,0,size,StandardCharsets.UTF_8).split(" "));
            //reset bytes and size
            this.bytes = null;
            this.size=0;
            return output;
        }
        if (nextByte == '\0' & numOfZero == 1)  //needs to add a space - " "
            bytes[size] = " ".getBytes(StandardCharsets.UTF_8)[0];
        else
            bytes[size] = nextByte;     //nextByte isn't '\0'
        size++;
        if (size==bytes.length)
            bytes = Arrays.copyOf(bytes, size * 2);
        return null;
    }

    /**
     *
     * @param nextByte the next byte to consider for the currently decoded Message
     * @return if the last byte was received - returns Message
     *         else returns null
     */
    private Message decodeShortMsg(byte nextByte){
        bytes[size] = nextByte;
        size++;
        numOfZero--;
        if(numOfZero == 0) {    //op codes 5-7,9-10
            Message output = new Message(OP_CODE,bytesToShort(bytes));
            this.bytes = null;
            this.size=0;
            return output;
        }
        return null;
    }


    /**
     * the first 4 bytes, are the ACK\ERRROR OP_CODE and the OP_CODE which they refer to
     * @param message the message to encode
     * @return the array of bytes which is the encoded message
     */
    @Override
    public byte[] encode(Message message) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        byte[] OP_CODE = shortToBytes(message.getOP_CODE());    //12/13 ACK/ERROR
        byte[] COMMAND = shortToBytes(message.getAdditionalProps()); //1-11
        //the first 4 bytes
        bytes.write(OP_CODE[0]);
        bytes.write(OP_CODE[1]);
        bytes.write(COMMAND[0]);
        bytes.write(COMMAND[1]);
        String[] optional = message.getProperties();    //only for ACK messages
        if (optional.length!=0) {   //it is an ACK message with reply
            for (int i=0;i<optional.length;i++) {   //encode all the strings in optional
                byte[] encodedMessage = optional[i].getBytes(StandardCharsets.UTF_8);
                for (int j=0; j < encodedMessage.length;j++){   //add to bytes all the encoded bytes
                    bytes.write(encodedMessage[j]);
                }
            }
        }
        if (message.getOP_CODE()==12)   //add '\0' at the end of ACK
            bytes.write('\0');
        return bytes.toByteArray();
    }

    /**
     * thank you
     */
    private short bytesToShort(byte[] byteArray){
        short result = (short) ((byteArray[0] & 0xff) << 8);
        result += (short) (byteArray[1] & 0xff);
        return result;
    }

    /**
     * very much
     */
    private byte[] shortToBytes(short num){
        byte[] bytesArray = new byte[2];
        bytesArray[0] = (byte) ((num>>8)&0xFF);
        bytesArray[1] = (byte) (num&0xFF);
        return bytesArray;
    }

    /**
     * Assistance class to set the parsing pattern of the message
     * @param <K> Integer for how many bytes to read
     * @param <V> boolean for which kind of bytes to read
     */
    private class Pair<K,V> {
        private final K first;
        private final V second;

        public Pair(K _first, V _second){
            first = _first;
            second = _second;
        }
    }
}