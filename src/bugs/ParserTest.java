package bugs;

import static org.junit.Assert.*;

import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;

import org.junit.Before;
import org.junit.Test;

import tree.Tree;


public class ParserTest {
    Parser parser;

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testParser() {
        parser = new Parser("");
        parser = new Parser("2 + 2");
    }

    @Test
    public void testIsExpression() {
        Tree<Token> expected;
        
        use("250");
        assertTrue(parser.isExpression());
        assertStackTopEquals(createNode("250.0"));
        
        use("hello");
        assertTrue(parser.isExpression());
        assertStackTopEquals(createNode("hello"));

        use("(xyz + 3)");
        assertTrue(parser.isExpression());
        assertStackTopEquals(tree("+", "xyz", "3.0"));

        use("a + b + c");
        assertTrue(parser.isExpression());
        assertStackTopEquals(tree("+", tree("+", "a", "b"), "c"));

        use("a * b * c");
        assertTrue(parser.isExpression());
        assertStackTopEquals(tree("*", tree("*", "a", "b"), "c"));

        use("3 * 12.5 - 7");
        assertTrue(parser.isExpression());
        assertStackTopEquals(tree("-", tree("*", "3.0", "12.5"), createNode("7.0")));

        use("12 * 5 - 3 * 4 / 6 + 8");
        assertTrue(parser.isExpression());
        expected = tree("+",
                      tree("-",
                         tree("*", "12.0", "5.0"),
                         tree("/",
                            tree("*", "3.0", "4.0"),
                            "6.0"
                           )
                        ),
                      "8.0"
                     );
        assertStackTopEquals(expected);
                     
        use("12 * ((5 - 3) * 4) / 6 + (8)");
        assertTrue(parser.isExpression());
        expected = tree("+",
                      tree("/",
                         tree("*",
                            "12.0",
                            tree("*",
                               tree("-","5.0","3.0"),
                               "4.0")),
                         "6.0"),
                      "8.0");
        assertStackTopEquals(expected);
        
        use("");
        assertFalse(parser.isExpression());
        
        use("#");
        assertFalse(parser.isExpression());

        try {
            use("17 +");
            assertFalse(parser.isExpression());
            fail();
        }
        catch (SyntaxException e) {
        }
        try {
            use("22 *");
            assertFalse(parser.isExpression());
            fail();
        }
        catch (SyntaxException e) {
        }
    }

    @Test
    public void testUnaryOperator() {       
        use("-250");
        assertTrue(parser.isExpression());
        assertStackTopEquals(tree("-", "250.0"));
        
        use("+250");
        assertTrue(parser.isExpression());
        assertStackTopEquals(tree("+", "250.0"));
        
        use("- hello");
        assertTrue(parser.isExpression());
        assertStackTopEquals(tree("-", "hello"));

        use("-(xyz + 3)");
        assertTrue(parser.isExpression());
        assertStackTopEquals(tree("-", tree("+", "xyz", "3.0")));

        use("(-xyz + 3)");
        assertTrue(parser.isExpression());
        assertStackTopEquals(tree("+", tree("-", "xyz"), "3.0"));

        use("+(-xyz + 3)");
        assertTrue(parser.isExpression());
        assertStackTopEquals(tree("+",
                                        tree("+",
                                                   tree("-", "xyz"), "3.0")));
    }

    @Test
    public void testIsTerm() {        
        use("12");
        assertTrue(parser.isTerm());
        assertStackTopEquals(createNode("12.0"));
        
        use("12.5");
        assertTrue(parser.isTerm());
        assertStackTopEquals(createNode("12.5"));

        use("3*12");
        assertTrue(parser.isTerm());
        assertStackTopEquals(tree("*", "3.0", "12.0"));

        use("x * y * z");
        assertTrue(parser.isTerm());
        assertStackTopEquals(tree("*", tree("*", "x", "y"), "z"));
        
        use("20 * 3 / 4");
        assertTrue(parser.isTerm());
        assertEquals(tree("/", tree("*", "20.0", "3.0"), createNode("4.0")),
                     stackTop());

        use("20 * 3 / 4 + 5");
        assertTrue(parser.isTerm());
        assertEquals(tree("/", tree("*", "20.0", "3.0"), "4.0"),
                     stackTop());
        followedBy(parser, "+ 5");
        
        use("");
        assertFalse(parser.isTerm());
        followedBy(parser, "");
        
        use("#");
        assertFalse(parser.isTerm());followedBy(parser, "#");

    }

