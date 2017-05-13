import java.util.ArrayList;
//TODO REFACTOR PARSE METHODS
/* A parser of CalcLand.
 * CFG for CalcLang:
 *
 *<statements> -> <statement>
 *<statements> -> <statement> <statements>
 *<statement> -> IDENTIFIER = <expression>
 *<statement> -> message STRING 
 *<statement> -> print <expression>
 *<statement> -> newline
 *<statement> -> input STRING IDENTIFIER
 *
 * E instead of <expression>, T, F, V, N  (see page 20)
 * E -> T
 * E -> T + E
 * E -> T - E
 * T -> F
 * T -> F*T
 * T -> F/T
 * F -> N
 * F -> IDENTIFIER
 * F -> (E)  
 * F -> - F
 * F -> IDENTIFIER ( E )
 * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Context free grammar:
 * (E = ) -> -> ->
 * V = E -> SHOW E (show) -> MESSAGE STRING (keyword is "msg")
 * 	-> INPUT STRING V (STRING is the token for a "whatever") 
 * 		-> NEWLINE (use E instead of , etc.)
 * 
 * E -> T
 * E -> T + E | T - E
 * T -> F
 * T -> F * T | F / T
 * F -> N
 * (from lexical phase: number)
 * F -> V
 * (from lexical phase: variable)
 * F -> (E)
 * F -> - F
 * F -> BIFN ( E )
 * 
 * @author Taylor Woehrle
 */

public class CalcParser {
	static /*The list of tokens found by a lexer to put into a parse tree.*/
	ArrayList<Token> tokenList;
	/*The current tree root.*/
	static Node gCurrentRoot;
	/*The currentIndex of the lexer*/
	 int gCurrentIndex;
	
	 public Node getRoot() {
		 return gCurrentRoot;
	 }
	/* The constructor for the parseTree*/
	CalcParser(final Lexer calcLex) {
		gCurrentIndex = 0;
		tokenList = calcLex.getList();
		gCurrentRoot = parseStatements();
	}
	
	/* Parses a statements node.
	 * Note: statements nodes are different than statement nodes.
	 *<statements> -> <statement>
	 *<statements> -> <statement> <statements>
	 */
	private Node parseStatements() {
		Node currentNode = new Node(new Token("statements", "statements"));
		//check if there are any more tokens to be processed
		try {
			tokenList.get(gCurrentIndex);
		} catch (IndexOutOfBoundsException e) { //if no tokens are remaining in the list, return
			return currentNode;
		}
		//set first child
		currentNode.setFirst(parseStatement());
		//check again to see if more statements are needed, if so set second child
		try {
			currentNode.setSecond(parseStatements());
		} catch (IndexOutOfBoundsException e) { //if no tokens are remaining in the list, return
			return currentNode;
		}
		return currentNode;
	}
	
	/*
	 * Parses a statementNode
	 * <statement> -> IDENTIFIER = <expression>
 	 * <statement> -> message STRING
 	 * <statement> -> show IDENTIFIER
 	 * <statement> -> print <expression>
 	 * <statement> -> newline
 	 * <statement> -> input STRING IDENTIFIER
 	 */
	private Node parseStatement() {
		try {
			tokenList.get(gCurrentIndex);
		} catch (IndexOutOfBoundsException e) { //if no tokens are remaining print error
			System.out.println(e);
			System.exit(0);
		}
		Token currentToken = tokenList.get(gCurrentIndex); //get the current Token
		Node currentNode = new Node(new Token("statement", "statement"));
		
		if (currentToken.getKind().equals("variable")) { //IDENTIFIER = <expression>
			try {
				gCurrentIndex ++; //increment since terminal = to be added
				Token secondToken = tokenList.get(gCurrentIndex);
				gCurrentIndex ++; //increment to see if expression exists 
				Token thirdToken = tokenList.get(gCurrentIndex);
				if (secondToken.getKind().equals("=")) {
					currentNode.setFirst(new Node(currentToken));
					currentNode.setSecond(new Node(secondToken));
					currentNode.setThird(parseExpression());
				} else
					printError("Expression parsing error: variable not followed by an = and an expression: " +
				currentToken.toString() + " at Index: " + gCurrentIndex);
			}
			catch (IndexOutOfBoundsException e) {
				printError("Expression parsing error: variable not followed by an = and an expression: ");
			}
			
		} else if (currentToken.getDetails().equals("msg")) { // <statement> -> message STRING
			gCurrentIndex ++;
			Token secondToken = tokenList.get(gCurrentIndex);
			gCurrentIndex ++; //increment second time since terminal msg and strings added
			currentNode.setFirst(new Node(currentToken));
			if (!secondToken.getKind().equals("string")) {
				printError ("msg followed by a " + secondToken.getKind()
											+ " token instead of a string");
			}
			currentNode.setSecond(new Node(secondToken));
		} else if (currentToken.getDetails().equals("print")
				|| currentToken.getDetails().equals("show")) { //<statement> -> print <expression
			currentNode.setFirst(new Node(currentToken));
			gCurrentIndex ++; //increment index terminal print added
			currentNode.setSecond(parseExpression());
		} else if (currentToken.getDetails().equals("newline")) { //<statement> -> newline
			currentNode.setFirst(new Node(currentToken));
			gCurrentIndex ++; //increment index terminal newline added
		} else if (currentToken.getDetails().equals("input")) { //<statement> -> input STRING IDENTIFIER
			currentNode.setFirst(new Node(currentToken));
			gCurrentIndex ++; //increment terminal input added as first Child
			try {
				if( tokenList.get(gCurrentIndex).getKind().equals("string")) {
					currentNode.setSecond(new Node(tokenList.get(gCurrentIndex)));
					gCurrentIndex ++; // terminal string added as second Child
					if(tokenList.get(gCurrentIndex).getKind().equals("variable")) {
						currentNode.setThird(new Node (tokenList.get(gCurrentIndex)));
						gCurrentIndex ++; //added a terminal variable token
					} else
						printError("input prompt was not followed by an indentifier");
				}
			} catch (IndexOutOfBoundsException e) {
				printError("Input token not followed by a string and identifier");
			}
	 	} else { //error
		printError("Invalid token during parseStatement() phase: " + currentToken.toString() + " index: " + gCurrentIndex +
				" next token is " + tokenList.get(gCurrentIndex + 1).toString());
	 	}
		return currentNode;
	}

