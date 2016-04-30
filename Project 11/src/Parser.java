import java.io.File;

public class Parser {

	private Writer writer;
	private Tokenizer tokenizer;
	private SymbolTable symbolTable;
	private String currentClass;
	private String currentSubroutine;

	private int labelIndex;

	public Parser(File inFile, File outFile) {

		tokenizer = new Tokenizer(inFile);
		writer = new Writer(outFile);
		symbolTable = new SymbolTable();

		labelIndex = 0;

	}
	
	//returns the current function with the subroutine
	
	private String currentFunction() {

		if (currentClass.length() != 0 && currentSubroutine.length() != 0) {
			return currentClass + "." + currentSubroutine;
		}

		return "";
	}
		
	
	private String parseType() {

		tokenizer.advance();

		if (tokenizer.tokenType() == TokenType.KEYWORD
				&& (tokenizer.keyWord() == Keyword.INT
						|| tokenizer.keyWord() == Keyword.CHAR || tokenizer
						.keyWord() == Keyword.BOOLEAN)) {
			return tokenizer.getCurrentToken();
		}

		if (tokenizer.tokenType() == TokenType.IDENTIFIER) {
			return tokenizer.identifier();
		}

		System.exit(-1);

		return "";
	}

	public void parseClass() {

		tokenizer.advance();

		if (tokenizer.tokenType() != TokenType.KEYWORD
				|| tokenizer.keyWord() != Keyword.CLASS) {
			System.out.println(tokenizer.getCurrentToken());
			System.exit(-1);
		}

		tokenizer.advance();

		if (tokenizer.tokenType() != TokenType.IDENTIFIER) {
			System.exit(-1);
		}

		currentClass = tokenizer.identifier();

		requireSymbol('{');

		parseClassVarDec();
		parseSubroutine();

		requireSymbol('}');

		if (tokenizer.hasMoreTokens()) {
			throw new IllegalStateException("Unexpected tokens");
		}

		writer.close();

	}

	private void parseClassVarDec() {

		tokenizer.advance();

		if (tokenizer.tokenType() == TokenType.SYMBOL
				&& tokenizer.symbol() == '}') {
			tokenizer.pointerBack();
			return;
		}

		if (tokenizer.tokenType() != TokenType.KEYWORD) {
			System.exit(-1);
		}

		if (tokenizer.keyWord() == Keyword.CONSTRUCTOR
				|| tokenizer.keyWord() == Keyword.FUNCTION
				|| tokenizer.keyWord() == Keyword.METHOD) {
			tokenizer.pointerBack();
			return;
		}

		if (tokenizer.keyWord() != Keyword.STATIC
				&& tokenizer.keyWord() != Keyword.FIELD) {
			System.exit(-1);
		}

		Kind kind = null;
		String type = "";
		String name = "";
		
		if (tokenizer.keyWord() == Keyword.STATIC) {
			kind = Kind.STATIC;
		} else if (tokenizer.keyWord() == Keyword.FIELD) {
			kind = Kind.FIELD;
		}

		type = parseType();

		boolean varNamesDone = false;

		while (true) {

			tokenizer.advance();
			if (tokenizer.tokenType() != TokenType.IDENTIFIER) {
				System.exit(-1);
			}

			name = tokenizer.identifier();

			symbolTable.addToMap(name, type, kind);

			tokenizer.advance();

			if (tokenizer.tokenType() != TokenType.SYMBOL
					|| (tokenizer.symbol() != ',' && tokenizer.symbol() != ';')) {
				System.exit(-1);
			}

			if (tokenizer.symbol() == ';') {
				break;
			}

		}

		parseClassVarDec();
	}

	private void parseSubroutine() {

		tokenizer.advance();

		if (tokenizer.tokenType() == TokenType.SYMBOL
				&& tokenizer.symbol() == '}') {
			tokenizer.pointerBack();
			return;
		}

		if (tokenizer.tokenType() != TokenType.KEYWORD
				|| (tokenizer.keyWord() != Keyword.CONSTRUCTOR
						&& tokenizer.keyWord() != Keyword.FUNCTION && tokenizer
						.keyWord() != Keyword.METHOD)) {
			System.exit(-1);
		}

		Keyword keyword = tokenizer.keyWord();

		symbolTable.clearSubroutine();

		if (tokenizer.keyWord() == Keyword.METHOD) {
			symbolTable.addToMap("this", currentClass, Kind.ARG);
		}

		String type = "";

		tokenizer.advance();
		if (tokenizer.tokenType() == TokenType.KEYWORD
				&& tokenizer.keyWord() == Keyword.VOID) {
			type = "void";
		} else {
			tokenizer.pointerBack();
			type = parseType();
		}

		tokenizer.advance();
		if (tokenizer.tokenType() != TokenType.IDENTIFIER) {
			System.exit(-1);
		}

		currentSubroutine = tokenizer.identifier();

		requireSymbol('(');

		parseParameterList();

		requireSymbol(')');

		parseSubroutineBody(keyword);

		parseSubroutine();

	}

