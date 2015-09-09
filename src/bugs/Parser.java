package bugs;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.*;

import tree.Tree;

/**
 * Parser for numeric expressions. Used as starter code for
 * the Bugs language parser in CIT594, Spring 2015.
 * 
 * @author Dave Matuszek
 * @version February 2015
 */
public class Parser {
    /** The tokenizer used by this Parser. */
    StreamTokenizer tokenizer = null;
    /** The number of the line of source code currently being processed. */
    private int lineNumber = 1;

    /**
     * The stack used for holding Trees as they are created.
     */
    public Stack<Tree<Token>> stack = new Stack<>();

    /**
     * Constructs a Parser for the given string.
     * @param text The string to be parsed.
     */
    public Parser(String text) {
        Reader reader = new StringReader(text);
        tokenizer = new StreamTokenizer(reader);
        tokenizer.parseNumbers();
        tokenizer.eolIsSignificant(true);
        tokenizer.slashStarComments(true);
        tokenizer.slashSlashComments(true);
        tokenizer.lowerCaseMode(false);
        tokenizer.ordinaryChars(33, 47);
        tokenizer.ordinaryChars(58, 64);
        tokenizer.ordinaryChars(91, 96);
        tokenizer.ordinaryChars(123, 126);
        tokenizer.quoteChar('\"');
        lineNumber = 1;
    }

    /**
     * Tries to build an &lt;expression&gt; on the global stack.
     * <pre>&lt;expression&gt; ::= &lt;arithmetic expression&gt; {  &lt;comparator&gt; &lt;arithmetic expression&gt; }
</pre>
     * A <code>SyntaxException</code> will be thrown if the add_operator
     * is present but not followed by a valid &lt;expression&gt;.
     * @return <code>true</code> if an expression is parsed.
     */
    public boolean isExpression() {
        if (!isArithmeticExpression()) return false;
        while (isComparator()) {
            if (!isArithmeticExpression()) error("Illegal expression after comparator");
            makeTree(2, 3, 1);
        }
        return true;
    }

    /**
     * Tries to build an &lt;expression&gt; on the global stack.
     * <pre>&lt;expression&gt; ::= &lt;term&gt; { &lt;add_operator&gt; &lt;expression&gt; }</pre>
     * A <code>SyntaxException</code> will be thrown if the add_operator
     * is present but not followed by a valid &lt;expression&gt;.
     * @return <code>true</code> if an expression is recognized.
     */
    public boolean isArithmeticExpression() {
        if (!isTerm())
            return false;
        while (isAddOperator()) {
            if (!isTerm()) error("Error in expression after '+' or '-'");
            makeTree(2, 3, 1);
        }
        return true;
    }

    /**
     * Tries to build a &lt;term&gt; on the global stack.
     * <pre>&lt;term&gt; ::= &lt;factor&gt; { &lt;multiply_operator&gt; &lt;term&gt; }</pre>
     * A <code>SyntaxException</code> will be thrown if the multiply_operator
     * is present but not followed by a valid &lt;term&gt;.
     * @return <code>true</code> if a term is parsed.
     */

    public boolean isTerm() {
        if (!isFactor()) {
            return false;
        }
        while (isMultiplyOperator()) {
            if (!isFactor()) {
                error("No term after '*' or '/'");
            }
            makeTree(2, 3, 1);
        }
        return true;
    }

    /**
     * Tries to build a &lt;factor&gt; on the global stack.
     * <pre>&lt;factor&gt; ::= [ &lt;unsigned factor&gt; ] &lt;name&gt;</pre>
     * A <code>SyntaxException</code> will be thrown if the opening
     * parenthesis is present but not followed by a valid
     * &lt;expression&gt; and a closing parenthesis.
     * @return <code>true</code> if a factor is parsed.
     */
    public boolean isFactor() {
        if(symbol("+") || symbol("-")) {
            if (isUnsignedFactor()) {
                makeTree(2, 1);
                return true;
            }
            error("No factor following unary plus or minus");
            return false; // Can't ever get here
        }
        return isUnsignedFactor();
    }

    /**
     * Tries to build an &lt;unsigned factor&gt; on the global stack.
     * <pre>&lt;unsigned factor&gt; ::= &lt;variable&gt; . &lt;variable&gt;
     *                    | &lt;function call&gt;
     *                    | &lt;variable&gt;
     *                    | &lt;number&gt;
     *                    | "(" &lt;expression&gt; ")"</pre>
     * A <code>SyntaxException</code> will be thrown if the opening
     * parenthesis is present but not followed by a valid
     * &lt;expression&gt; and a closing parenthesis.
     * @return <code>true</code> if a factor is parsed.
     */
    public boolean isUnsignedFactor() {
        if (name()) {
            if (symbol(".")) {
                // reference to another Bug
                if (name()) {
                    makeTree(2, 3, 1);
                }
                else error("Incorrect use of dot notation");
            }
            else if (isParameterList()) {
                // function call
                pushNewNode("call");
                makeTree(1, 3, 2);
            }
            else {
                // just a variable; leave it on the stack
            }
        }
        else if (number()) {
            // leave the number on the stack
        }
        else if (symbol("(")) {
            stack.pop();
            if (!isExpression()) {
                error("Error in parenthesized expression");
            }
            if (!symbol(")")) {
                error("Unclosed parenthetical expression");
            }
            stack.pop();
        }
        else {
            return false;
        }
       return true;
    }
    
