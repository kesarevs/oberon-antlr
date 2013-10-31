import oberon.*;
import java.io.*;
import java.lang.Exception;

public class ConstVariableContainer extends VariableContainer {
    public ConstVariableContainer(Object value) {
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