package bgu.spl.net.impl.BGRSServer.PassiveObjects;

import java.util.LinkedList;
import java.util.List;

public class User {
    private final String username;
    private final String password;
    private final boolean authorization;
    private final List<Short> courseNumberList;

    public User(String _username, String _password, boolean _authorization){
        username=_username;
        password=_password;
        authorization=_authorization;
        courseNumberList=new LinkedList<>();
    }

    public List<Short> getCourseList() {
        return courseNumberList;
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

    public boolean equals(User user){
        return (username.equals(user.username) & password.equals(user.password) & authorization == user.authorization);
    }

    public void addCourseToList(Short courseNum){
        courseNumberList.add(courseNum);
    }

    public void removeCourseFromList(Short courseNumber){
        courseNumberList.remove(courseNumber);
    }
}
