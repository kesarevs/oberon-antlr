import oberon.*;

import java.util.HashMap;
import java.util.HashSet;
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

public class NumberVisitor extends OberonBaseVisitor<VariableContainer> {
    Map<String, VariableContainer> memory = new HashMap<String, VariableContainer>();
    Map<String, String> types = new HashMap<String, String>();

    @Override 
    public VariableContainer visitModule(OberonParser.ModuleContext ctx) { 
        List<TerminalNode> moduleName = ctx.ID();
        String name = moduleName.get(0).getText();
        String nameAtTheEnd = moduleName.get(1).getText();
        if (!name.equals(nameAtTheEnd)) {
            throw new ModuleNotFoundException("Error: \"End " + nameAtTheEnd + ".\"\nExpected: \"End " + name + ".\"");
        }
        //TODO: Add reference on module class.
        memory.put(name, new ConstVariableContainer(new Object()));
        return visitChildren(ctx); 
    }

/*
    @Override 
    public VariableContainer visitSet(OberonParser.SetContext ctx) {
        //TODO: This set is like real set. We need oberon set. I don't understand what is it. add operators for set (+, -, *, /)
        //http://www.inf.ethz.ch/personal/wirth/Articles/Oberon/SETs.pdf
        ArrayList<VariableContainer> set = new ArrayList<VariableContainer>();
        if (ctx.caselabellist() != null) {
            ArrayList<VariableContainer> segments = (ArrayList<VariableContainer>) visit(ctx.caselabellist()).getValue();
            for(int j = 0; j < segments.size(); j++) {
                VariableContainer caseitem = segments.get(j);
                if (caseitem.getValue() instanceof Range) {
                    Range r = (Range) caseitem.getValue();
                    for (int i = r.left(); i <= r.right(); ++i) {
                        set.add(new VariableContainer(i));
                    }
                } else {
                    set.add(caseitem);
                }
            }
        }
        return new VariableContainer(set); 
    }
*/

    @Override 
    public VariableContainer visitAnint(OberonParser.AnintContext ctx) { 
        return new VariableContainer(Integer.parseInt(ctx.INT().getText()));
    }

    @Override 
    public VariableContainer visitReal(OberonParser.RealContext ctx) { 
        return new VariableContainer(Float.parseFloat(ctx.REAL().getText()));
    }

    @Override 
    public VariableContainer visitBool(OberonParser.BoolContext ctx) { 
        //TODO: Add operator not.
        if (ctx.K_FALSE() == null) {
            return new VariableContainer(true);
        }
        return new VariableContainer(false);
    }

//TODO: Normal case.
    @Override 
    public VariableContainer visitCasestatement(OberonParser.CasestatementContext ctx) { 
        VariableContainer result = visit(ctx.expression());
        List<OberonParser.CaseitemContext> caseitems = ctx.caseitem();
        for (OberonParser.CaseitemContext it : caseitems) {
            ArrayList<VariableContainer> segments = visit(it.caselabellist()).getList();
            for(VariableContainer caseitem : segments) {
                if (caseitem.getValue() instanceof Range && result.getType() == Type.INT) {
                    Range r = (Range) caseitem.getValue();
                    if (r.contains(result.getInt())) {
                        return visit(it.statementsequence());
                    }
                } else if (result.isEqual(caseitem).getBool()) {
                    return visit(it.statementsequence());
                }
            }
        }
        if (ctx.K_ELSE() != null) {
            return visit(ctx.statementsequence());
        }
        return null; 
    }

    @Override 
    public VariableContainer visitCaselabellist(OberonParser.CaselabellistContext ctx) { 
        List<OberonParser.CaselabelsContext> labels = ctx.caselabels();
        ArrayList<VariableContainer> segments = new ArrayList<VariableContainer>();
        for(OberonParser.CaselabelsContext l : labels) {
            segments.add(visit(l));
        }
        return new VariableContainer(segments); 
    }

    @Override 
    public VariableContainer visitCaselabels(OberonParser.CaselabelsContext ctx) { 
        List<OberonParser.ExpressionContext> expr = ctx.expression();
        VariableContainer result = visit(expr.get(0));
        if (ctx.RANGESEP() != null) {
            VariableContainer nextRang = visit(expr.get(1));
            if (result.getType() == Type.INT && nextRang.getType() == Type.INT) {
                return new VariableContainer(new Range(result.getInt(), nextRang.getInt()));
            } else {
                throw new TypeCastException("Can't cast variable to INTEGER in CASE range.");
            }
        }
        return result; 
    }

