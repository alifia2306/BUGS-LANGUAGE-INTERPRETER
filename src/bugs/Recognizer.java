package bugs;


import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;

/**
 * This class consists of a number of methods that "recognize" strings
 * composed of Tokens that follow the indicated grammar rules for each
 * method.
 * <p>Each method may have one of three outcomes:
 * <ul>
 *   <li>The method may succeed, returning <code>true</code> and
 *      consuming the tokens that make up that particular nonterminal.</li>
 *   <li>The method may fail, returning <code>false</code> and not
 *       consuming any tokens.</li>
 *   <li>(Some methods only) The method may determine that an
 *       unrecoverable error has occurred and throw a
 *       <code>SyntaxException</code></li>.
 * </ul>
 * @author David Matuszek
 * @version February 2015
 */
public class Recognizer {
    /** The tokenizer used by this Parser. */
    StreamTokenizer tokenizer = null;
    /** The number of the line of source code currently being processed. */
    int lineNumber;
    
    /**
     * Constructs a Recognizer for the given string.
     * @param text The string to be recognized.
     */
    public Recognizer(String text) {
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
     * Tries to recognize a &lt;program&gt;
     * <pre>&lt;program&gt; ::= [&lt;allbugs code&gt;]
     *                          &lt;bug definition&gt;
     *                          {&lt;bug definition&gt;}
     * </pre>
     * @return <code>true</code> if a program is recognized.
     */ 
    public boolean isProgram() {
    	if (isAllbugsCode());
    	if (!isBugDefinition()) return false;
    	while(isBugDefinition());
    	if (!nextTokenMatches(Token.Type.EOF)) error("No EOF at the end of program code");
    	return true;
    }
    
    /**
     * Tries to recognize &lt;allbugs code&gt;
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
    	if (!keyword("Allbugs")) return false;
    	if (!symbol("{")) error("No starting brace in allbugs code");
    	if (!isEol()) error("No EOL after starting brace in allbugs code");
    	while(isVarDeclaration());
    	while(isFunctionDefinition());
    	if (!symbol("}")) error("No ending brace in allbugs code");
    	if (!isEol()) error("No EOL at the end of allbugs code");

    	return true;
    }
    
    /**
     * Tries to recognize a &lt;bug definition&gt;
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
    	if (!isEol()) error("No EOL after starting brace in bug definition");
    	while(isVarDeclaration());
    	if(isInitializationBlock());
    	if(!isCommand()) error("No command in bug definition");
    	while(isCommand());
    	while(isFunctionDefinition());
    	if (!symbol("}")) error("No ending brace in bug definition");
    	if (!isEol()) error("No EOL at the end of bug definition");
    	
    	return true;
    }
    
    /**
     * Tries to recognize a &lt;var declaration&gt;
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
    	while(symbol(","))
    		if (!name()) error("No name after , in declaration");
    	if (!isEol()) error("No EOL in the end of var declaration");
    	return true;
    }
 
    /**
     * Tries to recognize an &lt;initialization block&gt;
     * <pre>&lt;initialization block&gt; ::= "initially" &lt;block&gt;</pre>
     * A <code>SyntaxException</code> will be thrown if <ul>
     * <li> the "initially" keyword is present but not followed by a &lt;block&gt;</li>
     * @return <code>true</code> if a initialization block is recognized.
     */ 
    public boolean isInitializationBlock() {
    	if (!keyword("initially")) return false;
    	if (!isBlock()) error("No block after initially keyword");
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
    	return isAction() || isStatement();
    }
 
    /**
     * Tries to recognize a &lt;statement&gt;.
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
     * Tries to recognize an &lt;action&gt;.
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
     * Tries to recognize a &lt;move action&gt;
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
    	return true;
    }
    
    /**
     * Tries to recognize an &lt;moveto action&gt;
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
    	if (!isExpression()) error("No expression after , in moveto action");
    	if (!isEol()) error("No eol at the end of moveto command");
    	return true;
    }
    
    /**
     * Tries to recognize a &lt;turn action&gt;
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
    	return true;
    }

    /**
     * Tries to recognize a &lt;turnto action&gt;
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
    	return true;
    }
    
    /**
     * Tries to recognize an &lt;line action&gt;
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
    		if (!isExpression()) error("Missing expression at " + (i+1) + " pos in line action");
    	}
    	if (!isEol()) error("Missing EOL at the end of line action");
    	return true;
    }
 
    /**
     * Tries to recognize an &lt;assignment statement&gt;
     * <pre>&lt;assignment statement&gt; ::= &lt;variable&gt; "=" &lt;expression&gt; &lt;eol&gt;</pre>
     * A <code>SyntaxException</code> will be thrown if <ul>
     * <li> A &lt;variable&gt; and "=" symbol is present but not followed by an &lt;expression&gt;</li>
     * <li> no &lt;eol&gt; is present in the end of the statement</li>
     * @return <code>true</code> if a assignment statement is recognized.
     */ 
    public boolean isAssignmentStatement() {
    	if (!isVariable()) return false;
    	if (!symbol("=")) error("No '=' after variable");
    	if (!isExpression()) error("No expression after assignment operator");
    	if (!isEol()) error("No EOL at the end of assignment statement");
    	return true;
    }
    
    /**
     * Tries to recognize a &lt;loop statement&gt;
     * <pre>&lt;loop statement&gt; ::= "loop" &lt;block&gt;</pre>
     * A <code>SyntaxException</code> will be thrown if <ul>
     * <li> the "loop" keyword is present but not followed by an &lt;block&gt;</li>
     * @return <code>true</code> if a loop statement is recognized.
     */ 
    public boolean isLoopStatement() {
    	if (!keyword("loop")) return false;
    	if (!isBlock()) error("No block in loop");
    	return true;
    }
    
    /**
     * Tries to recognize a &lt;exit if statement&gt;
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
    	if (!isExpression()) error("No expression after exit if");
    	if (!isEol()) error("No EOL at the end of exit if statement");
    	return true;
    }
    
    /**
     * Tries to recognize a &lt;switch statement&gt;
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
    	if (!isEol()) error("No EOL after switch {");
    	while (keyword("case")) {
    		if (!isExpression()) error("No expression after case");
    		if (!isEol()) error("No EOL after case expression");
    		while (isCommand());
    	}
    	if (!symbol("}")) error("No closing brace in switch statement");
    	if (!isEol()) error("No EOL at the end of switch");
    	return true;
    }

    /**
     * Tries to recognize a &lt;return statement&gt;
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
    	return true;
    }
    
    /**
     * Tries to recognize a &lt;do statement&gt;
     * <pre>&lt;do statement&gt; ::= "do" &lt;variable&gt; [&lt;parameter list&gt;] &lt;eol&gt;</pre>
     * A <code>SyntaxException</code> will be thrown if <ul>
     * <li> the "do" keyword is present but not followed by a &lt;variable&gt;</li>
     * <li> no &lt;eol&gt; is present in the end of the statement</li>
     * @return <code>true</code> if a do statement is recognized.
     */    
    public boolean isDoStatement() {
    	if (!keyword("do")) return false;
    	if (!isVariable()) error("No variable after do");
    	if (isParameterList());
    	if (!isEol()) error("No EOL at end of do statement");
    	return true;
    }

    /**
     * Tries to recognize a &lt;color statement&gt;
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
    	return true;
    }
    
    /**
     * Tries to recognize a &lt;block&gt;
     * <pre>&lt;block&gt; ::= "{" &lt;eol&gt; { &lt;command&gt;} "}"</pre>
     * A <code>SyntaxException</code> will be thrown if <ul>
     * <li> the "{" symbol is present but not followed by a &lt;eol&gt;</li>
     * <li> the "{" symbol is present but not followed by a "}"</li>
     * <li> no &lt;eol&gt; is present in the end of block</li>
     * @return <code>true</code> if a block is recognized.
     */
    public boolean isBlock() {
    	if (!symbol("{")) return false;
    	if (!isEol()) error("No EOL after starting brace in block");
    	while(isCommand());
    	if (!symbol("}")) error("No ending brace in block");
    	if (!isEol()) error("No EOL after ending brace in block");
    	return true;
    }
    
    /**
     * Tries to recognize an &lt;expression&gt;
     * <pre>&lt;expression&gt; ::= &lt;arithmetic expression&gt; { &lt;comparator&gt;  &lt;arithmetic expression&gt;}</pre>
     * A <code>SyntaxException</code> will be thrown if there a &lt;comparator&gt; is found but no &lt;arithmetic expression&gt;
     * follows it
     * @return <code>true</code> if an expression is recognized 
     */
    public boolean isExpression() {
    	if (!isArithmeticExpression()) return false;
        while (isComparator())
        	if(!isArithmeticExpression()) error("Error in expression after comparator");
        return true;
    }
    
    /**
     * Tries to recognize a &lt;comparator&gt;
     * <pre>&lt;comparator&gt; ::= "<" | "<=" | "=" | "!=" | ">=" | ">"</pre>
     * @return <code>true</code> if a comparator is recognized
     */
    public boolean isComparator() {
    	if(symbol("<") || symbol(">")) {
    		symbol("=");
    		return true;
    	}
    	
    	if (symbol("!")) {
    		if(symbol("=")) return true;
    		pushBack();
    		return false;
    	}
    	
    	if(symbol("=")) return true;
    	
    	return false;
    }
    
    /**
     * Tries to recognize a &lt;function definition&gt;
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
    	if (!name()) error("No function name in definition");
    	if (keyword("using")) {
    		if (!isVariable()) error("No variable defined after keyword using");
    		while (symbol(","))
    			if (!isVariable()) 
    				error("No variable defined after ,");
    	}
    	if (!isBlock()) error("No block in function definition");
    	return true;
    }
    
    /**
     * Tries to recognize a &lt;function call&gt;.
     * <pre>&lt;function call&gt; ::= &lt;NAME&gt; &lt;parameter list&gt;</pre>
     * @return <code>true</code> if a function call is recognized.
     */
    public boolean isFunctionCall() {
    	if (!name()) return false;
    	if (!isParameterList()) return false;
    	return true;
    }

    /**
     * Tries to recognize a &lt;parameter list&gt;.
     * <pre>&lt;parameter list&gt; ::= "(" [ &lt;expression&gt; { "," &lt;expression&gt; } ] ")"</pre>
     * A <code>SyntaxException</code> will be thrown if <ul>
     * <li>A "," symbol is present but not followed by an &lt;expression&gt; </li>
     * <li>A starting "(" is present but not followed by an ending ")"</li>
     * @return <code>true</code> if a parameter list is recognized.
     */
    public boolean isParameterList() {
        if (!symbol("(")) return false;
        if (isExpression()) {
            while (symbol(",")) {
                if (!isExpression()) error("No expression after ','");
            }
        }
        if (!symbol(")")) error("Parameter list doesn't end with ')'");
        return true;
    }
   
    /**
     * Tries to recognize a &lt;eol&gt;.
     * <pre>&lt;eol&gt; ::= &lt;EOL&gt; { &lt;EOL&gt; }</pre>
     * @return <code>true</code> if an eol is recognized.
     */
    public boolean isEol() {
    	if (!eol()) return false;
    	while(eol());
    	return true;
    }

    /**
     * Tries to recognize an &lt;arithmetic expression&gt;.
     * <pre>&lt;arithmetic expression&gt; ::= [ &lt;add_operator&gt; ] &lt;term&gt; { &lt;add_operator&gt; &lt;term&gt; }</pre>
     * A <code>SyntaxException</code> will be thrown if the add_operator
     * is present but not followed by a valid &lt;expression&gt;.
     * @return <code>true</code> if an expression is recognized.
     */
    public boolean isArithmeticExpression() {
        if (!isTerm())
            return false;
        while (isAddOperator()) {
            if (!isTerm()) error("Error in expression after '+' or '-'");
        }
        return true;
    }

    /**
     * Tries to recognize a &lt;term&gt;.
     * <pre>&lt;term&gt; ::= &lt;factor&gt; { &lt;multiply_operator&gt; &lt;term&gt;}</pre>
     * A <code>SyntaxException</code> will be thrown if the multiply_operator
     * is present but not followed by a valid &lt;term&gt;.
     * @return <code>true</code> if a term is recognized.
     */
    public boolean isTerm() {
        if (!isFactor()) return false;
        while (isMultiplyOperator()) {
            if (!isFactor()) error("No term after '*' or '/'");
        }
        return true;
    }

    /**
     * Tries to recognize a &lt;factor&gt;.
     * <pre>&lt;factor&gt; ::= [ &lt;add operator&gt; ] &lt;unsigned factor&gt;</pre>
     * @return <code>true</code> if a factor is parsed.
     */
    public boolean isFactor() {
        if(symbol("+") || symbol("-")) {
            if (isUnsignedFactor()) {
                return true;
            }
            error("No factor following unary plus or minus");
            return false; // Can't ever get here
        }
        return isUnsignedFactor();
    }

    /**
     * Tries to recognize an &lt;unsigned factor&gt;.
     * <pre>&lt;factor&gt; ::= &lt;name&gt; "." &lt;name&gt;
     *           | &lt;name&gt; "(" &lt;parameter list&gt; ")"
     *           | &lt;name&gt;
     *           | &lt;number&gt;
     *           | "(" &lt;expression&gt; ")"</pre>
     * A <code>SyntaxException</code> will be thrown if the opening
     * parenthesis is present but not followed by a valid
     * &lt;expression&gt; and a closing parenthesis.
     * @return <code>true</code> if a factor is recognized.
     */
    public boolean isUnsignedFactor() {
        if (isVariable()) {
            if (symbol(".")) {              // reference to another Bug
                if (name()) return true;
                error("Incorrect use of dot notation");
            }
            else if (isParameterList()) return true; // function call
            else return true;                        // just a variable
        }
        if (number()) return true;
        if (symbol("(")) {
            if (!isExpression()) error("Error in parenthesized expression");
            if (!symbol(")")) error("Unclosed parenthetical expression");
            return true;
       }
       return false;
    }

    /**
     * Tries to recognize an &lt;add_operator&gt;.
     * <pre>&lt;add_operator&gt; ::= "+" | "-"</pre>
     * @return <code>true</code> if an addop is recognized.
     */
    public boolean isAddOperator() {
        return symbol("+") || symbol("-");
    }

    /**
     * Tries to recognize a &lt;multiply_operator&gt;.
     * <pre>&lt;multiply_operator&gt; ::= "*" | "/"</pre>
     * @return <code>true</code> if a multiply_operator is recognized.
     */
    public boolean isMultiplyOperator() {
        return symbol("*") || symbol("/");
    }

    /**
     * Tries to recognize a &lt;variable&gt;.
     * <pre>&lt;variable&gt; ::= &lt;NAME&gt;</pre>
     * @return <code>true</code> if a variable is recognized.
     */
    public boolean isVariable() {
        return name();
    }

//----- Private "helper" methods

    /**
     * Tests whether the next token is a number. If it is, the token
     * is consumed, otherwise it is not.
     *
     * @return <code>true</code> if the next token is a number.
     */
    private boolean number() {
        return nextTokenMatches(Token.Type.NUMBER);
    }

    /**
     * Tests whether the next token is a name. If it is, the token
     * is consumed, otherwise it is not.
     *
     * @return <code>true</code> if the next token is a name.
     */
    private boolean name() {
        return nextTokenMatches(Token.Type.NAME);
    }

    /**
     * Tests whether the next token is the expected name. If it is, the token
     * is consumed, otherwise it is not.
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
     * the token is consumed, otherwise it is not.
     *
     * @param expectedSymbol The String value of the token we expect
     *    to encounter next.
     * @return <code>true</code> if the next token is the expected symbol.
     */
    boolean symbol(String expectedSymbol) {
        return nextTokenMatches(Token.Type.SYMBOL, expectedSymbol);
    }

    /**
     * Tests whether the next token has the expected type. If it does,
     * the token is consumed, otherwise it is not. This method would
     * normally be used only when the token's value is not relevant.
     *
     * @param type The expected type of the next token.
     * @return <code>true</code> if the next token has the expected type.
     */
    boolean nextTokenMatches(Token.Type type) {
        Token t = nextToken();
        if (t.type == type) return true;
        pushBack();
        return false;
    }

    /**
     * Tests whether the next token has the expected type and value.
     * If it does, the token is consumed, otherwise it is not. This
     * method would normally be used when the token's value is
     * important.
     *
     * @param type The expected type of the next token.
     * @param value The expected value of the next token; must
     *              not be <code>null</code>.
     * @return <code>true</code> if the next token has the expected type.
     */
    boolean nextTokenMatches(Token.Type type, String value) {
        Token t = nextToken();
        if (type == t.type && value.equals(t.value)) return true;
        pushBack();
        return false;
    }

    /**
     * Returns the next Token.
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
     * Returns the most recent Token to the tokenizer.
     */
    void pushBack() {
        tokenizer.pushBack();  
        if (tokenizer.ttype == tokenizer.TT_EOL) lineNumber--;
    }

    /**
     * Utility routine to throw a <code>SyntaxException</code> with the
     * given message.
     * @param message The text to put in the <code>SyntaxException</code>.
     */
    private void error(String message) {
        throw new SyntaxException("Line " + lineNumber + ": " + message);
    }
}
