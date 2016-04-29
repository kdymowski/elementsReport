import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tokenizer {

	private String currentToken;
	private TokenType currentTokenType;
	private int counter;
	private ArrayList<String> tokens;

	private static Pattern tokenPatterns;
	private static String keyWordReg;
	private static String symbolReg;
	private static String intReg;
	private static String strReg;
	private static String idReg;

	private static HashMap<String, Keyword> keyWordMap = new HashMap<String, Keyword>();
	private static HashSet<Character> opSet = new HashSet<Character>();

	public Tokenizer(File inFile) {

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

		try {

			Scanner scanner = new Scanner(inFile);
			String preprocessed = "";
			String line = "";

			while (scanner.hasNext()) {

				line = noComments(scanner.nextLine()).trim();

				if (line.length() > 0) {
					preprocessed += line + "\n";
				}
			}

			preprocessed = noBlockComments(preprocessed).trim();

			initRegs();

			Matcher m = tokenPatterns.matcher(preprocessed);
			tokens = new ArrayList<String>();
			counter = 0;

			while (m.find()) {

				tokens.add(m.group());

			}

		} catch (FileNotFoundException e) {

			e.printStackTrace();

		}

		currentToken = "";
		currentTokenType = TokenType.NONE;

	}

	private void initRegs() {

		keyWordReg = "";

		for (String seg : keyWordMap.keySet()) {

			keyWordReg += seg + "|";

		}

		symbolReg = "[\\&\\*\\+\\(\\)\\.\\/\\,\\-\\]\\;\\~\\}\\|\\{\\>\\=\\[\\<]";
		intReg = "[0-9]+";
		strReg = "\"[^\"\n]*\"";
		idReg = "[a-zA-Z_]\\w*";

		tokenPatterns = Pattern.compile(idReg + "|" + keyWordReg + symbolReg
				+ "|" + intReg + "|" + strReg);
	}

	public boolean hasMoreTokens() {
		return counter < tokens.size();
	}

	public void advance() {

		if (hasMoreTokens()) {
			currentToken = tokens.get(counter);
			counter++;
		} else {
			throw new IllegalStateException("No more tokens");
		}

		if (currentToken.matches(keyWordReg)) {
			currentTokenType = TokenType.KEYWORD;
		} else if (currentToken.matches(symbolReg)) {
			currentTokenType = TokenType.SYMBOL;
		} else if (currentToken.matches(intReg)) {
			currentTokenType = TokenType.INT_CONST;
		} else if (currentToken.matches(strReg)) {
			currentTokenType = TokenType.STRING_CONST;
		} else if (currentToken.matches(idReg)) {
			currentTokenType = TokenType.IDENTIFIER;
		} else {

			throw new IllegalArgumentException("Unknown token:" + currentToken);
		}

	}

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

			throw new IllegalStateException("Current token is not a keyword!");
		}
	}

	public char symbol() {

		if (currentTokenType == TokenType.SYMBOL) {

			return currentToken.charAt(0);

		} else {
			throw new IllegalStateException("Current token is not a symbol!");
		}
	}

	public String identifier() {

		if (currentTokenType == TokenType.IDENTIFIER) {

			return currentToken;

		} else {
			throw new IllegalStateException(
					"Current token is not an identifier! current type:"
							+ currentTokenType);
		}
	}

	public int intVal() {

		if (currentTokenType == TokenType.INT_CONST) {

			return Integer.parseInt(currentToken);
		} else {
			throw new IllegalStateException(
					"Current token is not an integer constant!");
		}
	}

	public String stringVal() {

		if (currentTokenType == TokenType.STRING_CONST) {

			return currentToken.substring(1, currentToken.length() - 1);

		} else {
			throw new IllegalStateException(
					"Current token is not a string constant!");
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

	public static String noComments(String strIn) {

		int position = strIn.indexOf("//");

		if (position != -1) {

			strIn = strIn.substring(0, position);

		}

		return strIn;
	}

	public static String noSpaces(String strIn) {
		String result = "";

		if (strIn.length() != 0) {

			String[] segs = strIn.split(" ");

			for (String s : segs) {
				result += s;
			}
		}

		return result;
	}

	public static String noBlockComments(String strIn) {

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