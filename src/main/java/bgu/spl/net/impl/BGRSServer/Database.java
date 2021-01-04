package bgu.spl.net.impl.BGRSServer;

import bgu.spl.net.impl.BGRSServer.PassiveObjects.Course;
import bgu.spl.net.impl.BGRSServer.PassiveObjects.User;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

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
    private List<Course> coursesList;
    private List<User> registeredUserList;
    private List<User> loggedInUserList;

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
        coursesList = new LinkedList<>();
        File courseFile = new File(coursesFilePath);
        int courseSerialNumber = 0;
        try (Scanner myScanner = new Scanner(courseFile)){
            while(myScanner.hasNextLine()){
                String currCourse = myScanner.nextLine();
                coursesList.add(new Course(currCourse, courseSerialNumber));
            }
        }catch (IOException e){
            e.printStackTrace();
            return false;
        }
        registeredUserList = new LinkedList<>();
        loggedInUserList = new LinkedList<>();
        return true;
    }

    public boolean isRegistered(User user){
        for (User curr: registeredUserList) {
            if (curr.equals(user))
                return true;
        }
        return false;
    }

    public boolean checkMatch(String user, String password){
        for (User curr: registeredUserList) {
            if (curr.getUsername().equals(user) && curr.getPassword().equals(password))
                return true;
        }
        return false;
    }

    public boolean isRegistered(String username){
        for (User curr: registeredUserList) {
            if (curr.getUsername().equals(username))
                return true;
        }
        return false;
    }

    public boolean isLoggedIn(String username){
        for (User curr: loggedInUserList) {
            if (curr.getUsername().equals(username))
                return true;
        }
        return false;
    }

    public void register(User user){
        registeredUserList.add(user);
    }

    public User getUserByName(String username){
        for (User user:registeredUserList) {
            if (user.getUsername().equals(username))
                return user;
        }
        return null;
    }

    public void login(User user){
        loggedInUserList.add(user);
    }

    public void logout(User user){
        loggedInUserList.remove(user);
    }

    public boolean isCourseExist(Short courseNumber){
        for (Course course:coursesList){
            if (course.getCourseNum() == courseNumber){
                return true;
            }
        }
        return false;
    }

    public boolean isCourseAvailable(Short courseNumber){
        for (Course course:coursesList){
            if (course.getCourseNum() == courseNumber) {
                synchronized (course) {
                    return (course.getNumOfMaxStudents() > course.getRegisteredStudents().size());
                }
            }
        }
        return false;
    }

    public Course getCoursByNum(Short courseNumber){
        for (Course course:coursesList){
            if (course.getCourseNum() == courseNumber)
                return course;
        }
        return null;
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
        for (Course course:coursesList){
            if (course.getCourseNum() == courseNumber) {
                if (checkKdam(user,course) & course.getNumOfMaxStudents() > course.getRegisteredStudents().size()) {
                    synchronized (course) {
                        if (course.getNumOfMaxStudents() > course.getRegisteredStudents().size())
                            return course.registerUser(user);
                    }
                }
            }
        }
        return false;   //no course with courseNumber or
        // not enough place in course or
        // dont have all the kdam courses.
    }

    public boolean unregisterFromCourse(User user, Short courseNumber){
        for (Course course:coursesList){
            if (course.getCourseNum() == courseNumber) {
                synchronized (course) {
                    return course.unregisterUser(user);
                }
            }
        }
        return false;   //no course with courseNumber or
        // not enough place in course or
        // dont have all the kdam courses.
    }
}