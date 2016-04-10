import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.*;

public class Tokenizer {
	public static ArrayList<String> inputList = new ArrayList<String>();
	public static ArrayList<String> tokenList = new ArrayList<String>();
	public static PrintStream writer;

	public static void main(String[] args) throws IOException {
		try {
			initScanner();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(inputList.toString());
		initSplitter();
		System.out.println(tokenList.toString());
		
		try {
			writer = new PrintStream("out.txt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (String s: tokenList){
			writer.println(s);
		}
		writer.close();
	}

	public static void initScanner() throws FileNotFoundException {
		Scanner s = new Scanner(new File("Main.jack"));
		while (s.hasNext()) {
			String line = s.next();
			if (line.equals("//"))
				s.nextLine();
			else if (line.equals("/**")) {
				while (!s.next().equals("*/")) {
					continue;
				}
			} else
				inputList.add(line);
		}
		s.close();
	}

	public static void initSplitter() {
		String crap = "";
		for(String s : inputList){
			crap += s + " ";
		}
		while(!crap.isEmpty()){
			crap = crap.trim();
			String expr = "([A-Za-z0-9]+)|((\\{)|(\\})|(\\()|(\\))|(\\[)|(\\])|(\\.)|(\\,)|(\\;)|(\\:)|(\\+)|(\\-)|(\\*)|(\\/)|(\\&)|(\\<)|(\\>)|(\\=)|(\\~))";
			Pattern p = Pattern.compile(expr);
			Matcher m = p.matcher(crap);
			if (m.lookingAt()) {
				String test = m.group();
				tokenList.add(test);
				crap = crap.substring(test.length());
				//crap = crap.replace(test, "");
			}else{
				crap = crap.substring(1);
				String str = "";
				int count = 0;
				while(crap.charAt(count) != '"'){
					str += crap.charAt(count);
					count++;
				}
				tokenList.add(str);
				crap = crap.substring(count + 1);
			}
		}
	}
}