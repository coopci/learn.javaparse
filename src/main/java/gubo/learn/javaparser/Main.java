package gubo.learn.javaparser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Optional;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;

public class Main {

	static void processField(FieldDeclaration f) {

		Optional<AnnotationExpr> optionalAutowired = f
				.getAnnotationByName("Autowired");
		Optional<AnnotationExpr> optionalQualifier = f
				.getAnnotationByName("Qualifier");
		if (optionalAutowired.isPresent() && optionalQualifier.isPresent()) {
			// qualifier.get().
			SingleMemberAnnotationExpr qualifier = (SingleMemberAnnotationExpr) optionalQualifier
					.get();

			String beanName = qualifier.getMemberValue().toString();

			System.out.println("beanName = " + beanName);
			// qualifier.remove();
			f.remove(qualifier);
			NodeList<MemberValuePair> nodes = new NodeList<MemberValuePair>();
			// nodes.add(new MemberValuePair("name", beanName));
			NormalAnnotationExpr resource = new NormalAnnotationExpr(new Name(
					"Resource"), new NodeList<MemberValuePair>());

			optionalAutowired.get().replace(resource);

		}
		// System.out.println("Check field at line " + f.getRange().map(r ->
		// r.begin.line).orElse(-1));

	}

	static void processMethod(MethodDeclaration m) {
		
		for(Statement stmt : m.getBody().get().getStatements()) {
			for (Node n : stmt.getChildNodes()) {
				if (n instanceof MethodCallExpr) {
					MethodCallExpr mc = (MethodCallExpr) n;
					
					String name = mc.getNameAsString();
					String scope = mc.getScope().toString();
					System.out.println("scope:" + scope + ", name:" + name);
					mc.setName("replacedMethod");
				}
				System.out.println(n);
			}
			
		}
		
		
	}

	public static void main(String[] args) throws FileNotFoundException {
		System.out.println(System.getProperty("user.dir"));
		FileInputStream in = new FileInputStream(
				"./src/main/java/gubo/learn/javaparser/tests/WebController.java");

		// parse the file
		CompilationUnit compilationUnit = JavaParser.parse(in);
		// Read doc of LexicalPreservingPrinter.setup
		compilationUnit = LexicalPreservingPrinter.setup(compilationUnit);
		//compilationUnit.findAll(FieldDeclaration.class).stream()
		// .filter(f -> f.isPublic() && !f.isStatic())
		//		.forEach(f -> processField(f));
		
		System.out.println("After process: ");
		// System.out.println(compilationUnit);
		System.out.println(LexicalPreservingPrinter.print(compilationUnit));

		
		
		StaticMethodReplacer rep = new StaticMethodReplacer("","");
		rep.processFile(new File("./src/main/java/gubo/learn/javaparser/tests/WebController.java"));
	}

}
