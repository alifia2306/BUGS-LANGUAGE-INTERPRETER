package bugs;

import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Stack;
import java.util.Map.Entry;

import javax.management.RuntimeErrorException;

import tree.Tree;

public class Bug extends Thread{
	double x,y,angle;
	String BugName;
	Color BugColor;
	HashMap<String, Double> map;
	HashMap<String, Tree<Token>> functions;
	Stack<HashMap<String, Double>> scopes;
	Interpreter interpreter;
	int flag;
	double result;
	Tree<Token> bugTree;
	static Random rand = new Random();
    private int counter;        // A counter this worker must increment 
    private boolean blocked;    // If true, this worker cannot work
	
	/**
	 * Constructor for Bug Class.
	 */
    
    Bug(){
    	
    	x = 0;
		y = 0;
		angle = 0;
		BugName = "";
		BugColor = null;
		flag = 0;
		result = 0;
		map = new HashMap<String, Double>();
		functions = new HashMap<String, Tree<Token>>();
		scopes = new Stack<HashMap<String, Double>>();
		scopes.push(map);
		interpreter = new Interpreter();
    }
    
    /**
	 * Parameterized Constructor for Bug Class.
	 */
	Bug(Tree<Token> bugTree,Interpreter interpreter){ 
		x = 0;
		y = 0;
		angle = 0;
		BugName = "";
		BugColor = null;
		flag = 0;
		result = 0;
		
		synchronized(this){
			this.interpreter = interpreter;
			map = new HashMap<String, Double>();
			functions = new HashMap<String, Tree<Token>>();
			scopes = new Stack<HashMap<String, Double>>();
			scopes.push(map);
			BugName = bugTree.getChild(0).getValue().value;
		}
		this.bugTree = bugTree; // confirm with someone
		interpret(bugTree.getChild(1));
		interpret(bugTree.getChild(2));
		interpret(bugTree.getChild(4));
		

	}
	
	/**
	 * Getter for x
	 * @return x
	 */
	public double getX(){
		return x;
	}
	
	/**
	 * Getter for y
	 * @return y
	 */
	public double getY(){
		return y;
	}
	
	/**
	 * Getter for angle
	 * @return angle
	 */
	public double getAngle(){
		return angle;
	}
	
	
	/**
	 * Method to get BugName
	 * @return BugName
	 */
	public String getBugName() { return BugName; }
    
	
	/**
	 * setter for blocked
	 */
    public void setBlocked(boolean b) { blocked = b; }
    
    /**
	 * getter for blocked
	 * @return blocked
	 */

    public boolean isBlocked() { return blocked; }

    
    /**
	 * getter for counter
	 * @return counter
	 */
    int getCounter() { return counter; }

    /** Repeatedly: Get permission to work; work; signal completion */
    @Override
    public void run() {
    	interpret(bugTree.getChild(3));
        interpreter.terminateBug(this);
        // Thread dies upon exit
    }

    /** Pause for a random amount of time */
    private void pause() {
        try { sleep(rand.nextInt(100)); }
        catch (InterruptedException e) {}
    }

	/**
	 * Stores values into variables using HashMap.
	 * @param variable in which value is stored.
	 * @param value to be stored.
	 */
    synchronized void store(String variable, double value){
		if(variable.equals("x")){ x = value; return;}
		else if(variable.equals("y")) {y = value; return;}
		else if(variable.equals("angle")){ angle = value; return;}
		
		HashMap<String, Double> subMap;
		int size = scopes.size();
		int i = size - 1;
		
		while(i >= 0){
			subMap = scopes.get(i);
			if(subMap.get(variable) != null){
				subMap.put(variable,value);
				return;
			}
			i--;
		}
		if(interpreter.map.get(variable) != null){
			interpreter.map.put(variable,value);
			return;
		}
		else{
			throw new RuntimeException("Variable not in the hashmap");
		}
	}
	
