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
    private OberonParser.ProcedurebodyContext ref;
    List<String> orderedArgs = new ArrayList<String>();
    private Map<String, Type> formalArgs = new HashMap<String, Type>();
    private Type returnType = Type.INVALID;
    //TODO: validation, pass-by-reference

    public ProcedureInfo(OberonParser.ProcedurebodyContext ref)
    { 
    	this.ref = ref;
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

    public OberonParser.ProcedurebodyContext GetRef()
    {
        return ref;
    }

    public Map<String, VariableContainer> MapArgs(List<VariableContainer> args)
    {
    	/*if !Validate()
    		throw new RuntimeException("AAA!");*/
    	Map<String, VariableContainer> result = new HashMap<String, VariableContainer>();
    	Iterator<String> iterator = orderedArgs.iterator();
    	for(VariableContainer arg : args)
    		result.put(iterator.next(), arg);
    	return result;
    }

    private Boolean Validate()
    {
        return true;
    }

    public String ToString()
    {
    	return "Arguments: " + formalArgs.toString() + 
    		   " return type: " + returnType.toString() + 
    		   " reference set: " + new Boolean((ref != null)).toString();
    }
}