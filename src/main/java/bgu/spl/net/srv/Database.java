package bgu.spl.net.srv;

import bgu.spl.net.PassiveObjects.Course;
import bgu.spl.net.PassiveObjects.User;

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
        try (Scanner myScanner = new Scanner(courseFile)){
            while(myScanner.hasNextLine()){
                String currCourse = myScanner.nextLine();
                coursesList.add(new Course(currCourse));
            }
        }catch (IOException e){
            e.printStackTrace();
            return false;
        }
        registeredUserList = new LinkedList<>();
        loggedInUserList = new LinkedList<>();
        return true;
    }

    private boolean isRegistered(User user){
        for (User curr: registeredUserList) {
            if (curr.equals(user))
                return true;
        }
        return false;
    }

    private boolean isLoggedIn(User user){
        for (User curr: loggedInUserList) {
            if (curr.getUsername().equals(user.getUsername()))
                return true;
        }
        return false;
    }

    private boolean register(User user){
            if (isRegistered(user))
                return false;
        registeredUserList.add(user);
        return true;
    }

    public boolean adminReg(User user){
        return register(user);
    }

    public boolean studentReg(User user){
        return register(user);
    }

    private User getUserByName(String username){
        for (User user:registeredUserList) {
            if (user.getUsername().equals(username))
                return user;
        }
        return null;
    }

    private boolean checkMatch(String username, String password){
        for (User user:registeredUserList) {
            if (user.getUsername().equals(username) && !user.getPassword().equals(password))
                return false;
        }
        return true;
    }

    public boolean login(User user){
        if (isLoggedIn(user) | !isRegistered(user))
            return false;
        loggedInUserList.add(user);
        return true;
    }

    public boolean logout(User user){
        if (!isLoggedIn(user) | !isRegistered(user))
            return false;
        loggedInUserList.remove(user);
        return true;
    }
}