    @Override 
    public VariableContainer visitTerm(OberonParser.TermContext ctx) { 
        List<OberonParser.MuloperatorContext> operators = ctx.muloperator();
        List<OberonParser.FactorContext> values = ctx.factor();
        VariableContainer result = visit(values.get(0));
        for(int i = 1; i < values.size(); i++) {
            VariableContainer nextNum = visit(values.get(i));
            switch (operators.get(i - 1).op.getType()) {
                case OberonParser.MULT:
                    result = result.multiply(nextNum);
                    break;
                case OberonParser.DIVISION:
                    result = result.divide(nextNum);
                    break;
                case OberonParser.ET:
                    result = result.logicAnd(nextNum);
                    break;
                case OberonParser.DIV:
                    result = result.div(nextNum);
                    break;
                case OberonParser.MOD:
                    result = result.mod(nextNum);
                    break;
            }
        }
        return result;
    }

    @Override 
    public VariableContainer visitSimpleexpression(OberonParser.SimpleexpressionContext ctx) {
        List<OberonParser.TermContext> values = ctx.term();
        List<OberonParser.AddoperatorContext> operators = ctx.addoperator();
        VariableContainer result = visit(values.get(0));
        if(ctx.MINUS() != null) {
            result = result.negative();
        }
        for(int i = 1; i < values.size(); i++) {
            VariableContainer nextVal = visit(values.get(i));
            switch (operators.get(i - 1).op.getType()) {
                case OberonParser.MINUS:
                    return result.difference(nextVal);
                case OberonParser.PLUS:
                    return result.sum(nextVal);
                case OberonParser.OR:
                    return result.logicOr(nextVal);
            }
        }
        return result;
    }

    @Override 
    public VariableContainer visitExpression(OberonParser.ExpressionContext ctx) { 
        List<OberonParser.SimpleexpressionContext> values = ctx.simpleexpression();
        OberonParser.RelationContext operators = ctx.relation();
        VariableContainer result = visit(values.get(0));
        if (operators != null) {
            VariableContainer nextVal = visit(values.get(1));
            switch(operators.op.getType()) {
                /*
                    todo:
                    op=IN ;
                */
                case OberonParser.EQUAL:
                    return result.isEqual(nextVal);
                case OberonParser.UNEQUAL:
                    return result.isEqual(nextVal).not();
                case OberonParser.LESS:
                    return result.isLess(nextVal);
                case OberonParser.GREATER:
                    return result.isGreater(nextVal);
                case OberonParser.LESSOREQ:
                    return result.isGreater(nextVal).not();
                case OberonParser.GREATEROREQ:
                    return result.isLess(nextVal).not();
                case OberonParser.IN:
                    return result.in(nextVal);
            }
        }
        return result; 
    }

    @Override 
    public VariableContainer visitDesignator(OberonParser.DesignatorContext ctx) { 
        String varName = ctx.qualident(0).getText();
        if (!memory.containsKey(varName)) {
            throw new VariableNotDeclaredException("Variable " + varName + " is not declared.");
        }
        VariableContainer element = memory.get(varName);
        if (ctx.isArray != null) {
            for (OberonParser.ExplistContext d : ctx.explist()) {
                for (OberonParser.ExpressionContext expr : d.expression()) {
                    Integer dimension = visit(expr).getInt();
                    element = element.getList().get(dimension);
                }
            }
        }
        return element;
    }

    @Override 
    public VariableContainer visitAssignment(OberonParser.AssignmentContext ctx) {
        VariableContainer result = visit(ctx.expression());
        VariableContainer var = visit(ctx.designator());
        var.setValue(result);
        System.out.println(ctx.designator().getText() + " := " + var.getValue());
        return result;
    }
    
    public ArrayList<VariableContainer> createArray(Integer len, VariableContainer defVal) {
        ArrayList<VariableContainer> arr = new ArrayList<VariableContainer>();
        for(int i = 0; i < len; i++) {
            arr.add(defVal.clone());
        }
        return arr;
    }