    /**
     * Tries to build an &lt;parameter list&gt; on the global stack.
     * <pre>&ltparameter list&gt; ::= "(" [ &lt;expression&gt; { "," &lt;expression&gt; } ] ")"
     * @return <code>true</code> if a parameter list is recognized.
     */
    public boolean isParameterList() {
        if (!symbol("(")) return false;
        stack.pop(); // remove open paren
        pushNewNode("var");
        if (isExpression()) {
            makeTree(2, 1);
            while (symbol(",")) {
                stack.pop(); // remove comma
                if (!isExpression()) error("No expression after ','");
                makeTree(2, 1);
            }
        }
        if (!symbol(")")) error("Parameter list doesn't end with ')'");
        stack.pop(); // remove close paren
        return true;
    }
    
    /**
     * Tries to build an &lt;block&gt; on the global stack.
     * <pre>&lt;block&gt; ::= "{" &lt;eol&gt; { &lt;command&gt;} "}"</pre>
     * A <code>SyntaxException</code> will be thrown if <ul>
     * <li> the "{" symbol is present but not followed by a &lt;eol&gt;</li>
     * <li> the "{" symbol is present but not followed by a "}"</li>
     * <li> no &lt;eol&gt; is present in the end of block</li>
     * @return <code>true</code> if a block is recognized.
     */
    public boolean isBlock() {
    	if (!symbol("{")) return false;
    	stack.pop();
    	if (!isEol()) error("No EOL after starting brace in block");
    	stack.pop();
    	pushNewNode("block");
    	while(isCommand()) {
    		makeTree(2,1);
    	}
    	if (!symbol("}")) error("No ending brace in block");
    	stack.pop();
    	if (!isEol()) error("No EOL after ending brace in block");
    	stack.pop();
    	return true;
    }

    /**
     * Tries to recognize an &lt;add_operator&gt; and put it on the global stack.
     * <pre>&lt;add_operator&gt; ::= "+" | "-"</pre>
     * @return <code>true</code> if an addop is recognized.
     */
    public boolean isAddOperator() {
        return symbol("+") || symbol("-");
    }

    /**
     * Tries to recognize a &lt;multiply_operator&gt; and put it on the global stack.
     * <pre>&lt;multiply_operator&gt; ::= "*" | "/"</pre>
     * @return <code>true</code> if a multiply_operator is recognized.
     */
    public boolean isMultiplyOperator() {
        return symbol("*") || symbol("/");
    }
    
    /**
     * Tries to parse a &lt;variable&gt;; same as &lt;isName&gt;.
     * <pre>&lt;variable&gt; ::= &lt;NAME&gt;</pre>
     * @return <code>true</code> if a variable is parsed.
     */
    public boolean isVariable() {
        return name();
    }
    
    /**
     *Tries to build an &lt;program&gt; on the global stack.
     * <pre>&lt;program&gt; ::= [&lt;allbugs code&gt;]
     *                          &lt;bug definition&gt;
     *                          {&lt;bug definition&gt;}
     * </pre>
     * @return <code>true</code> if a program is recognized.
     */ 

    
    public boolean isProgram() {
        if(isEol()){
            stack.pop();
        }
        
        if (isAllbugsCode()) {
            
        } else
             pushNewNode("Allbugs");

        if (!isBugDefinition()) return false;
        pushNewNode("list");
        makeTree(1, 2);
        while(isBugDefinition()) {
            makeTree(2, 1);
        }
        if (!nextTokenMatches(Token.Type.EOF)) error("No EOF at the end of program code");
        stack.pop();
        
        pushNewNode("program");
        makeTree(1, 3, 2);
        
        return true;
    }
    
