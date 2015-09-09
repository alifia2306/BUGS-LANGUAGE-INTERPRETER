package bugs;

import static org.junit.Assert.*;

import java.awt.Color;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.Iterator;

import javax.jws.soap.SOAPBinding.Use;

import org.junit.Test;

import tree.Tree;
import bugs.ParserTest;

public class BugTest {
	Parser parser;
	@Test
	public void testStore(){
		Bug bug = new Bug();
		bug.store("x", 30.0);
		bug.store("y", 45.0);
		bug.store("angle", 45);
		bug.interpretVar(tree("var", "a"));
		bug.interpretAssign(tree("assign","a","3.0"));
		assertEquals(30.0,bug.x,0.001);
		assertEquals(45.0,bug.y,0.001);
		assertEquals(45.0,bug.angle,0.001);
		assertEquals(3.0,bug.map.get("a"),0.001);
	}
	
	@Test
	public void testFetch(){
		Interpreter interpret = new Interpreter();
		Bug bug = new Bug();
		bug.store("x", 30.0);
		bug.store("y", 45.0);
		bug.store("angle", 45);
		bug.interpretVar(tree("var", "a"));
		bug.store("a", 3.0);
		assertEquals(30.0,bug.fetch("x"),0.001);
		assertEquals(45.0,bug.fetch("y"),0.001);
		assertEquals(45.0,bug.fetch("angle"),0.001);
		assertEquals(3.0,bug.fetch("a"),0.001);
	}
	
	@Test(expected = RuntimeException.class)
	public void testFetchException(){
		Bug bug = new Bug();
		bug.store("x", 30.0);
		bug.store("y", 45.0);
		bug.store("angle", 45);
		bug.store("a", 3.0);
		bug.fetch("b");
		fail();	
	}

	@Test
	public void testMove() {
		Interpreter interpret = new Interpreter();
		Bug bug = new Bug();
		bug.x = 20;
		bug.y = 25;
		bug.angle = 180;
		
		Parser parsr = new Parser("move 10\n");
		assertTrue(parsr.isMoveAction());
		bug.interpretMove(parsr.stack.pop());
		assertEquals(10.0,bug.x,0.001);
		assertEquals(25.0,bug.y, 0.001);
		
		Parser parser1 = new Parser("move 45\n");
		assertTrue(parser1.isMoveAction());
		bug.angle = -45;
		bug.interpretMove(parser1.stack.pop());
		assertEquals(41.82,bug.x,0.001);
		assertEquals(56.82,bug.y, 0.001);
		
		Parser parser2 = new Parser("move 10 + 5\n");
		assertTrue(parser2.isMoveAction());
		bug.angle = 450;
		bug.interpretMove(parser2.stack.pop());
		assertEquals(41.82,bug.x,0.001);
		assertEquals(41.82,bug.y, 0.001);
		
	}
	
	@Test
	public void testMoveTo() {
		
		Bug bug = new Bug();
		bug.x = 20;
		bug.y = 25;
		bug.angle = 180;
		
		Parser parsr = new Parser("moveto 10, 15\n");
		assertTrue(parsr.isMoveToAction());
		bug.interpretMoveTo(parsr.stack.pop());
		assertEquals(10.0,bug.x,0.001);
		assertEquals(15.0,bug.y, 0.001);
		
		Parser parser1 = new Parser("moveto 50 - 10 + 5, 55\n");
		assertTrue(parser1.isMoveToAction());
		bug.interpretMoveTo(parser1.stack.pop());
		assertEquals(45,bug.x,0.001);
		assertEquals(55,bug.y, 0.001);
		
		Parser parser2 = new Parser("moveto 10 + 5,30\n");
		assertTrue(parser2.isMoveToAction());
		bug.interpretMoveTo(parser2.stack.pop());
		assertEquals(15,bug.x,0.001);
		assertEquals(30,bug.y, 0.001);
	}
	
