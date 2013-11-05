import oberon.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.lang.Exception;
import java.util.HashSet;

public class VariableContainer implements Cloneable {

    private Integer myInt;
    private Boolean myBool;
    private Float myReal;
    private Object myObject;
    private ArrayList<VariableContainer> myList;
    private Range myRange;

    private Type type; 

    public VariableContainer(Range value) {
        type = Type.RANGE;
        myRange = value;
        myObject = value;
    }

    public VariableContainer(ArrayList<VariableContainer> value) {
        type = Type.LIST;
        myList = value;
        myObject = value;
    }

    public VariableContainer(VariableContainer value) {
        type = value.getType();
        setValue(value);
    }

    public VariableContainer(Object value) {
        type = Type.OBJECT;
        myObject = value;
    }

    public VariableContainer(Integer value) {
        type = Type.INT;
        myObject = value;
        myInt = value;
    }

    public VariableContainer(Boolean value) {
        type = Type.BOOL;
        myObject = value;
        myBool = value;
    }

    public VariableContainer(Float value) {
        type = Type.REAL;
        myObject = value;
        myReal = value;
    }

    public Type getType() {
        return type;
    }

    public Object getValue() {
        return myObject;
    }

    public ArrayList<VariableContainer> getList() {
        assertNot(Type.LIST);
        return myList;
    }

    public Range getRange() {
        assertNot(Type.RANGE);
        return myRange;
    }


    public Integer getInt() {
        assertNot(Type.INT);
        return myInt;
    }

    public Boolean getBool() {
        assertNot(Type.BOOL);
        return myBool;
    }

    public Float getReal() {
        assertNot(Type.REAL);
        return myReal;
    }

    public void setValue(VariableContainer a) {
        assertNot(a.getType());
        switch (type) {
            case INT:
                myInt = a.getInt();
                break;
            case REAL:
                myReal = a.getReal();
                break;
            case BOOL: 
                myBool = a.getBool();
                break;
            case RANGE:
                myRange = a.getRange();
                break;
            case LIST:
                myList = a.getList();
                break;
        }
        myObject = a.getValue();
    }

    public VariableContainer isEqual(VariableContainer a) {
        assertNot(a.getType());
        Boolean result;
        switch (type) {
            case INT:
                result = myInt == a.getInt();
                break;
            case REAL:
                result = myReal == a.getReal();
                break;
            case BOOL: 
                result = myBool == a.getBool();
                break;
            default:
                throw new TypeCastException("Bad operand for =.");
        }
        return new VariableContainer(result);
    }

    public VariableContainer isLess(VariableContainer a) {
        assertNot(a.getType());
        Boolean result;
        switch (type) {
            case INT:
                result = myInt < a.getInt();
                break;
            case REAL:
                result = myReal < a.getReal();
                break;
            default:
                throw new TypeCastException("Bad operand for <.");
        }
        return new VariableContainer(result);
    }

    public VariableContainer isGreater(VariableContainer a) {
        assertNot(a.getType());
        Boolean result;
        switch (type) {
            case INT:
                result = myInt > a.getInt();
                break;
            case REAL:
                result = myReal > a.getReal();
                break;
            default:
                throw new TypeCastException("Bad operand for >.");
        }
        return new VariableContainer(result);
    }

    public VariableContainer contains(VariableContainer a) {
        Boolean result = false;
        switch (type) {
            case RANGE:
                result = myRange.contains(a);
                break;
            default:
                throw new TypeCastException("Bad operand for IN operator.");
        }
        return new VariableContainer(result);
    }


    public VariableContainer logicOr(VariableContainer a) {
        assertNot(a.getType());
        assertNot(Type.BOOL);
        return new VariableContainer(a.getBool() || myBool);
    }

    public VariableContainer logicAnd(VariableContainer a) {
        assertNot(a.getType());
        assertNot(Type.BOOL);
        return new VariableContainer(a.getBool() && myBool);
    }

    public VariableContainer not() {
        assertNot(Type.BOOL);
        return new VariableContainer(!myBool);
    }

    public VariableContainer negative() {
        switch (type) {
            case INT:
                return new VariableContainer(- myInt);
            case REAL:
                return new VariableContainer(- myReal);
            default: 
                throw new TypeCastException("Can't cast to number.");
        }
    }

    public VariableContainer multiply(VariableContainer a) {
        assertNot(a.getType());
        switch (type) {
            case INT:
                return new VariableContainer(myInt * a.getInt());
            case REAL:
                return new VariableContainer(myReal * a.getReal());
            default:
                throw new TypeCastException("No '*' operator for this type.");
        }
    }

    public VariableContainer divide(VariableContainer a) {
        assertNot(a.getType());
        if (type == Type.REAL) {
            return new VariableContainer(myReal / a.getReal());
        }
        throw new TypeCastException("No '/' operator for this type.");
    }

    public VariableContainer sum(VariableContainer a) {
        assertNot(a.getType());
        switch (type) {
            case INT:
                return new VariableContainer(myInt + a.getInt());
            case REAL:
                return new VariableContainer(myReal + a.getReal());
            default:
                throw new TypeCastException("No '+' operator for this type.");
        }
    }

    public VariableContainer difference(VariableContainer a) {
        assertNot(a.getType());
        switch (type) {
            case INT:
                return new VariableContainer(myInt - a.getInt());
            case REAL:
                return new VariableContainer(myReal - a.getReal());
            default:
                throw new TypeCastException("No '+' operator for this type.");
        }
    }

    public VariableContainer div(VariableContainer a) {
        assertNot(a.getType());
        if (type == Type.INT) {
            return new VariableContainer(myInt / a.getInt());
        }
        throw new TypeCastException("No 'DIV' operator for this type.");
    }

    public VariableContainer mod(VariableContainer a) {
        assertNot(a.getType());
        if (type == Type.INT) {
            return new VariableContainer(myInt % a.getInt());
        }
        throw new TypeCastException("No 'MOD' operator for this type.");
    }

    public String toString() {
        switch (type) {
            case INT:
                return String.valueOf(myInt);
            case REAL:
                return String.valueOf(myReal);
            case BOOL: 
                return String.valueOf(myBool);
            case LIST:
                return String.valueOf(myList);
        }
        return String.valueOf(myObject);
    }

    @Override
    protected VariableContainer clone() {
        VariableContainer cloned = null;
        try {
            cloned = (VariableContainer) super.clone();
            if (cloned.getType() == Type.LIST) {
                ArrayList<VariableContainer> clonedLst = new ArrayList<VariableContainer>();
                for(VariableContainer item: cloned.myList) {
                    clonedLst.add(item.clone());
                }
                cloned.myList = clonedLst;
            }
        } catch (CloneNotSupportedException ex) {}
        return cloned;
    }

    public void assertNot(Type expected) {
        if (type != expected) {
            throw new TypeCastException("Can't cast " + type + " to " + expected + " type.");
        }
    }
}

class TypeCastException extends RuntimeException {
    public TypeCastException(String message) {
        super(message);
    }
}

enum Type { INT, REAL, BOOL, OBJECT, LIST, RANGE };