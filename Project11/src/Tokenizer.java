import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashMap;
import java.util.HashSet;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Tokenizer {

	/* currentToken stores the current token as a string
	 * 
	 * currentTokenType stores the type of the current token
	 * 
	 * counter is used to index through the array list of tokens, 
	 * necessary for lookahead/lookback
	 * 
	 * tokens stores the entire program as an array list of individual tokens,
	 * having broken apart said program into said tokens
	 */
	
	private String currentToken; 
	private TokenType currentTokenType;
	private int counter;
	private ArrayList<String> tokens;

	private static String keyWordRegex;
	private static String symbolRegex = "[\\&\\*\\+\\(\\)\\.\\/\\,\\-\\]\\;\\~\\}\\|\\{\\>\\=\\[\\<]";
	private static String intRegex = "[0-9]+";
	private static String strRegex = "\"[^\"\n]*\"";
	private static String idRegex = "[a-zA-Z_]\\w*";

	private static HashMap<String, Keyword> keyWordMap = new HashMap<String, Keyword>();
	private static HashSet<Character> opSet = new HashSet<Character>();

	public Tokenizer(File inFile) {
		
		//Stores keywords and operator symbols in their respective data structures
		
		keyWordMap.put("class", Keyword.CLASS);
		keyWordMap.put("constructor", Keyword.CONSTRUCTOR);
		keyWordMap.put("function", Keyword.FUNCTION);
		keyWordMap.put("method", Keyword.METHOD);
		keyWordMap.put("field", Keyword.FIELD);
		keyWordMap.put("static", Keyword.STATIC);
		keyWordMap.put("var", Keyword.VAR);
		keyWordMap.put("int", Keyword.INT);
		keyWordMap.put("char", Keyword.CHAR);
		keyWordMap.put("boolean", Keyword.BOOLEAN);
		keyWordMap.put("void", Keyword.VOID);
		keyWordMap.put("true", Keyword.TRUE);
		keyWordMap.put("false", Keyword.FALSE);
		keyWordMap.put("null", Keyword.NULL);
		keyWordMap.put("this", Keyword.THIS);
		keyWordMap.put("let", Keyword.LET);
		keyWordMap.put("do", Keyword.DO);
		keyWordMap.put("if", Keyword.IF);
		keyWordMap.put("else", Keyword.ELSE);
		keyWordMap.put("while", Keyword.WHILE);
		keyWordMap.put("return", Keyword.RETURN);
		opSet.add('+');
		opSet.add('-');
		opSet.add('*');
		opSet.add('/');
		opSet.add('&');
		opSet.add('|');
		opSet.add('<');
		opSet.add('>');
		opSet.add('=');
		
		/* Reads in the file, removes single line comments as they are encountered,
		 * then removes block comments at the end. Regular expressions are used to 
		 * break apart the remaining string into individual tokens, which are then
		 * stored in the tokens array. currentToken is initialized to an empty string,
		 * and currentTokenType is initialized to NONE.
		 */
		
		currentToken = "";
		currentTokenType = TokenType.NONE;
		
		try {
			Scanner s = new Scanner(inFile);
			String line = "";
			String data = "";
			while (s.hasNext()) {
				line = removeComments(s.nextLine()).trim();
				if (line.length() > 0) {
					data += line + "\n";
				}
			}
			data = removeCommentBlocks(data).trim();
			Pattern p = createRegularExpression();
			Matcher m = p.matcher(data);
			tokens = new ArrayList<String>();
			counter = 0;
			while (m.find()) {
				tokens.add(m.group());
			}
			s.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/* Creates a pattern out of the regular expressions needed to break
	 * apart the program into individual tokens. 
	 */

	private Pattern createRegularExpression() {

		//The keyword regex is all of the keywords ORed together
		
		keyWordRegex = "";
		for (String s : keyWordMap.keySet()) {
			keyWordRegex += s + "|";
		}

		/* The symbol regex handles all symbols (operators and others).
		 * 
		 * The int regex handles all integers (any combination of numbers 0-9).
		 * 
		 * The string regex handles all string literals, dealing with 
		 * quotation marks (both those used to identify the string literal as
		 * well as any within the string literal itself) and newline characters.
		 * 
		 * The identifier regex handles all identifiers (which may start with any
		 * alphanumeric character a-z (upper or lowercase) or an underscore, followed
		 * by any combination of non-whitespace characters).
		 */

		return Pattern.compile(idRegex + "|" + keyWordRegex + symbolRegex
				+ "|" + intRegex + "|" + strRegex);
	}

	//Method to determine whether there are more tokens
	
	public boolean hasMoreTokens() {
		return counter < tokens.size();
	}

	/* The advance() method is used to read the next token in the program,
	 * provided that such a token exists. It then determines the type of this
	 * token.
	 */
	
	public void advance() {
		if (hasMoreTokens()) {
			currentToken = tokens.get(counter);
			counter++;
		} else {
			throw new IllegalStateException("No more tokens");
		}

		if (currentToken.matches(keyWordRegex)) {
			currentTokenType = TokenType.KEYWORD;
		} else if (currentToken.matches(symbolRegex)) {
			currentTokenType = TokenType.SYMBOL;
		} else if (currentToken.matches(intRegex)) {
			currentTokenType = TokenType.INT_CONST;
		} else if (currentToken.matches(strRegex)) {
			currentTokenType = TokenType.STRING_CONST;
		} else if (currentToken.matches(idRegex)) {
			currentTokenType = TokenType.IDENTIFIER;
		} else {
			System.exit(-1);
		}
	}
	
	/* Getters for currentToken, currentTokenType, etc.
	 */
	
	public String getCurrentToken() {
		return currentToken;
	}

	public TokenType tokenType() {
		return currentTokenType;
	}

	public Keyword keyWord() {
		if (currentTokenType == TokenType.KEYWORD) {
			return keyWordMap.get(currentToken);
		} else {
			throw new IllegalStateException("not a keyword");
		}
	}

	public char symbol() {
		if (currentTokenType == TokenType.SYMBOL) {
			return currentToken.charAt(0);
		} else {
			throw new IllegalStateException("not a symbol");
		}
	}

	public String identifier() {
		if (currentTokenType == TokenType.IDENTIFIER) {
			return currentToken;
		} else {
			throw new IllegalStateException("not an identifier");
		}
	}

	public int intVal() {
		if (currentTokenType == TokenType.INT_CONST) {
			return Integer.parseInt(currentToken);
		} else {
			throw new IllegalStateException(
					"not an integer constant");
		}
	}

	public String stringVal() {
		if (currentTokenType == TokenType.STRING_CONST) {
			return currentToken.substring(1, currentToken.length() - 1);
		} else {
			throw new IllegalStateException(
					"not a string constant");
		}
	}

	public void pointerBack() {
		if (counter > 0) {
			counter--;
			currentToken = tokens.get(counter);
		}
	}

	public boolean isOp() {
		return opSet.contains(symbol());
	}

	//Removes single line comments
	
	public static String removeComments(String strIn) {
		int position = strIn.indexOf("//");
		if (position != -1) {
			strIn = strIn.substring(0, position);
		}
		return strIn;
	}

	//Removes whitespace
	
	public static String removeSpaces(String strIn) {
		String result = "";
		if (strIn.length() != 0) {
			String[] segs = strIn.split(" ");
			for (String s : segs) {
				result += s;
			}
		}
		return result;
	}

	//Removes block comments
	
	public static String removeCommentBlocks(String strIn) {
		int startIndex = strIn.indexOf("/*");
		if (startIndex == -1)
			return strIn;
		String result = strIn;
		int endIndex = strIn.indexOf("*/");
		while (startIndex != -1) {
			if (endIndex == -1) {
				return strIn.substring(0, startIndex - 1);
			}
			result = result.substring(0, startIndex)
					+ result.substring(endIndex + 2);
			startIndex = result.indexOf("/*");
			endIndex = result.indexOf("*/");
		}
		return result;
	}
}