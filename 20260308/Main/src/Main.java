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
public class Main {
    public static void main(String[] args) {
        Date date = new Date();
        date.printDate();
//        date.setDate(2026, 3, 8);
//        date.printDate();
    }
}