	private void parseSubroutineBody(Keyword keyword) {

		requireSymbol('{');

		parseVarDec();

		writeFunctionDec(keyword);

		parseStatement();

		requireSymbol('}');
	}

	private void writeFunctionDec(Keyword keyword) {

		writer.writeFunction(currentFunction(), symbolTable.getVarCount(Kind.VAR));

		if (keyword == Keyword.METHOD) {

			writer.writePush(Segment.ARG, 0);
			writer.writePop(Segment.POINTER, 0);

		} else if (keyword == Keyword.CONSTRUCTOR) {

			writer.writePush(Segment.CONST, symbolTable.getVarCount(Kind.FIELD));
			writer.writeCall("Memory.alloc", 1);
			writer.writePop(Segment.POINTER, 0);
		}
	}

	private void parseStatement() {

		tokenizer.advance();

		if (tokenizer.tokenType() == TokenType.SYMBOL
				&& tokenizer.symbol() == '}') {
			tokenizer.pointerBack();
			return;
		}

		if (tokenizer.tokenType() != TokenType.KEYWORD) {
			System.exit(-1);
		} else {
			if (tokenizer.keyWord() == Keyword.DO) {
				parseDo();
			} else if (tokenizer.keyWord() == Keyword.LET) {
				parseLet();
			} else if (tokenizer.keyWord() == Keyword.WHILE) {
				parseWhile();
			} else if (tokenizer.keyWord() == Keyword.RETURN) {
				parseReturn();
			} else if (tokenizer.keyWord() == Keyword.IF) {
				parseIf();
			} else {
				System.exit(-1);
			}
		}

		parseStatement();
	}

	private void parseParameterList() {

		tokenizer.advance();
		if (tokenizer.tokenType() == TokenType.SYMBOL
				&& tokenizer.symbol() == ')') {
			tokenizer.pointerBack();
			return;
		}

		String type = "";

		tokenizer.pointerBack();
		while (true) {

			type = parseType();

			tokenizer.advance();
			if (tokenizer.tokenType() != TokenType.IDENTIFIER) {
				System.exit(-1);
			}

			symbolTable.addToMap(tokenizer.identifier(), type, Kind.ARG);

			tokenizer.advance();
			if (tokenizer.tokenType() != TokenType.SYMBOL
					|| (tokenizer.symbol() != ',' && tokenizer.symbol() != ')')) {
				System.exit(-1);
			}

			if (tokenizer.symbol() == ')') {
				tokenizer.pointerBack();
				break;
			}

		}

	}

	private void parseVarDec() {

		tokenizer.advance();

		if (tokenizer.tokenType() != TokenType.KEYWORD
				|| tokenizer.keyWord() != Keyword.VAR) {
			tokenizer.pointerBack();
			return;
		}

		String type = parseType();

		while (true) {

			tokenizer.advance();

			if (tokenizer.tokenType() != TokenType.IDENTIFIER) {
				System.exit(-1);
			}

			symbolTable.addToMap(tokenizer.identifier(), type, Kind.VAR);

			tokenizer.advance();

			if (tokenizer.tokenType() != TokenType.SYMBOL
					|| (tokenizer.symbol() != ',' && tokenizer.symbol() != ';')) {
				System.exit(-1);
			}

			if (tokenizer.symbol() == ';') {
				break;
			}

		}

		parseVarDec();

	}

	private void parseDo() {

		parseSubroutineCall();

		requireSymbol(';');

		writer.writePop(Segment.TEMP, 0);
	}