	/**
	 * Fetches values for variables
	 * @param variable to be fetched
	 * @return
	 */
    synchronized double fetch(String variable){
		if(variable.equals("x")) return x;
		if(variable.equals("y")) return y;
		if(variable.equals("angle")) return angle;
		HashMap<String, Double> subMap;
		int size = scopes.size();
		int i = size - 1;
		
		while(i >= 0){
			subMap = scopes.get(i);
			if(subMap.get(variable) != null){
				return subMap.get(variable);
			}
			i--;
		}
		if(interpreter.map.get(variable) != null){
			return interpreter.map.get(variable);
		}
		else{
			throw new RuntimeException("Variable " + variable + "not in the hashmap");
		}
		
	}
	
	
	/**
	 * Method to evaluate nodes using helper methods.
	 * @param tree to be evaluated
	 * @return result in double
	 */
	public double evaluate(Tree<Token> tree){
		String symbol = tree.getValue().value;
		if(symbol.equals("+")){
			return evaluateAddition(tree);
		}

		else if(symbol.equals("-")){
			return evaluateSubtraction(tree);
		}
		
		else if(symbol.equals("*")){
			return evaluateMultiplication(tree);
		}
		else if(symbol.equals("/")){
			return evaluateDivision(tree);
		}
		else if(symbol.equals("<")){
			return evaluateLessThan(tree);
		}
		else if(symbol.equals(">")){
			return evaluateGreaterThan(tree);
		}
		
		else if(symbol.equals("!=")){
			return evaluateNotEqual(tree);
		}
		
		
		else if(symbol.equals("=")){
			return evaluateEqual(tree);
		}
		
		else if(symbol.equals("<=")){
			return evaluateLessThanEqual(tree);
		}
		
		else if(symbol.equals(">=")){
			return evaluateGreaterThanEqual(tree);
		}
		
		else if(symbol.equals("case")){ 
			return evaluateCase(tree);
		}
		else if(symbol.equals("call")){
			try {
				return evaluateCall(tree);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else if(symbol.equals(".")){
			try {
				return evaluateDot(tree);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else if(tree.getValue().type == Token.Type.NUMBER){ 
			return Double.parseDouble(tree.getValue().value);
		}
			tree.print();
			System.out.println("here");
			return fetch(symbol);
		
	}
	
	
	/**
	 * Method to evaluate addition nodes.
	 * @param tree to be evaluated
	 * @return result in double
	 */
	public double evaluateAddition(Tree<Token> tree){
		if(tree.getNumberOfChildren() == 2){
			return evaluate(tree.getChild(0)) + evaluate(tree.getChild(1));
		}
		else{
			return evaluate(tree.getChild(0));
		}
	}
	
	/**
	 * Method to evaluate subtraction nodes.
	 * @param tree to be evaluated
	 * @return result in double
	 */
	public double evaluateSubtraction(Tree<Token> tree){
		if(tree.getNumberOfChildren() == 2){
			return evaluate(tree.getChild(0)) - evaluate(tree.getChild(1));
		}
		else{
			return -1 * evaluate(tree.getChild(0));
		}
	}
	
	/**
	 * Method to evaluate multiplication nodes.
	 * @param tree to be evaluated
	 * @return result in double
	 */
	public double evaluateMultiplication(Tree<Token> tree){
		return evaluate(tree.getChild(0)) * evaluate(tree.getChild(1));
	}
	
	/**
	 * Method to evaluate division nodes.
	 * @param tree to be evaluated
	 * @return result in double
	 */
	public double evaluateDivision(Tree<Token> tree){
		double denominator = evaluate(tree.getChild(1));
	    if(denominator == 0.0)
	        throw new ArithmeticException("Divide by zero is undefined!");
	    return evaluate(tree.getChild(0)) / evaluate(tree.getChild(1));
	}
	
	/**
	 * Method to evaluate less than nodes.
	 * @param tree to be evaluated
	 * @return result in double
	 */
	public double evaluateLessThan(Tree<Token> tree){
		if(evaluate(tree.getChild(0)) < evaluate(tree.getChild(1)) && Math.abs(evaluate(tree.getChild(0)) - evaluate(tree.getChild(1))) > 0.001){
			return 1.0;
		}
		else return 0.0;
	}
	
	/**
	 * Method to evaluate less than equal nodes.
	 * @param tree to be evaluated
	 * @return result in double
	 */
	public double evaluateLessThanEqual(Tree<Token> tree){
		if(Math.abs(evaluate(tree.getChild(0)) - evaluate(tree.getChild(1))) <= 0.001 || (evaluate(tree.getChild(0)) < evaluate(tree.getChild(1)))){
			return 1.0;
		}
		else return 0.0;
	}
	
	/**
	 * Method to evaluate equal  nodes.
	 * @param tree to be evaluated
	 * @return result in double
	 */
	public double evaluateEqual(Tree<Token> tree){
		if(Math.abs(evaluate(tree.getChild(0)) - evaluate(tree.getChild(1))) <= 0.001){
			return 1.0;
		}
		else return 0.0;
	}
	
	/**
	 * Method to evaluate not equal nodes.
	 * @param tree to be evaluated
	 * @return result in double
	 */
	public double evaluateNotEqual(Tree<Token> tree){
		if(Math.abs(evaluate(tree.getChild(0)) - evaluate(tree.getChild(1))) <= 0.001){
			return 0.0;
		}
		else return 1.0;
	}
	
	/**
	 * Method to evaluate greater than nodes.
	 * @param tree to be evaluated
	 * @return result in double
	 */
	public double evaluateGreaterThan(Tree<Token> tree){
		if(evaluate(tree.getChild(0)) > evaluate(tree.getChild(1)) && Math.abs(evaluate(tree.getChild(0)) - evaluate(tree.getChild(1))) > 0.001){
			return 1.0;
		}
		else return 0.0;
	}
	
	/**
	 * Method to evaluate greater than equal nodes.
	 * @param tree to be evaluated
	 * @return result in double
	 */
	public double evaluateGreaterThanEqual(Tree<Token> tree){
		if(Math.abs(evaluate(tree.getChild(0)) - evaluate(tree.getChild(1))) <= 0.001 || (evaluate(tree.getChild(0)) > evaluate(tree.getChild(1)))){
			return 1.0;
		}
		else return 0.0;
	}
	
	/**
	 * Method to evaluate case nodes.
	 * @param tree to be evaluated
	 * @return result in double
	 */
	public double evaluateCase(Tree<Token> tree){
		double result = evaluate(tree.getChild(0));
		if(result <= 0.001 && result >= -0.001){} 
		
		else{
			interpret(tree.getChild(1));	
		}
		return result;
	}
	
	/**
	 * Method to evaluate dot nodes.
	 * @param tree to be evaluated
	 * @return result in double
	 */
	public double evaluateDot(Tree<Token> tree) throws Exception {
		if(!(tree.getChild(1).getValue().value.equals("x") || tree.getChild(1).getValue().value.equals("y") || tree.getChild(1).getValue().value.equals("angle") || interpreter.bugs.get(tree.getChild(0).getValue().value).map.get(tree.getChild(1).getValue().value) != null)){
			throw new Exception("Cannot Access Variable.");
		}
	
		double val = (interpreter.bugs.get(tree.getChild(0).getValue().value)).fetch(tree.getChild(1).getValue().value);
		
		return val;
	}
	/**
	 * Method to interpret nodes using helper methods.
	 * @param tree to be interpreted
	 */
	public void interpret(Tree<Token> tree){
		String node = tree.getValue().value;
	
		
		if(node.equals("list")){
			interpretList(tree);
		}
		
		else if(node.equals("var")){
			interpretVar(tree);
		}
		else if(node.equals("initially")){
			interpretInitially(tree);
		}
		else if(node.equals("block")){
			interpretBlock(tree);
		}
		else if(node.equals("move")){
			interpreter.getWorkPermit(this);
			interpretMove(tree);
			interpreter.completeCurrentTask(this);
		}
		else if(node.equals("moveto")){
			interpreter.getWorkPermit(this);
			interpretMoveTo(tree);
			interpreter.completeCurrentTask(this);
		}
		else if(node.equals("turn")){
			interpreter.getWorkPermit(this);
			interpretTurn(tree);		
			interpreter.completeCurrentTask(this);
		}
		
		else if(node.equals("turnto")){
			interpreter.getWorkPermit(this);
			interpretTurnTo(tree);
			interpreter.completeCurrentTask(this);
		}
		else if(node.equals("line")){
			interpreter.getWorkPermit(this);
			interpretLine(tree);
			interpreter.completeCurrentTask(this);
		}
		else if(node.equals("assign")){
			interpretAssign(tree);
		}
		else if(node.equals("loop")){
			interpretLoop(tree);
		}
		else if(node.equals("exit")){
			interpretExit(tree);
		}
		
		else if(node.equals("switch")){
			interpretSwitch(tree);
		}
		
		else if(node.equals("color")){
			interpretColor(tree);
		}
		else if(node.equals("function")){
			interpretFunction(tree);
		}
	
		else if(node.equals("return")){
			interpretReturn(tree);
		}
		
		
	}
	

	
	/**
	 * Method to interpret list nodes.
	 * @param tree to be interpreted
	 */
	public void interpretList(Tree<Token> tree){
		Tree<Token> listNode;
		int size = tree.getNumberOfChildren();
		int i = 0;
		while(i < size){
			
			listNode = tree.getChild(i);
			interpret(listNode);
			i++;
		}
	}
	
	/**
	 * Method to interpret var nodes.
	 * @param tree to be interpreted
	 */
	public void interpretVar(Tree<Token> tree){
		Tree<Token> varNode;
		int size = tree.getNumberOfChildren();
		int i = 0;
		while(i < size){
			
			varNode = tree.getChild(i);
			if(!varNode.getValue().value.equals("x") && !varNode.getValue().value.equals("y") && !varNode.getValue().value.equals("angle")){
				map.put(varNode.getValue().value, 0.0);
			}
			else{
				try {
					throw new Exception("x , y and angle should be global.");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			i++;
		}
	}
	
	/**
	 * Method to interpret initial block nodes.
	 * @param tree to be interpreted
	 */
	public void interpretInitially(Tree<Token> tree){
		interpret(tree.getChild(0));
		
	}
	
	/**
	 * Method to interpret block  nodes.
	 * @param tree to be interpreted
	 */
	public void interpretBlock(Tree<Token> tree){
		Tree<Token> commandNode;
		int size = tree.getNumberOfChildren();
		int i = 0;
		while(i < size){
			if(flag == 1){
				break;
			}
		
			commandNode = tree.getChild(i);	
			commandNode.print();
			System.out.println();
			if(commandNode.getValue().value.equals("call")){
				try {
					result = evaluate(commandNode);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			interpret(commandNode);
			i++;
		}
	}
	
	/**
	 * Method to interpret move nodes.
	 * @param tree to be interpreted
	 */
	public synchronized void interpretMove(Tree<Token> tree){
		double displacement = evaluate(tree.getChild(0));
		Command command = new Command();
		command.x1 = fetch("x");
		command.y1 = fetch("y");
		command.color = BugColor;
		store("x", fetch("x") + displacement * Math.cos(angle*Math.PI / 180));
		store("y", fetch("y") - displacement * Math.sin(angle*Math.PI / 180));
		command.x2 = fetch("x");
		command.y2 = fetch("y");
		interpreter.cmd.add(command);
		
	}
	
	/**
	 * Method to interpret move to nodes.
	 * @param tree to be interpreted
	 */
	public synchronized void interpretMoveTo(Tree<Token> tree){
		Command command = new Command();
		command.x1 = fetch("x");
		command.y1 = fetch("y");
		command.color = BugColor;
		store("x",evaluate(tree.getChild(0)));
		store("y",evaluate(tree.getChild(1)));
		command.x2 = fetch("x");
		command.y2 = fetch("y");
		interpreter.cmd.add(command);
	}
	

	
	public synchronized void interpretLine(Tree<Token> tree){
		Command command = new Command();
		command.x1 = evaluate(tree.getChild(0));
		command.y1 = evaluate(tree.getChild(1));
		command.x2 = evaluate(tree.getChild(2));
		command.y2 = evaluate(tree.getChild(3));
		command.color = BugColor;
		assert(command != null);
		interpreter.cmd.add(command);
		}
	
	/**
	 * Method to interpret turn nodes.
	 * @param tree to be interpreted
	 */
	public void interpretTurn(Tree<Token> tree){
		double turnAngle = evaluate(tree.getChild(0));
		double totalAngle = turnAngle + fetch("angle");
		if(totalAngle < 0)
			store("angle", (totalAngle % 360.0) + 360.0);
		else
			store("angle", (totalAngle % 360.0));
	}
	
	/**
	 * Method to interpret turn to nodes.
	 * @param tree to be interpreted
	 */
	public void interpretTurnTo(Tree<Token> tree){
		double turnToAngle = evaluate(tree.getChild(0));
		if(turnToAngle < 0){
			store("angle", (turnToAngle % 360.0) + 360.0);
		}
		else{
			store("angle", turnToAngle % 360.0);
		}
	}
	
	/**
	 * Method to interpret assign nodes.
	 * @param tree to be interpreted
	 */
	public void interpretAssign(Tree<Token> tree){
		String name = tree.getChild(0).getValue().value;
		if(!name.equals("call"))
			fetch(name);
		double value = evaluate(tree.getChild(1));
		store(name, value);
	}
	
	/**
	 * Method to interpret loop nodes.
	 * @param tree to be interpreted
	 */
	public void interpretLoop(Tree<Token> tree){
		Tree<Token> block;
		while(true){
			if(flag == 1){
				flag = 0;
				break;
			}
			block = tree.getChild(0);
			interpret(block);

		}
	}
	
	/**
	 * Method to interpret exit nodes.
	 * @param tree to be interpreted
	 */
	public void interpretExit(Tree<Token> tree){
		Tree<Token> condition = tree.getChild(0);
		double result = evaluate(condition);
		if(result <= 0.001 && result >= -0.001){}
		else{
			flag = 1;
		}
	}
	
	/**
	 * Method to interpret switch nodes.
	 * @param tree to be interpreted
	 */
	public void interpretSwitch(Tree<Token> tree){
		int size = tree.getNumberOfChildren();
		int i = 0;
		double result;
		while(i < size){
			result = evaluate(tree.getChild(i));
			if(result > 0.001 || result < -0.001){
				break;
			}
			i++;
		}
	}
	
	/**
	 * Method to interpret color nodes.
	 * @param tree to be interpreted
	 */
	public void interpretColor(Tree<Token> tree){
		String colorName = tree.getChild(0).getValue().value; 
		if(colorName.equals("black")){
			BugColor = Color.BLACK;
		}
		else if(colorName.equals("blue")){
			BugColor = Color.BLUE;
		}
		else if(colorName.equals("cyan")){
			BugColor = Color.CYAN;
		}
		else if(colorName.equals("darkGray")){
			BugColor = Color.DARK_GRAY;
		}
		else if(colorName.equals("gray")){
			BugColor = Color.GRAY;
		}
		else if(colorName.equals("green")){
			BugColor = Color.GREEN;
		}
		else if(colorName.equals("lightGray")){
			BugColor = Color.LIGHT_GRAY;
		}
		else if(colorName.equals("magenta")){
			BugColor = Color.MAGENTA;
		}
		else if(colorName.equals("orange")){
			BugColor = Color.ORANGE;
		}
		else if(colorName.equals("pink")){
			BugColor = Color.PINK;
		}
		else if(colorName.equals("red")){
			BugColor = Color.RED;
		}
		else if(colorName.equals("white")){
			BugColor = Color.WHITE;
		}
		else if(colorName.equals("yellow")){
			BugColor = Color.YELLOW;
		}
		else if(colorName.equals("brown")){
			BugColor = new Color(165,42,42);
		}
		else if(colorName.equals("purple")){
			BugColor = new Color(128,0,128);
		}
		else if(colorName.equals("none")){
			BugColor = null;
		}
		else{
			throw new RuntimeException("Invalid Color");
		}
		
	}
	
	/**
	 * Method to interpret function nodes.
	 * @param tree to be interpreted
	 */
	public void interpretFunction(Tree<Token> tree){
		String functionName = tree.getChild(0).getValue().value;
		functions.put(functionName, tree);
	}
	
	/**
	 * Method to interpret call nodes.
	 * @param tree to be interpreted
	 */
	public synchronized double evaluateCall(Tree<Token> tree) throws Exception{
		String functionName = tree.getChild(0).getValue().value;
		if(functionName.equals("distance")){
			Tree<Token> otherBug = tree.getChild(1).getChild(0);
			Bug distanceBug = interpreter.bugs.get(otherBug.getValue().value);
			double distance = 0;
			distance = Math.sqrt((x - distanceBug.getX())*(x - distanceBug.getX())+(y - distanceBug.getY())*(y - distanceBug.getY()));
			result = distance;
			return result;
		}
		
		if(functionName.equals("direction")){
			Tree<Token> otherBug = tree.getChild(1).getChild(0);
			Bug directionBug = interpreter.bugs.get(otherBug.getValue().value);
			double direction = 0;
			direction = Math.atan((y - directionBug.getY())/(x - directionBug.getX())); 
			result = direction;
			return result;
		}
		
		HashMap<String, Double> localMap = new HashMap<>();
		scopes.push(localMap);
		Tree<Token> varNode;
		
		Tree<Token> formalParameters;
		Tree<Token> funcTree = functions.get(functionName);
		if(funcTree != null){
			Tree<Token> childTree = funcTree.getChild(1);
			Tree<Token> treeVar = tree.getChild(1);
			int size = childTree.getNumberOfChildren();
			int sizeCall = treeVar.getNumberOfChildren();
			
			if(size != sizeCall){
				throw new Exception("Incorrect Number of Arguments.");
			}
			
			int i = 0;
			while(i < size){
				
				varNode = childTree.getChild(i);
				formalParameters = treeVar.getChild(i);
				localMap.put(varNode.getValue().value, evaluate(formalParameters));
				i++;
			}
		}
		
		else{
			
			funcTree = interpreter.functions.get(functionName);
			if(funcTree != null){
				Tree<Token> childTree = funcTree.getChild(1);
				Tree<Token> treeVar = tree.getChild(1);
				
				int size = childTree.getNumberOfChildren();
				int sizeCall = treeVar.getNumberOfChildren();
				
				if(size != sizeCall){
					throw new Exception("Incorrect Number of Arguments.");
				}
				
				int i = 0;
				while(i < size){
					
					varNode = childTree.getChild(i);
					formalParameters = treeVar.getChild(i);
					
					localMap.put(varNode.getValue().value, evaluate(formalParameters));
					i++;
				}
			}
			
			else{
				throw new RuntimeException("Function Not Found.");
			}
		}
		
		
		Tree<Token> blockTree = funcTree.getChild(2);		
		interpret(blockTree);
		double temp = result;
		result = 0;
		return temp;
	}
	
	/**
	 * Method to interpret return nodes.
	 * @param tree to be interpreted
	 */
	public void interpretReturn(Tree<Token> tree){
		result = evaluate(tree.getChild(0));
		scopes.pop();
	}
}
