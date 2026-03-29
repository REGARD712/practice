import java.util.Arrays;

class Student implements Comparable<Student>{
    private String name;
    private int score;

    public Student(String name, int score) {
        this.name = name;
        this.score = score;
    }

    @Override
    public String toString() {
        return "Student{" +
                "name='" + name + '\'' +
                ", score=" + score +
                '}';
    }

    @Override
    public int compareTo(Student o) {
        if (this.score < o.score) {
            return -1;
        } else if (this.score > o.score) {
            return 1;
        } else {
            return 0;
        }
    }
}

public class Main {
    public static void main(String[] args) {
        Student[] students = new Student[]{
                new Student("Zhangsan", 97),
                new Student("Lisi", 96),
                new Student("Wangwu", 98),
                new Student("Zhaoliu", 95)
        };

        Arrays.sort(students);

        System.out.println(Arrays.toString(students));
    }
}