	/* Parses an expression
	 * E instead of <expression>, T, F, V, N  (see page 20)
	 * E -> T
	 * E -> T + E
	 * E -> T - E
	 */
	private Node parseExpression() {
		Node currentNode = new Node(new Token("expression", "expression"));
		try {
			tokenList.get(gCurrentIndex);
		} catch (IndexOutOfBoundsException e) {
			printError("null value for Expression parsing found");
		}
		currentNode.setFirst(parseT());
		// CHECK FOR ANY + or - SYMBOLS
		try {
			if(tokenList.get(gCurrentIndex).getKind().equals("+")) { //E -> T + E
				currentNode.setSecond(new Node(tokenList.get(gCurrentIndex)));
				gCurrentIndex ++; //added terminal token
				try {
					//check for another expression following +
					tokenList.get(gCurrentIndex);
					currentNode.setThird(parseExpression());
				} catch (IndexOutOfBoundsException e){
					printError("+ not followed by an expression");
				}
			} else if (tokenList.get(gCurrentIndex).getKind().equals("-")) { //E -> T - E
				currentNode.setSecond(new Node (tokenList.get(gCurrentIndex)));
				gCurrentIndex ++; //added terminal token
				try {
					//check for another expression following -
					tokenList.get(gCurrentIndex);
					currentNode.setThird(parseExpression());
				} catch (IndexOutOfBoundsException e) {
					printError("- not followed by an expression");
				}
			}
		} catch (IndexOutOfBoundsException e) {
		}
		return currentNode;
	}

	/*Parses T in the defined CFL
	 * 
	 * T -> F
	 * T -> F * T
	 * T -> F / T
	 */
	private Node parseT() {
		Node currentNode = new Node(new Token("T", "T"));
		try {
			tokenList.get(gCurrentIndex);
		} catch (IndexOutOfBoundsException e) {
			printError("null value for T parsing found");
		}
		currentNode.setFirst(parseF());
		// CHECK FOR ANY * or / SYMBOLS
		try {
			if(tokenList.get(gCurrentIndex).getKind().equals("*")) {
				currentNode.setSecond(new Node(tokenList.get(gCurrentIndex)));
				gCurrentIndex ++; //added terminal token
				try {
					//check for another expression following *
					tokenList.get(gCurrentIndex);
					currentNode.setThird(parseT());
				} catch (IndexOutOfBoundsException e){
					printError("* not followed by an expression");
				}
			} else if (tokenList.get(gCurrentIndex).getKind().equals("/")) {
				currentNode.setSecond(new Node (tokenList.get(gCurrentIndex)));
				gCurrentIndex ++; //added terminal token
				try {
					//check for another expression following /
					tokenList.get(gCurrentIndex);
					currentNode.setThird(parseT());
				} catch (IndexOutOfBoundsException e){
					printError("/ not followed by an expression");
				}
			}
		} catch (IndexOutOfBoundsException e) {
		}
		return currentNode;
	}
	
