package gubo.learn.javaparser.tests;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class WebController {

	@Autowired
	@Qualifier("sdf")
	  public   String   someTest  ;
	
	   // Show LexicalPreservation
	String anotherFieldToShowWhiteSpaces 
	;
}