	@Test
	public void testTurn() {
		
		Bug bug = new Bug();
		bug.angle = 180;
		
		Parser parsr = new Parser("turn 10\n");
		assertTrue(parsr.isTurnAction());
		bug.interpret(parsr.stack.pop());
		assertEquals(190.0,bug.angle,0.001);
		
		Parser parser1 = new Parser("turn 405\n");
		assertTrue(parser1.isTurnAction());
		bug.interpretTurn(parser1.stack.pop());
		assertEquals(235.0,bug.angle,0.001);
		
		Parser parser2 = new Parser("turn -415\n");
		assertTrue(parser2.isTurnAction());
		bug.interpretTurn(parser2.stack.pop());
		assertEquals(180.0,bug.angle,0.001);
		
		bug.angle = 20;
		Parser parser3 = new Parser("turn -700\n");
		assertTrue(parser3.isTurnAction());
		bug.interpretTurn(parser3.stack.pop());
		assertEquals(40.0,bug.angle,0.001);
			
	}
	
	@Test
	public void testTurnTo() {
		
		Bug bug = new Bug();
		bug.x = 20;
		bug.y = 25;
		bug.angle = 180;
		
		Parser parsr = new Parser("turnto 10\n");
		assertTrue(parsr.isTurnToAction());
		bug.interpretTurnTo(parsr.stack.pop());
		assertEquals(10.0,bug.angle,0.001);
		
		Parser parser1 = new Parser("turnto -540\n");
		assertTrue(parser1.isTurnToAction());
		bug.interpretTurnTo(parser1.stack.pop());
		assertEquals(180.0,bug.angle,0.001);
		
		Parser parser2 = new Parser("turnto 630\n");
		assertTrue(parser2.isTurnToAction());
		bug.interpretTurnTo(parser2.stack.pop());
		assertEquals(270.0,bug.angle,0.001);
	}
	
	@Test
	public void testColor(){
		Bug bug = new Bug();
		Parser parsr = new Parser("color red\n");
		assertTrue(parsr.isColorStatement());
		bug.interpret(parsr.stack.pop());
		assertEquals(Color.RED,bug.BugColor);
		
		Parser parser1 = new Parser("color purple\n");
		assertTrue(parser1.isColorStatement());
		bug.interpret(parser1.stack.pop());
		assertEquals(new Color(128,0,128),bug.BugColor);
		
	}
	
	
	
	@Test(expected = RuntimeException.class)
	public void testColorException() {
		Parser parser = new Parser("color purpel\n");
		assertFalse(parser.isColorStatement());
		fail();
	}
	
	@Test
	public void testAddition(){
		Bug bug = new Bug();
		Parser parsr = new Parser("2 + 3\n");
		assertTrue(parsr.isExpression());
		double result = bug.evaluate(parsr.stack.pop());
		assertEquals(5.0, result, 0.001);
		
		Parser parser1 = new Parser("2 + 3 + 7\n");
		assertTrue(parser1.isExpression());
		result = bug.evaluate(parser1.stack.pop());
		assertEquals(12.0, result, 0.001);
		
		Parser parser2 = new Parser("+5\n");
		assertTrue(parser2.isExpression());
		result = bug.evaluate(parser2.stack.pop());
		assertEquals(5.0, result, 0.001);
		
		Parser parser3 = new Parser("a + 3\n");
		assertTrue(parser3.isExpression());
		bug.interpretVar(tree("var", "a"));
		bug.store("a", 0);
		Parser parser4 = new Parser("a = 4\n");
		assertTrue(parser4.isAssignmentStatement());
		bug.interpret(parser4.stack.pop());
		result = bug.evaluate(parser3.stack.pop());
		assertEquals(7.0, result, 0.001);
	}
	
	@Test
	public void testSubtraction(){
		Bug bug = new Bug();
		Parser parsr = new Parser("2 - 3\n");
		assertTrue(parsr.isExpression());
		double result = bug.evaluate(parsr.stack.pop());
		assertEquals(-1.0, result, 0.001);
		
		Parser parser1 = new Parser("2 - 3 + 7\n");
		assertTrue(parser1.isExpression());
		result = bug.evaluate(parser1.stack.pop());
		assertEquals(6.0, result, 0.001);
		
		Parser parser2 = new Parser("-5\n");
		assertTrue(parser2.isExpression());
		result = bug.evaluate(parser2.stack.pop());
		assertEquals(-5.0, result, 0.001);
	}
	
