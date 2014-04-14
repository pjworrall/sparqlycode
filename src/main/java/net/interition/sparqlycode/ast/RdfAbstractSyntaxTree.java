package net.interition.sparqlycode.ast;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.pmd.lang.ParserOptions;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.Java17Parser;
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceType;
import net.sourceforge.pmd.lang.java.ast.ASTFormalParameters;
import net.sourceforge.pmd.lang.java.ast.ASTName;

/*
 * 
 * Where this module should go really is to dor direct mapping between the AST tree and an RDF model using Jena or Sesami
 * This is just an expedient way to get the concept of querying and linking code across to people
 * 
 */

public class RdfAbstractSyntaxTree {

	private List<Node> clazzes = new ArrayList<Node>();
	private List<Node> methodz = new ArrayList<Node>();
	private String packageUri = "";
	private String sourceId = "";

	/*
	 * main for testing only
	 */

	public static void main(String args[]) {

		RdfAbstractSyntaxTree rdfAst = new RdfAbstractSyntaxTree();
		String fileName = args[0];
		rdfAst.setSourceId(args[1]);
		rdfAst.convert(fileName);

	}

	private void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}

	public void convert(String fileName) {

		Reader reader;
		try {
			reader = new FileReader(fileName);
		} catch (FileNotFoundException e) {
			System.out.println("file not found or problem opening it.");
			e.printStackTrace();
			return;
		}

		// instantiate a Java parser , for 1.7 in this case, with an empty
		// parser options object
		Java17Parser parser = new Java17Parser(new ParserOptions());

		// parse the code from the file to an AST and get the root node
		Node node = parser.parse(null, reader);

		// generate the prefixes
		genPrefixRdf(node);
		
		// create a root URI
		genRootRdf();

		// process the tree
		visit(node);

	}

	public void visit(Node node) {

		// if this node is X add myself to an instance var stack for this type
		// so it can be referred to by children. If a Class then create the Type
		// triple
		if (node.toString() == "ClassOrInterfaceDeclaration") {
			convertClassOrInterfaceDeclarationToRDF(node);
			clazzes.add(node);
		}

		if (node.toString() == "MethodDeclarator")
			methodz.add(node);

		List<Node> children = new ArrayList<Node>();

		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			children.add(node.jjtGetChild(i));
		}

		for (Node child : children) {
			this.visit(child);
		}

		convertNodeToRDF(node);

		// pop myself off the stack
		if (node.toString() == "ClassOrInterfaceDeclaration")
			clazzes.remove(node);
		if (node.toString() == "MethodDeclarator")
			methodz.remove(node);

	}

	public void convertNodeToRDF(Node node) {
		String astTypeName = node.toString();
		// only doing methods at the moment
		if (astTypeName == "MethodDeclarator")
			convertMethodDecoratorToRDF(node);

		if (astTypeName == "ImportDeclaration")
			convertImportDeclarationRDF(node);

	}

	private void convertImportDeclarationRDF(Node node) {
		// imports always expected to be associated with first Class

		String packageUri = this.createPackageUri(node.getFirstChildOfType(
				ASTName.class).getImage());

		System.out.println("<" + this.sourceId + ">" + "   cl:imports  " + "<"
				+ packageUri + ">    .");

	}

	public void convertClassOrInterfaceDeclarationToRDF(Node node) {

		String clazzNameUri = "jd:" + node.getImage();
		String astClazzType = "ast:" + node.toString();
		System.out.println(clazzNameUri + " " + "rdf:type" + " " + astClazzType
				+ "  .");

		// create association to source file URI
		
		System.out.println("<" + this.sourceId + ">" + "   cl:definesJavaClass  " + clazzNameUri + "  .");

	}

	public void convertMethodDecoratorToRDF(Node node) {

		// Get the last Class put on the stack - ie the closest parent of type
		// Class
		// Probably is a more elegant way to do that but hey
		// if (clazzes.size() == 0) throw unexpected exception
		Node astClazz = clazzes.get(clazzes.size() - 1);

		// build uri fragments (jd stands for Java Domain )
		// todo: make the prefix static globals in the class rather than
		// literals
		String methodNameUri = "jd:" + node.getImage();
		String nodeType = "ast:" + node.toString();

		// create type declaration triples
		System.out.println(methodNameUri + " " + "rdf:type" + " " + nodeType
				+ "  .");

		// associate class with method
		String clazzNameUri = "jd:" + astClazz.getImage(); // duplicate code -
															// refactor!!
		System.out.println(clazzNameUri + "  cl:hasMethod  " + methodNameUri
				+ "  .");

		// add basic rdf labels
		System.out.println(methodNameUri + " " + "rdf:label" + " " + "\""
				+ node.getImage() + "\"  .");

		// see if we can add parameter types (when not primitive!)
		List<ASTFormalParameters> parameters = node
				.findDescendantsOfType(ASTFormalParameters.class);
		for (ASTFormalParameters p : parameters) {
			List<ASTClassOrInterfaceType> args = p
					.findDescendantsOfType(ASTClassOrInterfaceType.class);
			for (ASTClassOrInterfaceType a : args) {
				System.out.println(methodNameUri + " " + "cl:hasArgumentOfType"
						+ " " + "\"" + a.getImage() + "\"  .");
			}
		}
	}

	public void genPrefixRdf(Node node) {
		// We expect child zero to be a package Node.
		// If not print warning and set a default.
		Node packageNode = node.jjtGetChild(0);
		Node nameNode = packageNode.jjtGetChild(0);
		String packageName = nameNode.getImage();

		if (packageNode.toString() == "PackageDeclaration") {
			packageUri = "http://" + packageName + "/id/";
			System.out.println("@prefix jd: " + "<" + packageUri + ">  .");
		} else {
			System.out
					.println("# Not able to determine Package so create a default PREFIX for jd.");
			System.out.println("@prefix jd: " + "<"
					+ "http://www.interition.net/ast/ref/default" + "> .");
		}
		// print AST prefix
		System.out.println("@prefix ast: " + "<"
				+ "http://www.interition.net/ast/ref/" + ">  .");

		// print sparqlcode prefix (the new model adding knowledge to the AST
		System.out.println("@prefix cl: " + "<"
				+ "http://www.interition.net/sparqlingcode/ref/" + ">  .");

		// rdf and rdfs

		System.out.println("@prefix rdfs: " + "<"
				+ "http://www.w3.org/2000/01/rdf-schema#" + ">  .");
		System.out.println("@prefix rdf: " + "<"
				+ "http://www.w3.org/1999/02/22-rdf-syntax-ns#" + ">  .");

	}

	public String createPackageUri(String packageName) {
		// there is a bug in this method intentionally in that it cannot deal
		// with On Demand import declarations (ie *).
		String clazzName = packageName.substring(
				packageName.lastIndexOf(".") + 1, packageName.length());
		String packagePath = packageName.substring(0,
				packageName.lastIndexOf("."));

		String packageUri = "http://" + packagePath + "/" + clazzName;
		return packageUri;
	}

	public void genRootRdf() {
		String subject = "<" + this.sourceId + ">";
		System.out.println(subject + "  rdf:type  " + "cl:SourceFile  .");
	}
	

}
