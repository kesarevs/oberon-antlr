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

    class ArgInfo
    {
        public Type type;
        public Boolean byValue;
    }
    Map<String, ArgInfo> formalArgs = new HashMap<String, ArgInfo>();
    Type returnType = Type.INVALID;
    String name;


    public ProcedureInfo(String name, OberonParser.ProcedurebodyContext ref)
    { 
    	this.name = name;
    	this.ref  = ref;
    }

    public void MergeParams(OberonParser.FpsectionContext fp)
    {
        ArgInfo info = new ArgInfo();
    	info.type = NumberVisitor.GetType(fp.type());
        info.byValue = fp.K_VAR() == null;
    	for(TerminalNode id : fp.idlist().ID())
    	{
    		formalArgs.put(id.getText(), info);
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
    		throw new InvalidArgumentException("Argument lists don't match in: " + name + " call");

    	Map<String, VariableContainer> result = new HashMap<String, VariableContainer>();
    	Iterator<String> orderedArgsIterator = orderedArgs.iterator();

    	for(VariableContainer arg : args)
    	{
    		String argName = orderedArgsIterator.next();
    		ArgInfo info = formalArgs.get(argName);
    		if(info == null || info.type != arg.getType())
    			throw new InvalidArgumentException("Argument lists don't match in: " + name + " call");

            if(info.byValue)
                result.put(argName, new VariableContainer(arg));
            else
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