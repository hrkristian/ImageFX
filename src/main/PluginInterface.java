package main;

import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.logging.Logger;

public class PluginInterface {

	ImageProcessor origin;

	public PluginInterface(ImageProcessor origin) {
		this.origin = origin;
	}

	public void compile() {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		final Logger logger = Logger.getGlobal();

		FileChooser fileChooser = new FileChooser();
		File[] files = { fileChooser.showOpenDialog(new Stage()) };
		StandardJavaFileManager stdFileManager = compiler.getStandardFileManager(null, null, null);
		Iterable<? extends JavaFileObject> compilationUnits = stdFileManager.getJavaFileObjectsFromFiles(Arrays.asList(files));

		compiler.getTask(null, stdFileManager, null, null, null, compilationUnits);

		try {
			stdFileManager.close();
		} catch (IOException e) { System.out.println("uh lol"); }

		/*
		JavaFileManager fileManager = new ForwardingJavaFileManager(stdFileManager) {
			public void flush() {
				logger.entering(StandardJavaFileManager.class.getName(), "flush");
				try {
					super.flush();
				} catch (IOException e) {
					System.out.println("Oh no :(");
				}
				logger.exiting(StandardJavaFileManager.class.getName(), "flush");
			}
		};
		*/


	}

	/**
	 * A file object used to represent source coming from a string.
	 */
	public class JavaSourceFromString extends SimpleJavaFileObject {
		/**
		 * The source code of this "file".
		 */
		final String code;

		/**
		 * Constructs a new JavaSourceFromString.
		 * @param name the name of the compilation unit represented by this file object
		 * @param code the source code for the compilation unit represented by this file object
		 */
		JavaSourceFromString(String name, String code) {
			super(URI.create("string:///" + name.replace('.','/') + Kind.SOURCE.extension), Kind.SOURCE);
			this.code = code;
		}

		@Override
		public CharSequence getCharContent(boolean ignoreEncodingErrors) {
			return code;
		}
	}
}