	@Test
	public void testMultiplication(){
		Bug bug = new Bug();
		Parser parsr = new Parser("2 * 3\n");
		assertTrue(parsr.isExpression());
		double result = bug.evaluate(parsr.stack.pop());
		assertEquals(6.0, result, 0.001);
		
		Parser parser1 = new Parser("2 * 3 + 7\n");
		assertTrue(parser1.isExpression());
		result = bug.evaluate(parser1.stack.pop());
		assertEquals(13.0, result, 0.001);
	}
	
	@Test
	public void testDivision(){
		Bug bug = new Bug();
		Parser parsr = new Parser("6 / 3\n");
		assertTrue(parsr.isExpression());
		double result = bug.evaluate(parsr.stack.pop());
		assertEquals(2.0, result, 0.001);
		
		Parser parser1 = new Parser("6 / 3 + 7\n");
		assertTrue(parser1.isExpression());
		result = bug.evaluate(parser1.stack.pop());
		assertEquals(9.0, result, 0.001);
		
	}
	
	@Test(expected = ArithmeticException.class)
	public void testDivisionException(){
		Bug bug = new Bug();
		Parser parsr = new Parser("6 / 0\n");
		assertTrue(parsr.isExpression());
		double result = bug.evaluate(parsr.stack.pop());
		fail();
	}
	
	@Test
	public void testLessThan(){
		Bug bug = new Bug();
		Parser parsr = new Parser("6 < 3\n");
		assertTrue(parsr.isExpression());
		double result = bug.evaluate(parsr.stack.pop());
		assertEquals(0.0, result, 0.001);
		
		Parser parser1 = new Parser("3 < 6\n");
		assertTrue(parser1.isExpression());
		result = bug.evaluate(parser1.stack.pop());
		assertEquals(1.0, result, 0.001);
		
		Parser parser2 = new Parser("3.0004 < 3.0");
		assertTrue(parser2.isExpression());
		result = bug.evaluate(parser2.stack.pop());
		assertEquals(0.0, result, 0.001);
	}
	
	@Test
	public void testLessThanEqual(){
		Bug bug = new Bug();
		Parser parsr = new Parser("3.0004 <= 3\n");
		assertTrue(parsr.isExpression());
		double result = bug.evaluate(parsr.stack.pop());
		assertEquals(1.0, result, 0.001);
		
		Parser parser1 = new Parser("7 <= 6\n");
		assertTrue(parser1.isExpression());
		result = bug.evaluate(parser1.stack.pop());
		assertEquals(0.0, result, 0.001);
		
		Parser parser2 = new Parser("6 <= 7\n");
		assertTrue(parser2.isExpression());
		result = bug.evaluate(parser2.stack.pop());
		assertEquals(1.0, result, 0.001);
	}
	
	@Test
	public void testEqual(){
		Bug bug = new Bug();
		Parser parsr = new Parser("3.0004 = 3\n");
		assertTrue(parsr.isExpression());
		double result = bug.evaluate(parsr.stack.pop());
		assertEquals(1.0, result, 0.001);
		
		Parser parser1 = new Parser("6 = 7\n");
		assertTrue(parser1.isExpression());
		result = bug.evaluate(parser1.stack.pop());
		assertEquals(0.0, result, 0.001);
	}
	
	@Test
	public void testNotEqual(){
		Bug bug = new Bug();
		Parser parsr = new Parser("3.0004 != 3\n");
		assertTrue(parsr.isExpression());
		double result = bug.evaluate(parsr.stack.pop());
		assertEquals(0.0, result, 0.001);
		
		Parser parser1 = new Parser("6 != 7\n");
		assertTrue(parser1.isExpression());
		result = bug.evaluate(parser1.stack.pop());
		assertEquals(1.0, result, 0.001);
	}
	
