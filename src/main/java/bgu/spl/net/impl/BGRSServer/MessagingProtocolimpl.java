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

    /**
     * checks which opcode was received and execute according to it
     * @param msg the received message
     * @return response to the message
     */
    @Override
    public Message process(Message msg) {
        Short OP_CODE = msg.getOP_CODE();
        //(hasn't logged) and it isn't ADMINREG, STUDENTREG or LOGIN OP_CODES
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

    /**
     * a function that is used by admingReg and studentReg. It registers a user to the database
     * @param props the array with the username (@props[0]) and password (@props[1]) of the user
     * @param COMMAND   if it is ADMINGREG or STUDENTREG
     * @param authorization true for adming and false for student
     * @return  an ACK message if succedes and ERROR if fails
     */
    private Message register(String[] props,Short COMMAND,boolean authorization){
        if (this.user != null |props.length<2 || database.isRegistered(props[0]))
            return new Message(ERROR,COMMAND);     //client is already logged in or not enough arguments or already registered
        User user = new User(props[0],props[1],authorization);  //creates new user to register to the database
        if (database.register(user))    //succeeded in registering to the database
            return new Message(ACK, COMMAND);
        else
            return new Message(ERROR,COMMAND);     //failed in registering to the database
    }

    /**
     * registering an admin to the database
     * @param props the array with the username (@props[0]) and password (@props[1]) of the admin
     * @return  an ACK message if succedes and ERROR if fails
     */
    private Message admingReg(String[] props){
        return register(props,ADMINREG,true);
    }

    /**
     * registering a student to the database
     * @param props the array with the username (@props[0]) and password (@props[1]) of the student
     * @return  an ACK message if succedes and ERROR if fails
     */
    private Message studentReg(String[] props){
        return register(props,STUDENTREG,false);
    }

    /**
     * logs in a user to the database
     * @param props the array with the username (@props[0]) and password (@props[1]) of the student
     * @return  an ACK message if succedes and ERROR if fails
     */
    private Message login(String[] props){
        //check if there aren't enough arguments, or user isn't registered, or the given password doesn't match
        // to the user's password or user is already logged in
        if (props.length<2 || (user==null & !database.isRegistered(props[0])) ||
                !database.checkMatch(props[0],props[1]) || database.isLoggedIn(props[0]))
            return new Message(ERROR,LOGIN);
        if (user==null)
            user = database.getUserByName(props[0]);    //get the user with the propreate username from database
        if (database.login(user))
            return new Message(ACK,LOGIN);
        return new Message(ERROR,LOGIN);
    }

    /**
     * logs out the client from the server, also terminates the protocol
     * @return  an ACK message
     */
    private Message logout(){
        database.logout(user);  //logout from database
        shouldTerminate = true; //change shouldTerminate to true
        return new Message(ACK,LOGOUT);
    }

    /**
     * registers the client to a course
     * @param courseNumber the course number to register to
     * @return  an ACK message if succedes and ERROR if fails
     */
    private Message courseReg(Short courseNumber){
        //checks that the client isn't an admin and tries to register it to coursse
        if (!user.isAdmin() && database.registerToCourse(user, courseNumber))
            return new Message(ACK, COURSEREG);
        return new Message(ERROR,COURSEREG);
    }

    /**
     * check kdam courses of a given course
     * @param courseNumber the course number to return its kdam courses
     * @return  an ACK message if succedes and ERROR if fails
     */
    private Message kdamCheck(Short courseNumber){
        List<Course> kdamCourses = getKdamCoursesByOrder(courseNumber); //gets the kdam courses in order
        if (kdamCourses==null)  //if it is null, no course with courseNumber exists and returns error
            return new Message(ERROR,KDAMCHECK);
        String[] data = new String[kdamCourses.size()];
        int i = 0;
        //put in data all the course numbers in order
        for (Course curr : kdamCourses){
            data[i] = Short.toString(curr.getCourseNum());
            i++;
        }
        String[] output = {Arrays.toString(data).replace(", ",",")};    //delete all the spaces
        return new Message(ACK,output,KDAMCHECK);
    }

    /**
     * assistant function for kdamCheck. it returns all the kdam courses of a given course
     * by order in the Courses.txt file.
     * @param courseNumber the course number to return its kdam courses
     * @return  null if no course with courseNumber exists, otherwise a list with all the kdamCourses sorted by
     *          serial number
     */
    private List<Course> getKdamCoursesByOrder(short courseNumber){
        Course courseData = database.getCoursByNum(courseNumber);   //get the course from the database
        if (courseData==null)   //null if no such course exists
            return null;
        List<Short> kdamCourseNumbers = courseData.getKdamCoursesList();    //get the kdam courses list by short
        List<Course> kdamCourses = new LinkedList<>();
        //make a list of Course of the short value
        for (Short shrt : kdamCourseNumbers){
            kdamCourses.add(database.getCoursByNum(shrt));
        }
        Comparator<Course> cmp = Comparator.comparingInt(Course::getSerialNumber); //comparator to compare courses
                                                                                    //by serial number
        kdamCourses.sort(cmp);  //sort the Courses list
        return kdamCourses;
    }

    /**
     *  an admin OP CODE, checks the status of a given course
     * @param courseNumber the course number to checks its status
     * @return  an ACK message if succedes and ERROR if fails
     */
    private Message courseStatus(Short courseNumber){
        Course courseData = database.getCoursByNum(courseNumber);   //gets the course from database
        if (!user.isAdmin() | courseData==null) {   //if the client isn't an admin or there isn't such course return ERROR
            return new Message(ERROR, COURSESTAT);
        }
        String[] data = {courseData.toString()};
        return new Message(ACK,data,COURSESTAT);
    }

    /**
     * an admin OP CODE, checks the status of a given student
     * @param props holds the username at props[0]
     * @return  an ACK message if succedes and ERROR if fails
     */
    private Message studentStatus(String[] props){
        if (!user.isAdmin() | props.length<1)   //if the client isn't an admin or there aren't enough arguments return ERROR
            return new Message(ERROR,STUDENTSTAT);
        User user = database.getUserByName(props[0]);   //get the user to check its status
        String output = "Student: " + props[0] + "\n" + "Courses: " + getCoursesNumbersByOrder(user).replace(", ",",");
        String[] data = {output};
        return new Message(ACK,data,STUDENTSTAT);
    }

    /**
     * a student OP CODE, checks if the student is registered to a given course
     * @param courseNumber the course number to check if the user is registered to
     * @return "REGISTERED" if registered, "NOT REGISTERED" if not registered or ERROR message if fails
     */
    private Message isRegistered(Short courseNumber){
        Course course = database.getCoursByNum(courseNumber);   // get the course from database
        if (user.isAdmin() | course==null)  //if the user is an admin or there isn't such course return ERROR
            return new Message(ERROR,ISREGIDTERED);
        if(course.getRegisteredStudents().contains(user)) { //if the user is registered
            String[] data = {"REGISTERED"}; //return "REGISTERED"
            return new Message(ACK,data,ISREGIDTERED);
        }
        String[] data = {"NOT REGISTERED"}; //else return "NOT REGISTERED"
        return new Message(ACK,data,ISREGIDTERED);
    }

    /**
     * a student OP CODE, unregisters the student from a given course
     * @param courseNumber the course number to unregister from
     * @return  an ACK message if succedes and ERROR if fails
     */
    private Message unregister(Short courseNumber){
        //if the user isn't an admin and it succeeded in unregistering from the course return ACK
        if (!user.isAdmin() && database.unregisterFromCourse(user,courseNumber))
            return new Message(ACK,UNREGISTER);
        return new Message(ERROR,UNREGISTER);
    }

    /**
     * a student OP CODE
     * @return a list of the courses the student is registered to by order in Courses.txt
     */
    private Message myCourses(){
        if (user.isAdmin()) //if client is an admin return ERROR
            return new Message(ERROR,MYCOURSES);
        String[] data = {getCoursesNumbersByOrder(user).replace(", ",",")};
        return new Message(ACK,data,MYCOURSES);
    }

    /**
     *
     * @param user to get his courses
     * @return  a list of courses numbers that the user is registered to ordered by the place in Courses.txt,
     *          returns as String
     */
    private String getCoursesNumbersByOrder(User user) {
        List<Course> courses = user.getRegisteredCoursesByOrder();  //get the courses by order
        short[] coursesNumbers = new short[courses.size()];
        int counter = 0;
        //copy the courses numbers to coursesNumbers
        for (Course course : courses){
            coursesNumbers[counter] = course.getCourseNum();
            counter++;
        }
        return Arrays.toString(coursesNumbers);
    }
}