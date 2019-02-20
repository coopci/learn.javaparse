package gubo.learn.javaparser.tests;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import static gubo.learn.javaparser.tests.StaticClass1.*;
import static gubo.learn.javaparser.tests.StaticClass1.staticMethod1;

import gubo.learn.javaparser.tests.StaticClass1;
import gubo.learn.javaparser.tests.StaticClass2;
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
	   
    public void func1() {
    	int n = 2;
    	n = StaticClass1.staticMethod1(1); // This will be changed to StaticClass2.staticMethod2(1)
    	StaticClass1.staticMethod1(1); // This will be changed to StaticClass2.staticMethod2(1)
    	StaticClass3.staticMethod1(1); // This will NOT be changed to StaticClass2.staticMethod2(1)
    	  staticMethod1(1); // This will be changed to StaticClass2.staticMethod2(1)
    	gubo.learn.javaparser.tests.StaticClass1.staticMethod1(1); // This will be changed to StaticClass2.staticMethod2(1)
    	staticMethod1(gubo.learn.javaparser.tests.StaticClass1.staticMethod1(1)); // This will be changed to StaticClass2.staticMethod2(1)
    	StaticClass1.staticMethodNotMatch(); // This will NOT be changed to StaticClass2.staticMethod2(1)
    }
}
