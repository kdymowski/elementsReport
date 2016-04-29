import java.io.File;
import java.util.ArrayList;

public class Main {

	public static ArrayList<File> getJackFiles(File dir) {

		File[] files = dir.listFiles();

		ArrayList<File> result = new ArrayList<File>();

		if (files == null)
			return result;

		for (File f : files) {

			if (f.getName().endsWith(".jack")) {
				result.add(f);
			}

		}
		return result;

	}

	public static void main(String[] args) {

		String fileInName = args[0];
		File fileIn = new File(fileInName);

		String fileOutPath = "";

		File fileOut;

		ArrayList<File> jackFiles = new ArrayList<File>();

		if (fileIn.isFile()) {

			String path = fileIn.getAbsolutePath();

			if (!path.endsWith(".jack")) {

				throw new IllegalArgumentException(
						"File must have .jack extension");

			}

			jackFiles.add(fileIn);

		} else if (fileIn.isDirectory()) {

			jackFiles = getJackFiles(fileIn);

			if (jackFiles.size() == 0) {
				throw new IllegalArgumentException("No .jack file found");
			}

		}

		for (File f : jackFiles) {

			fileOutPath = f.getAbsolutePath().substring(0,
					f.getAbsolutePath().lastIndexOf("."))
					+ ".vm";
			fileOut = new File(fileOutPath);

			Parser compilationEngine = new Parser(f, fileOut);
			compilationEngine.parseClass();

		}

	}

}