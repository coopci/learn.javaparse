package gubo.learn.roast;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.AnnotationSource;
import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;

// This really works: replacing @Autowired and @Qualifier with @Resource
public class Main {

	public static void main(String[] args) throws FileNotFoundException {
		JavaClassSource aClass = Roaster.parse(JavaClassSource.class, new File("./src/main/java/gubo/learn/javaparser/tests/WebController.java"));

		for (FieldSource<JavaClassSource> field: aClass.getFields()) {
			String beanName = null;
			AnnotationSource<JavaClassSource> qualifier = field.getAnnotation("Qualifier");
			if (qualifier!=null) {
				beanName = qualifier.getLiteralValue();
				field.removeAnnotation(qualifier);
			}
			
			AnnotationSource<JavaClassSource> autowired = field.getAnnotation("Autowired");
			
			field.removeAnnotation(autowired);
			if (beanName != null) {
				AnnotationSource<JavaClassSource> resource = field.addAnnotation();
				resource.setName("Resource");
				resource.setLiteralValue("name", beanName);	
			}
		}
		System.out.println(aClass.toUnformattedString());
	}

}
