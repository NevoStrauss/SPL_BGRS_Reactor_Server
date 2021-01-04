package bgu.spl.net.impl.BGRSServer;

import bgu.spl.net.impl.BGRSServer.PassiveObjects.Message;
import bgu.spl.net.srv.Reactor;

public class ReactorMain {
    public static void main(String[] args) {
        int numOfThreads = Integer.parseInt(args[1]);
        int port = Integer.parseInt(args[0]);
        Reactor<Message> server = new Reactor<>(
                numOfThreads,
                port,
                () -> new MessagingProtocolimpl(),
                () -> new MessageEncoderDecoderimp());
        server.serve();
    }
}