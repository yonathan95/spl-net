package bgu.spl.net;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

/**
 * Passive object representing the Database where all courses and users are stored.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You can add private fields and methods to this class as you see fit.
 */
public class Database {
    private HashMap<String,String> students;
    private HashMap<String,String> administrators;
    private HashMap<String,ArrayList<Integer>> studentCourses;
    private HashMap<Integer,Course> courses;
    private ArrayList<String> loggedIn;

    private static class DatabaseHolder{
        private static Database instance = new Database();
    }
    /**
     * Constructs the only Database instance of this class.
     */
    //to prevent user from creating new Database
    private Database() {
        students = new HashMap<>();
        administrators = new HashMap<>();
        studentCourses= new HashMap<>();
        courses = new HashMap<>();
        loggedIn = new ArrayList<>();

    }

    /**
     * Retrieves the single instance of this class.
     */
    public static Database getInstance() {
        return DatabaseHolder.instance;
    }

    /**
     * loades the courses from the file path specified
     * into the Database, returns true if successful.
     */
    public boolean initialize(String coursesFilePath) {
        try {
            ArrayList<String> coursesFile = (ArrayList<String>)Files.readAllLines(Paths.get(coursesFilePath));
            for (int i = 0; i < coursesFile.size(); ++i){
                String[] line = coursesFile.get(i).split("\\|");
                int  courseNum = Integer.parseInt(line[0]);
                String courseName = line[1];
                int[] kdamCoursesList;
                if (line[2].equals("[]")){
                    kdamCoursesList = new int[0];
                }
                else{
                    kdamCoursesList = Stream.of(line[2].substring(1,line[2].length()-1).split(",")).mapToInt(Integer::parseInt).toArray();
                }
                int numOfMaxStudents = Integer.parseInt(line[3]);
                Course course = new Course(courseNum, courseName, kdamCoursesList, numOfMaxStudents);
                courses.put(courseNum,course);
            }
        }catch(Exception ignored){
            return false;
        }
        return true;
    }

    /**
     * Registers a student to the system
     */
    public int registerStudent(String studentUsername, String studentPassword){
        if(isRegisteredToServer(studentUsername) == Consts.IS_REGISTERED){
            return Consts.IS_REGISTERED;
        }
        students.put(studentUsername,studentUsername);
        return Consts.REGISTERED_STUDENT_SUCCESSFULLY;
    }

    /**
     * Registers administrator to the system
     */
    public int registerAdministrator(String administratorUsername, String administratorPassword){
        if(isRegisteredToServer(administratorUsername) == Consts.IS_REGISTERED){
            return Consts.IS_REGISTERED;
        }
        administrators.put(administratorUsername,administratorPassword);
        return Consts.REGISTERED_ADMINISTRATOR_SUCCESSFULLY;
    }

    /**
     * Checks if the user is Registered
     */
    public int isRegisteredToServer(String username){
        if(students.containsKey(username)| administrators.containsKey(username)){
            return Consts.IS_REGISTERED;
        }
        return Consts.NOT_REGISTERED;
    }

    /**
     * Checks if the user is logged in
     */
    public int isLoggedIn(String username){
        if(loggedIn.contains(username)){
            return Consts.IS_LOGGED_IN;
        }
        return Consts.NOT_LOGGED_IN;
    }

    /**
     * Login the user if possible
     */
    public int login(String username, String password){
        if(isLoggedIn(username) == Consts.IS_LOGGED_IN){
            return Consts.IS_LOGGED_IN;
        }
        if (students.containsKey(username)){
            if (students.get(username).equals(password)) {
                loggedIn.add(username);
                return Consts.REGISTERED_STUDENT_SUCCESSFULLY;
            } else {
                return Consts.WRONG_PASSWORD;
            }
        }

        else if (administrators.containsKey(username)){
                if (administrators.get(username).equals(password)) {
                    loggedIn.add(username);
                    return Consts.REGISTERED_ADMINISTRATOR_SUCCESSFULLY;
                } else {
                    return Consts.WRONG_PASSWORD;
                }
        }
        else{
            return Consts.NOT_REGISTERED;
        }
    }

