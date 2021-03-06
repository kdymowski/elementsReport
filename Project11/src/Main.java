import java.io.File;
import java.util.ArrayList;

/*
 * reads in files either directory or file 
 */

public class Main {

	public static ArrayList<File> getFiles(File dir) {

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

		File out;

		ArrayList<File> files = new ArrayList<File>();

		if (fileIn.isFile()) {

			String path = fileIn.getAbsolutePath();

			if (!path.endsWith(".jack")) {

				throw new IllegalArgumentException(
						"File must have .jack extension");

			}

			files.add(fileIn);

		} else if (fileIn.isDirectory()) {

			files = getFiles(fileIn);

			if (files.size() == 0) {
				throw new IllegalArgumentException("No .jack file found");
			}

		}

		for (File f : files) {

			fileOutPath = f.getAbsolutePath().substring(0,
					f.getAbsolutePath().lastIndexOf("."))
					+ ".vm";
			out = new File(fileOutPath);

			Parser compilationEngine = new Parser(f, out);
			compilationEngine.parseClass();

		}

	}

}