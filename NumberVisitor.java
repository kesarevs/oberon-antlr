import oberon.*;
import java.util.*;
import java.io.*;
import java.lang.Exception;

import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;

public class NumberVisitor extends OberonBaseVisitor<VariableContainer> {
    Map<String, String> types = new HashMap<String, String>();

    boolean shouldVisit = true;
    VariableContainer procedureReturn;

    Stack< Map<String, VariableContainer> > locals = new Stack< Map<String, VariableContainer> >();
    Map<String, VariableContainer> globals = new  HashMap<String, VariableContainer>();
    Map<String, ProcedureInfo> functionNodes = new HashMap<String, ProcedureInfo>();

    private VariableContainer lookup(String name)
    {
        if(!locals.empty() && locals.peek().containsKey(name))
            return locals.peek().get(name);
        if(globals.containsKey(name))
            return globals.get(name);
        return null;
    }

    @Override 
    public VariableContainer visitModule(OberonParser.ModuleContext ctx) { 
        List<TerminalNode> moduleName = ctx.ID();
        String name = moduleName.get(0).getText();
        String nameAtTheEnd = moduleName.get(1).getText();
        if (!name.equals(nameAtTheEnd)) {
            throw new ModuleNotFoundException("Error: \"End " + nameAtTheEnd + ".\"\nExpected: \"End " + name + ".\"");
        }
        //TODO: Add reference on module class.
        globals.put(name, new ConstVariableContainer(new Object()));
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

    public VariableContainer writeVars(List<VariableContainer> args) {
        for(VariableContainer var : args) {
            System.out.print(var);
            System.out.print(" ");
        }
        return null;
    }

    public VariableContainer writelnVars(List<VariableContainer> args) {
        writeVars(args);
        System.out.println();
        return null;
    }

    public VariableContainer readToVar(VariableContainer var) {
        Scanner sc = new Scanner(System.in);
        sc.useLocale(Locale.US);
        VariableContainer readedResult;
        if (sc.hasNextInt()) {
            readedResult = new VariableContainer(sc.nextInt());
        } else if (sc.hasNextFloat()) {
            readedResult = new VariableContainer(sc.nextFloat());
        } else if (sc.hasNext("TRUE.?")) {
            readedResult = new VariableContainer(true);
        } else if (sc.hasNext("FALSE.?")) {
            readedResult = new VariableContainer(false);
        } else {
            throw new ThisFunctionalityDoesNotSupport("");
        }
        var.setValue(readedResult);
        return var;
    }

    @Override 
    public VariableContainer visitCasestatement(OberonParser.CasestatementContext ctx) { 
        VariableContainer result = visit(ctx.expression());
        List<OberonParser.CaseitemContext> caseitems = ctx.caseitem();
        for (OberonParser.CaseitemContext it : caseitems) {
            ArrayList<VariableContainer> segments = visit(it.caselabellist()).getList();
            for(VariableContainer caseitem : segments) {
                if (caseitem == null) {
                    break;
                }
                if (caseitem.getType() == Type.RANGE) {
                    if (caseitem.contains(result).getBool()) {
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
            return new VariableContainer(new Range(result, nextRang));
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
                    result = result.difference(nextVal);
                    break;
                case OberonParser.PLUS:
                    result = result.sum(nextVal);
                    break;
                case OberonParser.OR:
                    result = result.logicOr(nextVal);
                    break;
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
                    return nextVal.contains(result);
            }
        }
        return result; 
    }

    @Override 
    public VariableContainer visitDesignator(OberonParser.DesignatorContext ctx) { 
        String varName = ctx.qualident().getText();
        VariableContainer element = lookup(varName);
        if (element == null) {
            throw new VariableNotDeclaredException("Variable " + varName + " is not declared.");
        }
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
        return result;
    }
    
    public ArrayList<VariableContainer> createArray(Integer len, VariableContainer defVal) {
        ArrayList<VariableContainer> arr = new ArrayList<VariableContainer>();
        for(int i = 0; i < len; i++) {
            arr.add(defVal.clone());
        }
        return arr;
    }

    public VariableContainer visitFactor(OberonParser.FactorContext ctx) {
        if (ctx.simpleexpression() != null) {
            return visit(ctx.simpleexpression());
        } else if (ctx.NOT() != null) {
            return visit(ctx.factor()).not();
        } else {
            return visitChildren(ctx);
        }
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
            dimension.assertNot(Type.INT);
            defVal = new VariableContainer(createArray(dimension.getInt(), defVal));
        }
        return defVal;
    }

    @Override 
    public VariableContainer visitVariabledeclaration(OberonParser.VariabledeclarationContext ctx) { 
        OberonParser.TypeContext type = ctx.type();
        List<OberonParser.IdentdefContext> varLst = ctx.identlist().identdef();
        for(OberonParser.IdentdefContext var : varLst) {
            String varName = var.getText();

            Map<String, VariableContainer> scope = locals.empty() ? globals : locals.peek();
            if (scope.containsKey(varName)) {
                throw new VariableDeclarationException("Variable " + varName + " already declared.");
            }
            switch (ctx.type().getText()) {
                case "INTEGER":
                    scope.put(varName, new VariableContainer(0));
                    break;
                case "REAL":
                    scope.put(varName, new VariableContainer(0f));
                    break;
                case "BOOLEAN":
                    scope.put(varName, new VariableContainer(false));
                    break;
            }
            if (ctx.type().isArr != null) {
                scope.put(varName, visit(ctx.type().isArr));
            }
        }
        return null; 
    }

    @Override 
    public VariableContainer visitConstantdeclaration(OberonParser.ConstantdeclarationContext ctx) { 
        String varName = ctx.identdef().getText();
        VariableContainer val = visit(ctx.expression());
            Map<String, VariableContainer> scope = locals.empty() ? globals : locals.peek();
        if (scope.containsKey(varName)) {
            throw new VariableDeclarationException("Variable " + varName + " already declared.");
        }
        scope.put(varName, new ConstVariableContainer(val));
        return val;
    }

    @Override 
    public VariableContainer visitWhilestatement(OberonParser.WhilestatementContext ctx) { 
        OberonParser.ExpressionContext exp = ctx.expression();
        OberonParser.StatementsequenceContext loop = ctx.statementsequence();
        Boolean doNext = visit(exp).getBool();;
        while (doNext) {
            visit(loop);
            doNext = visit(exp).getBool();
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
            doNext = visit(exp).getBool();
        } while (doNext);
        return null; 
    }

    @Override 
    public VariableContainer visitIfstatement(OberonParser.IfstatementContext ctx) { 
        List<OberonParser.ExpressionContext> expr = ctx.expression();
        List<OberonParser.StatementsequenceContext> statement = ctx.statementsequence();
        for (int i = 0; i < expr.size(); i++) {
            VariableContainer result = visit(expr.get(i));
            if (result.getBool()) {
                return visit(statement.get(i));
            }
        }
        //If all else-if failed and else exist, go to else
        if (expr.size() != statement.size()) {
            return visit(statement.get(statement.size() - 1));
        }
        return null;
    }

    @Override
    public VariableContainer visitStatementsequence(OberonParser.StatementsequenceContext ctx)
    {
        VariableContainer result = null;
        for(OberonParser.StatementContext stat : ctx.statement())
        {
            result = visit(stat);
            if(stat.K_RETURN() != null)
                break;
        }
        return result;
    }

    public static Type GetType(OberonParser.TypeContext tc)
    {
        if(tc.isArr != null)
            return Type.LIST;
        OberonParser.BaseTypesContext base = tc.baseTypes();
        if(base == null)
            return Type.INVALID;

        if(base.K_INTEGER() != null)
            return Type.INT;
        if(base.K_REAL() != null)
            return Type.REAL;
        if(base.K_BOOL() != null)
            return Type.BOOL;

        return Type.INVALID;
    }

    @Override 
    public VariableContainer visitProcedurecall(OberonParser.ProcedurecallContext ctx)
    {
        String functionName = ctx.ID().getText();
        if (functionName.equals("READ")) {
            String varName = ctx.actualparameters().explist().getText();
            VariableContainer element = lookup(varName);
            if (element == null) {
                throw new VariableNotDeclaredException("Variable " + varName + " is not declared.");
            }
            return readToVar(element);
        }
        List<VariableContainer> args = new ArrayList<VariableContainer>();
        OberonParser.ExplistContext expressions = ctx.actualparameters().explist();
        if(expressions != null)
        {
            for(OberonParser.ExpressionContext expr : expressions.expression())
                args.add(visit(expr));
        }
        if (functionName.equals("WRITELN")) {
            return writelnVars(args);
        }
        if (functionName.equals("WRITE")) {
            return writeVars(args);
        }

        ProcedureInfo funcNode = functionNodes.get(functionName);
        if(funcNode == null)
            throw new ProcedureNotFoundException("No procedure found " + functionName);

        Map<String, VariableContainer> scope = new HashMap<String, VariableContainer>();

        locals.push(funcNode.MapArgs(args));
        VariableContainer result = ExecuteProcedure(funcNode);
        locals.pop();
        return result;
    }


    private VariableContainer ExecuteProcedure(ProcedureInfo node)
    {
        OberonParser.ProcedurebodyContext body = node.GetRef();
        if(body.declarationsequence() != null)
            visit(body.declarationsequence());
        VariableContainer result = null;
        if(body.statementsequence() != null)
            result = visit(body.statementsequence());

        //TODO: void procedures still may have RETURN value in body
        if(node.GetReturn() != Type.INVALID && (result == null || result.getType() != node.GetReturn()))
            throw new ProcedureDefinitionException("Procedure return type doesn't match in: " + node.GetName() +
                                                   " required: " + node.GetReturn() + 
                                                   " given: " + (result == null ? result : result.getType()));
        return result;
    }

    @Override 
    public VariableContainer visitProcedurebody(OberonParser.ProcedurebodyContext ctx)
    {
        if(ctx.declarationsequence() != null)
            visit(ctx.declarationsequence());
        VariableContainer result = null;
        if(ctx.statementsequence() != null)
            result = visit(ctx.statementsequence());
        return result;
    }

    @Override
    public VariableContainer visitProceduredeclaration(OberonParser.ProceduredeclarationContext ctx)
    {
        String name = ctx.procedureheading().identdef().ID().getText();
        if(!name.equals(ctx.ID().getText()))
            throw new ProcedureDefinitionException("Function signature doesn't match: " + name + " " + ctx.ID().getText());
        if(functionNodes.containsKey(name))
            throw new ProcedureDefinitionException("Procedure redefinition: " + name);

        ProcedureInfo function = new ProcedureInfo(name, ctx.procedurebody());

        OberonParser.ParamsContext params = ctx.procedureheading().formalparameters().params();
        if(params != null)
            for(OberonParser.FpsectionContext fp : params.fpsection())
                function.MergeParams(fp);

        OberonParser.TypeContext returnType = ctx.procedureheading().formalparameters().type();
        if(returnType != null)
            function.SetReturn(GetType(returnType));

        functionNodes.put(name, function);
        return null;     
    }


}

class ProcedureDefinitionException extends RuntimeException {
    public ProcedureDefinitionException(String message) {
        super(message);
    }    
}

class ProcedureNotFoundException extends RuntimeException {
    public ProcedureNotFoundException(String message) {
        super(message);
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