	private void parseLet() {

		tokenizer.advance();
		if (tokenizer.tokenType() != TokenType.IDENTIFIER) {
			System.exit(-1);
		}

		String varName = tokenizer.identifier();

		tokenizer.advance();
		if (tokenizer.tokenType() != TokenType.SYMBOL
				|| (tokenizer.symbol() != '[' && tokenizer.symbol() != '=')) {
			System.exit(-1);
		}

		boolean expExist = false;

		if (tokenizer.symbol() == '[') {

			expExist = true;

			writer.writePush(getSeg(symbolTable.kindOf(varName)),
					symbolTable.indexOf(varName));

			parseExpression();

			requireSymbol(']');

			writer.writeArithmetic(Command.ADD);
		}

		if (expExist)
			tokenizer.advance();

		parseExpression();

		requireSymbol(';');

		if (expExist) {

			writer.writePop(Segment.TEMP, 0);

			writer.writePop(Segment.POINTER, 1);

			writer.writePush(Segment.TEMP, 0);
			writer.writePop(Segment.THAT, 0);
		} else {

			writer.writePop(getSeg(symbolTable.kindOf(varName)),
					symbolTable.indexOf(varName));

		}
	}

	private Segment getSeg(Kind kind) {		
		if (kind == Kind.FIELD) {
			return Segment.THIS;
		} else if (kind == Kind.STATIC) {
			return Segment.STATIC;
		} else if (kind == Kind.VAR) {
			return Segment.LOCAL;
		} else if (kind == Kind.ARG) {
			return Segment.ARG;
		} else {
			return Segment.NONE;
		}
	}

	private void parseWhile() {

		String continueLabel = createNewLabel();
		String topLabel = createNewLabel();

		writer.writeLabel(topLabel);

		requireSymbol('(');

		parseExpression();

		requireSymbol(')');

		writer.writeArithmetic(Command.NOT);
		writer.writeIf(continueLabel);

		requireSymbol('{');

		parseStatement();

		requireSymbol('}');

		writer.writeGoto(topLabel);

		writer.writeLabel(continueLabel);
	}

	private String createNewLabel() {
		return "LABEL_" + (labelIndex++);
	}

	private void parseReturn() {

		tokenizer.advance();

		if (tokenizer.tokenType() == TokenType.SYMBOL
				&& tokenizer.symbol() == ';') {

			writer.writePush(Segment.CONST, 0);
		} else {

			tokenizer.pointerBack();

			parseExpression();

			requireSymbol(';');
		}

		writer.writeReturn();

	}

	private void parseIf() {

		String elseLabel = createNewLabel();
		String endLabel = createNewLabel();

		requireSymbol('(');

		parseExpression();

		requireSymbol(')');

		writer.writeArithmetic(Command.NOT);
		writer.writeIf(elseLabel);

		requireSymbol('{');

		parseStatement();

		requireSymbol('}');

		writer.writeGoto(endLabel);

		writer.writeLabel(elseLabel);

		tokenizer.advance();
		if (tokenizer.tokenType() == TokenType.KEYWORD
				&& tokenizer.keyWord() == Keyword.ELSE) {

			requireSymbol('{');

			parseStatement();

			requireSymbol('}');
		} else {
			tokenizer.pointerBack();
		}

		writer.writeLabel(endLabel);

	}

	private void parseTerm() {

		tokenizer.advance();

		if (tokenizer.tokenType() == TokenType.IDENTIFIER) {

			String tempId = tokenizer.identifier();

			tokenizer.advance();
			if (tokenizer.tokenType() == TokenType.SYMBOL
					&& tokenizer.symbol() == '[') {

				writer.writePush(getSeg(symbolTable.kindOf(tempId)),
						symbolTable.indexOf(tempId));

				parseExpression();

				requireSymbol(']');

				writer.writeArithmetic(Command.ADD);

				writer.writePop(Segment.POINTER, 1);

				writer.writePush(Segment.THAT, 0);

			} else if (tokenizer.tokenType() == TokenType.SYMBOL
					&& (tokenizer.symbol() == '(' || tokenizer.symbol() == '.')) {

				tokenizer.pointerBack();
				tokenizer.pointerBack();
				parseSubroutineCall();
			} else {

				tokenizer.pointerBack();

				writer.writePush(getSeg(symbolTable.kindOf(tempId)),
						symbolTable.indexOf(tempId));
			}

		} else {

			if (tokenizer.tokenType() == TokenType.INT_CONST) {

				writer.writePush(Segment.CONST, tokenizer.intVal());
			} else if (tokenizer.tokenType() == TokenType.STRING_CONST) {

				String str = tokenizer.stringVal();

				writer.writePush(Segment.CONST, str.length());
				writer.writeCall("String.new", 1);

				for (int i = 0; i < str.length(); i++) {
					writer.writePush(Segment.CONST, (int) str.charAt(i));
					writer.writeCall("String.appendChar", 2);
				}

			} else if (tokenizer.tokenType() == TokenType.KEYWORD
					&& tokenizer.keyWord() == Keyword.TRUE) {

				writer.writePush(Segment.CONST, 0);
				writer.writeArithmetic(Command.NOT);

			} else if (tokenizer.tokenType() == TokenType.KEYWORD
					&& tokenizer.keyWord() == Keyword.THIS) {

				writer.writePush(Segment.POINTER, 0);

			} else if (tokenizer.tokenType() == TokenType.KEYWORD
					&& (tokenizer.keyWord() == Keyword.FALSE || tokenizer
							.keyWord() == Keyword.NULL)) {

				writer.writePush(Segment.CONST, 0);
			} else if (tokenizer.tokenType() == TokenType.SYMBOL
					&& tokenizer.symbol() == '(') {

				parseExpression();

				requireSymbol(')');
			} else if (tokenizer.tokenType() == TokenType.SYMBOL
					&& (tokenizer.symbol() == '-' || tokenizer.symbol() == '~')) {

				char s = tokenizer.symbol();

				parseTerm();

				if (s == '-') {
					writer.writeArithmetic(Command.NEG);
				} else {
					writer.writeArithmetic(Command.NOT);
				}

			} else {
				System.exit(-1);
			}
		}

	}

