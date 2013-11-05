import oberon.*;
import java.io.*;
import java.lang.Exception;
import java.util.ArrayList;

public class ConstVariableContainer extends VariableContainer implements Cloneable{
    public ConstVariableContainer(ArrayList<VariableContainer> value) {
        super(value);
    }

    public ConstVariableContainer(Range value) {
        super(value);
    }

    public ConstVariableContainer(VariableContainer value) {
        super(value);
    }

    public ConstVariableContainer(Object value) {
        super(value);
    }

    public ConstVariableContainer(Integer value) {
        super(value);
    }

    public ConstVariableContainer(Boolean value) {
        super(value);
    }

    public ConstVariableContainer(Float value) {
        super(value);
    }

    @Override
    public void setValue(VariableContainer a) {
        throw new ConstVariableChanging("Const variable changing.");
    }
}

class ConstVariableChanging extends RuntimeException {
    public ConstVariableChanging(String message) {
        super(message);
    }
}