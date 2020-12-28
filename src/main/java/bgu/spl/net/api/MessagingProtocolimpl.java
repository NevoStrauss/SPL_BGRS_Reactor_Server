package bgu.spl.net.api;

public class MessagingProtocolimpl implements MessagingProtocol{

    private boolean shouldTerminate=false;

    @Override
    public Object process(Object msg) {
        byte[] logout = {00,04};
        shouldTerminate = logout.equals(msg);
        return null;
    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }
}
