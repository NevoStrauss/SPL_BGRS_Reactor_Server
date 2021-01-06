package bgu.spl.net.impl.BGRSServer.PassiveObjects;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class User {
    private final String username;
    private final String password;
    private final boolean authorization;
    private final List<Course> registeredCourses;
    private boolean sorted = false;

    public User(String _username, String _password, boolean _authorization){
        username=_username;
        password=_password;
        authorization=_authorization;
        registeredCourses=new LinkedList<>();
    }

    public List<Course> getCourseList() {
        return registeredCourses;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public boolean isAdmin() {
        return authorization;
    }

    public void addCourseToList(Course course){
        registeredCourses.add(course);
        sorted = false;
    }

    public void removeCourseFromList(Course course){
        registeredCourses.remove(course);
    }

    public String toString(){
        return username;
    }

    public List<Course> getRegisteredCoursesByOrder(){
        if (!sorted){
            Comparator<Course> cmp = Comparator.comparingInt(Course::getSerialNumber);
            registeredCourses.sort(cmp);
        }
        return registeredCourses;
    }
}