	/*Parses F in the defined CFL
	 * F -> N
 	 * F -> IDENTIFIER
 	 * F -> (E)  
 	 * F -> - F
 	 * F -> IDENTIFIER ( E )
 	 * F -> BIFN ( E )
	 */
	private Node parseF() {
		//check to see if null
		try {
			tokenList.get(gCurrentIndex);
		} catch (IndexOutOfBoundsException e) {
			printError("null value for F parsing found");
		}
		Token currentToken = tokenList.get(gCurrentIndex);
		Node currentNode = new Node (new Token("F", "F"));

		if (currentToken.getKind().equals("number")) { //F -> N
			currentNode.setFirst(new Node (currentToken));
			gCurrentIndex ++; //added terminal number
		} else if (currentToken.getKind().equals("variable")) { //F -> IDENTIFIER | IDENTIFIER ( E ) 
			currentNode.setFirst(new Node(currentToken));
			gCurrentIndex ++; //added terminal variable
			try {
				if (tokenList.get(gCurrentIndex).getKind().equals("(")) {
					currentNode = parseFParen(tokenList.get(gCurrentIndex), new Node (tokenList.get(gCurrentIndex)));
				}
			} catch (IndexOutOfBoundsException e) { //if not do nothing
			}
		} else if (currentToken.getKind().equals("(")) { //F -> (E)
			currentNode = parseFParen(currentToken, currentNode);
		} else if (currentToken.getKind().equals("-")) {	// F -> - F
			currentNode.setFirst(new Node(currentToken));
			gCurrentIndex ++; //added terminal - token
			try {
				tokenList.get(gCurrentIndex);
				currentNode.setSecond(parseF());
			} catch (IndexOutOfBoundsException e) {	
				printError("Error in ParseF(). No number folloing a -"); 
			}
		} else if (currentToken.getKind().equals("function")) { //F -> BIFN ( E )
			currentNode.setFirst(new Node(currentToken));
			gCurrentIndex ++; //added terminal variable
			try {
				currentNode.setSecond(new Node (currentToken));
				gCurrentIndex ++; //added terminal '('
				currentNode.setThird(parseExpression()); // get expression
				//check for a ) following expresssion
				if(tokenList.get(gCurrentIndex).getKind().equals(")")) {
					currentNode.setFourth(new Node (tokenList.get(gCurrentIndex)));
					gCurrentIndex ++; //added terminal )
				} else {
					printError("Unclosed expression, expected a ) at index " +gCurrentIndex + " got:" +
							tokenList.get(gCurrentIndex).toString());
				return null;
			    }
			} catch (IndexOutOfBoundsException e) { 
				printError("cannot end file with a BIFN without params"); 
			}
		} else { //INVALID TOKEN FOR F
			printError("Invalid token for F phase of parsing: " + currentToken.toString() + " Index: " + gCurrentIndex);
		}
		return currentNode;
	}
	
	/* Works with parseF to get ( E )
	 *  @param currentToken the first terminal val.
	 *  @param currentNode The current F node.
	 */
	public Node parseFParen(Token currentToken, Node currentNode) {
		if(!(currentToken.getKind().equals("(")) || !(currentNode.getKind().equals("F")))
			printError("parseFParen called without currentToken or currentNode"
					+ "being '(' called with " +currentToken.getKind() + " instead Index:"
					+gCurrentIndex);
		currentNode.setFirst(new Node (currentToken));
		gCurrentIndex ++; //added terminal '('
		currentNode.setSecond(parseExpression()); // get expression
		//check for a ) following expresssion
		try {
			tokenList.get(gCurrentIndex);
		} catch (IndexOutOfBoundsException e) {
			printError("Unclosed expression, expected a ) at index " +gCurrentIndex + " got:" +
					tokenList.get(gCurrentIndex).toString());
		}
		if(tokenList.get(gCurrentIndex).getKind().equals(")")) {
			currentNode.setThird(new Node (tokenList.get(gCurrentIndex)));
			gCurrentIndex ++; //added terminal )
		} else
			printError("Unclosed expression, expected a ) at index " +gCurrentIndex + " got:" +
					tokenList.get(gCurrentIndex).toString());
		return currentNode;
	}
	
	/* Prints the created parse Tree 
	 * @param root The node to start printing downwards from.
	 */
	public void printTree(Node root) {
		Node currentNode = root;
		if (currentNode != null) {
			currentNode.getSecond();
			System.out.println("Info : " + currentNode.getInfo() + " Kind:" + currentNode.getKind());
			if(currentNode.getFirst() != null) {
				System.out.print("First ");
				Node child = currentNode.getFirst();
				printTree (child);
			} if(currentNode.getSecond() != null) {
				System.out.print("\tsecond ");
				Node child = currentNode.getSecond();
				printTree(child);
			} if(currentNode.getThird() != null) {
				System.out.print("\t\tthird ");
				Node child = currentNode.getThird();
				printTree(child);
			} if (currentNode.getFourth() != null) {
				System.out.print("\t\t\tfourth ");
				Node child = currentNode.getFourth();
				printTree(child);
			}
		}
	}
	
    /* Prints an error message and kills the program.
     * @param message The error message to print.
     */
    private static void printError (final String message) {
    	System.out.println (message);
   	 	System.exit(1);
    }
    
    public static void main (String [] args) {
    	Lexer testLex = new Lexer ("lexerTest2.txt");
    	CalcParser testParse = new CalcParser(testLex);
    	System.out.println("Parsing Successful!");
    	testParse.printTree(gCurrentRoot);
    }
}
