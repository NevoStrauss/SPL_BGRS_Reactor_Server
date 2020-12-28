package bgu.spl.net.PassiveObjects;

import java.util.LinkedList;
import java.util.List;

public class Course {
    private short courseNum;
    private String courseName;
    private List<Short> kdamCoursesList;
    private int numOfMaxStudents;
    private int numOfRegisteredStudents = 0;
    private final List<User> registeredStudents = new LinkedList<>();

    public Course(short _courseNum, String _courseName, List<Short> _KdamCoursesList, int _numOfMaxStudents){
        courseNum = _courseNum;
        courseName = _courseName;
        kdamCoursesList = _KdamCoursesList;
        numOfMaxStudents = _numOfMaxStudents;

    }

    public Course(String course){
        String[] courseData = course.split("\\|");
        courseNum = Short.parseShort(courseData[0]);
        courseName = courseData[1];
        String[] kdamCoursesListString = courseData[2].substring(1,courseData[2].length()-1).split(",");
        kdamCoursesList = new LinkedList<>();
        for (String s : kdamCoursesListString){
            if (!s.equals(""))
                kdamCoursesList.add(Short.valueOf(s));
        }
        numOfMaxStudents = Integer.parseInt(courseData[3]);
    }

    public int getCourseNum() {
        return courseNum;
    }

    public String getCourseName() {
        return courseName;
    }

    public List<Short> getKdamCoursesList() {
        return kdamCoursesList;
    }

    public int getNumOfMaxStudents() {
        return numOfMaxStudents;
    }

    public List<User> getRegisteredStudents(){
        return registeredStudents;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public void setCourseNum(short courseNum) {
        this.courseNum = courseNum;
    }

    public void setKdamCoursesList(List<Short> kdamCoursesList) {
        this.kdamCoursesList = kdamCoursesList;
    }

    public void setNumOfMaxStudents(int numOfMaxStudents) {
        this.numOfMaxStudents = numOfMaxStudents;
    }

    public boolean registerUser(User user){
        if (!registeredStudents.contains(user) & numOfRegisteredStudents<numOfMaxStudents){
            registeredStudents.add(user);
            numOfRegisteredStudents++;
            return true;
        }
        return false;
    }

    public String toString(){
        return ("\n"+ "\n" +"Course name: "+ courseName + "\n" + "Course number: " + courseNum + "\n" + "Kdam courses: "
                + kdamCoursesList.toString() + "\n" +"Number of max students: " + numOfMaxStudents
                + "\n" + "Number of registered students: " +numOfRegisteredStudents+ "\n");
    }
}