    /**
     * Tries to build an &lt;allbugs code&gt; on the global stack.
     * <pre>&lt;allbugs code&gt ::= "Allbugs" "{" &lt;eol&gt;
     *                                {&lt;var declaration&gt;}
     *                                {&lt;function declaration&gt;} 
     *                              "}" &lt;eol&gt;</pre>
     * A <code>SyntaxException</code> will be thrown if <ul>
     * <li> the "Allbugs" keyword is present but not followed by a starting "{"</li>
     * <li> no &lt;eol&gt; after starting "{"</li>
     * <li> no ending "}" at the end of all bugs code</li>
     * <li> no &lt;eol&gt; is present in the end of the code</li>
     * @return <code>true</code> if a allbugs code is recognized.
     */ 

public boolean isAllbugsCode() {
        
        boolean varTree = false;
        boolean funcTree = false;
        
        if (!keyword("Allbugs")) return false;
        if (!symbol("{")) error("No starting brace in allbugs code");
        stack.pop();
        if (!isEol()) error("No EOL after starting brace in allbugs code");
        stack.pop();
        
        pushNewNode("list");
        while(isVarDeclaration()){
            makeTree(2, 1);
        }
         
        pushNewNode("list");
        while(isFunctionDefinition()){
            makeTree(2, 1);
        }
        
        makeTree(3, 2, 1);
        if (!symbol("}")) error("No ending brace in allbugs code");
        stack.pop();
        if (!isEol()) error("No EOL at the end of allbugs code");
        stack.pop();

        return true;
    }
    /**
     * Tries to build an &lt;bug definition&gt; on the global stack.
     * <pre>&lt;bug definition&gt ::= "Bug" &lt;name&gt; "{" &lt;eol&gt;
     *                                     {&lt;var declaration&gt;}
     *                                     [&lt;initialization block&gt;]
     *                                     &lt;command&gt;
     *                                     {&lt;command&gt;}
     *                                     {&lt;function definition&gt;}
     *                                "}" &lt;eol&gt;</pre>
     * A <code>SyntaxException</code> will be thrown if <ul>
     * <li> the "Bug" keyword is present but not followed by a &lt;name&gt;</li>
     * <li> the "Bug" keyword and &lt;name&gt; is present but not followed by a "{"</li>
     * <li> no &lt;eol&gt; after starting "{"</li>
     * <li> no &lt;command&gt; in the definition</li>
     * <li> no ending "}" is present at the end of the definition</li>
     * <li> no &lt;eol&gt; is present in the end of the definition</li>
     * @return <code>true</code> if a bug definition is recognized.
     */ 
   	

    
public boolean isBugDefinition() {     
    if (!keyword("Bug")) return false;
    if (!name()) error("No name in bug definition");
    if (!symbol("{")) error("No starting brace in bug definition");
    stack.pop();
    if (!isEol()) error("No EOL after starting brace in bug definition");
    stack.pop();
    
    pushNewNode("list");
    while (isVarDeclaration()){
        makeTree(2,1);
    }
    
    if(isInitializationBlock()){
        
    } else{
    	pushNewNode("block"); // change made
    	pushNewNode("initially");
    	makeTree(1,2);
    }
        
    
    if(!isCommand()) error("No command in bug definition");
    pushNewNode("block");
    makeTree(1, 2);
    while(isCommand()) {
        makeTree(2, 1);
    }
    
    pushNewNode("list");
    while (isFunctionDefinition()){
        makeTree(2,1);
    }

    makeTree(6, 5, 4, 3, 2, 1);
    
    if (!symbol("}")) error("No ending brace in bug definition");
    stack.pop();
    if (!isEol()) error("No EOL at the end of bug definition");
    stack.pop();
    
    return true;
}
    /**
     * Tries to build an &lt;var declaration&gt; on the global stack.
     * <pre>&lt;var declaration&gt ::= "var" &lt;NAME&gt; { "," &lt;NAME&gt;} &lt;eol&gt;</pre>
     * A <code>SyntaxException</code> will be thrown if <ul>
     * <li> the "var" keyword is present but not followed by a &lt;NAME&gt;</li>
     * <li> the "," symbol is present but not followed by a &lt;NAME&gt;</li>
     * <li> no &lt;eol&gt; is present in the end of the declaration</li>
     * @return <code>true</code> if a var declaration is recognized.
     */ 
    public boolean isVarDeclaration() {
    	if (!keyword("var")) return false;
    	if (!name()) error("No name in var declaration");
    	makeTree(2,1);
    	while(symbol(",")){
    		stack.pop();
    		if (!name()) error("No name after , in declaration");
    		makeTree(2, 1);
    	}
    	if (!isEol()) error("No EOL in the end of var declaration");
    	stack.pop();
    	return true;
    }
 
    /**
     *Tries to build an &lt;initialization block&gt; on the global stack.
     * <pre>&lt;initialization block&gt; ::= "initially" &lt;block&gt;</pre>
     * A <code>SyntaxException</code> will be thrown if <ul>
     * <li> the "initially" keyword is present but not followed by a &lt;block&gt;</li>
     * @return <code>true</code> if a initialization block is recognized.
     */ 
    public boolean isInitializationBlock() {
    	if (!keyword("initially")) return false;
    	if (!isBlock()) error("No block after initially keyword");
    	makeTree(2, 1);
    	return true;
    }
  
