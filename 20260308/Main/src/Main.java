class Date {
    private int year;
    private int month;
    private int day;
    public Date() {
        this(1999, 2, 3);
    }
    public Date(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
    }

    public void setDate(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
    }

    public void printDate() {
        System.out.println(this.year + "/" + this.month + "/" + this.day);
    }
}
class Student{
    private String name;
    private String gender;
    private int age;
    private double score;
    public static String classRoom = "A0807";  //静态成员变量
    public Student(String name, String gender, int age, double score) {
        this.name = name;
        this.gender = gender;
        this.age = age;
        this.score = score;
    }
}
public class Main {
    public static void main(String[] args) {
        //静态成员变量可以直接通过类名访问
        System.out.println(Student.classRoom);
        Student s1 = new Student("Zhangsan", "男", 18, 3.8);
        Student s2 = new Student("Xiaohong", "女", 19, 4.0);
        Student s3 = new Student("Jimmy", "男", 18, 2.6);

        //也可以通过对象访问，但是classRoom是三个对象共享的
        System.out.println(s1.classRoom);
        System.out.println(s2.classRoom);
        System.out.println(s3.classRoom);
    }
}
