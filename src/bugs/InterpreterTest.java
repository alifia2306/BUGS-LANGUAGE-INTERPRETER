package bugs;

import static org.junit.Assert.*;

import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;

import org.junit.Test;

import tree.Tree;

public class InterpreterTest {
	Parser parser;
	@Test
	public void testAllBugs(){
		Interpreter i = new Interpreter();
		Parser parser = new Parser("Allbugs {\n" + "var a,b,c\n" +
		 "}\n");
		assertTrue(parser.isAllbugsCode());
		i.interpret(parser.stack.pop());
		assertEquals(0.0, i.map.get("a"), 0.001);
		assertEquals(0.0, i.map.get("b"), 0.001);
		assertEquals(0.0, i.map.get("c"), 0.001);
		
		Interpreter i2 = new Interpreter();
		Parser parser2 = new Parser("Allbugs {\n" + "var a,b,c\n" +
				"define f1 using a,b" + "{\n" + "move 45\n" + "}\n" +
		 "}\n" );
		assertTrue(parser2.isAllbugsCode());
		i2.interpret(parser2.stack.pop());
		assertEquals(0.0, i2.map.get("a"), 0.001);
		assertEquals(0.0, i2.map.get("b"), 0.001);
		assertEquals(0.0, i2.map.get("c"), 0.001);
		Parser parsr = new Parser("define f1 using a,b" +
				"{\n" +
				"move 45\n" +
				"}\n");
		assertTrue(parsr.isFunctionDefinition());
		assertEquals(i2.functions.get("f1"), parsr.stack.peek());		
	}
	
	@Test
	public void testVar(){
		Interpreter bug = new Interpreter();
		
		Parser parsr = new Parser("var a,b,c\n");
		assertTrue(parsr.isVarDeclaration());
		bug.interpret(parsr.stack.pop());
		assertEquals(0.0, bug.map.get("a"), 0.001);
		assertEquals(0.0, bug.map.get("b"), 0.001);
		assertEquals(0.0,  bug.map.get("c"), 0.001);
		
		Parser parser1 = new Parser("var a,b,c, d, e\n");
		assertTrue(parser1.isVarDeclaration());
		bug.interpret(parser1.stack.pop());
		assertEquals(0.0,  bug.map.get("a"), 0.001);
		assertEquals(0.0,  bug.map.get("b"), 0.001);
		assertEquals(0.0,  bug.map.get("c"), 0.001);
		assertEquals(0.0,  bug.map.get("d"), 0.001);
		assertEquals(0.0,  bug.map.get("e"), 0.001);
		
		
	}
	
	@Test
	public void testList(){
		Interpreter bug = new Interpreter();
		Tree<Token> treeList = tree("list",tree("var","a"),tree("var","b"));
		bug.interpret(treeList);
		assertEquals(0.0, bug.map.get("a"),0.001);
		assertEquals(0.0, bug.map.get("b"),0.001);
		
		Tree<Token> treeList2 = tree("list",tree("var","c", "d"));
		bug.interpret(treeList2);
		assertEquals(0.0, bug.map.get("c"),0.001);
		assertEquals(0.0, bug.map.get("d"),0.001);
		
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