	private void parseSubroutineCall() {

		tokenizer.advance();
		if (tokenizer.tokenType() != TokenType.IDENTIFIER) {
			System.exit(-1);
		}

		String name = tokenizer.identifier();
		int nArgs = 0;

		tokenizer.advance();
		if (tokenizer.tokenType() == TokenType.SYMBOL
				&& tokenizer.symbol() == '(') {

			writer.writePush(Segment.POINTER, 0);

			nArgs = parseExpressionList() + 1;

			requireSymbol(')');

			writer.writeCall(currentClass + '.' + name, nArgs);

		} else if (tokenizer.tokenType() == TokenType.SYMBOL
				&& tokenizer.symbol() == '.') {

			String objName = name;

			tokenizer.advance();

			if (tokenizer.tokenType() != TokenType.IDENTIFIER) {
				System.exit(-1);
			}

			name = tokenizer.identifier();

			String type = symbolTable.typeOf(objName);

			if (type.equals("int") || type.equals("boolean")
					|| type.equals("char") || type.equals("void")) {
				System.exit(-1);
			} else if (type.equals("")) {
				name = objName + "." + name;
			} else {
				nArgs = 1;

				writer.writePush(getSeg(symbolTable.kindOf(objName)),
						symbolTable.indexOf(objName));
				name = symbolTable.typeOf(objName) + "." + name;
			}

			requireSymbol('(');

			nArgs += parseExpressionList();

			requireSymbol(')');

			writer.writeCall(name, nArgs);
		} else {
			System.exit(-1);
		}

	}

	private void parseExpression() {

		parseTerm();

		while (true) {
			tokenizer.advance();

			if (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.isOp()) {

				String opCmd = "";

				if (tokenizer.symbol() == '+') {
					opCmd = "add";
				} else if (tokenizer.symbol() == '-') {
					opCmd = "sub";
				} else if (tokenizer.symbol() == '*') {
					opCmd = "call Math.multiply 2";
				} else if (tokenizer.symbol() == '/') {
					opCmd = "call Math.divide 2";
				} else if (tokenizer.symbol() == '<') {
					opCmd = "lt";
				} else if (tokenizer.symbol() == '>') {
					opCmd = "gt";
				} else if (tokenizer.symbol() == '=') {
					opCmd = "eq";
				} else if (tokenizer.symbol() == '&') {
					opCmd = "and";
				} else if (tokenizer.symbol() == '|') {
					opCmd = "or";
				} else {
					System.exit(-1);
				}

				parseTerm();

				writer.writeCommand(opCmd, "", "");

			} else {
				tokenizer.pointerBack();
				break;
			}

		}

	}

	private int parseExpressionList() {
		int nArgs = 0;

		tokenizer.advance();

		if (tokenizer.tokenType() == TokenType.SYMBOL
				&& tokenizer.symbol() == ')') {
			tokenizer.pointerBack();
		} else {
			nArgs = 1;
			tokenizer.pointerBack();

			parseExpression();

			while (true) {
				tokenizer.advance();
				if (tokenizer.tokenType() == TokenType.SYMBOL
						&& tokenizer.symbol() == ',') {

					parseExpression();
					nArgs++;
				} else {
					tokenizer.pointerBack();
					break;
				}

			}
		}

		return nArgs;
	}

	private void requireSymbol(char symbol) {
		tokenizer.advance();
		if (tokenizer.tokenType() != TokenType.SYMBOL
				|| tokenizer.symbol() != symbol) {
			System.exit(-1);
		}
	}
}