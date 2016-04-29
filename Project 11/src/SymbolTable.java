import java.util.HashMap;
import java.util.Map;

public class SymbolTable {

	private HashMap<String, Symbol> classMap;
	private HashMap<String, Symbol> subMap;
	private HashMap<Kind, Integer> indexMap;

	public SymbolTable() {
		classMap = new HashMap<String, Symbol>();
		subMap = new HashMap<String, Symbol>();

		indexMap = new HashMap<Kind, Integer>();
		indexMap.put(Kind.ARG, 0);
		indexMap.put(Kind.FIELD, 0);
		indexMap.put(Kind.STATIC, 0);
		indexMap.put(Kind.VAR, 0);

	}

	public void startSubroutine() {
		subMap.clear();
		indexMap.put(Kind.VAR, 0);
		indexMap.put(Kind.ARG, 0);
	}

	public void define(String name, String type, Kind kind) {

		if (kind == Kind.ARG || kind == Kind.VAR) {

			int index = indexMap.get(kind);
			Symbol symbol = new Symbol(type, kind, index);
			indexMap.put(kind, index + 1);
			subMap.put(name, symbol);

		} else if (kind == Kind.STATIC || kind == Kind.FIELD) {

			int index = indexMap.get(kind);
			Symbol symbol = new Symbol(type, kind, index);
			indexMap.put(kind, index + 1);
			classMap.put(name, symbol);

		}

	}

	public int varCount(Kind kind) {
		return indexMap.get(kind);
	}

	public Kind kindOf(String name) {

		Symbol symbol = lookUp(name);

		if (symbol != null)
			return symbol.kind;

		return Kind.NONE;
	}

	public String typeOf(String name) {

		Symbol symbol = lookUp(name);

		if (symbol != null)
			return symbol.type;

		return "";
	}

	public int indexOf(String name) {

		Symbol symbol = lookUp(name);

		if (symbol != null)
			return symbol.index;

		return -1;
	}

	private Symbol lookUp(String name) {

		if (classMap.get(name) != null) {
			return classMap.get(name);
		} else if (subMap.get(name) != null) {
			return subMap.get(name);
		} else {
			return null;
		}
	}
}

class Symbol {

	public String type;
	public Kind kind;
	public int index;

	public Symbol(String type, Kind kind, int index) {
		this.type = type;
		this.kind = kind;
		this.index = index;
	}
}