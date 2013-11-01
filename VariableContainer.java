import oberon.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.lang.Exception;
import java.util.HashSet;

public class VariableContainer {
    private Object value;

    public VariableContainer(Object value) {
        this.value = value;
    }

    public VariableContainer negative() {
        if (value instanceof Integer) {
            return new VariableContainer(- (Integer) value);
        } else if (value instanceof Float) {
            return new VariableContainer(- (Float) value);
        } else {
            throw new TypeCastException("Can't cast to number.");
        }
    }

    public VariableContainer not() {
        assertNotBool("Boolean expected.");
        return new VariableContainer(!((Boolean) value));
    }

    public Object getValue() {
        return value;
    }

    public void setValue(VariableContainer a) {
        assertUnEqClass(value, a.getValue());
        value = a.getValue();
    }

    private void assertUnEqClass(Object a, Object b) {
        if (a.getClass() != b.getClass()) {
            throw new TypeCastException("Differente type finded.");
        }
    }

    public void assertNotBool(String message) {
        if (! (value instanceof Boolean)) {
            throw new TypeCastException(message);
        }
    }

    public void assertNotList(String message) {
        if (! (value instanceof List)) {
            throw new TypeCastException(message);
        }
    }

    public VariableContainer in(VariableContainer a) {
        a.assertNotList("Bad operands for IN operator.");
        List<VariableContainer> set = (List<VariableContainer>) a.getValue();
        for (int i = 0; i < set.size(); i++) {
            VariableContainer item = set.get(i);
            if ((Boolean) isEqual(item).getValue()) {
                return new VariableContainer(true);
            }
        }
        return new VariableContainer(false);
    }

    public VariableContainer isEqual(VariableContainer a) {
        assertUnEqClass(value, a.getValue());
        Boolean result;
        if (value instanceof Integer) {
            result = (Integer) value == (Integer) a.getValue();
        } else if (value instanceof Float) {
            result = Math.abs((Float) value - (Float) a.getValue()) < 1e-6;
        } else if (value instanceof Boolean) {
            result = (Boolean) value == (Boolean) a.getValue();
        } else {
            throw new TypeCastException("Bad operand for =.");
        }
        return new VariableContainer(result);
    }

    public VariableContainer isLess(VariableContainer a) {
        assertUnEqClass(value, a.getValue());
        Boolean result;
        if (value instanceof Integer) {
            result = (Integer) value < (Integer) a.getValue();
        } else if (value instanceof Float) {
            result = (Float) value < (Float) a.getValue();
        } else {
            throw new TypeCastException("Bad operand for <.");
        }
        return new VariableContainer(result);
    }

    public VariableContainer isGreater(VariableContainer a) {
        assertUnEqClass(value, a.getValue());
        Boolean result;
        if (value instanceof Integer) {
            result = (Integer) value > (Integer) a.getValue();
        } else if (value instanceof Float) {
            result = (Float) value > (Float) a.getValue();
        } else {
            throw new TypeCastException("Bad operand for >.");
        }
        return new VariableContainer(result);
    }


    public VariableContainer logicOr(VariableContainer a) {
        assertUnEqClass(value, a.getValue());
        assertNotBool("Can't cast to BOOLEAN in | operator.");
        return new VariableContainer((Boolean) a.getValue() || (Boolean) value);
    }

    public VariableContainer logicAnd(VariableContainer a) {
        assertUnEqClass(value, a.getValue());
        assertNotBool("Can't cast to BOOLEAN in & operator.");
        return new VariableContainer((Boolean) a.getValue() && (Boolean) value);
    }

    public VariableContainer multiply(VariableContainer a) {
        assertUnEqClass(value, a.getValue());
        if (value instanceof Integer) {
            return new VariableContainer((Integer) a.getValue() * (Integer) value);
        }
        if (value instanceof Float) {
            return new VariableContainer((Float) a.getValue() * (Float) value);
        }
        throw new TypeCastException("No '*' operator for this type.");
    }

    public VariableContainer divide(VariableContainer a) {
        assertUnEqClass(value, a.getValue());
        if (value instanceof Float) {
            return new VariableContainer((Float) value / (Float) a.getValue());
        }
        throw new TypeCastException("No '/' operator for this type.");
    }

    public VariableContainer sum(VariableContainer a) {
        assertUnEqClass(value, a.getValue());
        if (value instanceof Integer) {
            return new VariableContainer((Integer) a.getValue() + (Integer) value);
        }
        if (value instanceof Float) {
            return new VariableContainer((Float) a.getValue() + (Float) value);
        }
        throw new TypeCastException("No '+' operator for this type.");
    }

    public VariableContainer difference(VariableContainer a) {
        assertUnEqClass(value, a.getValue());
        if (value instanceof Integer) {
            return new VariableContainer((Integer) value - (Integer) a.getValue());
        }
        if (value instanceof Float) {
            return new VariableContainer((Float) value - (Float) a.getValue());
        }
        throw new TypeCastException("No '-' operator for this type.");
    }

    public VariableContainer div(VariableContainer a) {
        assertUnEqClass(value, a.getValue());
        if (value instanceof Integer) {
            return new VariableContainer((Integer) value / (Integer) a.getValue());
        }
        throw new TypeCastException("No 'DIV' operator for this type.");
    }

    public VariableContainer mod(VariableContainer a) {
        assertUnEqClass(value, a.getValue());
        if (value instanceof Integer) {
            return new VariableContainer((Integer) value % (Integer) a.getValue());
        }
        throw new TypeCastException("No 'MOD' operator for this type.");
    }
}

class TypeCastException extends RuntimeException {
    public TypeCastException(String message) {
        super(message);
    }
}