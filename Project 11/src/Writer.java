import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;

public class Writer {

	private static HashMap<Command, String> commands = new HashMap<Command, String>();
	private static HashMap<Segment, String> segments = new HashMap<Segment, String>();
	private PrintWriter printWriter;

	public Writer(File fOut) {

		try {
			printWriter = new PrintWriter(fOut);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		segments.put(Segment.CONST, "constant");
		segments.put(Segment.ARG, "argument");
		segments.put(Segment.LOCAL, "local");
		segments.put(Segment.STATIC, "static");
		segments.put(Segment.THIS, "this");
		segments.put(Segment.THAT, "that");
		segments.put(Segment.POINTER, "pointer");
		segments.put(Segment.TEMP, "temp");

		commands.put(Command.ADD, "add");
		commands.put(Command.SUB, "sub");
		commands.put(Command.NEG, "neg");
		commands.put(Command.EQ, "eq");
		commands.put(Command.GT, "gt");
		commands.put(Command.LT, "lt");
		commands.put(Command.AND, "and");
		commands.put(Command.OR, "or");
		commands.put(Command.NOT, "not");
	}

	public void writePush(Segment segment, int index) {
		writeCommand("push", segments.get(segment), String.valueOf(index));
	}

	public void writePop(Segment segment, int index) {
		writeCommand("pop", segments.get(segment), String.valueOf(index));
	}

	public void writeArithmetic(Command command) {
		writeCommand(commands.get(command), "", "");
	}

	public void writeLabel(String label) {
		writeCommand("label", label, "");
	}

	public void writeGoto(String label) {
		writeCommand("goto", label, "");
	}

	public void writeIf(String label) {
		writeCommand("if-goto", label, "");
	}

	public void writeCall(String name, int nArgs) {
		writeCommand("call", name, String.valueOf(nArgs));
	}

	public void writeFunction(String name, int nLocals) {
		writeCommand("function", name, String.valueOf(nLocals));
	}

	public void writeReturn() {
		writeCommand("return", "", "");
	}

	public void writeCommand(String cmd, String arg1, String arg2) {

		printWriter.print(cmd + " " + arg1 + " " + arg2 + "\n");

	}

	public void close() {
		printWriter.close();
	}

}
