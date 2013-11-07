import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ArrayList;
import java.util.Stack;
import java.util.List;
import java.io.*;
import java.lang.Exception;
import java.util.Iterator;

import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;


public class ProcedureInfo {
    OberonParser.ProcedurebodyContext ref;
    List<String> orderedArgs = new ArrayList<String>();
    Map<String, Type> formalArgs = new HashMap<String, Type>();
    Type returnType = Type.INVALID;
    String name;

    //TODO: validation, pass-by-reference

    public ProcedureInfo(String name, OberonParser.ProcedurebodyContext ref)
    { 
    	this.name = name;
    	this.ref  = ref;
    }

    public void MergeParams(OberonParser.FpsectionContext fp)
    {
    	Type type = NumberVisitor.GetType(fp.type());
    	for(TerminalNode id : fp.idlist().ID())
    	{
    		formalArgs.put(id.getText(), type);
    		orderedArgs.add(id.getText());
    	}
    }

    public void SetReturn(Type type)
    {
    	returnType = type;
    }

    public Type GetReturn()
    {
    	return returnType;
    }

    public OberonParser.ProcedurebodyContext GetRef()
    {
        return ref;
    }

    public Map<String, VariableContainer> MapArgs(List<VariableContainer> args)
    {
    	if(args.size() != orderedArgs.size())
    		throw new InvalidArgumentException("Argument lists don't math in: " + name + " call");

    	Map<String, VariableContainer> result = new HashMap<String, VariableContainer>();
    	Iterator<String> orderedArgsIterator = orderedArgs.iterator();

    	for(VariableContainer arg : args)
    	{
    		String argName = orderedArgsIterator.next();
    		Type type = formalArgs.get(argName);
    		if(type == null || type != arg.getType())
    			throw new InvalidArgumentException("Argument lists don't math in: " + name + " call");
    		result.put(argName, arg);
    	}

    	return result;
    }

    public String GetName()
    {
    	return name;
    }

    public String toString()
    {
    	return "Arguments: " + formalArgs.toString() + 
    		   " return type: " + returnType.toString() + 
    		   " reference set: " + new Boolean((ref != null)).toString();
    }
}

class InvalidArgumentException extends RuntimeException {
    public InvalidArgumentException(String message) {
        super(message);
    }    
}