import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Stack;

public class Parser {
	public static ArrayList<String> tokenList = new ArrayList<String>();
	public static ArrayList<tokenType> typeList = new ArrayList<tokenType>();
	String token;
	public int counter = 0;
	public static SymbolTable table;
	public String CLASSNAME;
	public static PrintStream writer;
	public Stack<String> expressions = new Stack<>();

	public Parser(ArrayList<String> tokenList, ArrayList<tokenType> typeList) {
		this.tokenList = tokenList;
		this.typeList = typeList;
		try {
			writer = new PrintStream("out.vm");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void parseClass() {
		if (tokenList.get(counter).equals("class")) {
			counter++;
			if (typeList.get(counter).equals(tokenType.IDENTIFIER)) {
				CLASSNAME = tokenList.get(counter);
				counter++;
				if (tokenList.get(counter).equals("{")) {
					counter++;
					parseClassVarDec();
					while (tokenList.get(counter).equals("constructor")
							|| tokenList.get(counter).equals("method")
							|| tokenList.get(counter).equals("function")) {
						parseSubroutine();
						counter++;
					}
				}
			}
		}
		if (tokenList.get(counter).equals("}")) {
			return;
		} else
			System.exit(-1);
	}

	public void parseClassVarDec() {
		while (tokenList.get(counter).equals("field")
				|| tokenList.get(counter).equals("static")) {
			String kind = tokenList.get(counter);
			counter++;
			String type = tokenList.get(counter);
			if (tokenList.get(counter).equals("int")
					|| tokenList.get(counter).equals("char")
					|| tokenList.get(counter).equals("boolean")
					|| typeList.get(counter).equals(tokenType.IDENTIFIER)) {
				counter++;
				if (typeList.get(counter).equals(tokenType.IDENTIFIER)) {
					table.addToClassMap(tokenList.get(counter), kind, type);
					counter++;
					if (tokenList.get(counter).equals(",")) {
						counter++;
						while (tokenList.get(counter).equals(",")
								|| typeList.get(counter).equals(
										tokenType.IDENTIFIER)) {
							if (!tokenList.get(counter).equals(","))
								table.addToClassMap(tokenList.get(counter), kind, type);
							
							counter++;
						}
						if (tokenList.get(counter).equals(";")) {
							counter++;
							parseClassVarDec();
						}
					} else if (tokenList.get(counter).equals(";")) {
						counter++;
						parseClassVarDec();
					}
				}
			}
		}
		return;
	}

	public void parseSubroutine() {
		table.startSub();
		table.addToSubMap("this", "argument", CLASSNAME);
		if (tokenList.get(counter).equals("constructor")) {
			counter++;
			if (typeList.get(counter).equals(tokenType.IDENTIFIER)) {
				counter++;
				if (tokenList.get(counter).equals("new")) {
					counter++;
					if (tokenList.get(counter).equals("(")) {
						counter++;
						parseParameterList();
					}
					if (tokenList.get(counter).equals(")")) {
						counter++;
						if (tokenList.get(counter).equals("{")) {
							counter++;
							parseVarDec();
							writer.println("function " + CLASSNAME + "." + "new " + (table.subCounterVar - 1));
							parseStatements();
							if (tokenList.get(counter).equals("}")) {
								return;
							}
						}
					}
				}
			}
		} else if (tokenList.get(counter).equals("method")) {
			counter++;
			if (tokenList.get(counter).equals("void")
					|| tokenList.get(counter).equals("int")
					|| tokenList.get(counter).equals("char")
					|| tokenList.get(counter).equals("boolean")
					|| typeList.get(counter).equals(tokenType.IDENTIFIER)) {
				counter++;
				if (typeList.get(counter).equals(tokenType.IDENTIFIER)) {
					String methodName = tokenList.get(counter);
					counter++;
					if (tokenList.get(counter).equals("(")) {
						counter++;
						parseParameterList(); // Might not need
					}
					if (tokenList.get(counter).equals(")")) {
						counter++;
						if (tokenList.get(counter).equals("{")) {
							counter++;
							parseVarDec();
							writer.println("function " + CLASSNAME + "." + methodName + " " + (table.subCounterVar - 1));
							parseStatements();
							if (tokenList.get(counter).equals("}")) {
								return;
							}
						}
					}
				}
			}
		} else if (tokenList.get(counter).equals("function")) {
			counter++;
			if (tokenList.get(counter).equals("void")
					|| tokenList.get(counter).equals("int")
					|| tokenList.get(counter).equals("char")
					|| tokenList.get(counter).equals("boolean")
					|| typeList.get(counter).equals(tokenType.IDENTIFIER)) {
				counter++;
				if (typeList.get(counter).equals(tokenType.IDENTIFIER)) {
					String methodName = tokenList.get(counter);
					counter++;
					if (tokenList.get(counter).equals("(")) {
						counter++;
						parseParameterList(); // Might not need
					}
					if (tokenList.get(counter).equals(")")) {
						counter++;
						if (tokenList.get(counter).equals("{")) {
							counter++;
							parseVarDec(); // !
							writer.println("function " + CLASSNAME + "." + methodName + " " + (table.subCounterVar - 1));
							parseStatements();
							if (tokenList.get(counter).equals("}")) {
								return;
							}
						}
					}
				}
			}
		}
		System.exit(-1);
	}

	public void parseParameterList() {
		if (tokenList.get(counter).equals(")")) {
			return;
		}
		if (tokenList.get(counter).equals("int")
				|| tokenList.get(counter).equals("char")
				|| tokenList.get(counter).equals("boolean")
				|| typeList.get(counter).equals(tokenType.IDENTIFIER)) {
			String type = tokenList.get(counter);
			counter++;
			if (typeList.get(counter).equals(tokenType.IDENTIFIER)) {
				counter++;
				table.addToSubMap(tokenList.get(counter), "argument", type);
				if (tokenList.get(counter).equals(",")) {
					counter++;
					parseParameterList();
				} else
					return;
			}
		}
		return;
	}

	public void parseVarDec() {
		
		if (tokenList.get(counter).equals("var")) {
			counter++;
			if (tokenList.get(counter).equals("int")
					|| tokenList.get(counter).equals("char")
					|| tokenList.get(counter).equals("boolean")
					|| typeList.get(counter).equals(tokenType.IDENTIFIER)) {
				String type = tokenList.get(counter);
				counter++;
				if (typeList.get(counter).equals(tokenType.IDENTIFIER)) {
					table.addToSubMap(tokenList.get(counter), "var", type);
					counter++;
					if (tokenList.get(counter).equals(",")) {
						counter++;
						while (tokenList.get(counter).equals(",")
								|| typeList.get(counter).equals(
										tokenType.IDENTIFIER)) {
							if(!tokenList.get(counter).equals(","))
								table.addToSubMap(tokenList.get(counter), "var", type);
							counter++;
						}
						if (tokenList.get(counter).equals(";")) {
							counter++;
							parseVarDec();
						}
					} else if (tokenList.get(counter).equals(";")) {
						counter++;
						parseVarDec();
					}
				}
			}
		}
		return;
	}

	public void parseStatements() {
		if (tokenList.get(counter).equals("}")) {
			return;
		}
		if (tokenList.get(counter).equals("do")) {
			counter++;
			parseDo();
			counter++;
			parseStatements();
		} else if (tokenList.get(counter).equals("let")) {
			counter++;
			parseLet();
			counter++;
			parseStatements();
		} else if (tokenList.get(counter).equals("while")) {
			counter++;
			parseWhile();
			counter++;
			parseStatements();
		} else if (tokenList.get(counter).equals("return")) {
			counter++;
			parseReturn();
			counter++;
			parseStatements();
		} else if (tokenList.get(counter).equals("if")) {
			counter++;
			parseIf();
			counter++;
			parseStatements();
		}
		return;
	}

	public void parseDo() {
		subroutineCall();
		writer.println("pop temp 0");
		return;
		/*
		 * if (typeList.get(counter).equals(tokenType.IDENTIFIER)) { counter++;
		 * while (tokenList.get(counter).equals(".") ||
		 * typeList.get(counter).equals(tokenType.IDENTIFIER)) { counter++; } if
		 * (tokenList.get(counter).equals("(")) { counter++; if
		 * (typeList.get(counter).equals(tokenType.INT_CONST) ||
		 * typeList.get(counter).equals(tokenType.STRING_CONST)) { counter++;
		 * //parseTerm(); } else { counter++; parseExpressionList(); } if
		 * (tokenList.get(counter).equals(")")) { counter++; if
		 * (tokenList.get(counter).equals(";")) { return; } } } }
		 */
	}

	public void parseLet() {
		if (typeList.get(counter).equals(tokenType.IDENTIFIER)) {
			counter++;
			if (tokenList.get(counter).equals("[")) {
				counter++;
				parseExpression();
				if (tokenList.get(counter).equals("]")) {
					counter++;
				}
				/*
				 * if (typeList.get(counter).equals(tokenType.IDENTIFIER) ||
				 * typeList.get(counter).equals(tokenType.INT_CONST)) {
				 * counter++; if (tokenList.get(counter).equals("]")) { if
				 * (tokenList.get(counter).equals("=")) { counter++; if
				 * (typeList.get(counter).equals( tokenType.INT_CONST)) {
				 * counter++; if (tokenList.get(counter).equals(";")) { return;
				 * } } else if (typeList.get(counter).equals(
				 * tokenType.IDENTIFIER)) { counter++; while
				 * (tokenList.get(counter).equals(".") ||
				 * typeList.get(counter).equals( tokenType.IDENTIFIER)) {
				 * counter++; } if (tokenList.get(counter).equals("(")) {
				 * counter++; if (typeList.get(counter).equals(
				 * tokenType.INT_CONST) || typeList.get(counter).equals(
				 * tokenType.STRING_CONST)) { counter++; parseTerm(); } else {
				 * counter++; parseExpressionList(); } if
				 * (tokenList.get(counter).equals(")")) { counter++; if
				 * (tokenList.get(counter).equals(";")) { return; } } } } } } }
				 */
			}
			if (tokenList.get(counter).equals("=")) {
				counter++;
				parseExpression();
				return;
				/*
				 * if (typeList.get(counter).equals(tokenType.INT_CONST)) {
				 * counter++; if (tokenList.get(counter).equals(";")) { return;
				 * } } else if
				 * (typeList.get(counter).equals(tokenType.IDENTIFIER)) {
				 * counter++; while (tokenList.get(counter).equals(".") ||
				 * typeList.get(counter).equals( tokenType.IDENTIFIER)) {
				 * counter++; } if (tokenList.get(counter).equals("(")) {
				 * counter++; if
				 * (typeList.get(counter).equals(tokenType.INT_CONST) ||
				 * typeList.get(counter).equals( tokenType.STRING_CONST)) {
				 * counter++; // parseTerm(); } else { counter++;
				 * parseExpressionList(); } if
				 * (tokenList.get(counter).equals(")")) { counter++; if
				 * (tokenList.get(counter).equals(";")) { return; } } } }
				 */
			}
		}
		System.exit(-1);
	}

	public void parseWhile() {
		if (tokenList.get(counter).equals("(")) {
			counter++;
			parseExpression();
			if (tokenList.get(counter).equals(")")) {
				counter++;
				if (tokenList.get(counter).equals("{")) {
					counter++;
					if (!tokenList.get(counter).equals("}")) {
						parseStatements();
					} else
						return;
				}
			}
		}
		return;
	}

	public void parseReturn() {
		// parseTerm();
		if (tokenList.get(counter).equals(";")) {
			writer.println("push constant 0");
			writer.println("return");
			return;
		} else
			parseExpression();
		System.exit(-1);
	}

	public void parseIf() {
		if (tokenList.get(counter).equals("(")) {
			counter++;
			parseExpression();
			if (tokenList.get(counter).equals(")")) {
				counter++;
				if (tokenList.get(counter).equals("&")
						|| tokenList.get(counter).equals("|")
						|| tokenList.get(counter).equals("<")
						|| tokenList.get(counter).equals(">")
						|| tokenList.get(counter).equals("+")
						|| tokenList.get(counter).equals("-")
						|| tokenList.get(counter).equals("*")
						|| tokenList.get(counter).equals("/")
						|| tokenList.get(counter).equals("=")) {
					parseExpression();
					counter++;
				}
				if (tokenList.get(counter).equals(")")) {
					counter++;
				}
				if (tokenList.get(counter).equals("{")) {
					counter++;
					if (!tokenList.get(counter).equals("}")) {
						parseStatements();
					} else
						return;
				}
			}
		}
		return;
	}

	public void parseExpression() {
		while (!tokenList.get(counter).equals(")")
				&& !tokenList.get(counter).equals("]")
				&& !tokenList.get(counter).equals(";")) {
			processTerm();
			counter++;
			if (tokenList.get(counter).equals("(")
					|| tokenList.get(counter).equals("[")) {
				parseExpression();
				//counter++;
			}
		}
		return;
	}

	public void parseTerm() {

		System.exit(-1);
	}

	public int parseExpressionList() {
		int nExp = 0;
		while (!tokenList.get(counter).equals(")")) {
			nExp++;
			parseExpression();
			counter++;
			if (tokenList.get(counter).equals(",")) {
				counter++;
			}
		}
		while (!expressions.isEmpty()) {
			writeExpression(expressions.pop());
		}
		return nExp;
	}

	public void runner() {
		table = new SymbolTable();
		parseClass();
		writer.close();
	}
	
	public void subroutineCall() {
		if (tokenList.get(counter + 1).equals("(")) {
			String subroutineName = tokenList.get(counter);
			writer.println("push pointer 0");
			counter++;
			counter++;
			int numberOfExpressions = parseExpressionList();
			writer.println("call " + CLASSNAME + "." + subroutineName + " " + numberOfExpressions);
		} else if (tokenList.get(counter + 1).equals(".")) {
			if (table.classMap.containsKey(tokenList.get(counter))) {
				String varName = table.classMap.get(tokenList.get(counter)).type;
				writer.println("push " + table.classMap.get(tokenList.get(counter)).kind + " " + table.classMap.get(tokenList.get(counter)).index);
				counter++;
				counter++;
				String subroutineName = tokenList.get(counter);
				counter++;
				counter++;
				int numberOfExpressions = parseExpressionList();
				counter++;
				writer.println("call " + varName + "." + subroutineName + " " + numberOfExpressions);
			} else if (table.subMap.containsKey(tokenList.get(counter))) {
				String varName = table.subMap.get(tokenList.get(counter)).type;
				writer.println("push " + table.subMap.get(tokenList.get(counter)).kind + " " + table.subMap.get(tokenList.get(counter)).index);
				counter++;
				counter++;
				String subroutineName = tokenList.get(counter);
				counter++;
				counter++;
				int numberOfExpressions = parseExpressionList();
				counter++;
				writer.println("call " + varName + "." + subroutineName + " " + numberOfExpressions);
			} else {
				String currentClass = tokenList.get(counter);
				counter++;
				counter++;
				String subroutineName = tokenList.get(counter);
				counter++;
				counter++;
				int numberOfExpressions = parseExpressionList();
				counter++;
				writer.println("call " + currentClass + "." + subroutineName + " " + numberOfExpressions);
			}
		}
	}
	
	public void processTerm() {
		boolean isIntConstant = false;
		try {
			Integer.parseInt(tokenList.get(counter));
			isIntConstant = true;
		} catch (NumberFormatException ex) {
			
		}
		if (tokenList.get(counter).equals("-")) {
			if (tokenList.get(counter - 1).equals(",") || tokenList.get(counter - 1).equals("int") || tokenList.get(counter - 1).equals("(")) {
				writer.println("push constant " + tokenList.get(counter + 1));
				writer.println("neg");
				counter++;
			}
		} else if (isIntConstant) {
			writer.println("push constant " + tokenList.get(counter));
		} else if (tokenList.get(counter).equals("+") || tokenList.get(counter).equals("-") || tokenList.get(counter).equals("*") || tokenList.get(counter).equals("/")) {
			expressions.push(tokenList.get(counter));
		}
	}
	
	public void writeExpression(String s) {
		if (s.equals("+")) {
			writer.println("add");
		} else if (s.equals("-")) {
			writer.println("subtract");
		} else if (s.equals("*")) {
			writer.println("call Math.multiply 2");
		} else if (s.equals("/")) {
			writer.println("call Math.divide 2");
		}
	}
}