    /**
     * Tries to recognize a &lt;command&gt;.
     * <pre>&lt;command&gt; ::= &lt;action&gt;
     *                      | &lt;statement&gt;
     * </pre>
     * A <code>SyntaxException</code> will be thrown if <ul>
     * <li>isAction() throws a <code>SyntaxException</code></li>
     * <li>isStatement() throws a <code>SyntaxException</code></li>
     * @return <code>true</code> if a command is recognized.
     */
    public boolean isCommand() {
    	boolean bool = isAction() || isStatement();
    	return bool;
    }
 
    /**
     * Tries to build an &lt;statement&gt; on the global stack.
     * <pre>&lt;statement&gt; ::= &lt;assignment statement&gt;
     *                      | &lt;loop statement&gt;
     *                      | &lt;exit if statement&gt;
     *                      | &lt;switch statement&gt;
     *                      | &lt;return statement&gt;
     *                      | &lt;do statement&gt;
     *                      | &lt;color statement&gt;
     * </pre>
     * A <code>SyntaxException</code> will be thrown if <ul>
     * <li>isAssignmentStatement() throws a <code>SyntaxException</code></li>
     * <li>isLoopStatement() throws a <code>SyntaxException</code></li>
     * <li>isExitIfStatement() throws a <code>SyntaxException</code></li>
     * <li>isSwitchStatement() throws a <code>SyntaxException</code></li>
     * <li>isReturnStatement() throws a <code>SyntaxException</code></li>
     * <li>isDoStatement() throws a <code>SyntaxException</code></li>
     * <li>isColorStatement() throws a <code>SyntaxException</code></li>
     * @return <code>true</code> if a statement is recognized.
     */
    public boolean isStatement() {
    	return isAssignmentStatement() || isLoopStatement() || isExitIfStatement()
    			|| isSwitchStatement() || isReturnStatement() || isDoStatement() || isColorStatement();
    }
    
    /**
     * Tries to build an &lt;action&gt; on the global stack.
     * <pre>&lt;action&gt; ::= &lt;move action&gt;
     *                      | &lt;moveto action&gt;
     *                      | &lt;turn action&gt;
     *                      | &lt;turnto action&gt;
     *                      | &lt;line action&gt;
     * </pre>
     * A <code>SyntaxException</code> will be thrown if <ul>
     * <li>isMoveAction() throws a <code>SyntaxException</code></li>
     * <li>isMoveToAction() throws a <code>SyntaxException</code></li>
     * <li>isTurnAction() throws a <code>SyntaxException</code></li>
     * <li>isTurnToAction() throws a <code>SyntaxException</code></li>
     * <li>isLineAction() throws a <code>SyntaxException</code></li>
     * @return <code>true</code> if an action is recognized.
     */
    public boolean isAction() {
    	return isMoveAction() || isMoveToAction() || isTurnAction()
    			|| isTurnToAction() || isLineAction();
    }
    
    /**
     * Tries to build an &lt;move action&gt; on the global stack.
     * <pre>&lt;move action&gt; ::= "move" &lt;expression&gt; &lt;eol&gt;</pre>
     * A <code>SyntaxException</code> will be thrown if <ul>
     * <li> the "move" keyword is present but not followed by an &lt;expression&gt;</li>
     * <li> no &lt;eol&gt; is present in the end of the action</li>
     * @return <code>true</code> if a move action is recognized.
     */ 
    public boolean isMoveAction() {
    	if (!keyword("move")) return false;
    	if (!isExpression()) error("No expression after move action");
    	if (!isEol()) error("No eol at the end of move command");
    	stack.pop();
    	makeTree(2,1);
    	return true;
    }
    
    /**
     * Tries to build an &lt;moveto action&gt; on the global stack.
     * <pre>&lt;moveto action&gt; ::= "moveto" &lt;expression&gt; "," &lt;expression&gt;  &lt;eol&gt;</pre>
     * A <code>SyntaxException</code> will be thrown if <ul>
     * <li> A "moveto" keyword is present but not followed by an &lt;expression&gt;</li>
     * <li> A"," symbol is not present after "moveto" keyword and first &lt;expression&gt;</li>
     * <li> A "," symbol is present but not followed by an &lt;expression&gt;</li>
     * <li> no &lt;eol&gt; is present in the end of the action</li>
     * @return <code>true</code> if a moveto action is recognized.
     */ 
    public boolean isMoveToAction() {
    	if (!keyword("moveto")) return false;
    	if (!isExpression()) error("No expression after moveto action");
    	if (!symbol(",")) error("No , in moveto action");
    	stack.pop();
    	if (!isExpression()) error("No expression after , in moveto action");
    	if (!isEol()) error("No eol at the end of moveto command");
    	stack.pop();
    	makeTree(3, 2, 1);
    	return true;
    }
    
