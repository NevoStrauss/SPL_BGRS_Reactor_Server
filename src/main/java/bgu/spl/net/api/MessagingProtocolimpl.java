package bgu.spl.net.api;

import bgu.spl.net.PassiveObjects.Course;
import bgu.spl.net.PassiveObjects.Message;
import bgu.spl.net.PassiveObjects.User;
import bgu.spl.net.srv.Database;

public class MessagingProtocolimpl implements MessagingProtocol<Message>{
    private boolean shouldTerminate=false;
    private final Short ADMINREG = 1;
    private final Short STUDENTREG = 2;
    private final Short LOGIN = 3;
    private final Short LOGOUT = 4;
    private final Short COURSEREG = 5;
    private final Short KDAMCHECK = 6;
    private final Short COURSESTAT = 7;
    private final Short STUDENTSTAT = 8;
    private final Short ISREGIDTERED = 9;
    private final Short UNREGISTER = 10;
    private final Short MYCOURSES = 11;
    private final Short ACK = 12;
    private final Short ERROR = 13;
    private final Database database = Database.getInstance();
    private User user=null;

    @Override
    public Message process(Message msg) {
        Short OP_CODE = msg.getOP_CODE();
        if ((user==null || !database.isLoggedIn(user.getUsername())) & OP_CODE>3)
            return new Message(ERROR,OP_CODE);
        Message output = null;
        switch (OP_CODE){
            case 1: output = admingReg(msg.getProperties());
            break;
            case 2: output = studentReg(msg.getProperties());
            break;
            case 3: output = login(msg.getProperties());
            break;
            case 4: output = logout();
            break;
            case 5: output = courseReg(msg.getAdditionalProps());
            break;
            case 6: output = kdamCheck(msg.getAdditionalProps());
            break;
            case 7: output = courseStatus(msg.getAdditionalProps());
            break;
            case 8: output = studentStatus(msg.getProperties());
            break;
            case 9: output = isRegistered(msg.getAdditionalProps());
            break;
            case 10: output = unregister(msg.getAdditionalProps());
            break;
            case 11: output = myCourses();
            break;
        }
        return output;
    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }

    private Message register(String[] props,Short COMMAND,boolean authorization){
        if (props.length<2 | database.isRegistered(props[0]))
            return new Message(ERROR,COMMAND);     //not enough arguments or already registered
        if (user == null)
            user = new User(props[0],props[1],authorization);
        database.register(user);
        return new Message(ACK, COMMAND);      //succeeded
    }

    private Message admingReg(String[] props){
        return register(props,ADMINREG,true);
    }

    private Message studentReg(String[] props){
        return register(props,STUDENTREG,false);
    }

    private Message login(String[] props){
        if (props.length<2 || (user==null & !database.isRegistered(props[0])) || !database.checkMatch(props[0],props[1]) || database.isLoggedIn(props[0]))
            return new Message(ERROR,LOGIN);    // not enough arguments, or not registered, or password doesnt match or already logged in
        if (user==null)
            user = database.getUserByName(props[0]);
       database.login(user);
       return new Message(ACK,LOGIN);
    }

    private Message logout(){
        database.logout(user);
        shouldTerminate = true;
        return new Message(ACK,LOGOUT);
    }

    private Message courseReg(Short courseNumber){
        if (!user.isAdmin() && database.registerToCourse(user, courseNumber))
            return new Message(ACK, COURSEREG);
        return new Message(ERROR,COURSEREG);
    }

    private Message kdamCheck(Short courseNumer){
        Course courseData = database.getCoursByNum(courseNumer);
        String[] data = {courseData.getKdamCoursesList().toString()};
        return new Message(ACK,data,KDAMCHECK);
    }

    private Message courseStatus(Short courseNumber){
        Course courseData = database.getCoursByNum(courseNumber);
        if (!user.isAdmin() | courseData==null) {
            return new Message(ERROR, COURSESTAT);
        }
        String[] data = {courseData.toString()};
        return new Message(ACK,data,COURSESTAT);
    }

    private Message studentStatus(String[] props){
        if (!user.isAdmin() | props.length<1)
            return new Message(ERROR,STUDENTSTAT);
        String[] data = {props[0],user.getCourseList().toString()};
        return new Message(ACK,data,STUDENTSTAT);
    }

    private Message isRegistered(Short courseNumber){
        Course course = database.getCoursByNum(courseNumber);
        if (user.isAdmin() | course==null)
            return new Message(ERROR,ISREGIDTERED);
        if(course.getRegisteredStudents().contains(user)) {
            String[] data = {"REGISTERED"};
            return new Message(ACK,data,ISREGIDTERED);
        }
        String[] data = {"NOT REGISTERED"};
        return new Message(ACK,data,ISREGIDTERED);
    }

    private Message unregister(Short courseNumber){
        if (!user.isAdmin() && database.unregisterFromCourse(user,courseNumber))
            return new Message(ACK,UNREGISTER);
        return new Message(ERROR,UNREGISTER);
    }

    private Message myCourses(){
        if (user.isAdmin())
            return new Message(ERROR,MYCOURSES);
        String[] data = {user.getCourseList().toString()};
        return new Message(ACK,data,MYCOURSES);
    }
}
