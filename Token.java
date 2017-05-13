public class Token { 
	private String kind;
	private String data;
	public Token( String k, String d )
	{ kind = k; data = d; }

	public boolean isKind( String s ) {
		return kind.equals( s );
	} 

	public String getKind() {
		return kind;
	}

	public String getDetails() {
		return data;
	}

	public String toString() {
		return "[" + kind + "," + data + "]";
	}
} 