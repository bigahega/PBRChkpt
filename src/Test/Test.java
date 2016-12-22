package Test;

/**
 * Created by Berkin GÜLER (bguler15@ku.edu.tr) on 16.08.2016.
 */
public class Test {

    private static void m1(byte... args) {
        System.out.println("byte one got executed");
    }

    private static void m1(int... args) {
        System.out.println("int one got executed");
    }

    public static void main(String... args) {
        m1(400);
    }

}
