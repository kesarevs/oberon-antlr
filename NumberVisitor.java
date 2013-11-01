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
        //make const
        memory.put(name, new VariableContainer(new Object()));
        return visitChildren(ctx); 
    }

    @Override 
    public VariableContainer visitSet(OberonParser.SetContext ctx) {
        List<VariableContainer> set = new ArrayList<VariableContainer>();
        if (ctx.caselabellist() != null) {
            List<VariableContainer> segments = (List<VariableContainer>) visit(ctx.caselabellist()).getValue();
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
        for (int i = 0; i < caseitems.size(); i++) {
            List<VariableContainer> segments = (List<VariableContainer>) visit(caseitems.get(i).caselabellist()).getValue();
            for(int j = 0; j < segments.size(); j++) {
                VariableContainer caseitem = segments.get(j);
                if (caseitem.getValue() instanceof Range && result.getValue() instanceof Integer) {
                    Range r = (Range) caseitem.getValue();
                    if (r.contains((Integer) result.getValue())) {
                        return visit(caseitems.get(i).statementsequence());
                    }
                } else if ((Boolean) result.isEqual(caseitem).getValue()) {
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
    public VariableContainer visitCaselabellist(OberonParser.CaselabellistContext ctx) { 
        List<OberonParser.CaselabelsContext> labels = ctx.caselabels();
        List<VariableContainer> segments = new ArrayList<VariableContainer>();
        for(int i = 0; i < labels.size(); i++) {
            segments.add(visit(labels.get(i)));
        }
        return new VariableContainer(segments); 
    }

    @Override 
    public VariableContainer visitCaselabels(OberonParser.CaselabelsContext ctx) { 
        List<OberonParser.ExpressionContext> expr = ctx.expression();
        Object result = visit(expr.get(0)).getValue();
        if (ctx.RANGESEP() != null) {
            Object nextRang = visit(expr.get(1)).getValue();
            if (result instanceof Integer && nextRang instanceof Integer) {
                return new VariableContainer(new Range((Integer) result, (Integer) nextRang));
            } else {
                throw new TypeCastException("Can't cast variable to INTEGER in CASE range.");
            }
        }
        return new VariableContainer(result); 
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
    public VariableContainer visitFactor(OberonParser.FactorContext ctx) {
        if (ctx.simpleexpression() != null) {
            return visit(ctx.simpleexpression());
        }
        if (ctx.designator() != null) {
            String varName = ctx.designator().getText();
            if (!memory.containsKey(varName)) {
                throw new VariableNotDeclaredException("Variable " + varName + " is not declared.");
            }
            return memory.get(varName);
        }
        return visitChildren(ctx); 
    }

    @Override 
    public VariableContainer visitAssignment(OberonParser.AssignmentContext ctx) {
        VariableContainer result = visit(ctx.expression());
        String varName = ctx.designator().getText();
        if (!memory.containsKey(varName)) {
            throw new VariableNotDeclaredException("Variable " + varName + " is not declared.");
        }
        memory.get(varName).setValue(result);
        System.out.println(varName + " := " + result.getValue());
        return result;
    }

    @Override 
    public VariableContainer visitVariabledeclaration(OberonParser.VariabledeclarationContext ctx) { 
        OberonParser.TypeContext type = ctx.type();
        List<OberonParser.IdentdefContext> varLst = ctx.identlist().identdef();
        Object defVal = 0;
        switch (ctx.type().getText()) {
            case "INTEGER":
                defVal = 0;
                break;
            case "REAL":
                defVal = 0.0f;
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
            memory.put(varName, new VariableContainer(defVal));
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
        memory.put(varName, new ConstVariableContainer(val.getValue()));
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
            doNext = (Boolean) result.getValue();
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
            doNext = (Boolean) result.getValue();
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
            if ((Boolean) result.getValue()) {
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