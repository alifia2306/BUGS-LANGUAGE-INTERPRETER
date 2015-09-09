package bugs;


import static org.junit.Assert.*;

import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;

import org.junit.Before;
import org.junit.Test;


/**
 * Test class for Bugs recognizer.
 * @author David Matuszek
 */
public class RecognizerTest {
    
    Recognizer r0, r1, r2, r3, r4, r5, r6, r7, r8;
    
    /**
     * Constructor for RecognizerTest.
     */
    public RecognizerTest() {
        r0 = new Recognizer("2 + 2");
        r1 = new Recognizer("");
    }


    @Before
    public void setUp() throws Exception {
        r0 = new Recognizer("");
        r1 = new Recognizer("250");
        r2 = new Recognizer("hello");
        r3 = new Recognizer("(xyz + 3)");
        r4 = new Recognizer("12 * 5 - 3 * 4 / 6 + 8");
        r5 = new Recognizer("12 * ((5 - 3) * 4) / 6 + (8)");
        r6 = new Recognizer("17 +");
        r7 = new Recognizer("22 *");
        r8 = new Recognizer("#");
        
    }
    

    @Test
    public void testRecognizer() {
        r0 = new Recognizer("");
        r1 = new Recognizer("2 + 2");
    }
    
    @Test
    public void testIsProgram() {
    	Recognizer r1 = new Recognizer("Bug bob {\n" +
				   						"line 5,6, 7, 8\n" + 
				   						"}\n");
    	
    	Recognizer r2 = new Recognizer("Allbugs {\n" +
    		    
    									"var a,b,c\n\n" +
	    								  
	    								"define f1 using a,b" +
							    			"{\n" +
											  "line 5,6, 7, 8\n" +
											  "color red\n" +
											  "a = 2+2\n" +
											  "exit if a>b\n" +
											  "return (2+2*8)\n" +
										"}\n"+
    								  "}\n" +
    								  "Bug sally {\n" +
    										    
    									"var a,b,c\n\n" +
    
						    			"initially {\n" +
										  "line 5,6, 7, 8\n" +
										  "color red\n" +
										  "a = 2+2\n" +
										  "exit if a>b\n" +
										  "return (2+2*8)\n" +
										  "}\n" +
										  
										  "line 5,6, 7, 8\n" +
	    								  "color red\n" +
	    								  "a = 2+2\n" +
	    								  "exit if a>b\n" +
	    								  "return (2+2*8)\n" +
	    								  
	    								  "define f1 using a,b" +
							    			"{\n" +
											  "line 5,6, 7, 8\n" +
											  "color red\n" +
											  "a = 2+2\n" +
											  "exit if a>b\n" +
											  "return (2+2*8)\n" +
											  "}\n"+
    								  "}\n"
										);
    	
    	Recognizer r3 = new Recognizer("Allbugs {\n" +
    		    
    									"var a,b,c\n\n" +
	    								  
	    								"define f1 using a,b" +
							    			"{\n" +
											  "line 5,6, 7, 8\n" +
											  "color red\n" +
											  "a = 2+2\n" +
											  "exit if a>b\n" +
											  "return (2+2*8)\n" +
										"}\n"+
    								  "}\n" +
    								  "Bug sally {\n" +
    										    
    									"var a,b,c\n\n" +
    
						    			"initially {\n" +
										  "line 5,6, 7, 8\n" +
										  "color red\n" +
										  "a = 2+2\n" +
										  "exit if a>b\n" +
										  "return (2+2*8)\n" +
										  "}\n" +
										  
										  "line 5,6, 7, 8\n" +
	    								  "color red\n" +
	    								  "a = 2+2\n" +
	    								  "exit if a>b\n" +
	    								  "return (2+2*8)\n" +
	    								  
	    								  "define f1 using a,b" +
							    			"{\n" +
											  "line 5,6, 7, 8\n" +
											  "color red\n" +
											  "a = 2+2\n" +
											  "exit if a>b\n" +
											  "return (2+2*8)\n" +
											  "}\n"+
    								  "}\n" +
									  "Bug bob {\n" +
   			 					      	"line 5,6, 7, 8\n" + 
   								      "}\n");
	   	Recognizer r4 = new Recognizer( "line 5,6, 7, 8\n");
	   	
	   	assertTrue(r1.isProgram());
	   	assertTrue(r2.isProgram());
	   	assertTrue(r3.isProgram());
	   	assertFalse(r4.isProgram());
	}
    