    @Test
    public void testIsFactor() {
        use("12");
        assertTrue(parser.isFactor());
        assertStackTopEquals(createNode("12.0"));

        use("hello");
        assertTrue(parser.isFactor());
        assertStackTopEquals(createNode("hello"));
        
        use("(xyz + 3)");
        assertTrue(parser.isFactor());
        assertStackTopEquals(tree("+", "xyz", "3.0"));
        
        use("12 * 5");
        assertTrue(parser.isFactor());
        assertStackTopEquals(createNode("12.0"));
        followedBy(parser, "* 5.0");
        
        use("17 +");
        assertTrue(parser.isFactor());
        assertStackTopEquals(createNode("17.0"));
        followedBy(parser, "+");

        use("");
        assertFalse(parser.isFactor());
        followedBy(parser, "");
        
        use("#");
        assertFalse(parser.isFactor());
        followedBy(parser, "#");
    }

    @Test
    public void testIsFactor2() {
        use("hello.world");
        assertTrue(parser.isFactor());
        assertStackTopEquals(tree(".", "hello", "world"));
        
        use("foo(bar)");
        assertTrue(parser.isFactor());
        assertStackTopEquals(tree("call", "foo",
                                        tree("var", "bar")));
        
        use("foo(bar, baz)");
        assertTrue(parser.isFactor());
        assertStackTopEquals(tree("call", "foo",
                                        tree("var", "bar", "baz")));
        
        use("foo(2*(3+4))");
        assertTrue(parser.isFactor());
        assertStackTopEquals(tree("call", "foo",
                                 tree("var",
                                     tree("*", "2.0",
                                         tree("+", "3.0", "4.0")))));
    }
    
    @Test
	public void testIsEol() {
		use("\n a \n");
		assertTrue(parser.isEol());
		assertFalse(parser.isEol());
		assertTrue(parser.isFactor());
	}
    
    @Test
    public void testIsComparator(){
    	use("!= >= abc");
		assertTrue(parser.isComparator());
		assertTrue(parser.isComparator());
		assertFalse(parser.isComparator());
		
		try{
			use("!*");
			assertFalse(parser.isComparator());
			fail();
		}
		catch(Exception e){}
		
    }
    
    @Test
	public void testIsCommand() {
		
		use("move 3 + 4/5 \n");
		assertTrue(parser.isCommand());
		assertStackTopEquals(tree("move", tree("+", "3.0", tree("/", "4.0", "5.0"))));
	}

	@Test
	public void testIsStatement() {
		
		use("return 3 + 4/5 \n");
		assertTrue(parser.isStatement());
		assertStackTopEquals(tree("return", tree("+", "3.0", tree("/", "4.0", "5.0"))));
		
	}

	@Test
	public void testIsAction() {
		use("move 3 + 4/5 \n");
		assertTrue(parser.isAction());
		assertStackTopEquals(tree("move", tree("+", "3.0", tree("/", "4.0", "5.0"))));
		
		
		use("turn  3 + 4/5 \n");
		assertTrue(parser.isAction());
		assertStackTopEquals(tree("turn", tree("+", "3.0", tree("/", "4.0", "5.0"))));
		
		use("turnto  3 + 4/5 \n");
		assertTrue(parser.isAction());
		assertStackTopEquals(tree("turnto", tree("+", "3.0", tree("/", "4.0", "5.0"))));
	}

    @Test
    public void testMoveAction(){
    	use("13");
		assertFalse(parser.isMoveAction());
		
    	try{
    		use("move");
    		assertFalse(parser.isMoveAction());
    		fail();
    	}
    	catch (SyntaxException e) {
        }
    	use("move 45\n");
        assertTrue(parser.isMoveAction());
        assertStackTopEquals(tree("move", "45.0"));
        
    	use("move 12 + 3\n");
    	assertTrue(parser.isMoveAction());
    	assertStackTopEquals(tree("move", tree("+", "12.0", "3.0")));
    }
    
