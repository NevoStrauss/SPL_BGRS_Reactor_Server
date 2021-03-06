package bgu.spl.net.impl.BGRSServer.PassiveObjects;

import java.util.Arrays;

public class Message {
    private final Short OP_CODE;
    private final String[] properties;
    private final Short additionalProps;


    public Message(Short _OP_CODE, String[] props, Short _additionalProps){
        OP_CODE = _OP_CODE;
        properties = props;
        additionalProps = _additionalProps;
    }

    public Message(Short _OP_CODE, String[] props){
        OP_CODE = _OP_CODE;
        properties = props;
        additionalProps = null;
    }

    public Message(Short _OP_CODE){
        OP_CODE=_OP_CODE;
        properties = new String[0];
        additionalProps = null;
    }

    public Message(Short _OP_CODE, Short _addtionalProps){
        OP_CODE=_OP_CODE;
        properties = new String[0];
        additionalProps = _addtionalProps;
    }

    public Short getOP_CODE() {
        return OP_CODE;
    }

    public String[] getProperties() {
        return properties;
    }

    public Short getAdditionalProps(){return additionalProps;}

    public String toString(){
        return OP_CODE + "\n" + Arrays.toString(properties) + "\n" + additionalProps;
    }
}