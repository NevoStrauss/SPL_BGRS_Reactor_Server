package bgu.spl.net.srv;

import bgu.spl.net.PassiveObjects.Course;
import bgu.spl.net.PassiveObjects.User;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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
    private List<User> userList;

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
        userList = new LinkedList<>();
        return true;
    }

    public static void main(String[] args) {
        Database d = getInstance();
        System.out.println(d.coursesList);
    }
}