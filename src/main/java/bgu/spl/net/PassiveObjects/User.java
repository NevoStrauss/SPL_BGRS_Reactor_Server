package bgu.spl.net.PassiveObjects;

import java.util.LinkedList;
import java.util.List;

public class User {
    private final String username;
    private final String password;
    private final boolean authorization;
    private final List<Course> courseList;


    public User(String _username, String _password, boolean _authorization){
        username=_username;
        password=_password;
        authorization=_authorization;
        courseList=new LinkedList<>();
    }

    public List<Course> getCourseList() {
        return courseList;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public boolean isAuthorized() {
        return authorization;
    }

    public boolean equals(User user){
        return (username.equals(user.username) & password.equals(user.password) & authorization == user.authorization);
    }
}
