import java.util.ArrayList;



public class Parser {
	public static ArrayList<String> tokenList = new ArrayList<String>();
	public static ArrayList<tokenType> typeList = new ArrayList<tokenType>();
	String token;
	public int counter = 0;

	public Parser(ArrayList<String> tokenList, ArrayList<tokenType> typeList) {
		this.tokenList = tokenList;
		this.typeList = typeList;

	}

	public boolean parseClass() {
		if (tokenList.get(counter).equals("class")) {
			counter++;
			if (typeList.get(counter).equals(tokenType.IDENTIFIER)) {
				counter++;
				if (tokenList.get(counter).equals("{")) {
					counter++;
					return parseClassVarDec();

				}

			}

		}
		return false;

	}

	public boolean parseClassVarDec() {
		if (tokenList.get(counter).equals("constructor")
				|| tokenList.get(counter).equals("method")
				|| tokenList.get(counter).equals("function")) {
			return parseSubroutine();

		}
		if (tokenList.get(counter).equals("field")) {
			counter++;
			if (tokenList.get(counter).equals("int")
					|| tokenList.get(counter).equals("char")
					|| tokenList.get(counter).equals("boolean")
					|| typeList.get(counter).equals(tokenType.IDENTIFIER)) {
				counter++;
				if (typeList.get(counter).equals(tokenType.IDENTIFIER)) {
					counter++;
					if (tokenList.get(counter).equals(",")) {
						counter++;
						while (tokenList.get(counter).equals(",")
								|| typeList.get(counter).equals(
										tokenType.IDENTIFIER)) {
							counter++;

						}
						if (tokenList.get(counter).equals(";")) {
							counter++;
							return parseClassVarDec();

						}

					} else if (tokenList.get(counter).equals(";")) {
						counter++;
						return parseClassVarDec();

					}

				}

			}

		}
		return false;

	}