    @Test
    public void testIsAllbugsCode() {
    	Recognizer r1 = new Recognizer("Allbugs {\n" +
    		    
    									"var a,b,c\n\n" +
	    								  
	    								"define f1 using a,b" +
							    			"{\n" +
											  "line 5,6, 7, 8\n" +
											  "color red\n" +
											  "a = 2+2\n" +
											  "exit if a>b\n" +
											  "return (2+2*8)\n" +
										"}\n"+
    								  "}\n");
    	Recognizer r2 = new Recognizer("Allbugs {\n" + 
    								   "}\n");
    	Recognizer r3 = new Recognizer("line 5,6, 7, 8\n");
    	
    	assertTrue(r1.isAllbugsCode());
    	assertTrue(r2.isAllbugsCode());
    	assertFalse(r3.isAllbugsCode());
    	
    }
    
    @Test
    public void testIsBugDefinition() {
    	Recognizer r1 = new Recognizer("Bug sally {\n" +
    
    									"var a,b,c\n\n" +
    
						    			"initially {\n" +
										  "line 5,6, 7, 8\n" +
										  "color red\n" +
										  "a = 2+2\n" +
										  "exit if a>b\n" +
										  "return (2+2*8)\n" +
										  "}\n" +
										  
										  "line 5,6, 7, 8\n" +
	    								  "color red\n" +
	    								  "a = 2+2\n" +
	    								  "exit if a>b\n" +
	    								  "return (2+2*8)\n" +
	    								  
	    								  "define f1 using a,b" +
							    			"{\n" +
											  "line 5,6, 7, 8\n" +
											  "color red\n" +
											  "a = 2+2\n" +
											  "exit if a>b\n" +
											  "return (2+2*8)\n" +
											  "}\n"+
    								  "}\n");
    	Recognizer r2 = new Recognizer("Bug bob {\n" +
    			 					   "line 5,6, 7, 8\n" + 
    								   "}\n");
    	Recognizer r3 = new Recognizer( "line 5,6, 7, 8\n");
    	
    	assertTrue(r1.isBugDefinition());
    	assertTrue(r2.isBugDefinition());
    	assertFalse(r3.isBugDefinition());
    }
    
    @Test
    public void testIsEol() {
    	Recognizer r_a = new Recognizer("\n");
    	Recognizer r_b = new Recognizer("\n\n\n");
    	Recognizer r_c = new Recognizer("1");
    	assertTrue(r_a.isEol());
    	assertTrue(r_b.isEol());
    	assertFalse(r_c.isEol());
    }
    
    @Test
    public void testIsComparator() {
    	Recognizer r_a = new Recognizer("3 != 7");
    	Recognizer r_b = new Recognizer("6 >= 2");
    	Recognizer r_c = new Recognizer("6=6");
    	Recognizer r_d = new Recognizer("5>4");
    	Recognizer r_e = new Recognizer("4 <= 8");
    	Recognizer r_f = new Recognizer("2<1");
    	Recognizer r_g = new Recognizer("\n");
    	Recognizer r_h = new Recognizer("!a");
    	
    	assertTrue(r_a.isTerm());
    	assertTrue(r_a.isComparator()); followedBy(r_a, "7");
    	
    	assertTrue(r_b.isTerm());
    	assertTrue(r_b.isComparator()); followedBy(r_b, "2");
    	
    	assertTrue(r_c.isTerm());
    	assertTrue(r_c.isComparator()); followedBy(r_c, "6");
    	
    	assertTrue(r_d.isTerm());
    	assertTrue(r_d.isComparator()); followedBy(r_d, "4");
    	
    	assertTrue(r_e.isTerm());
    	assertTrue(r_e.isComparator()); followedBy(r_e, "8");
    	
    	assertTrue(r_f.isTerm());
    	assertTrue(r_f.isComparator()); followedBy(r_f, "1");
    	
    	assertFalse(r_g.isComparator());
    	
    	assertFalse(r_h.isComparator()); followedBy(r_h, "a");
    }
    
