import oberon.*;
import java.io.*;
import java.lang.Exception;

public class Range {
    private Integer left;
    private Integer right;

    public Range(Integer a, Integer b) {
        if (a > b) {
            throw new BadRangeBorders("Left operand greater then right.");
        }
        left = a;
        right = b;
    }

    public Boolean contains(Integer a) {
        return a >= left && a <= right;
    }
}

class BadRangeBorders extends RuntimeException {
    public BadRangeBorders(String message) {
        super(message);
    }
}