    /**
     * Tries to build an &lt;turn action&gt; on the global stack.
     * <pre>&lt;turn action&gt; ::= "turn" &lt;expression&gt; &lt;eol&gt;</pre>
     * A <code>SyntaxException</code> will be thrown if <ul>
     * <li> the "turn" keyword is present but not followed by an &lt;expression&gt;</li>
     * <li> no &lt;eol&gt; is present in the end of the action</li>
     * @return <code>true</code> if a turn action is recognized.
     */ 
    public boolean isTurnAction() {
    	if (!keyword("turn")) return false;
    	if (!isExpression()) error("No expression after turn action");
    	if (!isEol()) error("No eol at the end of turn command");
    	stack.pop();
    	makeTree(2,1);
    	return true;
    }

    /**
     * Tries to build an &lt;turnto action&gt; on the global stack.
     * <pre>&lt;turnto action&gt; ::= "turnto" &lt;expression&gt; &lt;eol&gt;</pre>
     * A <code>SyntaxException</code> will be thrown if <ul>
     * <li> the "turnto" keyword is present but not followed by an &lt;expression&gt;</li>
     * <li> no &lt;eol&gt; is present in the end of the action</li>
     * @return <code>true</code> if a turnto action is recognized.
     */ 
    public boolean isTurnToAction() {
    	if (!keyword("turnto")) return false;
    	if (!isExpression()) error("No expression after turn to action");
    	if (!isEol()) error("No eol at the end of turn to command");
    	stack.pop();
    	makeTree(2,1);
    	return true;
    }
    
    /**
     * Tries to build an &lt;line action&gt; on the global stack.
     * <pre>&lt;line action&gt; ::= "line" &lt;expression&gt; "," &lt;expression&gt; "," &lt;expression&gt; "," &lt;expression&gt; &lt;eol&gt;</pre>
     * A <code>SyntaxException</code> will be thrown if <ul>
     * <li> A "line" keyword is present but not followed by an &lt;expression&gt;</li>
     * <li> One of the 3 "," symbols is not present after "line" keyword and first &lt;expression&gt;</li>
     * <li> A "," symbol is present but not followed by an &lt;expression&gt;</li>
     * <li> no &lt;eol&gt; is present in the end of the action</li>
     * @return <code>true</code> if a line action is recognized.
     */ 
    public boolean isLineAction() {
    	if (!keyword("line")) return false;
    	if (!isExpression()) error("No expression after line action");
    	for (int i=1; i<=3; i++) {
    		if (!symbol(",")) error("Missing , at " + i + " pos in line action");
    		stack.pop();
    		if (!isExpression()) error("Missing expression at " + (i+1) + " pos in line action");
    	}
    	if (!isEol()) error("Missing EOL at the end of line action");
    	stack.pop();
    	makeTree(5,4,3,2,1);
    	return true;
    }
 
    /**
     *Tries to build an &lt;assignment statement&gt; on the global stack.
     * <pre>&lt;assignment statement&gt; ::= &lt;variable&gt; "=" &lt;expression&gt; &lt;eol&gt;</pre>
     * A <code>SyntaxException</code> will be thrown if <ul>
     * <li> A &lt;variable&gt; and "=" symbol is present but not followed by an &lt;expression&gt;</li>
     * <li> no &lt;eol&gt; is present in the end of the statement</li>
     * @return <code>true</code> if a assignment statement is recognized.
     */ 
    public boolean isAssignmentStatement() {
    	if (!isVariable()) return false;
    	if (!symbol("=")) error("No '=' after variable");
    	
    		stack.pop();
    		pushNewNode("assign");
    
    	if (!isExpression()) error("No expression after assignment operator");
    	if (!isEol()) error("No EOL at the end of assignment statement");
    	stack.pop();
    	makeTree(2, 3, 1);
    	return true;
    }
    
    /**
     * Tries to build an &lt;loop statement&gt; on the global stack.
     * <pre>&lt;loop statement&gt; ::= "loop" &lt;block&gt;</pre>
     * A <code>SyntaxException</code> will be thrown if <ul>
     * <li> the "loop" keyword is present but not followed by an &lt;block&gt;</li>
     * @return <code>true</code> if a loop statement is recognized.
     */ 
    public boolean isLoopStatement() {
    	if (!keyword("loop")) return false;
    	if (!isBlock()) error("No block in loop");
    	makeTree(2,1);
    	return true;
    }
    
    /**
     *Tries to build an &lt;exit if statement&gt; on the global stack.
     * <pre>&lt;exit if statement&gt; ::= "exit" "if "&lt;expression&gt; &lt;eol&gt;</pre>
     * A <code>SyntaxException</code> will be thrown if <ul>
     * <li> the "exit" keyword is present but not followed by an "if" keyword</li>
     * <li> the "exit" and "if" keywords are present but not followed by an &lt;expression&gt;</li>
     * <li> no &lt;eol&gt; is present in the end of the statement</li>
     * @return <code>true</code> if an exit if statement is recognized.
     */ 
    public boolean isExitIfStatement() {
    	if (!keyword("exit")) return false;
    	if (!keyword("if")) error("No if after exit in statement");
    	stack.pop();
    	if (!isExpression()) error("No expression after exit if");
    	if (!isEol()) error("No EOL at the end of exit if statement");
    	stack.pop();
    	makeTree(2,1);
    	return true;
    }
    