    @Test
    public void testIsExpression() {
    	Recognizer r_a = new Recognizer("2+2");
    	Recognizer r_b = new Recognizer("2");
    	Recognizer r_c = new Recognizer("2 > 1");
    	Recognizer r_d = new Recognizer("2+2 >= 4");
    	Recognizer r_e = new Recognizer("] 4");
    	
    	assertTrue(r_a.isExpression());
    	assertTrue(r_b.isExpression());
    	assertTrue(r_c.isExpression());
    	assertTrue(r_d.isExpression());
    	assertFalse(r_e.isExpression());
    }

    @Test
    public void testIsArithmeticExpression() {
        assertTrue(r1.isArithmeticExpression());
        assertTrue(r2.isArithmeticExpression());
        assertTrue(r3.isArithmeticExpression());
        assertTrue(r4.isArithmeticExpression());
        assertTrue(r5.isArithmeticExpression());

        assertFalse(r0.isArithmeticExpression());
        assertFalse(r8.isArithmeticExpression());

        try {
            assertFalse(r6.isArithmeticExpression());
            fail();
        }
        catch (SyntaxException e) {
        }
        try {
            assertFalse(r7.isArithmeticExpression());
            fail();
        }
        catch (SyntaxException e) {
        }
    }

    @Test
    public void testIsArithmeticExpressionWithUnaryMinus() {
        assertTrue(new Recognizer("-5").isArithmeticExpression());
        assertTrue(new Recognizer("12+(-5*10)").isArithmeticExpression());
        assertTrue(new Recognizer("+5").isArithmeticExpression());
        assertTrue(new Recognizer("12+(+5*10)").isArithmeticExpression());
    }

    @Test
    public void testIsTerm() {
        assertFalse(r0.isTerm()); // ""
        
        assertTrue(r1.isTerm()); // "250"
        
        assertTrue(r2.isTerm()); // "hello"
        
        assertTrue(r3.isTerm()); // "(xyz + 3)"
        followedBy(r3, "");
        
        assertTrue(r4.isTerm());  // "12 * 5 - 3 * 4 / 6 + 8"
        assertEquals(new Token(Token.Type.SYMBOL, "-"), r4.nextToken());
        assertTrue(r4.isTerm());
        followedBy(r4, "+ 8");

        assertTrue(r5.isTerm());  // "12 * ((5 - 3) * 4) / 6 + (8)"
        assertEquals(new Token(Token.Type.SYMBOL, "+"), r5.nextToken());
        assertTrue(r5.isTerm());
        followedBy(r5, "");
    }

    @Test
    public void testIsFactor() {
        assertTrue(r1.isFactor());
        assertTrue(r2.isFactor());
        assertTrue(r3.isFactor());
        assertTrue(r4.isFactor()); followedBy(r4, "* 5 - 3 * 4 / 6 + 8");
        assertTrue(r5.isFactor()); followedBy(r5, "* ((5");
        assertTrue(r6.isFactor()); followedBy(r6, "+");
        assertTrue(r7.isFactor()); followedBy(r7, "*");

        assertFalse(r0.isFactor());
        assertFalse(r8.isFactor()); followedBy(r8, "#");

        Recognizer r = new Recognizer("foo()");
        assertTrue(r.isFactor());
        r = new Recognizer("bar(5, abc, 2+3)+");
        assertTrue(r.isFactor()); followedBy(r, "+");

        r = new Recognizer("foo.bar$");
        assertTrue(r.isFactor()); followedBy(r, "$");
        
        r = new Recognizer("123.123");
        assertEquals(new Token(Token.Type.NUMBER, "123.123"), r.nextToken());
        
        r = new Recognizer("5");
        assertEquals(new Token(Token.Type.NUMBER, "5.0"), r.nextToken());
    }
    
