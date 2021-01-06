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
        int courseSerialNumber = 0;
        try (Scanner myScanner = new Scanner(courseFile)){
            while(myScanner.hasNextLine()){
                String currCourse = myScanner.nextLine();
                Course curr = new Course(currCourse, courseSerialNumber);
                coursesMap.putIfAbsent(curr.getCourseNum(),curr);
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

    public boolean checkMatch(String user, String password){
        String toCompare = registeredUserMap.get(user).getPassword();
        return toCompare.equals(password);
    }

    public boolean isRegistered(String username){
        return registeredUserMap.containsKey(username);

    }

    public boolean isLoggedIn(String username){
        return loggedInUserMap.containsKey(username);
    }

    public boolean register(User user){
        User user1 = registeredUserMap.putIfAbsent(user.getUsername(),user);
        return user1 == null;
    }

    public User getUserByName(String username){
        return registeredUserMap.get(username);
    }

    public void login(User user){
        loggedInUserMap.putIfAbsent(user.getUsername(),user);
    }

    public void logout(User user){
        loggedInUserMap.remove(user.getUsername());
    }

    public Course getCoursByNum(Short courseNumber){
        return coursesMap.get(courseNumber);
    }

    public boolean checkKdam(User user, Course course){
        List<Course> userCourses = user.getCourseList();
        List<Short> userCoursesNumbers = new LinkedList<>();
        for (Course course1 : userCourses){
            userCoursesNumbers.add(course1.getCourseNum());
        }
        for (Short curr:course.getKdamCoursesList()) {
            if (!userCoursesNumbers.contains(curr))
                return false;
        }
        return true;
    }


    public boolean registerToCourse(User user, Short courseNumber){
        Course curr = coursesMap.get(courseNumber);
        if (curr==null || !checkKdam(user,curr) || curr.getNumOfMaxStudents() == curr.getNumOfRegisteredStudents())
            return false;
        else{
            synchronized (curr){
                if (curr.getNumOfMaxStudents() > curr.getNumOfRegisteredStudents())
                    return curr.registerUser(user);
            }
        }
        return false;   //no course with courseNumber or
                        // not enough place in course or
                        // dont have all the kdam courses.
    }

    public boolean unregisterFromCourse(User user, Short courseNumber){
        Course curr = coursesMap.get(courseNumber);
        if (curr!=null) {
            synchronized (curr) {
                return curr.unregisterUser(user);
            }
        }
        return false;   //no course with courseNumber or
                        //user wasn't registered to the course
    }
}