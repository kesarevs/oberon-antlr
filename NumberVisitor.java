import oberon.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.List;
import java.io.*;
import java.lang.Exception;

import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;

public class NumberVisitor extends OberonBaseVisitor<Object> {
    Map<String, VariableContainer> memory = new HashMap<String, VariableContainer>();
    Map<String, String> types = new HashMap<String, String>();

    @Override 
    public Object visitModule(OberonParser.ModuleContext ctx) { 
        List<TerminalNode> moduleName = ctx.ID();
        String name = moduleName.get(0).getText();
        String nameAtTheEnd = moduleName.get(1).getText();
        if (!name.equals(nameAtTheEnd)) {
            throw new ModuleNotFoundException("Error: \"End " + nameAtTheEnd + ".\"\nExpected: \"End " + name + ".\"");
        }
        //TODO: Add reference on module class.
        memory.put(name, new VariableContainer(name, new Object(), true));
        return visitChildren(ctx); 
    }

    @Override 
    public Object visitAnint(OberonParser.AnintContext ctx) { 
        return Integer.parseInt(ctx.INT().getText());
    }

    @Override 
    public Object visitReal(OberonParser.RealContext ctx) { 
        return Float.parseFloat(ctx.REAL().getText());
    }

    private Object multiply(Object a, Object b) {
        if (a.getClass() == b.getClass()) {
            if (a instanceof Integer) {
                return (Integer) a * (Integer) b;
            }
            if (a instanceof Float) {
                return (Float) a * (Float) b;
            }
        }
        return a;
    }

    private Object divide(Object a, Object b) {
        if (a.getClass() == b.getClass()) {
            if (a instanceof Integer) {
                return (Integer) a / (Integer) b;
            }
            if (a instanceof Float) {
                return (Float) a / (Float) b;
            }
        }
        return a;
    }

    private Object sum(Object a, Object b) {
        if (a.getClass() == b.getClass()) {
            if (a instanceof Integer) {
                return (Integer) a + (Integer) b;
            }
            if (a instanceof Float) {
                return (Float) a + (Float) b;
            }
        }
        return a;
    }

    private Object difference(Object a, Object b) {
        if (a.getClass() == b.getClass()) {
            if (a instanceof Integer) {
                return (Integer) a - (Integer) b;
            }
            if (a instanceof Float) {
                return (Float) a - (Float) b;
            }
        }
        return a;
    }

    @Override 
    public Object visitTerm(OberonParser.TermContext ctx) { 
        List<OberonParser.MuloperatorContext> operators = ctx.muloperator();
        List<OberonParser.FactorContext> values = ctx.factor();
        Object result = visit(values.get(0));
        for(int i = 1; i < values.size(); i++) {
            Object nextNum = visit(values.get(i));
            switch (operators.get(i - 1).op.getType()) {
                case OberonParser.MULT:
                    result = multiply(result, nextNum);
                    break;
                case OberonParser.DIV:
                    result = divide(result, nextNum);
                    break;
            }
        }
        return result;
    }

    @Override 
    public Object visitSimpleexpression(OberonParser.SimpleexpressionContext ctx) {
        List<OberonParser.TermContext> values = ctx.term();
        List<OberonParser.AddoperatorContext> operators = ctx.addoperator();
        Object result = visit(values.get(0));
        if(ctx.MINUS() != null) {
            if (result instanceof Integer) {
                result = - (Integer) result;
            }
            if (result instanceof Float) {
                result = - (Float) result;
            }
        }
        for(int i = 1; i < values.size(); i++) {
            Object nextNum = visit(values.get(i));
            switch (operators.get(i - 1).op.getType()) {
                case OberonParser.MINUS:
                    result = difference(result, nextNum);
                    break;
                case OberonParser.PLUS:
                    result = sum(result, nextNum);
                    break;
            }
        }
        return result;
    }

    @Override 
    public Object visitExpression(OberonParser.ExpressionContext ctx) { 
        List<OberonParser.SimpleexpressionContext> values = ctx.simpleexpression();
        OberonParser.RelationContext operators = ctx.relation();
        Object result = visit(values.get(0));
        if (operators == null) {
            return result;
        }
        return result; 
    }

    @Override 
    public Object visitFactor(OberonParser.FactorContext ctx) {
        if (ctx.simpleexpression() != null) {
            Object result = visit(ctx.simpleexpression());
            return result;
        }
        if (ctx.designator() != null) {
            String varName = ctx.designator().getText();
            if (!memory.containsKey(varName)) {
                throw new VariableNotDeclaredException("Variable " + varName + " is not declared.");
            }
            return memory.get(varName).getValue();
        }
        return visitChildren(ctx); 
    }

    @Override 
    public Object visitAssignment(OberonParser.AssignmentContext ctx) {
        Object result = visit(ctx.expression());
        String varName = ctx.designator().getText();
        if (!memory.containsKey(varName)) {
            throw new VariableNotDeclaredException("Variable " + varName + " is not declared.");
        }
        memory.get(varName).setValue(result);
        System.out.println(varName + " := " + result);
        return result;
    }

    @Override 
    public Object visitVariabledeclaration(OberonParser.VariabledeclarationContext ctx) { 
        OberonParser.TypeContext type = ctx.type();
        List<OberonParser.IdentdefContext> varLst = ctx.identlist().identdef();
        Object defVal = 0;
        switch (ctx.type().getText()) {
            case "INTEGER":
                defVal = 0;
                break;
            case "REAL":
                defVal = 0.0;
                break;
        }
        for(int i = 0; i < varLst.size(); i++) {
            String varName = varLst.get(i).getText();
            if (memory.containsKey(varName)) {
                throw new VariableDeclarationException("Variable " + varName + " already declared.");
            }
            memory.put(varName, new VariableContainer(varName, defVal, false));
        }
        return defVal; 
    }

    @Override 
    public Object visitConstantdeclaration(OberonParser.ConstantdeclarationContext ctx) { 
        String varName = ctx.identdef().getText();
        Object defVal = visit(ctx.expression());
        if (memory.containsKey(varName)) {
            throw new VariableDeclarationException("Variable " + varName + " already declared.");
        }
        memory.put(varName, new VariableContainer(varName, defVal, true));
        return defVal;
    }
}

class TypeCastException extends RuntimeException {
    public TypeCastException(String message) {
        super(message);
    }
}

class VariableNotDeclaredException extends RuntimeException {
    public VariableNotDeclaredException(String message) {
        super(message);
    }
}

class VariableDeclarationException extends RuntimeException {
    public VariableDeclarationException(String message) {
        super(message);
    }
}

class ModuleNotFoundException extends RuntimeException {
    public ModuleNotFoundException(String message) {
        super(message);
    }
}