    @Test
    public void testIsVarDeclaration() {
    	Recognizer r = new Recognizer("var a,b,c\n\n");
    	assertTrue(r.isVarDeclaration());
    	
    	r = new Recognizer("exit if a>b\n");
    	assertFalse(r.isVarDeclaration());
    }
    
    @Test
    public void testIsInitializationBlock() {
    	Recognizer r = new Recognizer("initially {\n" +
				  "line 5,6, 7, 8\n" +
				  "color red\n" +
				  "a = 2+2\n" +
				  "exit if a>b\n" +
				  "return (2+2*8)\n" +
				  "}\n"); 
    	assertTrue(r.isInitializationBlock());
    	
    	r = new Recognizer("exit if a>b\n");
    	assertFalse(r.isInitializationBlock());
    }
    
    @Test
    public void testIsBlock() {
    	Recognizer r = new Recognizer("{\n" +
    								  "line 5,6, 7, 8\n" +
    								  "color red\n" +
    								  "a = 2+2\n" +
    								  "exit if a>b\n" +
    								  "return (2+2*8)\n" +
    								  "}\n"); 
    	assertTrue(r.isBlock());
    	
    	r = new Recognizer("exit if a>b\n");
    	assertFalse(r.isBlock());
    }
    
    @Test
    public void testIsFunctionDefinition() {
    	Recognizer r = new Recognizer("define f1 using a,b" +
						    			"{\n" +
										  "line 5,6, 7, 8\n" +
										  "color red\n" +
										  "a = 2+2\n" +
										  "exit if a>b\n" +
										  "return (2+2*8)\n" +
										  "}\n");
    	assertTrue(r.isFunctionDefinition());
    	
    	r = new Recognizer("exit if a>b\n");
    	assertFalse(r.isFunctionDefinition());
    }
    
    @Test
    public void testIsFunctionCall() {
    	Recognizer r = new Recognizer("f1() $");
        assertTrue(r.isFunctionCall()); followedBy(r, "$");
        r = new Recognizer("f2(5) $");
        assertTrue(r.isFunctionCall()); followedBy(r, "$");
        r = new Recognizer("f3(bar, x+3) $");
        assertTrue(r.isFunctionCall()); followedBy(r, "$");
    }
    
    @Test
    public void testIsParameterList() {
        Recognizer r = new Recognizer("() $");
        assertTrue(r.isParameterList()); followedBy(r, "$");
        r = new Recognizer("(5) $");
        assertTrue(r.isParameterList()); followedBy(r, "$");
        r = new Recognizer("(bar, x+3) $");
        assertTrue(r.isParameterList()); followedBy(r, "$");
    }
    
    @Test
    public void testisColorStatement() {
    	Recognizer r_a = new Recognizer("color red\n");
    	Recognizer r_c = new Recognizer("2+2");
    	
    	assertTrue(r_a.isColorStatement());
    	assertFalse(r_c.isColorStatement());
    }
    
    @Test
    public void testIsDoStatement() {
    	Recognizer r_a = new Recognizer("do b (1,2,3)\n");
    	Recognizer r_b = new Recognizer("2+2\n");
    	
    	assertTrue(r_a.isDoStatement());
    	assertFalse(r_b.isDoStatement());
    }
    
