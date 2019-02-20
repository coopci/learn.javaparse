package gubo.learn.roast;

import org.jboss.forge.roaster.model.source.AnnotationSource;
import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaSource;

public class JavaClassSourceProcessor {

    public String processClass(JavaClassSource aClass) {

        for (FieldSource<JavaClassSource> field: aClass.getFields()) {
            String beanName = null;
            AnnotationSource<JavaClassSource> qualifier = field.getAnnotation("Qualifier");
            if (qualifier!=null) {
                beanName = qualifier.getLiteralValue();
                field.removeAnnotation(qualifier);
            }

            AnnotationSource<JavaClassSource> autowired = field.getAnnotation("Autowired");
            if (autowired!=null) {
                field.removeAnnotation(autowired);
            }
            if (beanName != null) {
                AnnotationSource<JavaClassSource> resource = field.addAnnotation();
                resource.setName("Resource");
                resource.setLiteralValue("name", beanName);
            }
        }
        for (JavaSource<?> js : aClass.getNestedTypes()){

            if (js instanceof JavaClassSource){
                processClass((JavaClassSource) js);
            }
        }

        aClass.addImport("javax.annotation.Resource");
        adjustImports(aClass);
        return aClass.toUnformattedString();
    }
    
    void adjustImports(JavaClassSource aClass) {
        aClass.addImport("javax.annotation.Resource");

        String s = aClass.toUnformattedString();
        if (!s.contains("@Autowired")) {
            aClass.removeImport("org.springframework.beans.factory.annotation.Autowired");
        }
        if (!s.contains("@Qualifier")) {
            aClass.removeImport("org.springframework.beans.factory.annotation.Qualifier");
        }
    }
}