    @Test
    public void testTurnAction(){
    	use("13");
		assertFalse(parser.isTurnAction());
		
    	try{
    		use("turn");
    		assertFalse(parser.isTurnAction());
    		fail();
    	}
    	catch (SyntaxException e) {
        }
    	use("turn 45\n");
        assertTrue(parser.isTurnAction());
        assertStackTopEquals(tree("turn", "45.0"));
        
    	use("turn 12 + 3\n");
    	assertTrue(parser.isTurnAction());
    	assertStackTopEquals(tree("turn", tree("+", "12.0", "3.0")));
    }
    
    @Test
    public void testTurnToAction(){
    	use("13");
		assertFalse(parser.isTurnToAction());
		
    	try{
    		use("turnto");
    		assertFalse(parser.isTurnToAction());
    		fail();
    	}
    	catch (SyntaxException e) {
        }
    	use("turnto 45\n");
        assertTrue(parser.isTurnToAction());
        assertStackTopEquals(tree("turnto", "45.0"));
        
    	use("turnto 12 + 3\n");
    	assertTrue(parser.isTurnToAction());
    	assertStackTopEquals(tree("turnto", tree("+", "12.0", "3.0")));
    }
    
    @Test
    public void testReturnStatement(){
    	use("13");
		assertFalse(parser.isReturnStatement());
		
    	try{
    		use("return");
    		assertFalse(parser.isReturnStatement());
    		fail();
    	}
    	catch (SyntaxException e) {
        }
    	use("return 45\n");
        assertTrue(parser.isReturnStatement());
        assertStackTopEquals(tree("return", "45.0"));
        
    	use("return 12 + 3\n");
    	assertTrue(parser.isReturnStatement());
    	assertStackTopEquals(tree("return", tree("+", "12.0", "3.0")));
    }
    
    @Test
    public void testInitializationBlock(){
    	use("13");
		assertFalse(parser.isInitializationBlock());
		
    	try{
    		use("initially");
    		assertFalse(parser.isInitializationBlock());
    		fail();
    	}
    	catch (SyntaxException e) {
        }
    	use("initially {\n" + "move 45\n" + "}\n");
        assertTrue(parser.isInitializationBlock());
        assertStackTopEquals(tree("initially", tree("block",tree("move", "45.0"))));
        
        use("initially {\n" + "turn 15\n" + "}\n");
        assertTrue(parser.isInitializationBlock());
        assertStackTopEquals(tree("initially", tree("block",tree("turn", "15.0"))));
    }
    
    @Test
    public void testBlock(){
    	use("13");
		assertFalse(parser.isBlock());
    	use("{\n" +
    			"move 45\n" +
    			"}\n");
    	assertTrue(parser.isBlock());
    	assertStackTopEquals(tree("block",tree("move", "45.0")));
    	
    	use("{\n" +
    			
    			"}\n");
    	assertTrue(parser.isBlock());
    	assertStackTopEquals(tree("block"));
    }

    @Test
    public void testLoop(){
    	use("13");
		assertFalse(parser.isLoopStatement());
    	use("loop {\n" +
    			"move 45\n" +
    			"}\n");
    	assertTrue(parser.isLoopStatement());
    	assertStackTopEquals(tree("loop",tree("block",tree("move", "45.0"))));
    	
    	use("a = xyz + 3\n");
    	assertFalse(parser.isLoopStatement());
    }
    