    @Test
    public void testIsReturnStatement() {
    	Recognizer r_a = new Recognizer("return 123\n");
    	Recognizer r_b = new Recognizer("return (2+2*8)\n");
    	Recognizer r_c = new Recognizer("do p (2+2)\n");
    	
    	assertTrue(r_a.isReturnStatement());
    	assertTrue(r_b.isReturnStatement());
    	assertFalse(r_c.isReturnStatement());
    }
    
    @Test
    public void testIsSwitchStatement() {
    	Recognizer r = new Recognizer("switch {\n" +
    								  "case a\n" +
    								  " line 5,6,7,8\n" +
    								  "case b>a\n" +
    								  " color red\n" +
    								  "case 3/3\n" +
    								  " return 3*5\n" +
    								  "}\n");
    	assertTrue(r.isSwitchStatement());
    	
    	r = new Recognizer("exit if a>b\n");
    	assertFalse(r.isSwitchStatement());
    }
    
    @Test
    public void testIsExitIfStatement() {
    	Recognizer r_a = new Recognizer("exit if a>b\n");
    	Recognizer r_c = new Recognizer("do p (2+2)\n");
    	
    	assertTrue(r_a.isExitIfStatement());
    	assertFalse(r_c.isExitIfStatement());
    }
    
    @Test
    public void testIsLoopStatement() {
    	Recognizer r = new Recognizer("loop {\n" +
				  "line 5,6, 7, 8\n" +
				  "color red\n" +
				  "a = 2+2\n" +
				  "exit if a>b\n" +
				  "return (2+2*8)\n" +
				  "}\n"); 
    	assertTrue(r.isLoopStatement());
    	
    	r = new Recognizer("exit if a>b\n");
    	assertFalse(r.isLoopStatement());
    }

    @Test
    public void testIsAssignmentStatement() {
    	Recognizer r_a = new Recognizer("a = 2+2\n");
    	Recognizer r_c = new Recognizer("do p (2+2)\n");
    	
    	assertTrue(r_a.isAssignmentStatement());
    	assertFalse(r_c.isAssignmentStatement());
    }
    
    @Test
    public void testIsLineAction() {
    	Recognizer r_a = new Recognizer("line 5,6, 7, 8\n");
    	Recognizer r_b = new Recognizer("do p (2+2)\n");

    	assertTrue(r_a.isLineAction());
    	assertFalse(r_b.isLineAction());
    }
    
    @Test
    public void testIsTurnToAction() {
    	Recognizer r_a = new Recognizer("turnto 9/6\n\n");
    	Recognizer r_b = new Recognizer("do p (2+2)\n");

    	assertTrue(r_a.isTurnToAction());
    	assertFalse(r_b.isTurnToAction());
    }
    
    @Test
    public void testIsTurnAction() {
    	Recognizer r_a = new Recognizer("turn 9*6\n\n");
    	Recognizer r_b = new Recognizer("do p (2+2)\n");

    	assertTrue(r_a.isTurnAction());
    	assertFalse(r_b.isTurnAction());
    }
    
    @Test
    public void testIsMoveToAction() {
    	Recognizer r_a = new Recognizer("moveto 9+6, 9>7\n\n");
    	Recognizer r_b = new Recognizer("do p (2+2)\n");

    	assertTrue(r_a.isMoveToAction());
    	assertFalse(r_b.isMoveToAction());
    }
    
    @Test
    public void testIsMoveAction() {
    	Recognizer r_a = new Recognizer("move 9+6\n\n");
    	Recognizer r_b = new Recognizer("do p (2+2)\n");

    	assertTrue(r_a.isMoveAction());
    	assertFalse(r_b.isMoveAction());
    }
    
