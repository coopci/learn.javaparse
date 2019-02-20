package gubo.learn.roast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.AnnotationSource;
import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaSource;

// This really works: replacing @Autowired and @Qualifier with @Resource
public class Main {




	public static void process(File f, JavaClassSourceProcessor processor, boolean doReplace) throws IOException {
		if (f.isFile()) {
			System.out.println("Processing " + f.toString() + "...");
			JavaClassSource aClass = Roaster.parse(JavaClassSource.class, f);

			String output = processor.processClass(aClass);
			System.out.println(output);

			if (doReplace) {

				//
			}

		} else if (f.isDirectory()) {


			List<Path> paths = Files.walk(Paths.get(f.toString()))
					.filter(Files::isRegularFile)
					.filter(f1 -> f1.toString().endsWith(".java"))
					.collect(Collectors.toList());

			for (Path p : paths) {
				process(new File(p.toString()), processor, doReplace);
			}
		}
	}


	public static void main(String[] args) throws IOException {
		// File f = new File("./src/main/java/gubo/learn/javaparser/tests/WebController.java");
		File f = new File("./src/main/java/gubo/learn/javaparser/tests/");
		process(f, new JavaClassSourceProcessor(), false);
		


	}

}