    @Test
    public void testIsExitIfStatement(){
    	Tree<Token> expected;
    	use("exit if -(xyz + 3) \n");
        assertTrue(parser.isExitIfStatement());
        assertStackTopEquals(tree("exit", tree("-", tree("+", "xyz", "3.0"))));
        
        use("exit if 3 * 12.5 - 7 \n");
        assertTrue(parser.isExitIfStatement());
        assertStackTopEquals(tree("exit",tree("-", tree("*", "3.0", "12.5"), createNode("7.0"))));

        use("exit if 12 * 5 - 3 * 4 / 6 + 8 \n");
        assertTrue(parser.isExitIfStatement());
        expected = tree("exit",
		        		tree("+",
		                      tree("-",
		                         tree("*", "12.0", "5.0"),
		                         tree("/",
		                            tree("*", "3.0", "4.0"),
		                            "6.0"
		                           )
		                        ),
		                      "8.0"
		                     )
		          	);
        assertStackTopEquals(expected);
                     
        use("exit if 12 * ((5 - 3) * 4) / 6 + (8) \n");
        assertTrue(parser.isExitIfStatement());
        expected = tree("exit",
		        		tree("+",
		                      tree("/",
		                         tree("*",
		                            "12.0",
		                            tree("*",
		                               tree("-","5.0","3.0"),
		                               "4.0")),
		                         "6.0"),
		                      "8.0")
                  	);
        assertStackTopEquals(expected);
        
        use("");
        assertFalse(parser.isExitIfStatement());
    }
    
    @Test
    public void testColorStatement(){
    	use("13");
		assertFalse(parser.isColorStatement());
		
    	use("color red\n");
    	assertTrue(parser.isColorStatement());
    	assertStackTopEquals(tree("color","red"));
    	
    	use("color blue\n");
    	assertTrue(parser.isColorStatement());
    	assertStackTopEquals(tree("color","blue"));
    	
    	use("color green");
    	
    	try{
    		assertFalse(parser.isColorStatement());
    		fail();
    	}
    	catch (SyntaxException e) {
        }
    }
    
    @Test
    public void testAssignmentStatement(){
    	Tree<Token> expected;
    	use("13");
		assertFalse(parser.isAssignmentStatement());
		
    	use("a = xyz + 3\n");
    	assertTrue(parser.isAssignmentStatement());
    	assertStackTopEquals(tree("assign","a",tree("+","xyz","3.0")));
    	
    	 use("b = 12 * 5 - 3 * 4 / 6 + 8 \n");
         assertTrue(parser.isAssignmentStatement());
         expected = tree("assign","b",  
        		 		tree("+",
	                       tree("-",
	                          tree("*", "12.0", "5.0"),
	                          tree("/",
	                             tree("*", "3.0", "4.0"),
	                             "6.0"
	                            )
	                         ),
	                       "8.0"
	                      )
	             	);
         assertStackTopEquals(expected);
    }
    
    @Test
    public void testIsLineAction() {
    	use("13");
    	assertFalse(parser.isLineAction());
    	
    	use("line 5, 6, 7, 8\n");
    	assertTrue(parser.isLineAction());
    	assertStackTopEquals(tree("line", "5.0","6.0","7.0","8.0"));
    	
    	use("line hello, (xyz + 3), a * b * c, 8\n");
    	assertTrue(parser.isLineAction());
    	assertStackTopEquals(tree("line", "hello",
    							tree("+", "xyz", "3.0"),
    								tree("*",
    										tree("*", "a", "b"), 
    										"c"),
    						"8.0"));
    	
    	try {
    		use("line 90, 6, 76, 8");
        	assertFalse(parser.isLineAction());
            fail();
        }
        catch (SyntaxException e) {}
    }
    
    @Test
    public void testIsMoveToAction() {
    	use("13");
    	assertFalse(parser.isMoveToAction());
    	
    	use("moveto 5, 6\n");
    	assertTrue(parser.isMoveToAction());
    	assertStackTopEquals(tree("moveto", "5.0","6.0"));
    	
    	use("moveto (xyz + 3), a * b * c\n");
    	assertTrue(parser.isMoveToAction());
    	assertStackTopEquals(tree("moveto",
    							tree("+", "xyz", "3.0"),
    								tree("*",
    										tree("*", "a", "b"), 
    										"c")
    						));
    	
    	try {
    		use("moveto 90, 6, 76, 8");
        	assertFalse(parser.isMoveToAction());
            fail();
        }
        catch (SyntaxException e) {}
    }
    
