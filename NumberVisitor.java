import oberon.*;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
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

    @Override 
    public Object visitBool(OberonParser.BoolContext ctx) { 
        //TODO: Add operator not.
        if (ctx.K_FALSE() == null) {
            return true;
        }
        return false;
    }

    private Boolean isEqual(Object a, Object b) {
        if (a.getClass() == b.getClass()) {
            if (a instanceof Integer) {
                return (Integer) a == (Integer) b;
            }
            if (a instanceof Float) {
                return (Float) a == (Float) b;
            }
            if (a instanceof Boolean) {
                return (Boolean) a == (Boolean) b;
            }
        }
        throw new TypeCastException("Differente type finded in multiply operator.");
    }

    private Boolean isLess(Object a, Object b) {
        if (a.getClass() == b.getClass()) {
            if (a instanceof Integer) {
                return (Integer) a < (Integer) b;
            }
            if (a instanceof Float) {
                return (Float) a < (Float) b;
            }
        }
        throw new TypeCastException("Differente type finded in < operator.");
    }

    private Boolean isGreater(Object a, Object b) {
        if (a.getClass() == b.getClass()) {
            if (a instanceof Integer) {
                return (Integer) a > (Integer) b;
            }
            if (a instanceof Float) {
                return (Float) a > (Float) b;
            }
        }
        throw new TypeCastException("Differente type finded in > operator.");
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
        throw new TypeCastException("Differente type finded in multiply operator.");
    }

    private Object divide(Object a, Object b) {
        if (a.getClass() == b.getClass()) {
            if (a instanceof Float) {
                return (Float) a / (Float) b;
            }
        }
        throw new TypeCastException("Differente type finded in division operator.");
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
        throw new TypeCastException("Differente type finded in sum operator.");
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
        throw new TypeCastException("Differente type finded in minus operator.");
    }

    private Object logicOr(Object a, Object b) {
        if (!(a instanceof Boolean) || !(b instanceof Boolean)) {
            throw new TypeCastException("Can't cast to BOOLEAN in OR operator.");
        }
        return (Boolean) a || (Boolean) b;
    }

    private Object logicAnd(Object a, Object b) {
        if (!(a instanceof Boolean) || !(b instanceof Boolean)) {
            throw new TypeCastException("Can't cast to BOOLEAN in OR operator.");
        }
        return (Boolean) a && (Boolean) b;
    }

    private Object div(Object a, Object b) {
        if (a.getClass() == b.getClass()) {
            if (a instanceof Integer) {
                return (Integer) a / (Integer) b;
            }
        }
        throw new TypeCastException("Differente type finded in div operator.");
    }

    private Object mod(Object a, Object b) {
        if (a.getClass() == b.getClass()) {
            if (a instanceof Integer) {
                return (Integer) a % (Integer) b;
            }
        }
        throw new TypeCastException("Differente type finded in mod operator.");
    }

    @Override 
    public Object visitCasestatement(OberonParser.CasestatementContext ctx) { 
        Object result = visit(ctx.expression());
        List<OberonParser.CaseitemContext> caseitems = ctx.caseitem();
        for (int i = 0; i < caseitems.size(); i++) {
            //TODO: Normal cast.
            List<Object> segments = (List<Object>) visit(caseitems.get(i).caselabellist());
            for(int j = 0; j < segments.size(); j++) {
                Object caseitem = segments.get(j);
                if (isEqual(result, caseitem)) {
                    return visit(caseitems.get(i).statementsequence());
                }
            }
        }
        if (ctx.K_ELSE() != null) {
            return visit(ctx.statementsequence());
        }
        return null; 
    }

    @Override 
    public Object visitCaselabellist(OberonParser.CaselabellistContext ctx) { 
        List<OberonParser.CaselabelsContext> labels = ctx.caselabels();
        List<Object> segments = new ArrayList<Object>();
        for(int i = 0; i < labels.size(); i++) {
            Object result = visit(labels.get(i));
            if (result instanceof List) {
                //TODO: Normal cast.
                segments.addAll((List<Object>) result);
            } else {
                segments.add(result);
            }
        }
        return segments; 
    }

    @Override 
    public Object visitCaselabels(OberonParser.CaselabelsContext ctx) { 
        List<OberonParser.ExpressionContext> expr = ctx.expression();
        Object result = visit(expr.get(0));
        if (ctx.RANGESEP() != null) {
            Object nextRang = visit(expr.get(1));
            if (result instanceof Integer && nextRang instanceof Integer) {
                List<Object> range = new ArrayList<Object>();
                for(int i = (Integer)result; i <= (Integer)nextRang; i++) {
                    range.add(i);
                }
                return range;
            } else {
                throw new TypeCastException("Can't cast variable to INTEGER in CASE range.");
            }
        }
        return result; 
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
                case OberonParser.DIVISION:
                    result = divide(result, nextNum);
                    break;
                case OberonParser.ET:
                    result = logicAnd(result, nextNum);
                    break;
                case OberonParser.DIV:
                    result = div(result, nextNum);
                    break;
                case OberonParser.MOD:
                    result = mod(result, nextNum);
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
            if (result instanceof Boolean) {
                throw new TypeCastException("Can't cast from BOOLEAN to number.");
            }
        }
        for(int i = 1; i < values.size(); i++) {
            Object nextVal = visit(values.get(i));
            switch (operators.get(i - 1).op.getType()) {
                case OberonParser.MINUS:
                    result = difference(result, nextVal);
                    break;
                case OberonParser.PLUS:
                    result = sum(result, nextVal);
                    break;
                case OberonParser.OR:
                    result = logicOr(result, nextVal);
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
        if (operators != null) {
            Object nextVal = visit(values.get(1));
            switch(operators.op.getType()) {
                /*
                    todo:
                    op=IN ;
                */
                case OberonParser.EQUAL:
                    return isEqual(result, nextVal);
                case OberonParser.UNEQUAL:
                    return !isEqual(result, nextVal);
                case OberonParser.LESS:
                    return isLess(result, nextVal);
                case OberonParser.GREATER:
                    return isGreater(result, nextVal);
                case OberonParser.LESSOREQ:
                    return !isGreater(result, nextVal);
                case OberonParser.GREATEROREQ:
                    return !isLess(result, nextVal);
                case OberonParser.IN:
                    throw new ThisFunctionalityDoesNotSupport("Operator IN doesn't support.");
            }
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
    public Object visitDeclarationsequence(OberonParser.DeclarationsequenceContext ctx) { 
        //TODO: Add posibility to make local variable.
        return visitChildren(ctx); 
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
            case "BOOLEAN":
                defVal = false;
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

    @Override 
    public Object visitWhilestatement(OberonParser.WhilestatementContext ctx) { 
        OberonParser.ExpressionContext exp = ctx.expression();
        OberonParser.StatementsequenceContext loop = ctx.statementsequence();
        while (true) {
            Object result = visit(exp);
            Boolean isBool = result instanceof Boolean;
            if (isBool && !(Boolean) result) {
                break;
            } else if (!isBool) {
                throw new TypeCastException("Can't cast while expression to BOOLEAN.");
            }
            visit(loop);
        }
        return true;
    }

    @Override 
    public Object visitRepeatstatement(OberonParser.RepeatstatementContext ctx) { 
        OberonParser.ExpressionContext exp = ctx.expression();
        OberonParser.StatementsequenceContext loop = ctx.statementsequence();
        do {
            visit(loop);
            Object result = visit(exp);
            Boolean isBool = result instanceof Boolean;
            if (isBool && !(Boolean) result) {
                break;
            } else if (!isBool) {
                throw new TypeCastException("Can't cast repeat expression to BOOLEAN.");
            }
        } while (true);
        return true; 
    }

    @Override 
    public Object visitIfstatement(OberonParser.IfstatementContext ctx) { 
        List<OberonParser.ExpressionContext> expr = ctx.expression();
        List<OberonParser.StatementsequenceContext> statement = ctx.statementsequence();
        for (int i = 0; i < expr.size(); i++) {
            Object result = visit(expr.get(i));
            Boolean isBool = result instanceof Boolean;
            if (isBool && (Boolean) result) {
                visit(statement.get(i));
                break;
            } else if (!isBool) {
                throw new TypeCastException("Can't cast if expression to BOOLEAN.");
            }
            //If all else-if failed and else exist, go to else
            if (i != statement.size() - 1 && i == expr.size() - 1) {
                visit(statement.get(i + 1));
            }
        }
        return true;
    }


}

class ThisFunctionalityDoesNotSupport extends RuntimeException {
    public ThisFunctionalityDoesNotSupport(String message) {
        super(message);
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