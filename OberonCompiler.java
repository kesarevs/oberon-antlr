import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.FileInputStream;
import java.io.InputStream;

import oberon.*;

public class OberonCompiler {
    public static void main(String[] args) throws Exception {
        String inputFile = null;
        if (args.length > 0) {
            inputFile = args[0];
        }
        InputStream is = System.in;
        if (inputFile != null) {
            is = new FileInputStream(inputFile);
        }
        ANTLRInputStream input = new ANTLRInputStream(is);
        OberonLexer lexer = new OberonLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        OberonParser parser = new OberonParser(tokens);
        ParseTree tree = parser.module(); // parse

        NumberVisitor eval = new NumberVisitor();
        eval.visit(tree);
    }
}