    @Test
    public void testIsVarDeclaration() {
    	use("13");
    	assertFalse(parser.isVarDeclaration());
    	
    	use("var a,b,c\n");
    	assertTrue(parser.isVarDeclaration());
    	assertStackTopEquals(tree("var", "a","b","c"));
    	
    	use("var a, b, c, d, e\n");
    	assertTrue(parser.isVarDeclaration());
    	assertStackTopEquals(tree("var","a","b","c","d","e"));
    	
    	try {
    		use("var a, b, c, d, e");
        	assertFalse(parser.isVarDeclaration());
            fail();
        }
        catch (SyntaxException e) {}
    }
    
    @Test
    public void testFunctionDefinition() {
    	use("13");
    	assertFalse(parser.isFunctionDefinition());
    	
    	use("define f1 using a,b" +
    			"{\n" +
			"move 45\n" +
			"}\n");
    	assertTrue(parser.isFunctionDefinition());
    	assertStackTopEquals(tree("function", "f1",tree("var","a","b"),
    							tree("block",
    							tree("move","45.0"))));

    	try {
    		use("define f1 a,b" +
        			"{\n" +
        			"move 45\n" +
        			"}\n");
        	assertFalse(parser.isFunctionDefinition());
            fail();
        }
        catch (SyntaxException e) {}
    }
    
    @Test
    public void testAllbugsCode() {
    	use(" {\n" + "}\n");
    	assertFalse(parser.isAllbugsCode());
    	
    	use("Allbugs {\n" + "}\n");
    	assertTrue(parser.isAllbugsCode());
    	assertStackTopEquals(tree("Allbugs","list","list"));
    	
    	use("Allbugs {\n" +
    		    
				"var a,b,c\n" + "}\n");
    	assertTrue(parser.isAllbugsCode());
    	assertStackTopEquals(tree("Allbugs",tree("list",tree("var","a","b","c")),"list"));
    	
    	use("Allbugs {\n" +
    		    
				"var a,b,c\n" + "var a,b,c,d,e,f\n" + "}\n");
    	assertTrue(parser.isAllbugsCode());
		assertStackTopEquals(tree(
				"Allbugs",
				tree("list", tree("var", "a", "b", "c"),
						tree("var", "a", "b", "c", "d", "e", "f")), "list"));
    	try {
    		use("Allbugs {\n" +
				"var a,b,c\n" + "}");
        	assertFalse(parser.isAllbugsCode());
            fail();
        }
        catch (SyntaxException e) {}
    }
    
    @Test
    public void testisBugDefition(){
    	use("sally");
    	assertFalse(parser.isBugDefinition());
    	
    	use("Bug sally {\n" + "var a,b,c\n" +
    
						    			"initially {\n" + "move 45\n" + "}\n" +
										  
										  "move 45\n" +
	    					
										"define f1 using a,b" +
										"{\n" +
										"move 45\n" +
										"}\n" + "}\n");
    	assertTrue(parser.isBugDefinition());
		assertStackTopEquals(tree(
				"Bug",
				"sally",
				tree("list", tree("var", "a", "b", "c")),
				tree("initially", tree("block", tree("move", "45.0"))),
				tree("block", tree("move", "45.0")),
				tree("list",
						tree("function", "f1", tree("var", "a", "b"),
				tree("block",
				tree("move","45.0"))))));
    	
    	try{
    		use("Bug sally {" + "var a,b,c\n" +
    			    
						    			"initially {\n" + "move 45\n" + "}\n" +
										  
										  "move 45\n" +
	    					
										"define f1 using a,b" +
										"{\n" +
										"move 45\n" +
										"}\n" + "}\n");
    	assertFalse(parser.isBugDefinition());
    	fail();
    	}
    	catch(Exception e){}
    }
    
