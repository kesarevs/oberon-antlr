import oberon.*;
import java.io.*;
import java.lang.Exception;

public class Range {
    private Integer left;
    private Integer right;

    public Range(VariableContainer a, VariableContainer b) {
        if (a.isGreater(b).getBool()) {
            throw new BadRangeBorders("Left operand greater then right in RANGE.");
        }
        left = a.getInt();
        right = b.getInt();
    }

    public Boolean contains(VariableContainer a) {
        Integer val = a.getInt();
        return val >= left && val <= right;
    }

    public Integer left() {
        return left;
    }

    public Integer right() {
        return right;
    }
}

class BadRangeBorders extends RuntimeException {
    public BadRangeBorders(String message) {
        super(message);
    }
}