    /**
     * Tries to build an &lt;switch statement&gt; on the global stack.
     * <pre>&lt;switch statement&gt; ::= "switch" "{" &lt;eol&gt;
     *                                      { "case" &lt;expression&gt; &lt;eol&gt;
     *                                          {&lt;command&gt;}}
     *                                   "}" &lt;eol&gt;
     * </pre>
     * A <code>SyntaxException</code> will be thrown if <ul>
     * <li> the "switch" keyword is present but not followed by a starting "{"</li>
     * <li> no &lt;eol&gt; is present after switch "{"</li>
     * <li> "case" is present but not followed by an &lt;expression&gt;</li>
     * <li> no &lt;eol&gt; is present in the end of the case statement</li>
     * <li> the "switch" keyword and starting "{" is present but not followed by an ending "}";</li>
     * <li> no &lt;eol&gt; is present in the end of the switch statement</li>
     * @return <code>true</code> if a switch statement is recognized.
     */

    public boolean isSwitchStatement() {
        if (!keyword("switch")) return false;
        if (!symbol("{")) error("No starting brace after switch");
        stack.pop();
        if (!isEol()) error("No EOL after switch {");
        stack.pop();
        while (keyword("case")) {
            if (!isExpression()) error("No expression after case");
            makeTree(2,1);
            if (!isEol()) error("No EOL after case expression");
            stack.pop();
            
            pushNewNode("block");
            while (isCommand()) {
                makeTree(2, 1);
            }
            
            makeTree(2, 1);
            makeTree(2, 1);
        }
        if (!symbol("}")) error("No closing brace in switch statement");
        stack.pop();
        if (!isEol()) error("No EOL at the end of switch");
        stack.pop();
        return true;
    }

    /**
     * Tries to build an &lt;return statement&gt; on the global stack.
     * <pre>&lt;return statement&gt; ::= "return" &lt;expression&gt; &lt;eol&gt;</pre>
     * A <code>SyntaxException</code> will be thrown if <ul>
     * <li> the "return" keyword is present but not followed by a &lt;expression&gt;</li>
     * <li> no &lt;eol&gt; is present in the end of the statement</li>
     * @return <code>true</code> if a return statement is recognized.
     */ 
    public boolean isReturnStatement() {
    	if (!keyword("return")) return false;
    	if (!isExpression()) error("No expression after return");
    	if (!isEol()) error("No eol at the end of return statement");
    	stack.pop();
    	makeTree(2,1);
    	return true;
    }
    
    /**
     * Tries to build an &lt;do statement&gt; on the global stack.
     * <pre>&lt;do statement&gt; ::= "do" &lt;variable&gt; [&lt;parameter list&gt;] &lt;eol&gt;</pre>
     * A <code>SyntaxException</code> will be thrown if <ul>
     * <li> the "do" keyword is present but not followed by a &lt;variable&gt;</li>
     * <li> no &lt;eol&gt; is present in the end of the statement</li>
     * @return <code>true</code> if a do statement is recognized.
     */    
    public boolean isDoStatement() {
    	boolean paramList= false;
    	if (!keyword("do")) return false;
    	stack.pop();
    	pushNewNode("call");
    	if (!isVariable()) error("No variable after do");
    	if (isParameterList()) {
    		paramList = true;
    	}
    	if (!isEol()) error("No EOL at end of do statement");
    	stack.pop();
    	if (paramList)
    		makeTree(3, 2, 1);
    	else
    		makeTree(2, 1);
    	return true;
    }

    /**
     * Tries to build an &lt;color statement&gt; on the global stack.
     * <pre>&lt;color statement&gt; ::= "color" &lt;KEYWORD&gt; &lt;eol&gt;</pre>
     * A <code>SyntaxException</code> will be thrown if <ul>
     * <li> the "color" keyword is present but not followed by a &lt;KEYWORD&gt;</li>
     * <li> no &lt;eol&gt; is present in the end of the statement</li>
     * @return <code>true</code> if a color statement is recognized.
     */
    public boolean isColorStatement() {
    	if (!keyword("color")) return false;
    	if (!keyword()) error("No color name after color");
    	if (!isEol()) error("No EOL at the end of color statement");
    	stack.pop();
    	makeTree(2, 1);
    	return true;
    }
    
 
    