    @Test
    public void testIsProgram(){
    	
    	
		use("Allbugs {\n" +

		"var a,b,c\n" + "}\n" + "Bug sally {\n" + "var a,b,c\n" +

		"initially {\n" + "move 45\n" + "}\n" +

		"move 45\n" +

		"define f1 using a,b" + "{\n" + "move 45\n" + "}\n" + "}\n");
		assertTrue(parser.isProgram());
		assertStackTopEquals(tree(
				"program",
				tree("Allbugs", tree("list", tree("var", "a", "b", "c")),
						"list"),
				tree("list",
						tree("Bug",
								"sally",
								tree("list", tree("var", "a", "b", "c")),
								tree("initially",
										tree("block", tree("move", "45.0"))),
								tree("block", tree("move", "45.0")),
								tree("list",
										tree("function",
												"f1",
												tree("var", "a", "b"),
												tree("block",
														tree("move", "45.0"))))))));
    }
    
    @Test
    public void testIsSwitchStatement() {
    	use("switch {\n" +
    			  "case a\n" +
    			  "}\n");
    	assertTrue(parser.isSwitchStatement());
    	assertStackTopEquals(tree("switch",tree("case","a",tree("block"))));
        
    	use("switch {\n" +
		  "case a\n" +
		  " line 5,6,7,8\n" +
		  "}\n");
        assertTrue(parser.isSwitchStatement());
		assertStackTopEquals(tree(
				"switch",
				tree("case", "a",
						tree("block", tree("line", "5.0", "6.0", "7.0", "8.0")))));
        
        use("switch {\n" +
      		  "case a\n" +
      		  " line 5,6,7,8\n" +
      		  "case b>a\n" +
      		  " color red\n" +
      		  "}\n");
        assertTrue(parser.isSwitchStatement());
		assertStackTopEquals(tree(
				"switch",
				tree("case", "a",
						tree("block", tree("line", "5.0", "6.0", "7.0", "8.0"))),
				tree("case", tree(">", "b", "a"),
						tree("block", tree("color", "red")))));
        
        try{
        use("switch {" +
        		  "case a\n" +
        		  " line 5,6,7,8\n" +
        		  "case b>a\n" +
        		  " color red\n" +
        		  "}\n");
        assertFalse(parser.isSwitchStatement());
        fail();
        }
        catch(Exception e){}
    }
    @Test
    public void testIsDoStatement(){
    	use("a \n");
    	assertFalse(parser.isDoStatement());
    	
    	use("do a \n");
    	assertTrue(parser.isDoStatement());
    	assertStackTopEquals(tree("call","a"));
    	
    	use("do a (xyz + 3) \n");
    	assertTrue(parser.isDoStatement());
    	assertStackTopEquals(tree("call","a",tree("var",tree("+","xyz","3.0"))));
    	
    	use("do a (xyz + 3, a + b + c) \n");
    	assertTrue(parser.isDoStatement());
		assertStackTopEquals(tree(
				"call",
				"a",
				tree("var", tree("+", "xyz", "3.0"),
						tree("+", tree("+", "a", "b"), "c"))));
    	
    	try{
    		use("do a (xyz + 3, a + b + c) ");
        	assertFalse(parser.isDoStatement());
        	fail();
    	}
    	catch(Exception e){}
    }
    
    @Test
    public void testIsFunctionCall(){
    	use("alif \n");
    	assertFalse(parser.isFunctionCall());
    	
    	use("alif (xyz + 3) ");
    	assertTrue(parser.isFunctionCall());
    	assertStackTopEquals(tree("call","alif",tree("var",tree("+","xyz","3.0"))));
    	
    	use("alif (xyz + 3, a + b + c)");
    	assertTrue(parser.isFunctionCall());
		assertStackTopEquals(tree(
				"call",
				"alif",
				tree("var", tree("+", "xyz", "3.0"),
						tree("+", tree("+", "a", "b"), "c"))));
    }
    
    @Test
    public void testIsAddOperator() {
        use("+ - + $");
        assertTrue(parser.isAddOperator());
        assertTrue(parser.isAddOperator());
        assertTrue(parser.isAddOperator());
        assertFalse(parser.isAddOperator());
        followedBy(parser, "$");
    }

    @Test
    public void testIsMultiplyOperator() {
        use("* / $");
        assertTrue(parser.isMultiplyOperator());
        assertTrue(parser.isMultiplyOperator());
        assertFalse(parser.isMultiplyOperator());
        followedBy(parser, "$");
    }

