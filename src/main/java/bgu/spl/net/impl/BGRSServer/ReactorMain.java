package bgu.spl.net.impl.BGRSServer;

import bgu.spl.net.impl.BGRSServer.PassiveObjects.Message;

public class ReactorMain {
    public static void main(String[] args) {
        int numOfThreads = Integer.valueOf(args[1]);
        int port = Integer.valueOf(args[0]);
        Reactor<Message> server = new Reactor<>(
                numOfThreads,
                port,
                () -> new MessagingProtocolimpl(),
                () -> new MessageEncoderDecoderimp());
        server.serve();
    }
}