    @Test
    public void testIsAction() {
    	Recognizer r_a = new Recognizer("line 5,6, 7, 8\n");
    	Recognizer r_b = new Recognizer("turnto 9/6\n\n");
    	Recognizer r_c = new Recognizer("turn 9*6\n\n");
    	Recognizer r_d = new Recognizer("moveto 9+6, 9>7\n\n");
    	Recognizer r_e = new Recognizer("move 9+6\n\n");
    	Recognizer r_f = new Recognizer("do b (1,2,3)\n");
    	Recognizer r_g = new Recognizer("return (2+2*8)\n");
    	
    	assertTrue(r_a.isAction());
    	assertTrue(r_b.isAction());
    	assertTrue(r_c.isAction());
    	assertTrue(r_d.isAction());
    	assertTrue(r_e.isAction());
    	assertFalse(r_f.isAction());
    	assertFalse(r_g.isAction());
    }
    
    @Test
    public void testIsStatement(){
    	Recognizer r_a = new Recognizer("do b (1,2,3)\n");
    	Recognizer r_b = new Recognizer("return (2+2*8)\n");
    	Recognizer r_c = new Recognizer("a = 2+2\n");
    	Recognizer r_d = new Recognizer("exit if a>b\n");
    	Recognizer r_e = new Recognizer("color red\n");
    	Recognizer r_f = new Recognizer("line 5,6, 7, 8\n");
    	Recognizer r_g = new Recognizer("turnto 9/6\n\n");
    	Recognizer r_h = new Recognizer("2+2");
    	
    	assertTrue(r_a.isStatement());
    	assertTrue(r_b.isStatement());
    	assertTrue(r_c.isStatement());
    	assertTrue(r_d.isStatement());
    	assertTrue(r_e.isStatement());
    	assertFalse(r_f.isStatement());
    	assertFalse(r_g.isStatement());
    	assertFalse(r_h.isStatement());
    }
    
    @Test
    public void testIsCommand(){
    	Recognizer r_a = new Recognizer("do b (1,2,3)\n");
    	Recognizer r_b = new Recognizer("return (2+2*8)\n");
    	Recognizer r_c = new Recognizer("a = 2+2\n");
    	Recognizer r_d = new Recognizer("exit if a>b\n");
    	Recognizer r_e = new Recognizer("color red\n");
    	Recognizer r_f = new Recognizer("line 5,6, 7, 8\n");
    	Recognizer r_g = new Recognizer("turnto 9/6\n\n");
    	Recognizer r_h = new Recognizer("turn 9*6\n\n");
    	Recognizer r_i = new Recognizer("moveto 9+6, 9>7\n\n");
    	Recognizer r_j = new Recognizer("move 9+6\n\n");
    	Recognizer r_k = new Recognizer("2+2");
    	
    	assertTrue(r_a.isCommand());
    	assertTrue(r_b.isCommand());
    	assertTrue(r_c.isCommand());
    	assertTrue(r_d.isCommand());
    	assertTrue(r_e.isCommand());
    	assertTrue(r_f.isCommand());
    	assertTrue(r_g.isCommand());
    	assertTrue(r_h.isCommand());
    	assertTrue(r_i.isCommand());
    	assertTrue(r_j.isCommand());
    	assertFalse(r_k.isCommand());
    }


    @Test
    public void testIsAddOperator() {
        Recognizer r = new Recognizer("+ - $");
        assertTrue(r.isAddOperator());
        assertTrue(r.isAddOperator());
        assertFalse(r.isAddOperator());
        followedBy(r, "$");
    }

    @Test
    public void testIsMultiplyOperator() {
        Recognizer r = new Recognizer("* / $");
        assertTrue(r.isMultiplyOperator());
        assertTrue(r.isMultiplyOperator());
        assertFalse(r.isMultiplyOperator());
        followedBy(r, "$");
    }

    @Test
    public void testIsVariable() {
        Recognizer r = new Recognizer("foo 23 bar +");
        assertTrue(r.isVariable());
        
        assertFalse(r.isVariable());
        assertTrue(r.isFactor());
        
        assertTrue(r.isVariable());
        
        assertFalse(r.isVariable());
        assertTrue(r.isAddOperator());
    }

    @Test
    public void testSymbol() {
        Recognizer r = new Recognizer("++");
        assertEquals(new Token(Token.Type.SYMBOL, "+"), r.nextToken());
    }

