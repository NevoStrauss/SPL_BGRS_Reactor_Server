package bgu.spl.net.impl.BGRSServer;

import bgu.spl.net.impl.BGRSServer.PassiveObjects.Course;
import bgu.spl.net.impl.BGRSServer.PassiveObjects.User;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Passive object representing the Database where all courses and users are stored.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You can add private fields and methods to this class as you see fit.
 */
public class Database {
    private static class singleton{private static final Database singleton = new Database();}
    private ConcurrentHashMap<String, User> registeredUserMap;
    private ConcurrentHashMap<String,User> loggedInUserMap;
    private ConcurrentHashMap<Short, Course> coursesMap;

    //to prevent user from creating new Database
    private Database() {
        initialize("./Courses.txt");
    }

    /**
     * Retrieves the single instance of this class.
     */
    public static Database getInstance() {
        return singleton.singleton;
    }

    /**
     * loades the courses from the file path specified
     * into the Database, returns true if successful.
     */
    boolean initialize(String coursesFilePath) {
        coursesMap = new ConcurrentHashMap<>();
        File courseFile = new File(coursesFilePath);
        int courseSerialNumber = 0;     //indicates the order in the file
        try (Scanner myScanner = new Scanner(courseFile)){
            while(myScanner.hasNextLine()){
                String currCourse = myScanner.nextLine();   //get the string of the current course
                Course curr = new Course(currCourse, courseSerialNumber);   //creates a course with the string and the serial number
                coursesMap.putIfAbsent(curr.getCourseNum(),curr);       //put the new course in courseMap
                courseSerialNumber++;
            }
        }catch (IOException e){
            e.printStackTrace();
            return false;
        }
        registeredUserMap = new ConcurrentHashMap<>();
        loggedInUserMap = new ConcurrentHashMap<>();
        return true;
    }

    /**
     * checks if a given password and username is the actual password of the username
     * @param user  to check its password
     * @param password  the password to compare
     * @return  true if the passwords is equals
     */
    public boolean checkMatch(String user, String password){
        String toCompare = registeredUserMap.get(user).getPassword();
        return toCompare.equals(password);
    }

    /**
     * chcks if a given username is registered to the server
     * @param username  the username to check
     * @return true if registered
     */
    public boolean isRegistered(String username){
        return registeredUserMap.containsKey(username);

    }

    /**
     * chcks if a given username is logged in to the server
     * @param username  the username to check
     * @return true if logged in
     */
    public boolean isLoggedIn(String username){
        return loggedInUserMap.containsKey(username);
    }

    /**
     * registers a user to the server
     * @param user user to register
     * @return  true if succeeds
     */
    public boolean register(User user){
        User user1 = registeredUserMap.putIfAbsent(user.getUsername(),user);
        //if user1 is null the it succeeded, otherwise there is already a user with the given user.username
        return user1 == null;
    }

    /**
     * get a user that is registered to the server by his username
     * @param username the username of the user
     * @return the User if such exists, otherwise null
     */
    public User getUserByName(String username){
        return registeredUserMap.get(username);
    }

    /**
     * logs in a user to the server
     * @param user  the user to login
     * @return true if succeeded
     */
    public boolean login(User user){
        User user1 = loggedInUserMap.putIfAbsent(user.getUsername(),user);
        return user1 == null;
    }

    /**
     * logs out a user from the server
     * @param user the user to logout
     */
    public void logout(User user){
        loggedInUserMap.remove(user.getUsername());
    }

    /**
     * gets a course by his number
     * @param courseNumber the coursenumber of the course to get
     * @return  the course with the relevant courseNumber. if no such exists returns null
     */
    public Course getCoursByNum(Short courseNumber){
        return coursesMap.get(courseNumber);
    }

    /**
     * checks if a user can register to a course, aka is registered to all of his kdamCourses already
     * @param user  the user to check
     * @param course    the course to check its kdamCourses
     * @return true if the user can register to it, otherwise false
     */
    public boolean checkKdam(User user, Course course){
        List<Course> userCourses = user.getCourseList();    //gets the user's courses list
        List<Short> userCoursesNumbers = new LinkedList<>();    //get all the courseNumbers of the user's courses
        for (Course course1 : userCourses){
            userCoursesNumbers.add(course1.getCourseNum());
        }
        for (Short curr:course.getKdamCoursesList()) {  //goes through all the courses in the course kdam courses list
            if (!userCoursesNumbers.contains(curr)) //if there is a course in the courses kdam course list, that the
                                                    //user isn't registered to, return false
                return false;
        }
        return true;
    }

    /**
     * registers a user to a course
     * @param user the user to register
     * @param courseNumber the course number of the course
     * @return  true if succeeded, otherwise false
     */
    public boolean registerToCourse(User user, Short courseNumber){
        Course curr = coursesMap.get(courseNumber);
        //if there isn't such course, or the user doesn't have all the kdam courses, or there isn't any place left return false
        if (curr==null || !checkKdam(user,curr) || curr.getNumOfMaxStudents() == curr.getNumOfRegisteredStudents())
            return false;
        else{
            //sycnchronized becouse several clients can register at the same time and then there would be too much
            //students in the course
            synchronized (curr){
                //double check for the place in the course
                if (curr.getNumOfMaxStudents() > curr.getNumOfRegisteredStudents())
                    return curr.registerUser(user); //try to register the user to the course
            }
        }
        return false;
    }

    /**
     * unregisters a user from a course
     * @param user the user no unregister
     * @param courseNumber the course number to unregister from
     * @return true if succeeds, otherwise false
     */
    public boolean unregisterFromCourse(User user, Short courseNumber){
        Course curr = coursesMap.get(courseNumber);
        //checks if there is such course
        if (curr!=null) {
            synchronized (curr) {
                return curr.unregisterUser(user);   //try to unregister the user from the course
            }
        }
        return false;   //no course with courseNumber or
    }
}