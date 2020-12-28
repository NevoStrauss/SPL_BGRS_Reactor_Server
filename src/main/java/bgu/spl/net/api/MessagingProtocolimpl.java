package bgu.spl.net.api;

import bgu.spl.net.PassiveObjects.Message;
import bgu.spl.net.PassiveObjects.User;
import bgu.spl.net.srv.Database;

public class MessagingProtocolimpl implements MessagingProtocol<Message>{
    private boolean shouldTerminate=false;
    private final Short ACK = 12;
    private final Short ERROR = 13;
    private final Database database = Database.getInstance();
    private User user=null;

    @Override
    public Message process(Message msg) {
        shouldTerminate = msg.getOP_CODE()==4;
        return null;
    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }

    private Message admingReg(String[] props){
        if (props.length<2)
            return new Message(ERROR,new String[]{"1","Username or password are missing"});
        if (user == null)
            user = new User(props[0],props[1],true);
        boolean success = database.adminReg(user);
        if (success)
            return new Message(ACK, new String[]{"1"});
        return new Message(ERROR,new String[]{"1","Username already exists or Password is empty"});
    }

    private Message studentReg(String[] props){

        if (props.length<2)
            return new Message(ERROR,new String[]{"2","Username or password are missing"});
        if (user == null)
            user = new User(props[0],props[1],false);
        boolean success = database.studentReg(user);
        if (success)
            return new Message(ACK, new String[]{"2"});
        return new Message(ERROR,new String[]{"2","Username already exists or Password is empty"});
    }

    private Message login(String[] props){
        if (user==null || props.length<2 | !user.getPassword().equals(props[1]))
            return new Message(ERROR,new String[]{"3","Username or password are missing"});
        boolean success = database.login(user);
        if (success)
            return new Message(ACK, new String[]{"3"});
        return new Message(ERROR, new String[]{"3","Username is already logged in or not registered or password doesnt match"});
    }

    private Message logout(){
        if (user == null)
            return new Message(ERROR,new String[]{"3","User hasn't been registered yet"});
        boolean success = database.logout(user);
        if (success) {
            shouldTerminate = true;
            return new Message(ACK, new String[]{"3"});
        }
        return new Message(ERROR, new String[]{"3","Username is already logged in or not registered or password doesnt match"});
    }
}
