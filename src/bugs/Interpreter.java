package bugs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.Stack;

import tree.Tree;

public class Interpreter extends Thread{
	
	HashMap<String, Double> map;
	HashMap<String, Tree<Token>> functions;
	HashMap<String, Bug> bugs;
	ArrayList<Bug> Bugs;
	static String ast;
	private int numberOfBugs;
	ArrayList<Command> cmd;
	int delay;
	static int blocked;
	
	
	/**
	 * Constructor for Interpreter Class.
	 */
	Interpreter(){
		
		ast = BugsGui.prog;
		numberOfBugs = 0;
		delay = 500;
		blocked = 0;
		
		synchronized(this){	
			map = new HashMap<String, Double>();
			functions = new HashMap<String, Tree<Token>>();
			Bugs =  new ArrayList<Bug>();
			bugs = new HashMap<String, Bug>();
			cmd = new ArrayList<Command>();	
		}

	}
	
	private boolean verbose = false;

	 /** Coordinates all Bugs */
	@Override
	public void run(){

		while (Bugs.size() > 0) {
        	try {
				sleep(delay);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
            unblockAllBugs();
        }

	}
	
    /** Creates and coordinates all Bugs */
    public void startThings() {
    	interpretProgram();

        for (int i = 0; i < Bugs.size(); i++) {
        	Bugs.get(i).setBlocked(true);
        }
        
        for (int i = 0; i < Bugs.size(); i++) {
        	Bugs.get(i).start();
        }
        
    }

    /** Called by a Bug to try to get permission to work */
    synchronized void getWorkPermit(Bug bug) {
        String bugName = bug.getBugName();
        verbosePrint("    bug " + bugName + 
                     " is trying to get a work permit.");
        while (bug.isBlocked()) {
            try {
            	
                verbosePrint("    Bug " + bugName +
                             " is waiting.");
                wait();
            }
            catch (InterruptedException e) {
                verbosePrint("    Bug " + bugName +
                             " has been interrupted.");
            }
        }
        verbosePrint("Bug " + bugName + " got a work permit.");
    }
    
    /** Called by a Bug to indicate completion of work */
    synchronized void completeCurrentTask(Bug bug) {
        bug.setBlocked(true);
        verbosePrint("  Bug " + bug.getBugName() +
                     " has done work and is now blocked.");
        notifyAll();
    }

    /** Called by this TaskManager to allow all Bugs to work */
    synchronized void unblockAllBugs() {
        verbosePrint("    Master is trying to reset all.");
        while (countBlockedBugs() < Bugs.size()) {
            try {
                verbosePrint("    Master is waiting for all Bugs" +
                             " to be blocked.");
                wait();
            }
            catch (InterruptedException e) {
                verbosePrint("    Master has been interrupted.");
            }
        }
        for (Bug bug : Bugs) {
        	if(blocked == 0)
            bug.setBlocked(false);
        };
        verbosePrint("\nMaster has unblocked all Bugs.");
        notifyAll();  
        
    }
    
    /** Counts the number of currently blocked Bugs; since this is
     *  called from a synchronized method, it is effectively synchronized */
    private int countBlockedBugs() {
        int count = 0;
        for (Bug bug : Bugs) {
            if (bug.isBlocked()) {
                count++;
            }
        }
        return count;
    }
    
    /** Called by a bug to die; synchronized because it modifies the
     * ArrayList of bugs, which is used by other synchronized methods. */
    synchronized void terminateBug(Bug bug) {
        Bugs.remove(bug);
        bugs.remove(bug.BugName);
        System.out.println("* Bug " + bug.getBugName() +
                           " has terminated.");
    }

    
    /**
     * Method for printing.
     */
    private void printResultsSoFar() {
        for (Bug bug : Bugs) {
            System.out.print("Bug " + bug.getBugName() + 
                             " -> " + bug.getCounter() + "    ");
        }
        System.out.println();
    }
    
    /**
     * Method for printing if boolean is true.
     */
    private void verbosePrint(String s) {
        if (verbose) {
            System.out.println(s);
        }
    }
	
    
    /**
	 * Method to interpret nodes using helper methods.
	 * @param tree to be interpreted
	 */
	public synchronized void interpret(Tree<Token> tree){
		String node = tree.getValue().value;
		if(node.equals("Allbugs")){
			interpretAllbugs(tree);
		}
		else if(node.equals("list")){
			interpretList(tree);
		}
		else if(node.equals("function")){
			interpretFunction(tree);
		}
		else if(node.equals("var")){
			interpretVar(tree);
		}
		else if(node.equals("Bug")){
			interpretBug(tree);
		}		
	}
	
	/**
	 * Method to interpret AllBugs nodes.
	 * @param tree to be interpreted
	 */
	public void interpretAllbugs(Tree<Token> tree){
		if (tree.getNumberOfChildren() < 2) return;
		interpret(tree.getChild(0));
		interpret(tree.getChild(1));
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
	 * Method to interpret function nodes.
	 * @param tree to be interpreted
	 */
	private void interpretFunction(Tree<Token> tree){
		String functionName = tree.getChild(0).getValue().value;
		functions.put(functionName, tree);
	}
	
	/**
	 * Method to interpret var nodes.
	 * @param tree to be interpreted
	 */
	private void interpretVar(Tree<Token> tree){
		Tree<Token> varNode;
		int size = tree.getNumberOfChildren();
		int i = 0;
		while(i < size){
			
			varNode = tree.getChild(i);
			map.put(varNode.getValue().value, 0.0);
			i++;
		}
	}
	
	/**
	 * Method to interpret program nodes.
	 * @param tree to be interpreted
	 */
	private void interpretProgram(){
		ast = BugsGui.prog;
		Parser parser = new Parser(ast);
		parser.isProgram();
		Tree<Token> programTree = parser.stack.peek();
		if (programTree.getNumberOfChildren() < 2) return;		
		interpret(programTree.getChild(0));
		interpret(programTree.getChild(1));
	}
	
	
	/**
	 * Method to interpret bug nodes.
	 * @param tree to be interpreted
	 */
	private void interpretBug(Tree<Token> tree){
		Bug bug = new Bug(tree,this);
		bugs.put(tree.getChild(0).getValue().value, bug);
		Bugs.add(bug);
		numberOfBugs++;
	}
	
}