    /**
     * Logout the user if possible.
     */
    public int logout(String username){
        if (isLoggedIn(username) == Consts.NOT_LOGGED_IN){
            return Consts.NOT_LOGGED_IN;
        }
        loggedIn.remove(username);
        return Consts.LOGGED_OUT_SUCCESSFULLY;
    }

    /**
     * Check if the user is registered to the kdam courses.
     */
    public int canRegisterToCourse(String studentUsername, Course course){
        int [] courseKdams = course.getKdamCoursesList();
        for (int i = 0 ;i < courseKdams.length; ++i){
            if (!studentCourses.get(studentUsername).contains(courseKdams[i])){
                return Consts.DONT_HAVE_KDAMS;
            }
        }
        return Consts.HAVE_KDAMS;
    }

    /**
     * Register the student to the course if possible
     */
    public int registerCourse(String studentUsername, int courseNum){
        if (isLoggedIn(studentUsername) == Consts.NOT_LOGGED_IN){
            return Consts.NOT_LOGGED_IN;
        }
        if (!courses.containsKey(courseNum)){
            return Consts.NO_SUCH_COURSE;
        }
        Course course = courses.get(courseNum);
        if (course.getNumOfMaxStudents() >= course.getRegisteredStudents().size()){
            return Consts.COURSE_IS_FULL;
        }
        if (canRegisterToCourse(studentUsername,course) == Consts.DONT_HAVE_KDAMS){
            return Consts.DONT_HAVE_KDAMS;
        }
        course.registerStudent(studentUsername);
        studentCourses.get(studentUsername).add(courseNum);
        return Consts.REGISTERED_COURSE_SUCCESSFULLY;
    }

    /**
     * Returns the kdamCourses list of the course
     */
    public int[] kdamCheck(int courseNum){
        return courses.get(courseNum).getKdamCoursesList();
    }

    /**
     * Returns the course stat
     */
    public ArrayList<Object> courseStat(int courseNum){
        ArrayList<Object> output = new ArrayList<Object>();
        Course course = courses.get(courseNum);
        output.add(course.getCourseNum());
        output.add(course.getCourseName());
        output.add(course.getKdamCoursesList());
        output.add(course.getNumOfMaxStudents());
        output.add(course.getRegisteredStudents().size());
        return output;
    }

    /**
     * Returns the student stat
     */
    public ArrayList<Object> studentStat(String studentUsername){
        ArrayList<Object> output = new ArrayList<Object>();
        output.add(studentUsername);
        output.add(studentCourses.get(studentUsername));
        return output;
    }

    /**
     * Checks if a student is registered to a specific course
     */
    public int isRegisteredToCourse(String studentUsername, int courseNum){
        if (studentCourses.get(studentUsername).contains(courseNum)){
            return Consts.IS_REGISTERED_TO_COURSE;
        }
        return Consts.NOT_REGISTERED_TO_COURSE;
    }

    /**
     * Unregisters a student from a specific course if possible
     */
    public int unregisterFromCourse(String studentUsername, int courseNum){
        if (isRegisteredToCourse(studentUsername,courseNum) == Consts.NOT_REGISTERED_TO_COURSE){
            return Consts.NOT_REGISTERED_TO_COURSE;
        }
        studentCourses.get(studentUsername).remove(courseNum);
        courses.get(courseNum).unregisterStudent(studentUsername);
        return Consts.UNREGISTERED_FROM_COURSE_SUCCESSFULLY;
    }

    /**
     * Returns the courses the student is registered to
     */
    public ArrayList<Integer> getStudentCourses(String studentUsername){
        return studentCourses.get(studentUsername);
    }


}