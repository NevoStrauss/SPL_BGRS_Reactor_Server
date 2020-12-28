package bgu.spl.net.PassiveObjects;

public class Message {
    private final Short OP_CODE;
    private final String[] properties;

    public Message(Short _OP_CODE, String[] props){
        OP_CODE = _OP_CODE;
        properties = props;
    }

    public Message(Short _OP_CODE){
        OP_CODE=_OP_CODE;
        properties = new String[0];
    }

    public Short getOP_CODE() {
        return OP_CODE;
    }

    public String[] getProperties() {
        return properties;
    }
}
