import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
	public int classCounterStatic = 0;
	public int classCounterField = 0;
	public int subCounterArg = 0;
	public int subCounterVar = 0;
	Map<String, Symbol> classMap;
	Map<String, Symbol> subMap;

	public SymbolTable() {
		classMap = new HashMap<String, Symbol>();
		subMap = new HashMap<String, Symbol>();
	}

	public void addToClassMap(String str, String type) {
		if (type.equals("static")) {
			classMap.put(str, new Symbol(classCounterStatic, type));
			classCounterStatic++;
		} else {
			classMap.put(str, new Symbol(classCounterField, type));
			classCounterField++;
		}
	}

	public void addToSubMap(String str, String type) {
		if (type.equals("argument")) {
			subMap.put(str, new Symbol(subCounterArg, type));
			subCounterArg++;
		} else {
			subMap.put(str, new Symbol(subCounterVar, type));
			subCounterVar++;
		}
	}

	public void startSub() {
		subMap.clear();
		subCounterArg = 0;
		subCounterVar = 0;
	}

	public int indexOfClass(String key) {
		return classMap.get(key).index;
	}

	public int indexOfSub(String key) {
		return subMap.get(key).index;
	}

}

class Symbol {
	public int index;
	public String kind;
	
	public Symbol (int index, String kind) {
		super();
		this.index = index;
		this.kind = kind;
	}
}
