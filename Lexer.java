import java.util.ArrayList;
import java.util.Scanner;
import java.io.*;

/**
 * This program acts as a lexer for the simple calculator language.
 * It takes the info from a file and puts them into tokens and checks
 * to see if the tokens are part of the language.
 * @author Taylor Woehrle
 */
public class Lexer {
	/*The scanner that reads the input file*/
    private File scanFile;
    /*The state the scan method is in.*/
    private int scanState;
    /*A list of all the found tokens.*/
    private ArrayList<Token> list;
    /*Turns on when a " char is read and off when it is read again, in order
     * to flag when a string token should be being build.*/
    private boolean stringBuilding = false;
    
    /* Constructor for the lexer.
     * @param fileName the name of the file to process
     */
    public Lexer (String fileName) {
		scanState = 0;
		scanFile = (new File (fileName));
		try {
			list = createTokenList();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
    }

    //TODO break each state into own methods.
    /* Read a file line by line and then char by char calling methods
     * that handle creating tokens depending on the current scanState which is
     * determined by the last read char in a line. (state 0 is the starting state).
     * @return The list of tokens found in a file in the order in which they occur.
     */
    private ArrayList<Token> createTokenList() 
    		throws FileNotFoundException {
    	Scanner input = new Scanner(scanFile);
	    ArrayList<Token> list = new ArrayList<Token>(); //The list of all tokens.
	    String id = "";		// The value of the current token */
	    /* Go through each line of the file*/
	    while(input.hasNext() ) {
	    	String currentLine = input.nextLine() + '\n';	//the newline char is used to know
	    													//if there are no more chars on the line
	    													//the '$' is used by the parser to see
	    													//the end of each statement
	    	char currentChar = ' '; // the value of the current char in a token
	    	//get last char in previous loop (if applicable)
	    	id = "";
	    	scanState = 0; //reset state each new line
	    	for( int k = 0; k < currentLine.length(); k++ ) {
		    	// process next individual char in the file
		    	currentChar = currentLine.charAt(k);
		    	//state 0 is initial default state
		        if (scanState == 0 ) {
		        	if (isWhitespace (currentChar)) {
		        		id = "";
		        	} else if (isLetter (currentChar)) {
		        		id += currentChar;
		        		scanState = 1;
		        	} else if (currentChar == '\"') {
		        		id += currentChar;
		        		scanState = 1;
		        		stringBuilding = true;
		        	} else if (isDigit (currentChar) || isPoint(currentChar)) {
		        		id += currentChar;
		        		scanState = 2;
		        	} else if (isMathSymbol (currentChar)) {
		        		id += currentChar;
		        		scanState = 3;
		        	} else {
		        		printError("Unknown/Invalid symbol found in state 0: " + currentChar);
		        	}
		        }
		        
		        /* Char was a letter.
		         * Can be followed by anything printable
		         * SPECIAL CASES: if id is "sqrt", "sin", "cos", or "tan"
		         * 		-> the above are operations and may or may not have a white space before (
		         * Ends when whitespace or mathSymbol is found.
		        */
		        else if (scanState == 1) {
		        	if (isWhitespace (currentChar) && !stringBuilding) {
		        		if (isFunction(id))
		        			list.add(new Token("function", id));
		        		else if ("print".equals(id))
		        			list.add(new Token("print", "print")); 
		        		else if ("newline".equals(id))
		        			list.add(new Token("newline", "newline"));
		        		else if ("input".equals(id))
		        			list.add(new Token("input", "input"));
		        		else if ("msg".equals(id))
		        			list.add(new Token("msg", "msg"));
		        		else if ("show".equals(id))
		        			list.add(new Token("show", "show"));
		        		else if (id.contains(" ") || id.contains("\r") || id.contains("\n")
		        				|| id.contains("\t") || id.isEmpty())
		        			id = "";
		        		else
		        			list.add(new Token("variable", id));
		        		id = ""; //clear id
		        		scanState = 0;
		        	} else if(currentChar == '\"') {
		        		if (stringBuilding) {
		        			//remove " and add to list
		        			String quotelessStr = "";
		        			for(int i = 1; i < id.length(); i++) {
		        				quotelessStr += id.charAt(i);
		        			}
		        			list.add(new Token("string", quotelessStr));
		        			id = ""; //clear id
			        		stringBuilding = false; //building a string is done
		        		} else { //if first "
		        			if(id.length() > 0) { //if id is not empty, add it to the list (will be terminal)
		        				if (isFunction(id))
				        			list.add(new Token("function", id));
				        		else if ("print".equals(id))
				        			list.add(new Token("print", "print")); 
				        		else if ("newline".equals(id))
				        			list.add(new Token("newline", "newline"));
				        		else if ("input".equals(id))
				        			list.add(new Token("input", "input"));
				        		else if ("msg".equals(id))
				        			list.add(new Token("msg", "msg"));
				        		else if ("show".equals(id))
				        			list.add(new Token("show", "show"));
				        		else if (id.contains(" ") || id.contains("\r") || id.contains("\n")
				        				|| id.contains("\t") || id.isEmpty())
				        			id = "";
				        		else
				        			list.add(new Token("variable", id));
		        			}
		        			stringBuilding = true; //start building a string
		        			id = "\"";
		        		}
		        	} else if (isLetter(currentChar) || isDigit(currentChar)
	        									|| isPoint(currentChar)) {
		        		id += currentChar;
		        	} else if (isMathSymbol (currentChar) && !stringBuilding) {
		        		if (isFunction(id)) {
		        			list.add(new Token("function", id));
		        		} else {
		        			list.add(new Token("variable", id));
		        		}
		        		id = "" + currentChar;
		        		scanState = 3;
		        	} else if ( (isPrintable(currentChar) || isWhitespace(currentChar))
		        			&& stringBuilding) {
		        		id += currentChar;
		        		scanState = 1; //stay in same state
		        	} else {
		        		printError("Unknown/Invalid symbol found in state 1: " + currentChar);
		        	}
		        }
		        
		        /* Char is a digit, can be followed by other digits. If a decimal is found check to see
		         * there are no other decimals present. If there is refect this token and print error saying multiple
		         * decimals found. If letter is found print error.
		         * Ends with whitespace, eof, or mathSymbol.
		        */
		        else if (scanState == 2) {
		        	if (isWhitespace (currentChar)) {
		        		list.add(new Token ("number", id));
		        		id = "";
		        		scanState = 0;
		        	} else if (isLetter (currentChar)) {
		        		printError("Invalid Token: "
		        					+ "number " + id + "followed by a letter " + currentChar);
		        	}  else if (currentChar == '\"') {
		        		list.add(new Token ("number", id));
		        		id += currentChar;
		        		scanState = 1;
		        		stringBuilding = true;
		        	} else if (isDigit (currentChar)) {
		        		id += currentChar;
		        		scanState = 2;
		        	}  else if (isPoint (currentChar)) {
		        		for( int i = 0; i < id.length(); i++) {
		        			if (isPoint(id.charAt(i))) {
		        				printError("Invalid Token: Second decimal found in number " +id);
		        			}
		        		}
		        		id += currentChar;
		        		scanState = 2;
		        	} else if (isMathSymbol (currentChar)) {
		        		list.add(new Token ("number", id));
		        		id = "" + currentChar;
		        		scanState = 3;
		        	} else {
		        		printError("Unknown/Invalid symbol found in state 2: " + currentChar);
		        	}
		        }
		        
		        /* Char is a math symbol. 
		         * Is turned into a token no matter what follows.
		        */
		        else if (scanState == 3) {
		        	if (isWhitespace (currentChar)) {
		        		list.add(new Token(id, id));
		        		id = "";
		        		scanState = 0;
		        	} else if (isLetter (currentChar)) {
		        		list.add(new Token(id, id));
		        		id = currentChar + "";
		        		scanState = 1;
		        	}  else if (currentChar == '\"') {
		        		list.add(new Token ("number", id));
		        		scanState = 1;
		        		id = currentChar + "";
		        		stringBuilding = true;
		        	} else if (isDigit (currentChar) || isPoint (currentChar)) {
		        		list.add(new Token(id, id));
		        		id = currentChar + "";
		        		scanState = 2;
		        	} else if (isMathSymbol (currentChar)) {
		        		list.add(new Token(id, id));
		        		id = "" + currentChar;
		        		scanState = 3;
		        	} else {
		        		printError("Unknown/Invalid symbol found in state 3: " + currentChar);
		        	}
		        }
	    	} //search char in line end
	    } //search line end
	    input.close();
	    return list;
    }

	/* Checks to see if the code represents an alphabetic character.
     * @param code The code to check.
     * @return Returns a boolean value. True if the input code corresponds to
     * a char in the Latin alphabet. False otherwise.
     */
    private boolean isLetter (char code) {  
        return 'a' <= code && code <= 'z' ||
               'A' <= code && code <= 'Z';
     }
    
    /* Checks to see if the code represents a digit character.
     * @param code The code to check.
     * @return Returns a boolean value. True if the input code corresponds to
     * an integer char. False otherwise.
     */
     private boolean isDigit (char code) {  
    	 return '0' <= code && code <= '9';
     }

     /* Checks to see if the code represents an alphabetic character.
      * @param code The code to check.
      * @return Returns a boolean value. True if the input code corresponds to
      * a specified range of chars. False otherwise.
      */
     private boolean isPrintable (char code) {  
    	 return ' ' <= code && code <= '~';
     }
     
     /* Checks to see if the code represents whitespace.
      * @param code The code to check.
      * @return Returns a boolean value. True if the input corresponds to
      * whitespace. False otherwise.
      */
     private boolean isWhitespace (char code) {  
    	 return ' ' == code || '\r' == code ||
    			'\t' == code||
    			'\n' == code;
     }

     /* Checks to see if the code represents a point.
      * @param code The code to check.
      * @return Returns a boolean value. True if the input corresponds to
      * '.'. False otherwise.
      */
     private boolean isPoint (char code) {  
    	 return '.' == code;
     }

     /* Checks to see if the code represents a math symbol.
      * @param code The code to check.
      * @return Returns a boolean value. True if the input corresponds to
      * +, -, *, /, (, ), ^, %, or =. False otherwise.
      */
     private boolean isMathSymbol (char code) {  
    	 return '+' == code || '-' == code ||
    			'*' == code|| '/' == code ||
    			'(' == code|| ')' == code ||
    			'=' == code;
     }
     
     /* Checks to see if the code is a '-'.
      * @param code A char to check
      * @return Returns a boolean value. True if the input corresponds to
      * -. False otherwise.
      */
     private boolean isNegative (char code) {
    	 return '-' == code;
     }
     
     /* Checks to see if the code represents a supported math function.
      * @param code The code to check.
      * @return Returns a boolean value. True if the input corresponds to
      * sin, cos, or sqrt. False otherwise.
      */
     private boolean isFunction (String codeStr) {
    	 return "sin".equals(codeStr) || "cos".equals(codeStr)||
    			"sqrt".equals(codeStr);
     }
     
     /* Prints an error message and kills the program.
      * @param message The error message to print.
      */
     private static void printError (String message) {
    	 System.out.println (message);
    	 System.exit(1);
     }
     
     /*Gets the array list of all the foud tokens.*/
     public ArrayList<Token> getList () {
    	 return list;
     }
     
  	/* Prints out a test file named lexerTest2.txt.*/
  	public static void main(String[] args) {
  		Lexer lex = new Lexer( "lexerTest1" );
  		ArrayList<Token> list;
 		try {
 			System.out.println("List of tokens:\n");
 			list = lex.createTokenList();
 			System.out.print("\n[ ");
 			for( int k = 0; k < list.size(); k++ ) {
 	 			System.out.print( list.get(k).getDetails() + " (" + list.get(k).getKind() + "), " );
 	 			if ((k + 1) % 5 == 0) {
 	 				System.out.println();
 	 			}
 	 		}
 			System.out.print(" ]");
 			System.out.println("\nCOMPLETE! listSize = " + list.size());
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
      }
}