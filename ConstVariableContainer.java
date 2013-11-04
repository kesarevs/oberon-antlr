import oberon.*;
import java.io.*;
import java.lang.Exception;

public class ConstVariableContainer extends VariableContainer implements Cloneable{
    public ConstVariableContainer(Integer value) {
        super(value);
    }

    public ConstVariableContainer(Object value) {
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