    @Test
    public void testNextTokenMatchesType() {
        Recognizer r = new Recognizer("++abc");
        assertTrue(r.nextTokenMatches(Token.Type.SYMBOL));
        assertFalse(r.nextTokenMatches(Token.Type.NAME));
        assertTrue(r.nextTokenMatches(Token.Type.SYMBOL));
        assertTrue(r.nextTokenMatches(Token.Type.NAME));
    }

    @Test
    public void testNextTokenMatchesTypeString() {
        Recognizer r = new Recognizer("+abc+");
        assertTrue(r.nextTokenMatches(Token.Type.SYMBOL, "+"));
        assertTrue(r.nextTokenMatches(Token.Type.NAME, "abc"));
        assertFalse(r.nextTokenMatches(Token.Type.SYMBOL, "*"));
        assertTrue(r.nextTokenMatches(Token.Type.SYMBOL, "+"));
    }

    @Test
    public void testNextToken() {
        // NAME, KEYWORD, NUMBER, SYMBOL, EOL, EOF };
        Recognizer r = new Recognizer("abc move 25 *\n");
        assertEquals(new Token(Token.Type.NAME, "abc"), r.nextToken());
        assertEquals(new Token(Token.Type.KEYWORD, "move"), r.nextToken());
        assertEquals(new Token(Token.Type.NUMBER, "25.0"), r.nextToken());
        assertEquals(new Token(Token.Type.SYMBOL, "*"), r.nextToken());
        assertEquals(new Token(Token.Type.EOL, "\n"), r.nextToken());
        assertEquals(new Token(Token.Type.EOF, "EOF"), r.nextToken());
        
        r = new Recognizer("foo.bar 123.456");
        assertEquals(new Token(Token.Type.NAME, "foo"), r.nextToken());
        assertEquals(new Token(Token.Type.SYMBOL, "."), r.nextToken());
        assertEquals(new Token(Token.Type.NAME, "bar"), r.nextToken());
        assertEquals(new Token(Token.Type.NUMBER, "123.456"), r.nextToken());
    }

    @Test
    public void testPushBack() {
        Recognizer r = new Recognizer("abc 25");
        assertEquals(new Token(Token.Type.NAME, "abc"), r.nextToken());
        r.pushBack();
        assertEquals(new Token(Token.Type.NAME, "abc"), r.nextToken());
        assertEquals(new Token(Token.Type.NUMBER, "25.0"), r.nextToken());
    }
    
//  ----- "Helper" methods

    /**
     * This method is given a String containing some or all of the
     * tokens that should yet be returned by the Tokenizer, and tests
     * whether the Tokenizer in fact has those Tokens. To succeed,
     * everything in the given String must still be in the Tokenizer,
     * but there may be additional (untested) Tokens to be returned.
     * This method is primarily to test whether rejected Tokens are
     * pushed back appropriately.
     * 
     * @param recognizer The Recognizer whose Tokenizer is to be tested.
     * @param expectedTokens The Tokens we expect to get from the Tokenizer.
     */
    private void followedBy(Recognizer recognizer, String expectedTokens) {
        int expectedType;
        int actualType;
        StreamTokenizer actual = recognizer.tokenizer;

        Reader reader = new StringReader(expectedTokens);
        StreamTokenizer expected = new StreamTokenizer(reader);
        expected.ordinaryChar('-');
        expected.ordinaryChar('/');

        try {
            while (true) {
                expectedType = expected.nextToken();
                if (expectedType == StreamTokenizer.TT_EOF) break;
                actualType = actual.nextToken();
                assertEquals(expectedType, actualType);
                if (actualType == StreamTokenizer.TT_WORD) {
                    assertEquals(expected.sval, actual.sval);
                }
                else if (actualType == StreamTokenizer.TT_NUMBER) {
                    assertEquals(expected.nval, actual.nval, 0.001);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
