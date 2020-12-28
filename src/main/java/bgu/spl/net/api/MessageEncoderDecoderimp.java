package bgu.spl.net.api;

public class MessageEncoderDecoderimp implements MessageEncoderDecoder{
    @Override
    public Object decodeNextByte(byte nextByte) {
        return null;
    }

    @Override
    public byte[] encode(Object message) {
        return new byte[0];
    }
}
