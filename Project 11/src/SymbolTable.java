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
	
	//clear subroutine at the beginning

	public void clearSubroutine() {
		subMap.clear();
		indexMap.put(Kind.VAR, 0);
		indexMap.put(Kind.ARG, 0);
	}
	
	//function to add to map, either sub or class

	public void addToMap(String name, String type, Kind kind) {

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
	
	//get the var count from associated kind 

	public int getVarCount(Kind kind) {
		return indexMap.get(kind);
	}
	
	//returns the find of symbol
	
	public Kind kindOf(String name) {

		Symbol symbol = getSymbol(name);

		if (symbol != null)
			return symbol.kind;

		return Kind.NONE;
	}

	// returns the type of symbol
	
	public String typeOf(String name) {

		Symbol symbol = getSymbol(name);

		if (symbol != null)
			return symbol.type;

		return "";
	}
	
	// returns the index of symbol

	public int indexOf(String name) {

		Symbol symbol = getSymbol(name);

		if (symbol != null)
			return symbol.index;

		return -1;
	}
	
	// returns the symbol

	private Symbol getSymbol(String name) {

		if (classMap.get(name) != null) {
			return classMap.get(name);
		} else if (subMap.get(name) != null) {
			return subMap.get(name);
		} else {
			return null;
		}
	}
}

// storage for the symbol

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