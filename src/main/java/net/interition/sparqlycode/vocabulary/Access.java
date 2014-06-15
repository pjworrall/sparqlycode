package net.interition.sparqlycode.vocabulary;

import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.ProgramElementDoc;

public enum Access {
	PACKAGE_PRIVATE(1,"Package Private","The default if no access modifier is specified"),
	PRIVATE(2,"Private","A private access modifier"),
	PROTECTED(3,"Protected","A protected access modifier"),
	PUBLIC(4,"Public","A public access modifier") ;
	
	private int code;
	private String label;
	private String description;
	
	
	private Access(int code, String label, String description) {
        this.code = code;
        this.label = label;
        this.description = description;
    }
	
	public int getCode() {
        return code;
    }
 
    public String getLabel() {
        return label;
    }
 
    public String getDescription() {
        return description;
    }
    
    public static Access createAccessModifier(ProgramElementDoc doc) {
    	
		if(doc.isPrivate()) return Access.PRIVATE;
		if(doc.isProtected()) return Access.PROTECTED;
		if(doc.isPublic()) return Access.PUBLIC;
    	
		// we decided it was sufficient to default to assuming it is PACKAGE_PRIVATE at this stage
		
    	return Access.PACKAGE_PRIVATE;
    	
    }
    
}