    /**
     *Tries to build an &lt;comparator&gt; on the global stack.
     * <pre>&lt;comparator&gt; ::= "<" | "<=" | "=" | "!=" | ">=" | ">"</pre>
     * @return <code>true</code> if a comparator is recognized
     */
    public boolean isComparator() {
    	
        	if(symbol("<")) {
        		if (symbol("=")) {
        			stack.pop();
        			stack.pop();
        			pushNewNode("<=");
        		}
        		return true;
        	}
        	
        	if(symbol(">")) {
        		if (symbol("=")) {
        			stack.pop();
        			stack.pop();
        			pushNewNode(">=");
        		}
        		return true;
        	}
        	
        	if (symbol("!")) {
        		if(!symbol("=")) {
        			error("No '=' defined after '!'");}
        		else{
        			stack.pop();
        			stack.pop();
        			pushNewNode("!=");
        			return true;
        		}
        		pushBack();
        		return false;
        	}
        	
        	if(symbol("=")) return true;
        	
        	return false;
        }
    
    
    /**
     * Tries to build an &lt;function definition&gt; on the global stack.
     * <pre>&lt;function definition&gt; ::= "define" &lt;NAME&gt; [ "using" &lt;variable&gt; { "," &lt;variable&gt; }] &lt;block&gt;</pre>
     * A <code>SyntaxException</code> will be thrown if <ul>
     * <li> the "define" keyword is present but not followed by a &lt;NAME&gt;</li>
     * <li> the "using" keyword is present but not followed by a &lt;variable&gt;</li>
     * <li> the "," symbol is present but not followed by a &lt;variable&gt;</li>
     * <li> no &lt;block&gt; is present in the function definition</li>
     * @return <code>true</code> if a function definition is recognized.
     */
    public boolean isFunctionDefinition() {
        if (!keyword("define")) return false;
        stack.pop();
        pushNewNode("function");
        if (!name()) error("No function name in definition");
        makeTree(2,1);
        if (keyword("using")) {
            stack.pop();
            if (!isVariable())
                error("No variable defined after keyword using");
            pushNewNode("var");
            makeTree(1,2);  
            while (symbol(",")){
                stack.pop();
                if (!isVariable())
                    error("No variable defined after ,");
                makeTree(2,1);
            }
        } else {
            pushNewNode("var");
        }
        makeTree(2,1);
        if (!isBlock()) error("No block in function definition");
        makeTree(2,1);
        return true;
    }
    
    
    /**
     * Tries to build an &lt;function call&gt; on the global stack.
     * <pre>&lt;function call&gt; ::= &lt;NAME&gt; &lt;parameter list&gt;</pre>
     * @return <code>true</code> if a function call is recognized.
     */
    public boolean isFunctionCall() {
    	if (!name()) return false;
    	if (!isParameterList()) return false;
    	pushNewNode("call");
    	makeTree(1, 3, 2);
    	return true;
    }

  
   
    /**
     * Tries to recognize a &lt;eol&gt;.
     * <pre>&lt;eol&gt; ::= &lt;EOL&gt; { &lt;EOL&gt; }</pre>
     * @return <code>true</code> if an eol is recognized.
     */
    public boolean isEol() {
        if (!eol()) return false;
        while(eol()) {
            stack.pop();
        }
        return true;
    }

    


    //------------------------- Private "helper" methods
    
    /**
     * Creates a new Tree consisting of a single node containing a
     * Token with the correct type and the given <code>value</code>,
     * and pushes it onto the global stack. 
     *
     * @param value The value of the token to be pushed onto the global stack.
     */
    private void pushNewNode(String value) {
        stack.push(new Tree<>(new Token(Token.typeOf(value), value)));
    }

    /**
     * Tests whether the next token is a number. If it is, the token
     * is moved to the stack, otherwise it is not.
     * 
     * @return <code>true</code> if the next token is a number.
     */
    private boolean number() {
        return nextTokenMatches(Token.Type.NUMBER);
    }

    /**
     * Tests whether the next token is a name. If it is, the token
     * is moved to the stack, otherwise it is not.
     * 
     * @return <code>true</code> if the next token is a name.
     */
    private boolean name() {
        return nextTokenMatches(Token.Type.NAME);
    }

    /**
     * Tests whether the next token is the expected name. If it is, the token
     * is moved to the stack, otherwise it is not.
     * 
     * @param expectedName The String value of the expected next token.
     * @return <code>true</code> if the next token is a name with the expected value.
     */
    private boolean name(String expectedName) {
        return nextTokenMatches(Token.Type.NAME, expectedName);
    }

    /**
     * Tests whether the next token is the expected keyword. If it is, the token
     * is moved to the stack, otherwise it is not.
     *
     * @param expectedKeyword The String value of the expected next token.
     * @return <code>true</code> if the next token is a keyword with the expected value.
     */
    private boolean keyword(String expectedKeyword) {
        return nextTokenMatches(Token.Type.KEYWORD, expectedKeyword);
    }
    
   
    
    /**
     * Tests whether the next token is a keyword. If it is, the token
     * is moved to the stack, otherwise it is not.
     *
     * @return <code>true</code> if the next token is a keyword.
     */
    private boolean keyword() {
    	return nextTokenMatches(Token.Type.KEYWORD);
    }
   
