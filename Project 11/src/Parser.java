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

	private String currentFunction() {

		if (currentClass.length() != 0 && currentSubroutine.length() != 0) {

			return currentClass + "." + currentSubroutine;

		}

		return "";
	}

	private String compileType() {

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

	public void compileClass() {

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

		compileClassVarDec();
		compileSubroutine();

		requireSymbol('}');

		if (tokenizer.hasMoreTokens()) {
			throw new IllegalStateException("Unexpected tokens");
		}

		writer.close();

	}

	private void compileClassVarDec() {

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

		type = compileType();

		boolean varNamesDone = false;

		while (true) {

			tokenizer.advance();
			if (tokenizer.tokenType() != TokenType.IDENTIFIER) {
				System.exit(-1);
			}

			name = tokenizer.identifier();

			symbolTable.define(name, type, kind);

			tokenizer.advance();

			if (tokenizer.tokenType() != TokenType.SYMBOL
					|| (tokenizer.symbol() != ',' && tokenizer.symbol() != ';')) {
				System.exit(-1);
			}

			if (tokenizer.symbol() == ';') {
				break;
			}

		}

		compileClassVarDec();
	}

	private void compileSubroutine() {

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

		symbolTable.startSubroutine();

		if (tokenizer.keyWord() == Keyword.METHOD) {
			symbolTable.define("this", currentClass, Kind.ARG);
		}

		String type = "";

		tokenizer.advance();
		if (tokenizer.tokenType() == TokenType.KEYWORD
				&& tokenizer.keyWord() == Keyword.VOID) {
			type = "void";
		} else {
			tokenizer.pointerBack();
			type = compileType();
		}

		tokenizer.advance();
		if (tokenizer.tokenType() != TokenType.IDENTIFIER) {
			System.exit(-1);
		}

		currentSubroutine = tokenizer.identifier();

		requireSymbol('(');

		compileParameterList();

		requireSymbol(')');

		compileSubroutineBody(keyword);

		compileSubroutine();

	}

	private void compileSubroutineBody(Keyword keyword) {

		requireSymbol('{');

		compileVarDec();

		wrtieFunctionDec(keyword);

		compileStatement();

		requireSymbol('}');
	}

	private void wrtieFunctionDec(Keyword keyword) {

		writer.writeFunction(currentFunction(), symbolTable.varCount(Kind.VAR));

		if (keyword == Keyword.METHOD) {

			writer.writePush(Segment.ARG, 0);
			writer.writePop(Segment.POINTER, 0);

		} else if (keyword == Keyword.CONSTRUCTOR) {

			writer.writePush(Segment.CONST, symbolTable.varCount(Kind.FIELD));
			writer.writeCall("Memory.alloc", 1);
			writer.writePop(Segment.POINTER, 0);
		}
	}

	private void compileStatement() {

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
				compileDo();
			} else if (tokenizer.keyWord() == Keyword.LET) {
				compileLet();
			} else if (tokenizer.keyWord() == Keyword.WHILE) {
				compileWhile();
			} else if (tokenizer.keyWord() == Keyword.RETURN) {
				compileReturn();
			} else if (tokenizer.keyWord() == Keyword.IF) {
				compileIf();
			} else {
				System.exit(-1);
			}
		}

		compileStatement();
	}

	private void compileParameterList() {

		tokenizer.advance();
		if (tokenizer.tokenType() == TokenType.SYMBOL
				&& tokenizer.symbol() == ')') {
			tokenizer.pointerBack();
			return;
		}

		String type = "";

		tokenizer.pointerBack();
		while (true) {

			type = compileType();

			tokenizer.advance();
			if (tokenizer.tokenType() != TokenType.IDENTIFIER) {
				System.exit(-1);
			}

			symbolTable.define(tokenizer.identifier(), type, Kind.ARG);

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

	private void compileVarDec() {

		tokenizer.advance();

		if (tokenizer.tokenType() != TokenType.KEYWORD
				|| tokenizer.keyWord() != Keyword.VAR) {
			tokenizer.pointerBack();
			return;
		}

		String type = compileType();

		while (true) {

			tokenizer.advance();

			if (tokenizer.tokenType() != TokenType.IDENTIFIER) {
				System.exit(-1);
			}

			symbolTable.define(tokenizer.identifier(), type, Kind.VAR);

			tokenizer.advance();

			if (tokenizer.tokenType() != TokenType.SYMBOL
					|| (tokenizer.symbol() != ',' && tokenizer.symbol() != ';')) {
				System.exit(-1);
			}

			if (tokenizer.symbol() == ';') {
				break;
			}

		}

		compileVarDec();

	}

	private void compileDo() {

		compileSubroutineCall();

		requireSymbol(';');

		writer.writePop(Segment.TEMP, 0);
	}

	private void compileLet() {

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

			compileExpression();

			requireSymbol(']');

			writer.writeArithmetic(Command.ADD);
		}

		if (expExist)
			tokenizer.advance();

		compileExpression();

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

	private void compileWhile() {

		String continueLabel = newLabel();
		String topLabel = newLabel();

		writer.writeLabel(topLabel);

		requireSymbol('(');

		compileExpression();

		requireSymbol(')');

		writer.writeArithmetic(Command.NOT);
		writer.writeIf(continueLabel);

		requireSymbol('{');

		compileStatement();

		requireSymbol('}');

		writer.writeGoto(topLabel);

		writer.writeLabel(continueLabel);
	}

	private String newLabel() {
		return "LABEL_" + (labelIndex++);
	}

	private void compileReturn() {

		tokenizer.advance();

		if (tokenizer.tokenType() == TokenType.SYMBOL
				&& tokenizer.symbol() == ';') {

			writer.writePush(Segment.CONST, 0);
		} else {

			tokenizer.pointerBack();

			compileExpression();

			requireSymbol(';');
		}

		writer.writeReturn();

	}

	private void compileIf() {

		String elseLabel = newLabel();
		String endLabel = newLabel();

		requireSymbol('(');

		compileExpression();

		requireSymbol(')');

		writer.writeArithmetic(Command.NOT);
		writer.writeIf(elseLabel);

		requireSymbol('{');

		compileStatement();

		requireSymbol('}');

		writer.writeGoto(endLabel);

		writer.writeLabel(elseLabel);

		tokenizer.advance();
		if (tokenizer.tokenType() == TokenType.KEYWORD
				&& tokenizer.keyWord() == Keyword.ELSE) {

			requireSymbol('{');

			compileStatement();

			requireSymbol('}');
		} else {
			tokenizer.pointerBack();
		}

		writer.writeLabel(endLabel);

	}

	private void compileTerm() {

		tokenizer.advance();

		if (tokenizer.tokenType() == TokenType.IDENTIFIER) {

			String tempId = tokenizer.identifier();

			tokenizer.advance();
			if (tokenizer.tokenType() == TokenType.SYMBOL
					&& tokenizer.symbol() == '[') {

				writer.writePush(getSeg(symbolTable.kindOf(tempId)),
						symbolTable.indexOf(tempId));

				compileExpression();

				requireSymbol(']');

				writer.writeArithmetic(Command.ADD);

				writer.writePop(Segment.POINTER, 1);

				writer.writePush(Segment.THAT, 0);

			} else if (tokenizer.tokenType() == TokenType.SYMBOL
					&& (tokenizer.symbol() == '(' || tokenizer.symbol() == '.')) {

				tokenizer.pointerBack();
				tokenizer.pointerBack();
				compileSubroutineCall();
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

				compileExpression();

				requireSymbol(')');
			} else if (tokenizer.tokenType() == TokenType.SYMBOL
					&& (tokenizer.symbol() == '-' || tokenizer.symbol() == '~')) {

				char s = tokenizer.symbol();

				compileTerm();

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

	private void compileSubroutineCall() {

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

			nArgs = compileExpressionList() + 1;

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

			nArgs += compileExpressionList();

			requireSymbol(')');

			writer.writeCall(name, nArgs);
		} else {
			System.exit(-1);
		}

	}

	private void compileExpression() {

		compileTerm();

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

				compileTerm();

				writer.writeCommand(opCmd, "", "");

			} else {
				tokenizer.pointerBack();
				break;
			}

		}

	}

	private int compileExpressionList() {
		int nArgs = 0;

		tokenizer.advance();

		if (tokenizer.tokenType() == TokenType.SYMBOL
				&& tokenizer.symbol() == ')') {
			tokenizer.pointerBack();
		} else {
			nArgs = 1;
			tokenizer.pointerBack();

			compileExpression();

			while (true) {
				tokenizer.advance();
				if (tokenizer.tokenType() == TokenType.SYMBOL
						&& tokenizer.symbol() == ',') {

					compileExpression();
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