import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
	public int classCounterStatic = 0;
	public int classCounterField = 0;
	public int subCounter = 0;
	Map<String, Integer> classMap;
	Map<String, Integer> subMap;

	public SymbolTable() {
		classMap = new HashMap<String, Integer>();
		subMap = new HashMap<String, Integer>();
	}

	public void addToClassMap(String str, String type) {
		if (type.equals("static")) {
			classMap.put(str, classCounterStatic);
			classCounterStatic++;
		} else {

			classMap.put(str, classCounterField);
			classCounterField++;
		}
	}

	public void addToSubMap(String str) {
		subMap.put(str, subCounter);
		subCounter++;
	}

	public void startSub() {
		subMap.clear();
		subCounter = 0;
	}

	public int indexOfClass(String key) {
		return classMap.get(key);
	}

	public int indexOfSub(String key) {
		return subMap.get(key);
	}

}
