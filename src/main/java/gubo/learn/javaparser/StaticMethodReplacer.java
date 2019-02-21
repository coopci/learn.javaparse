package gubo.learn.javaparser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;

/**
 * Not thread-safe
 * Replace calls of a specific static method(ctor parameter from) to another specific static method(ctor parameter to).
 * Usage:
 * StaticMethodReplacer rep = StaticMethodReplacer("pkg1.Class1.staticMethod1","pkg2.Class2.staticMethod2");
 * String result = rep.replace(new File("..."));
 **/
public class StaticMethodReplacer implements IJavaClassSourceProcessor {

	final String from;
	final String to;

	static void checkStaticMethodName(String name) {
		if (name == null) {
			throw new IllegalArgumentException("Name can not be null.");
		}
		int firstDot = name.indexOf('.');
		if (firstDot < 0) {
			throw new IllegalArgumentException("A FQN of a static method must contain at least two dots: " + name);
		}
		int secondDot = name.indexOf('.', firstDot+1);
		if (secondDot < 0) {
			throw new IllegalArgumentException("A FQN of a static method must contain at least two dots: " + name);
		}
	}
	/**
	 * Both from and to must be fqname;
	 **/
	StaticMethodReplacer(String from, String to) {
		checkStaticMethodName(from);
		checkStaticMethodName(to);
		this.from = from;
		this.to = to;
	}

	/**
	 * Not thread-safe
	 **/ 
	public String processFile(File file) throws FileNotFoundException {
		if (file.isFile()) {
			FileInputStream in = new FileInputStream(file);

			// parse the file
			CompilationUnit compilationUnit = JavaParser.parse(in);
			// Read doc of LexicalPreservingPrinter.setup
			compilationUnit = LexicalPreservingPrinter.setup(compilationUnit);
			String output = process(compilationUnit);
			return output;
		} else {
			return null;
		}
	}

	String getClassFQN(String methodFQN) {
		int lastDotPos = methodFQN.lastIndexOf('.');
		return methodFQN.substring(0, lastDotPos);
	}
	String getClassSimpleName(String methodFQN) {
		String classFQN = this.getClassFQN(methodFQN);
		int lastDotPos = classFQN.lastIndexOf('.');
		if (lastDotPos < 0) {
			return classFQN;
		} else {
			return classFQN.substring(lastDotPos+1);
		}
	}
	String getMethodSimpleName(String methodFQN) {
		int lastDotPos = methodFQN.lastIndexOf('.');
		if (lastDotPos < 0) {
			return methodFQN;
		} else {
			return methodFQN.substring(lastDotPos+1);
		}
	}
	Set<String> getPossibleScopesOfFrom(CompilationUnit compilationUnit) {
		HashSet<String> ret = new HashSet<String>();
		
		String fromClassFQN = this.getClassFQN(this.from);
		String fromClassSimpleName = this.getClassSimpleName(this.from);
		ret.add(fromClassFQN);
		for(ImportDeclaration id : compilationUnit.findAll(ImportDeclaration.class)) {
			String importName = id.getNameAsString();
			
			if (importName.equals(this.from)) {
				ret.add(""); 
			} else if (importName.contains(fromClassFQN)) {
				if (id.isAsterisk() && id.isStatic()) {
					ret.add("");
				} else {
					ret.add(fromClassSimpleName);
				}
			}
			
		}
		// System.out.println("possibleScopesOfFrom:" + ret);
		return ret;
	}
	private Set<String> possibleScopesOfFrom;
	private String toScope;
	private String toName;
	// Track this number so that we can know if we need add import.
	private int replacedCount = 0;
	
	/**
	 * @return processed source code, null if not changed.
	 * @throws FileNotFoundException
	 **/
	String process(CompilationUnit compilationUnit) {
		
		this.possibleScopesOfFrom = getPossibleScopesOfFrom(compilationUnit);
		this.toScope = this.getClassSimpleName(this.to);
		this.toName = this.getMethodSimpleName(this.to);
		replacedCount = 0;
		for (MethodDeclaration md : compilationUnit
				.findAll(MethodDeclaration.class)) {
			processMethod(md);
		}
		if (replacedCount > 0) {
			compilationUnit.addImport(this.getClassFQN(this.to));	
		}
		return LexicalPreservingPrinter.print(compilationUnit);
	}
	void processNode(Node n) {
		if (n instanceof MethodCallExpr) {
			MethodCallExpr mc = (MethodCallExpr) n;
			processMethodCallExpr(mc);
		}
		// 这里不需要else， 即便是MethodCallExpr也有可能有MethodCallExpr类型的子节点
		// 例如 staticMethod1(gubo.learn.javaparser.tests.StaticClass1.staticMethod1(1));
		for (Node cn : n.getChildNodes()) {
			processNode(cn);
		}
	}
	void processMethod(MethodDeclaration md) {
		for (Statement stmt : md.getBody().get().getStatements()) {
			for (Node n : stmt.getChildNodes()) {
				processNode(n);
			}
		}
	}

	boolean matchFromScope(Optional<Expression> scope) {
		if (!scope.isPresent()) {
			return this.possibleScopesOfFrom.contains("");
		} else {
			return this.possibleScopesOfFrom.contains(scope.get().toString());
		}
	}
	boolean matchFromName(String name) {
		return this.from.endsWith("." + name);
	}
	boolean matchFrom(MethodCallExpr mc) {
		String name = mc.getNameAsString();
		Optional<Expression> scope = mc.getScope();
		return matchFromName(name) && matchFromScope(scope);
		
	}
	
	void doReplace(MethodCallExpr mc) {
		mc.setName("replacedMethod");
		mc.setName(this.toName);
		mc.setScope(new NameExpr(toScope));
		replacedCount ++;
	}
	void processMethodCallExpr(MethodCallExpr mc) {
		if (matchFrom(mc)) {
			doReplace(mc);
		}
	}

	public static void main(String[] args) throws IOException {
        StaticMethodReplacer rep = new StaticMethodReplacer(
                "gubo.learn.javaparser.tests.StaticClass1.staticMethod1",
                "gubo.learn.javaparser.tests.StaticClass2.staticMethod2");
//        String output = rep
//                .processFile(new File(
//                        "./src/main/java/gubo/learn/javaparser/tests/WebController.java"));
//        System.out.println(output);


        GlobCaller gc = new GlobCaller("*.java");
        gc.process(new File("./src/main/java/"), rep, false);
    }
}
