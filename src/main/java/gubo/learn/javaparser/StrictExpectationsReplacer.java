package gubo.learn.javaparser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;

/**
 * Not thread-safe
 * Replace mockit.StrictExpectations with mockit.Expectations.
 * StrictExpectations was removed since jmockit 1.36
 * Usage:
 * StrictExpectationsReplacer rep = StrictExpectationsReplacer();
 * String result = rep.replace(new File("..."));
 **/
public class StrictExpectationsReplacer implements IJavaClassSourceProcessor {
	private String to = "mockit.Expectations";
	//whether add a "new FullVerifications() {}" block at the end.
	final boolean addFullVerications;

	
	/**
	 * Both from and to must be fqname;
	 **/
	StrictExpectationsReplacer(boolean addFullVerications) {
		this.addFullVerications = addFullVerications;
	}
	// Quick check if we can ignore this file. 
	boolean shortCut(File file) throws FileNotFoundException {
		Scanner scanner = new Scanner(file);
		String content = scanner.useDelimiter("\\Z").next();
		scanner.close();
		if (!content.contains("StrictExpectations")) {
			return true;
		}
		
		return false;
	}
	/**
	 * Not thread-safe
	 * @throws IOException 
	 **/ 
	public String processFile(File file) throws FileNotFoundException {
		if (file.isFile()) {
			if (shortCut(file)) {
				System.out.println("shortcut");
				return null;
			}
			FileInputStream in = new FileInputStream(file);
			
			// parse the file
			CompilationUnit compilationUnit = JavaParser.parse(in);
			try {
				in.close();
			} catch (IOException e) {}
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
		ret.add("");
		ret.add("mockit.StrictExpectations");
		
		for(ImportDeclaration id : compilationUnit.findAll(ImportDeclaration.class)) {
			String importName = id.getNameAsString();
			if (importName.equals("mockit.StrictExpectations")) {
				ret.add(""); 
			}
		}
		return ret;
	}
	private Set<String> possibleScopesOfFrom;
	
	// Track this number so that we can know if we need add import.
	private int replacedCount = 0;
	
	/**
	 * @return processed source code, null if not changed.
	 * @throws FileNotFoundException
	 **/
	String process(CompilationUnit compilationUnit) {
		
		this.possibleScopesOfFrom = getPossibleScopesOfFrom(compilationUnit);
		
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
		if (n instanceof ObjectCreationExpr) {
			ObjectCreationExpr oc = (ObjectCreationExpr) n;
			processObjectCreationExpr(oc);
			
		}
		
		for (Node cn : n.getChildNodes()) {
			processNode(cn);
		}
	}
	void processMethod(MethodDeclaration md) {
		if (!md.getBody().isPresent()) {
			return;
		}
		for (Statement stmt : md.getBody().get().getStatements()) {
			for (Node n : stmt.getChildNodes()) {
				processNode(n);
			}
		}
		ObjectCreationExpr fullverify = new ObjectCreationExpr();
		fullverify.setType("FullVerifications");
		fullverify.setAnonymousClassBody(new NodeList<BodyDeclaration<?>>());
		md.getBody().get().addAndGetStatement(fullverify);
		
	}

	boolean matchFromScope(Optional<Expression> scope) {
		if (!scope.isPresent()) {
			return this.possibleScopesOfFrom.contains("");
		} else {
			return this.possibleScopesOfFrom.contains(scope.get().toString());
		}
	}
	boolean matchFromName(String name) {
		return name.endsWith("StrictExpectations");
	}

	void processObjectCreationExpr(ObjectCreationExpr oc) {		
		List<ClassOrInterfaceType> nodes = oc.findAll(ClassOrInterfaceType.class);
		boolean replaced = false;
		for (ClassOrInterfaceType node : nodes) {
			if (node.getNameAsString().equals("StrictExpectations")) {
				node.setName("Expectations");
				replaced = true;
			}
		}
		
	}

	public static void main(String[] args) throws IOException {
        StrictExpectationsReplacer rep = new StrictExpectationsReplacer(true);


        GlobCaller gc = new GlobCaller("*.java");
        gc.process(new File("./src/test/java/gubo/learn/javaparser/"), rep, false);
    }
}