    /**
     * Tests whether the next token is an EOL. If it is, the token
     * is moved to the stack, otherwise it is not.
     *
     * @return <code>true</code> if the next token is a EOL.
     */
    private boolean eol() {
    	return nextTokenMatches(Token.Type.EOL);
    }

    /**
     * Tests whether the next token is the expected symbol. If it is,
     * the token is moved to the stack, otherwise it is not.
     * 
     * @param expectedSymbol The single-character String that is expected
     *        as the next symbol.
     * @return <code>true</code> if the next token is the expected symbol.
     */
    private boolean symbol(String expectedSymbol) {
        return nextTokenMatches(Token.Type.SYMBOL, expectedSymbol);
    }

    /**
     * If the next Token has the expected type, it is used as the
     * value of a new (childless) Tree node, and that node
     * is then pushed onto the stack. If the next Token does not
     * have the expected type, this method effectively does nothing.
     * 
     * @param type The expected type of the next token.
     * @return <code>true</code> if the next token has the expected type.
     */
    private boolean nextTokenMatches(Token.Type type) {
        Token t = nextToken();
        if (t.type == type) {
            stack.push(new Tree<>(t));
            return true;
        }
        pushBack();
        return false;
    }

    /**
     * If the next Token has the expected type and value, it is used as
     * the value of a new (childless) Tree node, and that node
     * is then pushed onto the stack; otherwise, this method does
     * nothing.
     * 
     * @param type The expected type of the next token.
     * @param value The expected value of the next token; must
     *              not be <code>null</code>.
     * @return <code>true</code> if the next token has the expected type.
     */
    private boolean nextTokenMatches(Token.Type type, String value) {
        Token t = nextToken();
        if (type == t.type && value.equals(t.value)) {
            stack.push(new Tree<>(t));
            return true;
        }
        pushBack();
        return false;
    }

    /**
     * Returns the next Token. Increments the global variable
     * <code>lineNumber</code> when an EOL is returned.
     * 
     * @return The next Token.
     */
    Token nextToken() {
        int code;
        try { code = tokenizer.nextToken(); }
        catch (IOException e) { throw new Error(e); } // Should never happen
        switch (code) {
            case StreamTokenizer.TT_WORD:
                if (Token.KEYWORDS.contains(tokenizer.sval)) {
                    return new Token(Token.Type.KEYWORD, tokenizer.sval);
                }
                return new Token(Token.Type.NAME, tokenizer.sval);
            case StreamTokenizer.TT_NUMBER:
                return new Token(Token.Type.NUMBER, tokenizer.nval + "");
            case StreamTokenizer.TT_EOL:
                lineNumber++;
                return new Token(Token.Type.EOL, "\n");
            case StreamTokenizer.TT_EOF:
                return new Token(Token.Type.EOF, "EOF");
            default:
                return new Token(Token.Type.SYMBOL, ((char) code) + "");
        }
    }

    /**
     * Returns the most recent Token to the tokenizer. Decrements the global
     * variable <code>lineNumber</code> if an EOL is pushed back.
     */
    void pushBack() {
        tokenizer.pushBack();
        if (tokenizer.ttype == StreamTokenizer.TT_EOL) lineNumber--;
    }

    /**
     * Assembles some number of elements from the top of the global stack
     * into a new Tree, and replaces those elements with the new Tree.<p>
     * <b>Caution:</b> The arguments must be consecutive integers 1..N,
     * in any order, but with no gaps; for example, makeTree(2,4,1,5)
     * would cause problems (3 was omitted).
     * 
     * @param rootIndex Which stack element (counting from 1) to use as
     * the root of the new Tree.
     * @param childIndices Which stack elements to use as the children
     * of the root.
     */    
    void makeTree(int rootIndex, int... childIndices) {
        // Get root from stack
        Tree<Token> root = getStackItem(rootIndex);
        // Get other trees from stack and add them as children of root
        for (int i = 0; i < childIndices.length; i++) {
            root.addChild(getStackItem(childIndices[i]));
        }
        // Pop root and all children from stack
        for (int i = 0; i <= childIndices.length; i++) {
            stack.pop();
        }
        // Put the root back on the stack
        stack.push(root);
    }
    
    /**
     * Returns the n-th item from the top of the global stack (counting the
     * top element as 1).
     * 
     * @param n Which stack element to return.
     * @return The n-th element in the global stack.
     */
    private Tree<Token> getStackItem(int n) {
        return stack.get(stack.size() - n);
    }

    /**
     * Utility routine to throw a <code>SyntaxException</code> with the
     * given message.
     * @param message The text to put in the <code>SyntaxException</code>.
     */
    private void error(String message) {
        throw new SyntaxException("Line " + lineNumber + ": " + message);
    }
//    public static void main(String[] args) {
//		Parser parser = new Parser("var a,b,c\n");
//		parser.isVarDeclaration();
//		parser.stack.peek().print();
//	}
}

