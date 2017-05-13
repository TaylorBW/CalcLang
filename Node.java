/*  A Node holds one node of a parse tree
    with several pointers to children used
    depending on the kind of node.
*/

import java.util.*;
import java.io.*;

public class Node {
	
    private String kind ;  // non-terminal or terminal category for the node
    private String info;  // extra information about the node such as
                        // the actual identifier for an I
    /*An optional id for the node*/
    private int id;		
    // references to children in the parse tree
    private Node first = null;
    private Node second = null;
    private Node third = null;
    private Node fourth = null; 

    // construct a common node with no info specified
    // the largest branch in the tree will result from BIFN ( E )
    //so four children is the max used
    public Node( String k, Node one, Node two, Node three, Node four ) {
      kind = k;  info = "";  
      first = one;  second = two;  third = three; fourth = four;
    }

    // construct a node with specified info
    public Node( String inputKind, String ifo, Node one, Node two, Node three, Node four ) {
    	kind = inputKind;  info = ifo;  
    	first = one;  second = two;  third = three; four = fourth;
    }

    // construct a node that is essentially a token
    public Node( Token token ) {
    	kind = token.getKind();  info = token.getDetails();  
    	first = null;  second = null;  third = null; fourth = null;
    }

 // construct a node with specified info
    public Node( Token newToken, Node one, Node two, Node three, Node four ) {
    	kind = newToken.getKind();  info = newToken.getDetails();    
    	first = one;  second = two;  third = three; four = fourth;
    }
    
    void setId(int newId) {
    	id = newId;
    }
    
    public int getId() {
    	return id;
    }
    
    /* Get the category for the node.
     * @return A string of the category of the node's token.
     */
    public String getKind() {
    	return kind;
    }
    
    /* Get the actual contents of the node.
     * @return A string of what contents of the node's token.
     */
    public String getInfo() {
    	return info;
    }
    
    /* Gets the 1st child of the node.
     * @return The 1st child node.
     */
    public Node getFirst() {
    	return first;
    }
    
    /* Gets the 2nd child of the node.
     * @return The 2nd child node.
     */
    public Node getSecond() {
    	return second;
    }
    
    /* Gets the 3rd child of the node.
     * @return The 3rd child node.
     */
    public Node getThird() {
    	return third;
    }
    
    /* Gets the 4th child of the node.
     * @return The 4th child node.
     */
    public Node getFourth() {
    	return fourth;
    }
    
    /* Mutator for first child.
     * @param newFirst the new node for the first pointer.
     */
    void setFirst(Node newFirst) {
    	first = newFirst;
    }
    
    /* Mutator for second child.
     * @param newSecond the new node for the second pointer.
     */
    void setSecond(Node newSecond) {
    	second = newSecond;
    } 
    
    /* Mutator for third child.
     * @param newFirst the new node for the third pointer.
     */
    void setThird(Node newThird) {
    	third = newThird;
    }
    
    /* Mutator for fourth child.
     * @param newFirst the new node for the fourth pointer.
     */
    void setFourth(Node newFourth) {
    	fourth = newFourth;
    }

     /*Prints [Node.kind,Node.info]*/
    @Override
    public String toString() {
    	return "[" + kind + "," + info + "]";
    }

  	// produce array with the non-null children
  	// in order
  	private Node[] getChildren() {
  		int count = 0;
  		if( first != null ) count++;
  		if( second != null ) count++;
  		if( third != null ) count++;
  		if( fourth != null) count ++;
  		Node[] children = new Node[count];
  		int k=0;
  		if( first != null ) {  children[k] = first; k++; }
  		if( second != null ) {  children[k] = second; k++; }
  		if( third != null ) {  children[k] = third; k++; }
  		if( fourth != null) { children[k] = fourth; k++; }
  		return children;
  	}

  	/* Returns whether or not the node has a 2nd child.
  	 * 
  	 */
  	public boolean hasSecond() {
  		if(second == null) {
  			return false;
  		} else
  			return true;
  	}
  	
    /* Prints an error message and kills the program.
     * @param message The error message to print.
     */
    private static void printError (String message) {
 	   System.out.println (message);
 	   System.exit(1);
    }

}