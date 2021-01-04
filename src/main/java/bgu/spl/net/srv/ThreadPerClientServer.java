package bgu.spl.net.srv;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.MessagingProtocol;

import java.util.function.Supplier;

public class ThreadPerClientServer<T> extends BaseServer<T> {

    public ThreadPerClientServer(int port,
                                 Supplier<MessagingProtocol<T>> protocolFactory,
                                 Supplier<MessageEncoderDecoder<T>> encoderDecoderFactory){
        super(port,protocolFactory,encoderDecoderFactory);
    }
    @Override
    protected void execute(BlockingConnectionHandler<T> handler) {
        new Thread(handler).start();
    }
}