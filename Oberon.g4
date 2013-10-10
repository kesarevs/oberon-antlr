grammar Oberon;

options
{
    language = Java;
}

// The keywords
K_ARRAY : 'ARRAY' ;
K_BEGIN : 'BEGIN';
K_CASE : 'CASE' ;
K_CONST : 'CONST' ;
K_DO : 'DO' ;
K_ELSE : 'ELSE' ;
K_ELSIF : 'ELSIF' ;
K_END : 'END' ;
K_EXIT : 'EXIT' ;
K_IF : 'IF' ;
K_IMPORT : 'IMPORT' ;
K_LOOP : 'LOOP' ;
K_MODULE : 'MODULE' ;
K_NIL : 'NIL' ;
K_OF : 'OF' ;
K_POINTER : 'POINTER' ;
K_PROCEDURE : 'PROCEDURE' ;
K_RECORD : 'RECORD' ;
K_REPEAT : 'REPEAT' ;
K_RETURN : 'RETURN';
K_THEN : 'THEN' ;
K_TO : 'TO' ;
K_TYPE : 'TYPE' ;
K_UNTIL : 'UNTIL' ;
K_VAR : 'VAR' ;
K_WHILE : 'WHILE' ;
K_WITH : 'WITH' ;
K_TRUE : 'TRUE' ;
K_FALSE : 'FALSE' ;

// relations
UNEQUAL : '#' ;
LESS : '<' ;
GREATER : '>' ;
LESSOREQ : '<=' ;
GREATEROREQ : '>=' ;
IN : 'IN' ;
EQUAL : '=' ;

// operators
ASSIGN : ':=' ;
DIV : '/' ;
ET : '&' ;
MINUS : '-' ;
MOD : '%' ;
MULT : '*' ;
OR : '|' ;
PLUS : '+' ;

muloperator: MULT | DIV | MOD | ET ;
addoperator: PLUS | MINUS | OR ;
relation: EQUAL | UNEQUAL | LESS | GREATER | LESSOREQ | GREATEROREQ | IN;
assignment: designator ASSIGN expression ;


PERIOD : '.' ;
RANGESEP : '..' ;
SEMI : ';' ;
UPCHAR : '^' ;
COLON : ':' ;
COMMA : ',' ;


ID : ('a'..'z'|'A'..'Z')('a'..'z'|'A'..'Z'|'_'|'0'..'9')* ;
fragment DIGIT : '0'..'9' ;
fragment HEXDIGIT : '0'..'9'|'A'..'F' ;
stringliteral : '"' ~('\\'|'"')* '"' ;


// BaseTypes
INT   : ('-')?DIGIT+ ;
HEX : HEXDIGIT+ 'H' ;
REAL : ('-')?DIGIT+'.'DIGIT+ ;
BOOL : K_TRUE | K_FALSE ;
number: INT | HEX | REAL;

// Type declaration
typedeclaration: ID EQUAL type ;
type: qualident | arraytype | recordtype | pointertype | proceduretype ;

arraytype: K_ARRAY expression (',' expression) K_OF type;
recordtype: K_RECORD ('(' qualident ')')? fieldlistsequence K_END ;
pointertype: K_POINTER K_TO type ;
proceduretype: K_PROCEDURE formalparameters? ;




module : K_MODULE ID SEMI importlist? declarationsequence?
    (K_BEGIN statementsequence)? K_END ID PERIOD ;

importlist : K_IMPORT importitem (COMMA importitem)* SEMI ;

importitem : ID (ASSIGN ID)? ;

declarationsequence :
        ( K_CONST (constantdeclaration SEMI)*
        | K_TYPE (typedeclaration SEMI)*
        | K_VAR (variabledeclaration SEMI)*)
        (proceduredeclaration SEMI | forwarddeclaration SEMI)*
        ;

constantdeclaration: identdef EQUAL expression ;

identdef: ID MULT? ;

expression: simpleexpression (relation simpleexpression)? ;

simpleexpression:  (PLUS|MINUS)? term (addoperator term)* ;

term: factor (muloperator factor)* ;

factor: number
        | stringliteral
        | K_NIL
        | set
        | designator ('(' explist? ')')?
        ;

set: '{' elementlist? '}' ;

elementlist: element (COMMA element)* ;

element: expression (RANGESEP expression)? ;

designator: qualident
    ('[' explist ']'
        | '(' qualident ')'
        | UPCHAR )* ;

explist: expression (COMMA expression)* ;

actualparameters: '(' explist? ')' ;

qualident: (ID '.')* ID ;

fieldlistsequence: fieldlist (SEMI fieldlist) ;

fieldlist: (identlist COLON type)? ;

identlist: identdef (COMMA identdef)* ;

variabledeclaration: identlist COLON type ;

proceduredeclaration: procedureheading SEMI procedurebody ID ;

procedureheading: K_PROCEDURE MULT? identdef formalparameters? ;

formalparameters: '(' params? ')' (COLON qualident)? ;

params: fpsection (SEMI fpsection)* ;

fpsection: K_VAR? idlist COLON formaltype ;

idlist: ID (COMMA ID)* ;

formaltype: (K_ARRAY K_OF)* (qualident | proceduretype);

procedurebody: declarationsequence (K_BEGIN statementsequence)? K_END ;

forwarddeclaration: K_PROCEDURE UPCHAR? ID MULT? formalparameters? ;

statementsequence: statement SEMI (statement SEMI)* ;

statement: assignment
        | procedurecall
        | ifstatement
        | casestatement
        | whilestatement
        | repeatstatement
        | loopstatement
        | withstatement 
        | K_EXIT
        | K_RETURN expression?
        ;

procedurecall: designator actualparameters? ;

ifstatement: K_IF expression K_THEN statementsequence
        (K_ELSIF expression K_THEN statementsequence)*
        (K_ELSE statementsequence)? K_END ;

casestatement: K_CASE expression K_OF caseitem ('|' caseitem)*
    (K_ELSE statementsequence)? K_END ;

caseitem: caselabellist COLON statementsequence ;

caselabellist: caselabels (COMMA caselabels)* ;

caselabels: expression (RANGESEP expression)? ;

whilestatement: K_WHILE expression K_DO statementsequence K_END ;

repeatstatement: K_REPEAT statementsequence K_UNTIL expression ;

loopstatement: K_LOOP statementsequence K_END ;

withstatement: K_WITH qualident COLON qualident K_DO statementsequence K_END ;



WS : ( ' ' | '\t' | '\r' | '\n') -> skip;

