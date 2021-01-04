package bgu.spl.net.impl.BGRSServer;

import bgu.spl.net.impl.BGRSServer.PassiveObjects.Message;
import bgu.spl.net.srv.ThreadPerClientServer;

public class TPCMain {
    public static void main(String[] args) {
        ThreadPerClientServer<Message> server = new ThreadPerClientServer<>(
                Integer.parseInt(args[0]),
                ()->new MessagingProtocolimpl(),
                ()->new MessageEncoderDecoderimp());
        server.serve();
    }
}