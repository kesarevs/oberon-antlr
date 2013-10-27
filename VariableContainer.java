import oberon.*;
import java.io.*;
import java.lang.Exception;

public class VariableContainer {
    String varName;
    Object value;
    boolean isConst;
    public VariableContainer(String varName, Object value, boolean isConst) {
        this.varName = varName;
        this.value = value;
        this.isConst = isConst;
    }

    public String getName() {
        return varName;
    }

    public Object getValue() {
        return value;
    }

    public Object setValue(Object value) {
        if (isConst) {
            throw new ConstVariableChanging("Const variable changing:" + varName);
        }
        if (this.value.getClass() != value.getClass()) {
            throw new TypeCastException("Can't cast new value for " + varName + " to variable type.");
        }
        this.value = value;
        return value;
    }
}

class ConstVariableChanging extends RuntimeException {
    public ConstVariableChanging(String message) {
        super(message);
    }
}