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

    private Type type; 

    public VariableContainer(Range value) {
        type = Type.RANGE;
        myObject = value;
    }

    public VariableContainer(ArrayList<VariableContainer> value) {
        type = Type.LIST;
        myList = value;
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
        assertNotList("");
        return myList;
    }

    public Integer getInt() {
        assertNotInt("");
        return myInt;
    }

    public Boolean getBool() {
        assertNotBool("");
        return myBool;
    }

    public Float getReal() {
        assertNotReal("");
        return myReal;
    }

    public void setValue(VariableContainer a) {
        assertUnEqClass(a);
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
        }
        myObject = a.getValue();
    }

    public VariableContainer in(VariableContainer a) {
        /*
        a.assertNotList("Bad operands for IN operator.");
        List<VariableContainer> set = (List<VariableContainer>) a.getValue();
        for (int i = 0; i < set.size(); i++) {
            VariableContainer item = set.get(i);
            if ((Boolean) isEqual(item).getValue()) {
                return new VariableContainer(true);
            }
        }*/
        return new VariableContainer(false);
    }

    public VariableContainer isEqual(VariableContainer a) {
        assertUnEqClass(a);
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
        assertUnEqClass(a);
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
        assertUnEqClass(a);
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


    public VariableContainer logicOr(VariableContainer a) {
        assertUnEqClass(a);
        assertNotBool("Can't cast to BOOLEAN in | operator.");
        return new VariableContainer(a.getBool() || myBool);
    }

    public VariableContainer logicAnd(VariableContainer a) {
        assertUnEqClass(a);
        assertNotBool("Can't cast to BOOLEAN in & operator.");
        return new VariableContainer(a.getBool() && myBool);
    }

    public VariableContainer not() {
        assertNotBool("Boolean expected.");
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
        assertUnEqClass(a);
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
        assertUnEqClass(a);
        if (type == Type.REAL) {
            return new VariableContainer(myReal / a.getReal());
        }
        throw new TypeCastException("No '/' operator for this type.");
    }

    public VariableContainer sum(VariableContainer a) {
        assertUnEqClass(a);
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
        assertUnEqClass(a);
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
        assertUnEqClass(a);
        if (type == Type.INT) {
            return new VariableContainer(myInt / a.getInt());
        }
        throw new TypeCastException("No 'DIV' operator for this type.");
    }

    public VariableContainer mod(VariableContainer a) {
        assertUnEqClass(a);
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

    private void assertUnEqClass(VariableContainer a) {
        if (type != a.getType()) {
            throw new TypeCastException("Differente type finded.");
        }
    }

    public void assertNotBool(String message) {
        if (type != Type.BOOL) {
            throw new TypeCastException(message);
        }
    }

    public void assertNotInt(String message) {
        if (type != Type.INT) {
            throw new TypeCastException(message);
        }
    }

    public void assertNotReal(String message) {
        if (type != Type.REAL) {
            throw new TypeCastException(message);
        }
    }

    public void assertNotList(String message) {
        if (type != Type.LIST) {
            throw new TypeCastException(message);
        }
    }
}

class TypeCastException extends RuntimeException {
    public TypeCastException(String message) {
        super(message);
    }
}

enum Type { INT, REAL, BOOL, OBJECT, LIST, RANGE };