	@Test
	public void testGreaterThan(){
		Bug bug = new Bug();
		Parser parsr = new Parser("3.0004 > 3.0\n");
		assertTrue(parsr.isExpression());
		double result = bug.evaluate(parsr.stack.pop());
		assertEquals(0.0, result, 0.001);
		
		Parser parser1 = new Parser("6 > 7\n");
		assertTrue(parser1.isExpression());
		result = bug.evaluate(parser1.stack.pop());
		assertEquals(0.0, result, 0.001);
		
		Parser parser2 = new Parser("7 > 6\n");
		assertTrue(parser2.isExpression());
		result = bug.evaluate(parser2.stack.pop());
		assertEquals(1.0, result, 0.001);
	}
	
	@Test
	public void testGreaterThanEqual(){
		Bug bug = new Bug();
		Parser parsr = new Parser("3.0004 >= 3\n");
		assertTrue(parsr.isExpression());
		double result = bug.evaluate(parsr.stack.pop());
		assertEquals(1.0, result, 0.001);
		
		Parser parser1 = new Parser("6 >= 7\n");
		assertTrue(parser1.isExpression());
		result = bug.evaluate(parser1.stack.pop());
		assertEquals(0.0, result, 0.001);
	}
	
	@Test
	public void testCase(){
		Bug bug = new Bug();
		
		Parser parsr = new Parser("var a,b\n");
		assertTrue(parsr.isVarDeclaration());
		bug.interpret(parsr.stack.pop());
		
		Parser parser2 = new Parser("a = 3\n");
		assertTrue(parser2.isAssignmentStatement());
		bug.interpret(parser2.stack.pop());
		
		Parser parser3 = new Parser("b = 10\n");
		assertTrue(parser3.isAssignmentStatement());
		bug.interpret(parser3.stack.pop());
		
		Tree<Token> treeCase = tree("case", tree(">", "b", "a"),
				tree("block", tree("color", "red")));
		bug.evaluate(treeCase);
		assertEquals(Color.RED, bug.BugColor);
		
		Tree<Token> treeCase2 = tree("case", tree(">", "3", "1"),
				tree("block", tree("var", "a")));
		bug.evaluate(treeCase2);
		assertEquals(0.0, bug.fetch("a"), 0.001);
		
	}
	
