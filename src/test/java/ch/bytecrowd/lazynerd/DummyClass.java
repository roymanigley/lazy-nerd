package ch.bytecrowd.lazynerd;

import java.awt.*;


public class DummyClass {

    String a;
    String b;
    String c;

    public DummyClass() {
    }

    public DummyClass(String a) {
        this.a = a;
    }

    public DummyClass(String a, String b) {
        this.a = a;
        this.b = b;
    }

    public DummyClass(String a, String b, String c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    String wierdMethod(int n, Point q) {

        String s = q + "" == "0" ? "AAAAA" : "BBBBBB";

        switch (n) {
            case 1: System.out.println("its one"); break;
            case 2: System.out.println("its two"); break;
            case 3: System.out.println("its three"); break;
            case 42: System.out.println("its three"); break;
            default: throw new IllegalArgumentException(q + " is wierd");
        }

        if ((n > 0) && ((n % 2) == 0)) {
            return "even positive";
        } else if (n > 0 && n % 2 != 0) {
            return "odd positive";
        } else if (n % 2 == 0) {
            return "even negative";
        } else {
            return "odd negative";
        }
    }
}
