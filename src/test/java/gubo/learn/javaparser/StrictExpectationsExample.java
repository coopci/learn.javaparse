package gubo.learn.javaparser;

import org.junit.Test;

import mockit.Mocked;
import mockit.StrictExpectations;

public class StrictExpectationsExample {
	static public class Class1 {
		public String getName() {
			return "ad";
		}
	}
	@Mocked Class1 mock;
	@Test
	public void testMehtod1() {
		new StrictExpectations() {
			{
				mock.getName();
				result = "mocked in expectations";
			}
		};

		System.out.println("mock.getName() = " + mock.getName());
		
	}
}