	@Test
	public void testCall(){
		
		Bug bug2 = new Bug();
		
		Parser parser2 = new Parser("define drawSquare using size { \n" +
					 "x = 789.0 \n" +
			      
			         "return size + 20 \n" +
			         
			   " } \n");
		assertTrue(parser2.isFunctionDefinition());
		bug2.interpretFunction(parser2.stack.peek());
		
		
		Parser parser3 = new Parser("drawSquare(20) \n");
		assertTrue(parser3.isFunctionCall());
		try {
			bug2.evaluateCall(parser3.stack.peek());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertEquals(789.0, bug2.fetch("x"), 0.01);	
	}
	
	@Test
	public void testDot(){
		Interpreter interpreter = new Interpreter();
		
		Parser parser = new Parser("Bug sally {\n" + "var a,b,c\n" +
    
						    			"initially {\n"  + "x = 10.0 \n" + "}\n" + "y = 10.0\n" +
										  
										  "}\n");
		Parser parser2 = new Parser("Bug lily {\n" + "var a,b,c\n" +
			    
						    			"initially {\n"  + "x = sally.x \n" + "}\n" + "y = sally.x \n" +
										  
										  "}\n");
		
		assertTrue(parser.isBugDefinition());
		Bug bug = new Bug(parser.stack.peek(),interpreter);
		bug.interpret(parser.stack.peek());
		interpreter.bugs.put("sally", bug);
		assertTrue(parser2.isBugDefinition());
		Bug bug2 = new Bug(parser2.stack.peek(), interpreter);
		bug2.interpret(parser2.stack.peek());
		assertEquals(10.0,bug.fetch("x"),0.01);
		assertEquals(10.0,bug2.fetch("x"),0.01);
		
	}
	
	@Test
	public void testVar(){
		Bug bug = new Bug();
		
		Parser parsr = new Parser("var a,b,c\n");
		assertTrue(parsr.isVarDeclaration());
		bug.interpret(parsr.stack.pop());
		assertEquals(0.0, bug.fetch("a"), 0.001);
		assertEquals(0.0, bug.fetch("b"), 0.001);
		assertEquals(0.0, bug.fetch("c"), 0.001);
		
		Parser parser1 = new Parser("var a,b,c, d, e\n");
		assertTrue(parser1.isVarDeclaration());
		bug.interpret(parser1.stack.pop());
		assertEquals(0.0, bug.fetch("a"), 0.001);
		assertEquals(0.0, bug.fetch("b"), 0.001);
		assertEquals(0.0, bug.fetch("c"), 0.001);
		assertEquals(0.0, bug.fetch("d"), 0.001);
		assertEquals(0.0, bug.fetch("e"), 0.001);
		bug.x = 5;
		Parser parser2 = new Parser("var x\n");
		assertTrue(parser2.isVarDeclaration());
		bug.interpret(parser2.stack.pop());
		assertEquals(5.0, bug.fetch("x"), 0.001);
		
	}
	
	@Test
	public void testAssignment(){
		Bug bug = new Bug();
		Parser parsr = new Parser("var a,b,c\n");
		assertTrue(parsr.isVarDeclaration());
		bug.interpret(parsr.stack.pop());
		Parser parser2 = new Parser("a = 3 + b * c\n");
		assertTrue(parser2.isAssignmentStatement());
		bug.interpret(parser2.stack.pop());
		assertEquals(3.0, bug.fetch("a"),0.001);
		assertEquals(0.0, bug.fetch("b"),0.001);
		assertEquals(0.0, bug.fetch("c"),0.001);
		
		Parser parser3 = new Parser("var d,e\n");
		assertTrue(parser3.isVarDeclaration());
		bug.interpret(parser3.stack.pop());
		Parser parser4 = new Parser("d = 6\n");
		assertTrue(parser4.isAssignmentStatement());
		bug.interpret(parser4.stack.pop());
		Parser parser5 = new Parser("e = 3 + d + c \n");
		assertTrue(parser5.isAssignmentStatement());
		bug.interpret(parser5.stack.pop());
		assertEquals(9.0, bug.fetch("e"),0.001);
		assertEquals(6.0, bug.fetch("d"),0.001);
	}
	
	@Test(expected = RuntimeException.class)
	public void testAssignException(){
		Bug bug = new Bug();
		Parser parsr = new Parser("a = 3\n");
		assertTrue(parsr.isAssignmentStatement());
		bug.interpret(parsr.stack.pop());
		assertEquals(3.0, bug.fetch("a"),0.001);
		fail();
	}
	
	@Test
	public void testBlock(){
		Bug bug = new Bug();
		Parser parsr = new Parser("var a \n");
		assertTrue(parsr.isVarDeclaration());
		bug.interpret(parsr.stack.pop());
		
		Parser parser1 = new Parser("{\n" +
    			"a = 3\n" + "color red\n" +
    			"}\n");
		assertTrue(parser1.isBlock());
		bug.interpret(parser1.stack.pop());
		assertEquals(3.0, bug.fetch("a"),0.001);
		assertEquals(Color.RED,bug.BugColor);	
	}
	
	@Test
	public void testInitially(){
		Bug bug = new Bug();
		Parser parsr = new Parser("var a \n");
		assertTrue(parsr.isVarDeclaration());
		bug.interpret(parsr.stack.pop());
		
		Parser parser1 = new Parser("initially {\n" +
    			"a = 3\n" + "color red\n" +
    			"}\n");
		assertTrue(parser1.isInitializationBlock());
		bug.interpret(parser1.stack.pop());
		assertEquals(3.0, bug.fetch("a"),0.001);
		assertEquals(Color.RED,bug.BugColor);	
	}
	
	@Test
	public void testFunction(){
		Bug bug = new Bug();
		Parser parsr = new Parser("define f1 using a,b" +
									"{\n" +
									"move 45\n" +
									"}\n");
		assertTrue(parsr.isFunctionDefinition());
		bug.interpret(parsr.stack.peek());
		assertEquals(parsr.stack.peek(), bug.functions.get("f1"));
	}
	

	@Test
	public void testList(){
		Bug bug = new Bug();
		Tree<Token> treeList = tree("list",tree("var","a"),tree("var","b"));
		bug.interpret(treeList);
		assertEquals(0.0, bug.fetch("a"),0.001);
		assertEquals(0.0, bug.fetch("b"),0.001);
		
		Tree<Token> treeList2 = tree("list",tree("var","c", "d"));
		bug.interpret(treeList2);
		assertEquals(0.0, bug.fetch("c"),0.001);
		assertEquals(0.0, bug.fetch("d"),0.001);
		
		Parser parsr = new Parser("define f1 using a,b" +
									"{\n" +
									"move 45\n" +
									"}\n");
		Parser parsr2 = new Parser("define f2 using c" +
									"{\n" +
									"color red\n" +
									"}\n");
		assertTrue(parsr.isFunctionDefinition());
		assertTrue(parsr2.isFunctionDefinition());
		bug.interpret(tree("list",parsr.stack.peek(),parsr2.stack.peek()));
		assertEquals(parsr.stack.peek(), bug.functions.get("f1"));
		assertEquals(parsr2.stack.peek(), bug.functions.get("f2"));
		
	}
	
	@Test
	public void testLoopAndExit(){
		Interpreter interpreter = new Interpreter();
		Parser parsr = new Parser("loop {\n" +
									"move 10\n" +
									"exit if 1>0\n" +
									"}\n");
		assertTrue(parsr.isLoopStatement());
		Bug bug = new Bug();
		bug.interpretLoop(parsr.stack.peek());
		assertEquals(10.0,bug.x,0.001);
		assertEquals(0.0,bug.y,0.001);
		

	}
	
	@Test
	public void testSwitch(){
		Bug bug = new Bug();
		Parser parsr = new Parser("var s\n");
		assertTrue(parsr.isVarDeclaration());
		bug.interpret(parsr.stack.peek());
		bug.interpret(tree(
				"switch",
				tree("case", "1",
						tree("block", tree("assign", "s", "3.0"))), tree("case","0",tree("block",tree("color", "red")))));
		assertEquals(3.0, bug.fetch("s"), 0.001);
		
		bug.interpret(tree(
				"switch",
				tree("case", "0",
						tree("block", tree("assign", "s", "3.0"))), tree("case","1",tree("block",tree("color", "red")))));
		assertEquals(bug.BugColor, Color.RED);
		
		bug.interpret(tree(
				"switch",
				tree("case", "0",
						tree("block", tree("assign", "s", "9.0"))), tree("case","1",tree("block",tree("color", "black"),tree("assign", "s", "9.0")))));
		assertEquals(9.0, bug.fetch("s"), 0.001);
		assertEquals(bug.BugColor, Color.BLACK);
		
	}
	
	@Test
	public void testBug(){
		Interpreter interpreter = new Interpreter();
		Parser parsr = new Parser("Bug sally {\n" + "var a,b,c\n" +
    
						    			"initially {\n" + "move 45\n" + "}\n" +
										  
										  "move 45\n" +
	    					
										"define f1 using a,b" +
										"{\n" +
										"move 45\n" +
										"}\n" +
										"}\n");
		
		assertTrue(parsr.isBugDefinition());
		Bug bug = new Bug(parsr.stack.peek(),interpreter);
		bug.interpret(parsr.stack.peek());
		assertEquals("sally", bug.BugName);
		assertEquals(45.0, bug.x, 0.001);

		
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
