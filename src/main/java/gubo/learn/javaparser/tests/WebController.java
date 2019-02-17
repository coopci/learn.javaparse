package gubo.learn.javaparser.tests;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class WebController {

	@Autowired
	@Qualifier("sdf")
	  public   String   someTest  ;
	
	   // Show LexicalPreservation
	   @Resource(name="kkk")
	String anotherFieldToShowWhiteSpaces 
	;
	   
	   
	   public static class InnerOfWebController {
		   
		   @Autowired
			@Qualifier("sdf1")
			  public   String   someTest  ;
		   
	   }
}
