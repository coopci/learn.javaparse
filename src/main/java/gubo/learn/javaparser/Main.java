package gubo.learn.javaparser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;

public class Main {

	
	static void processField(FieldDeclaration f) {
		
		Optional<AnnotationExpr> optionalAutowired = f.getAnnotationByName("Autowired");
		Optional<AnnotationExpr> optionalQualifier = f.getAnnotationByName("Qualifier");
		if (optionalAutowired.isPresent() && optionalQualifier.isPresent()) {
			// qualifier.get().
			SingleMemberAnnotationExpr qualifier = (SingleMemberAnnotationExpr) optionalQualifier.get();
			
			String beanName = qualifier.getMemberValue().toString();
			
			System.out.println("beanName = " + beanName);
			// qualifier.remove();
			f.remove(qualifier);
			NodeList<MemberValuePair> nodes = new NodeList<MemberValuePair>();
			// nodes.add(new MemberValuePair("name", beanName));
			NormalAnnotationExpr resource = new NormalAnnotationExpr(new Name("Resource"), new NodeList<MemberValuePair>()); 
			
			
			optionalAutowired.get().replace(resource);
			
		}
		// System.out.println("Check field at line " + f.getRange().map(r -> r.begin.line).orElse(-1));
		
	}
	public static void main(String[] args) throws FileNotFoundException {
		System.out.println(System.getProperty("user.dir"));
		FileInputStream in = new FileInputStream("./src/main/java/gubo/learn/javaparser/tests/WebController.java");

		// parse the file
		CompilationUnit compilationUnit = JavaParser.parse(in);

		

		compilationUnit.findAll(FieldDeclaration.class).stream()
						// .filter(f -> f.isPublic() && !f.isStatic())
						.forEach( f -> processField(f));

		// Read doc of LexicalPreservingPrinter.setup
//		compilationUnit = LexicalPreservingPrinter.setup(compilationUnit);		
//		System.out.println("Printing with LexicalPreservingPrinter.print:");
//		System.out.println(LexicalPreservingPrinter.print(compilationUnit));
		
		System.out.println(compilationUnit);

	}

}
