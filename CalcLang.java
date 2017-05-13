import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.HashMap;

/*
 * This program asks for an input file.
 * The input then calls a lexer to make sure all
 * tokens are in the language. Then it calls a Parser
 * to ensure the grammar is correct.
 * Then it executes the language.
 * TODO exit clean when hashMap has no value stored for variable
 */

public class CalcLang {
	/*Holds a list of variables as key and their values as value*/
	private static HashMap<String, Double> varMap = new HashMap<>();
	
	public static void main (String [] args) {
		Scanner input = new Scanner(System.in);
		String fileName = getFile(input);
		System.out.println("Calling lexer...");
		Lexer attemptLex = new Lexer(fileName);
		ArrayList<Token> lexerList = attemptLex.getList();
		System.out.println("List of tokens created successfully, all words are in the language");
		System.out.println("Calling parser...");
		CalcParser attemptParser = new CalcParser(attemptLex);
		Node parseTreeRoot = attemptParser.getRoot();
		System.out.println("Parse Tree created successfully, all statements are in the grammar");
		System.out.println("Executing the code...\n");
		executeCalcLang(lexerList, parseTreeRoot, input);
		System.out.println("\n- Execution Complete -");
		input.close();
	}
	
	
	/* This gets the name of an input file from the user
	 * @return the file to read
	 */
	private static String getFile(Scanner nameIn) {
		System.out.println("enter the name of the file you want to read: ");
		String fileName = nameIn.next();
		return fileName;
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////
	/* This executes the code
	 * @param lexList the list of tokens returned by the Lexer
	 * @param currentRoot the current <statements> node
	 */
	private static void executeCalcLang(ArrayList<Token> lexList,
			Node currentRoot, Scanner input) {
		while( currentRoot.hasSecond()) { //eval another statement.
			getNextStatement(currentRoot.getFirst(), input); //if DNE then done
			currentRoot = currentRoot.getSecond();
		}
	}
	
	/* Go through the tree until you find the next statement.
	 * Will always be the left child of a statements node.
	 * <statement> -> IDENTIFIER = <expression>
	 * <statement> -> message STRING 
     * <statement> -> print <expression>
	 * <statement> -> newline
	 * <statement> -> input STRING IDENTIFIER
	 * @param currentRoot The starting node 
	 */
	private static void getNextStatement(Node currentNode, Scanner input) {
		Node firstChild = currentNode.getFirst();
		if(firstChild.getKind().equals("variable")) { 	//ID = E
			//map the node using it string name as the key and what it evals to as the double;
			varMap.put(firstChild.getInfo(), evalExpression(currentNode.getThird()));
		} else if (firstChild.getInfo().equals("msg")) { // message "STRING"
			System.out.println(currentNode.getSecond().getInfo());
		} else if (firstChild.getInfo().equals("print")) { //print <expression>
			System.out.print(evalExpression(currentNode.getSecond()));
		} else if (firstChild.getInfo().equals("newline")) { //newline
			System.out.print("\n");
		} else if (firstChild.getInfo().equals("show")) { //show <expression>
			System.out.print(evalExpression(currentNode.getSecond()));
		} else if ((firstChild.getInfo().equals("msg"))) {
			System.out.print(currentNode.getSecond().getInfo());
		}
		else if (firstChild.getInfo().equals("input")) { //input STRING IDENTIFIER
			System.out.print(currentNode.getSecond().getInfo());
			try {
				System.out.print(currentNode.getSecond().getInfo());
				String inputStr = input.next();
				varMap.put(currentNode.getThird().getInfo(), Double.parseDouble(inputStr));
			} catch (NullPointerException e) {
				printError("input not followed by a string then a variable");
			} catch (Exception e) { //cannot parse input to string?
				printError("input must be numeric! " + e);
			}
		} else {
			printError("Unknown type of node found"
					+ " in getNextStatement() " + currentNode.getInfo() + " firstChild I: "
					+ currentNode.getFirst().getInfo() + " K:"  + currentNode.getFirst().getKind());
		}
	}
	
	/* Evaluate an expression.
	 * E -> T
	 * E -> T + E
	 * E -> T - E
	 * Left child is always a T node, +, and - are terminals.
	 * @param currentRoot The starting node 
	 */
	private static Double evalExpression(Node currentNode) {
		double expressionValue = evalT(currentNode.getFirst());
		//see if has seconf children
		try {
			if(currentNode.getSecond().getKind().equals("+"))
				return  expressionValue + evalExpression(currentNode.getThird());
			else if (currentNode.getSecond().getKind().equals("-"))
				return expressionValue - evalExpression(currentNode.getThird());
		} catch (NullPointerException e) {} //used if trying to access null children
		return expressionValue;
	}
	
	/*  Evaluate a T node
	 *  T -> F
	 *  T -> F * T
	 *  T -> F / T
	 *  @param currentRoot The starting node 
	 */
	private static Double evalT(Node currentNode) {
		double fValue = evalF(currentNode.getFirst());
		try {
			if(currentNode.getSecond().getKind().equals("*"))
				return fValue * evalT(currentNode.getThird());
			else if (currentNode.getSecond().getKind().equals("/"))
				return fValue / evalT(currentNode.getThird());
		} catch (NullPointerException e) { //used if trying to access null children
		}
		return fValue;
	}
	
	/* Evaluate a F node
	 * F -> N
	 * F -> IDENTIFIER
	 * F -> (E)
	 * F -> - F
	 * F -> IDENTIFIER ( E )
	 * F -> BIFN ( E )
	 * @param currentRoot The starting node 
	 */
	private static Double evalF(Node currentNode) {		
		try { // F -> ( E )
			if (currentNode.getFirst().getKind().equals("(")) // F -> ( E )
				return evalExpression(currentNode.getSecond());
		} catch (NullPointerException e) {} //used if trying to access null children
		
		try { // F -> ID ( E )
			if (currentNode.getFirst().getKind().equals("variable")
						&& currentNode.getSecond().getKind().equals("(")) // F -> ID ( E )
				return varMap.get(currentNode.getFirst().getInfo()) 
						* evalExpression(currentNode.getThird());
		} catch (NullPointerException e) {} //used if trying to access null children
		
		if(currentNode.getFirst().getKind().equals("number")) // F -> N
			return Double.parseDouble(currentNode.getFirst().getInfo());
		
		else if (currentNode.getFirst().getKind().equals("-")) // F -> - F
			return -1 * evalF(currentNode.getSecond());
				
		
		else if (currentNode.getFirst().getKind().equals("variable")) { // F -> ID
			return varMap.get(currentNode.getFirst().getInfo());
				
		} else if (currentNode.getFirst().getKind().equals("function")) { // F -> BIFN ( E )
			return evalFunction(currentNode.getFirst(), currentNode.getThird());
		}
		
		else
			printError("unknown node in evalF found " + " Info: " + currentNode.getInfo());
		
		return null;			
	}
	
	/* Evaluates a function.
	 * Implemented functions are sqrt, cos, and sin.
	 * @param fnctNode The node that holds the function type.
	 * @param exprNode the node that holds the expression. 
	 * @return what the function performs on the express evaluates to.
	 */
	private static Double evalFunction(Node fnctNode, Node exprNode) {
		if (fnctNode.getInfo().equals("sqrt"))
			return Math.sqrt(evalExpression(exprNode));
		else if (fnctNode.getInfo().equals("sin"))
			return Math.sin(evalExpression(exprNode));
		else if (fnctNode.getInfo().equals("cos"))
			return Math.sin(evalExpression(exprNode));
		else {
			printError("Unknown function found in evalFunction " + fnctNode.getInfo());
			return null;
		}
	}

     /* Prints an error message and kills the program.
      * @param message The error message to print.
      */
     private static void printError (String message) {
    	 System.out.println (message);
    	 System.exit(1);
     }
	
}