    @Test
    public void testNextToken() {
        use("12 12.5 bogus switch + \n");
        assertEquals(new Token(Token.Type.NUMBER, "12.0"), parser.nextToken());
        assertEquals(new Token(Token.Type.NUMBER, "12.5"), parser.nextToken());
        assertEquals(new Token(Token.Type.NAME, "bogus"), parser.nextToken());
        assertEquals(new Token(Token.Type.KEYWORD, "switch"), parser.nextToken());
        assertEquals(new Token(Token.Type.SYMBOL, "+"), parser.nextToken());
        assertEquals(new Token(Token.Type.EOL, "\n"), parser.nextToken());
        assertEquals(new Token(Token.Type.EOF, "EOF"), parser.nextToken());
    }
    
   
   
//  ----- "Helper" methods
    
    /**
     * Sets the <code>parser</code> instance to use the given string.
     * 
     * @param s The string to be parsed.
     */
    private void use(String s) {
        parser = new Parser(s);
    }
    
    /**
     * Returns the current top of the stack.
     *
     * @return The top of the stack.
     */
    private Object stackTop() {
        return parser.stack.peek();
    }
    
    /**
     * Tests whether the top element in the stack is correct.
     *
     * @return <code>true</code> if the top element of the stack is as expected.
     */
    private void assertStackTopEquals(Tree<Token> expected) {
        assertEquals(expected, stackTop());
    }
    
    /**
     * This method is given a String containing some or all of the
     * tokens that should yet be returned by the Tokenizer, and tests
     * whether the Tokenizer in fact has those Tokens. To succeed,
     * everything in the given String must still be in the Tokenizer,
     * but there may be additional (untested) Tokens to be returned.
     * This method is primarily to test whether Tokens are pushed
     * back appropriately.
     * @param parser TODO
     * @param expectedTokens The Tokens we expect to get from the Tokenizer.
     */
    private void followedBy(Parser parser, String expectedTokens) {
        int expectedType;
        int actualType;
        StreamTokenizer actual = parser.tokenizer;

        Reader reader = new StringReader(expectedTokens);
        StreamTokenizer expected = new StreamTokenizer(reader);

        try {
            while (true) {
                expectedType = expected.nextToken();
                if (expectedType == StreamTokenizer.TT_EOF) break;
                actualType = actual.nextToken();
                assertEquals(typeName(expectedType), typeName(actualType));
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
    
    private String typeName(int type) {
        switch(type) {
            case StreamTokenizer.TT_EOF: return "EOF";
            case StreamTokenizer.TT_EOL: return "EOL";
            case StreamTokenizer.TT_WORD: return "WORD";
            case StreamTokenizer.TT_NUMBER: return "NUMBER";
            default: return "'" + (char)type + "'";
        }
    }
    
    /**
     * Returns a Tree node consisting of a single leaf; the
     * node will contain a Token with a String as its value. <br>
     * Given a Tree, return the same Tree.<br>
     * Given a Token, return a Tree with the Token as its value.<br>
     * Given a String, make it into a Token, return a Tree
     * with the Token as its value.
     * 
     * @param value A Tree, Token, or String from which to
              construct the Tree node.
     * @return A Tree leaf node containing a Token whose value
     *         is the parameter.
     */
    private Tree<Token> createNode(Object value) {
        if (value instanceof Tree) {
            return (Tree) value;
        }
        if (value instanceof Token) {
            return new Tree<Token>((Token) value);
        }
        else if (value instanceof String) {
            return new Tree<Token>(new Token((String) value));
        }
        assert false: "Illegal argument: tree(" + value + ")";
        return null; 
    }
    
    /**
     * Builds a Tree that can be compared with the one the
     * Parser produces. Any String or Token arguments will be
     * converted to Tree nodes containing Tokens.
     * 
     * @param op The String value to use in the Token in the root.
     * @param children The objects to be made into children.
     * @return The resultant Tree.
     */
    private Tree<Token> tree(String op, Object... children) {
        Tree<Token> tree = new Tree<Token>(new Token(op));
        for (int i = 0; i < children.length; i++) {
            tree.addChild(createNode(children[i]));
        }
        return tree;
    }
}