    @Override 
    public VariableContainer visitArraytype(OberonParser.ArraytypeContext ctx) { 
        List<OberonParser.ExpressionContext> sizeCont = ctx.explist().expression();
        VariableContainer defVal ;
        switch (ctx.type().getText()) {
            case "INTEGER":
                defVal = new VariableContainer(0);
                break;
            case "REAL":
                defVal = new VariableContainer(0f);
                break;
            case "BOOLEAN":
                defVal = new VariableContainer(false);
                break;
            default:
                throw new ThisFunctionalityDoesNotSupport("");
        }
        for(int i = sizeCont.size() - 1; i >= 0; i--) {
            VariableContainer dimension = visit(sizeCont.get(i));
            dimension.assertNotInt("Only INTEGER expression can set ARRAY demension.");
            defVal = new VariableContainer(createArray(dimension.getInt(), defVal));
        }
        return defVal;
    }

    @Override 
    public VariableContainer visitVariabledeclaration(OberonParser.VariabledeclarationContext ctx) { 
        OberonParser.TypeContext type = ctx.type();
        List<OberonParser.IdentdefContext> varLst = ctx.identlist().identdef();
        Object defVal = 0;
        for(OberonParser.IdentdefContext var : varLst) {
            String varName = var.getText();
            if (memory.containsKey(varName)) {
                throw new VariableDeclarationException("Variable " + varName + " already declared.");
            }
            switch (ctx.type().getText()) {
                case "INTEGER":
                    memory.put(varName, new VariableContainer(0));
                    break;
                case "REAL":
                    memory.put(varName, new VariableContainer(0f));
                    break;
                case "BOOLEAN":
                    memory.put(varName, new VariableContainer(false));
                    break;
            }
            if (ctx.type().isArr != null) {
                memory.put(varName, visit(ctx.type().isArr));
            }
        }
        return null; 
    }

    @Override 
    public VariableContainer visitConstantdeclaration(OberonParser.ConstantdeclarationContext ctx) { 
        String varName = ctx.identdef().getText();
        VariableContainer val = visit(ctx.expression());
        if (memory.containsKey(varName)) {
            throw new VariableDeclarationException("Variable " + varName + " already declared.");
        }
        switch (val.getType()) {
            case INT:
                memory.put(varName, new ConstVariableContainer(val.getInt()));
                break;
            case REAL:
                memory.put(varName, new ConstVariableContainer(val.getReal()));
                break;
            case BOOL:
                memory.put(varName, new ConstVariableContainer(val.getBool()));
                break;
        }
        return val;
    }

    @Override 
    public VariableContainer visitWhilestatement(OberonParser.WhilestatementContext ctx) { 
        OberonParser.ExpressionContext exp = ctx.expression();
        OberonParser.StatementsequenceContext loop = ctx.statementsequence();
        Boolean doNext = true;
        while (doNext) {
            visit(loop);
            VariableContainer result = visit(exp);
            result.assertNotBool("Can't cast while expression to BOOLEAN.");
            doNext = result.getBool();
        }
        return null;
    }

    @Override 
    public VariableContainer visitRepeatstatement(OberonParser.RepeatstatementContext ctx) { 
        OberonParser.ExpressionContext exp = ctx.expression();
        OberonParser.StatementsequenceContext loop = ctx.statementsequence();
        Boolean doNext = true;
        do {
            visit(loop);
            VariableContainer result = visit(exp);
            result.assertNotBool("Can't cast repeat expression to BOOLEAN.");
            doNext = result.getBool();
        } while (doNext);
        return null; 
    }

    @Override 
    public VariableContainer visitIfstatement(OberonParser.IfstatementContext ctx) { 
        List<OberonParser.ExpressionContext> expr = ctx.expression();
        List<OberonParser.StatementsequenceContext> statement = ctx.statementsequence();
        for (int i = 0; i < expr.size(); i++) {
            VariableContainer result = visit(expr.get(i));
            result.assertNotBool("Can't cast if expression to BOOLEAN.");
            if (result.getBool()) {
                visit(statement.get(i));
                break;
            }
            //If all else-if failed and else exist, go to else
            if (i != statement.size() - 1 && i == expr.size() - 1) {
                visit(statement.get(i + 1));
            }
        }
        return null;
    }


}

class ThisFunctionalityDoesNotSupport extends RuntimeException {
    public ThisFunctionalityDoesNotSupport(String message) {
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