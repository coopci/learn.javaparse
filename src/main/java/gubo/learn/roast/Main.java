package gubo.learn.roast;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.AnnotationSource;
import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaSource;

// This really works: replacing @Autowired and @Qualifier with @Resource
public class Main {



	public static void process(File f, JavaClassSourceProcessor p) throws FileNotFoundException {
		if (f.isFile()) {
			JavaClassSource aClass = Roaster.parse(JavaClassSource.class, f);

			String output = p.processClass(aClass);
			System.out.println(output);
		}

	}


	public static void main(String[] args) throws FileNotFoundException {
		File f = new File("./src/main/java/gubo/learn/javaparser/tests/WebController.java");
		process(f, new JavaClassSourceProcessor());
		


	}

}