	public boolean parseSubroutine() {
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
							parseStatements();
							if (tokenList.get(counter).equals("}")) {
								return true;

							}

						}

					}

				}

			}

		} else if (tokenList.get(counter).equals("method")) {
			if (tokenList.get(counter).equals("void")
					|| tokenList.get(counter).equals("int")
					|| tokenList.get(counter).equals("char")
					|| tokenList.get(counter).equals("boolean")
					|| typeList.get(counter).equals(tokenType.IDENTIFIER)) {
				counter++;
				if (typeList.get(counter).equals(tokenType.IDENTIFIER)) {
					counter++;
					if (tokenList.get(counter).equals("(")) {
						counter++;
						parseParameterList(); // Might not need

					}
					if (tokenList.get(counter).equals(")")) {
						counter++;
						if (tokenList.get(counter).equals("{")) {
							counter++;
							parseStatements();
							if (tokenList.get(counter).equals("}")) {
								return true;

							}

						}

					}

				}

			} else if (tokenList.get(counter).equals("function")) {
				if (tokenList.get(counter).equals("void")
						|| tokenList.get(counter).equals("int")
						|| tokenList.get(counter).equals("char")
						|| tokenList.get(counter).equals("boolean")
						|| typeList.get(counter).equals(tokenType.IDENTIFIER)) {
					counter++;
					if (typeList.get(counter).equals(tokenType.IDENTIFIER)) {
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
								parseStatements();
								if (tokenList.get(counter).equals("}")) {
									return true;

								}

							}

						}

					}

				}

			}

		}
		return false;

	}

	public boolean parseParameterList() {
		if (tokenList.get(counter).equals(")")) {
			return true;

		}
		if (tokenList.get(counter).equals("int")
				|| tokenList.get(counter).equals("char")
				|| tokenList.get(counter).equals("boolean")
				|| typeList.get(counter).equals(tokenType.IDENTIFIER)) {
			counter++;
			if (typeList.get(counter).equals(tokenType.IDENTIFIER)) {
				counter++;
				if (tokenList.get(counter).equals(",")) {
					counter++;
					parseParameterList();

				} else
					return true;

			}

		}
		return false;

	}

	public boolean parseVarDec() {
		if (tokenList.get(counter).equals("var")) {
			counter++;
			if (tokenList.get(counter).equals("int")
					|| tokenList.get(counter).equals("char")
					|| tokenList.get(counter).equals("boolean")
					|| typeList.get(counter).equals(tokenType.IDENTIFIER)) {
				counter++;
				if (typeList.get(counter).equals(tokenType.IDENTIFIER)) {
					counter++;
					if (tokenList.get(counter).equals(",")) {
						counter++;
						while (tokenList.get(counter).equals(",")
								|| typeList.get(counter).equals(
										tokenType.IDENTIFIER)) {
							counter++;

						}
						if (tokenList.get(counter).equals(";")) {
							counter++;
							return parseVarDec();

						}

					} else if (tokenList.get(counter).equals(";")) {
						counter++;
						return parseVarDec();

					}

				}

			}

		}
		return false;

	}

	public boolean parseStatements() {
		if (tokenList.get(counter).equals("}")) {
			return true;

		}
		if (tokenList.get(counter).equals("do")) {
			counter++;
			parseDo();

		} else if (tokenList.get(counter).equals("let")) {
			counter++;
			parseLet();

		} else if (tokenList.get(counter).equals("while")) {
			counter++;
			parseWhile();

		} else if (tokenList.get(counter).equals("return")) {
			counter++;
			parseReturn();

		} else if (tokenList.get(counter).equals("if")) {
			counter++;
			parseIf();

		}
		return false;

	}

	public boolean parseDo() {
		if (typeList.get(counter).equals(tokenType.IDENTIFIER)) {
			counter++;
			while (tokenList.get(counter).equals(".")
					|| typeList.get(counter).equals(tokenType.IDENTIFIER)) {
				counter++;

			}
			if (tokenList.get(counter).equals("(")) {
				counter++;
				if (typeList.get(counter).equals(tokenType.INT_CONST)
						|| typeList.get(counter).equals(tokenType.STRING_CONST)) {
					counter++;
					parseTerm();

				} else {
					counter++;
					parseExpressionList();

				}
				if (tokenList.get(counter).equals(")")) {
					counter++;
					if (tokenList.get(counter).equals(";")) {
						return true;

					}

				}

			}

		}
		return false;

	}

	public boolean parseLet() {
		if (typeList.get(counter).equals(tokenType.IDENTIFIER)) {
			counter++;
			if (tokenList.get(counter).equals("[")) {
				counter++;
				if (typeList.get(counter).equals(tokenType.IDENTIFIER)
						|| typeList.get(counter).equals(tokenType.INT_CONST)) {
					counter++;
					if (tokenList.get(counter).equals("]")) {
						if (tokenList.get(counter).equals("=")) {
							counter++;
							if (typeList.get(counter).equals(
									tokenType.INT_CONST)) {
								counter++;
								if (tokenList.get(counter).equals(";")) {
									return true;

								}

							} else if (typeList.get(counter).equals(
									tokenType.IDENTIFIER)) {
								counter++;
								while (tokenList.get(counter).equals(".")
										|| typeList.get(counter).equals(
												tokenType.IDENTIFIER)) {
									counter++;

								}
								if (tokenList.get(counter).equals("(")) {
									counter++;
									if (typeList.get(counter).equals(
											tokenType.INT_CONST)
											|| typeList.get(counter).equals(
													tokenType.STRING_CONST)) {
										counter++;
										parseTerm();

									} else {
										counter++;
										parseExpressionList();

									}
									if (tokenList.get(counter).equals(")")) {
										counter++;
										if (tokenList.get(counter).equals(";")) {
											return true;

										}

									}

								}

							}

						}

					}

				}

			} else if (tokenList.get(counter).equals("=")) {
				counter++;
				if (typeList.get(counter).equals(tokenType.INT_CONST)) {
					counter++;
					if (tokenList.get(counter).equals(";")) {
						return true;

					}

				} else if (typeList.get(counter).equals(tokenType.IDENTIFIER)) {
					counter++;
					while (tokenList.get(counter).equals(".")
							|| typeList.get(counter).equals(
									tokenType.IDENTIFIER)) {
						counter++;

					}
					if (tokenList.get(counter).equals("(")) {
						counter++;
						if (typeList.get(counter).equals(tokenType.INT_CONST)
								|| typeList.get(counter).equals(
										tokenType.STRING_CONST)) {
							counter++;
							parseTerm();

						} else {
							counter++;
							parseExpressionList();

						}
						if (tokenList.get(counter).equals(")")) {
							counter++;
							if (tokenList.get(counter).equals(";")) {
								return true;

							}

						}

					}

				}

			}

		}
		return false;

	}

	public boolean parseWhile() {
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
						return true;

				}

			}

		}
		return false;

	}

	public boolean parseReturn() {
		parseTerm();
		if (tokenList.get(counter).equals(";")) {
			return true;

		}
		return false;

	}

	public boolean parseIf() {
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
						return true;

				}

			}

		}
		return false;

	}

	public boolean parseExpression() {
		return false;

	}

	public boolean parseTerm() {

		return false;

	}

	public boolean parseExpressionList() {
		while (!tokenList.get(counter).equals(")")) {
			parseExpression();
			counter++;
			if (tokenList.get(counter).equals(",")) {
				counter++;

			}

		}
		return false;

	}

	public void runner() {
		parseClass();

	}

}