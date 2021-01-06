package bgu.spl.net.impl.BGRSServer;

import bgu.spl.net.api.MessagingProtocol;
import bgu.spl.net.impl.BGRSServer.PassiveObjects.Course;
import bgu.spl.net.impl.BGRSServer.PassiveObjects.Message;
import bgu.spl.net.impl.BGRSServer.PassiveObjects.User;

import java.util.*;

public class MessagingProtocolimpl implements MessagingProtocol<Message> {
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
        if (((user==null || !database.isLoggedIn(user.getUsername())) & OP_CODE>3))
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
        if (this.user != null |props.length<2 || database.isRegistered(props[0]))
            return new Message(ERROR,COMMAND);     //not enough arguments or already registered
        User user = new User(props[0],props[1],authorization);
        if (database.register(user))
            return new Message(ACK, COMMAND);      //succeeded
        else
            return new Message(ERROR,COMMAND);     //not enough arguments or already registered
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

    private Message kdamCheck(Short courseNumber){
        List<Course> kdamCourses = getKdamCoursesByOrder(courseNumber);
        String[] data = new String[kdamCourses.size()];
        int i = 0;
        for (Course curr : kdamCourses){
            data[i] = Short.toString(curr.getCourseNum());
            i++;
        }
        String[] output = {Arrays.toString(data).replace(", ",",")};
        return new Message(ACK,output,KDAMCHECK);
    }

    private List<Course> getKdamCoursesByOrder(short courseNumber){
        Course courseData = database.getCoursByNum(courseNumber);
        List<Short> kdamCourseNumbers = courseData.getKdamCoursesList();
        List<Course> kdamCourses = new LinkedList<>();
        for (Short shrt : kdamCourseNumbers){
            kdamCourses.add(database.getCoursByNum(shrt));
        }
        Comparator<Course> cmp = Comparator.comparingInt(Course::getSerialNumber);
        kdamCourses.sort(cmp);
        return kdamCourses;
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
        User user = database.getUserByName(props[0]);
        String output = "Student: " + props[0] + "\n" + "Courses: " + getCoursesNumbersByOrder(user).replace(", ",",");
        String[] data = {output};
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
        String[] data = {getCoursesNumbersByOrder(user).replace(", ",",")};
        return new Message(ACK,data,MYCOURSES);
    }

    private String getCoursesNumbersByOrder(User user) {
        List<Course> courses = user.getRegisteredCoursesByOrder();
        short[] coursesNumbers = new short[courses.size()];
        int counter = 0;
        for (Course course : courses){
            coursesNumbers[counter] = course.getCourseNum();
            counter++;
        }
        return Arrays.toString(coursesNumbers);
    }
}