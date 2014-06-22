package msdingfield.easyflow.reflect;

import msdingfield.easyflow.annotations.Input;
import msdingfield.easyflow.annotations.Operation;
import msdingfield.easyflow.annotations.Output;
import msdingfield.easyflow.annotations.Scope;


@Scope("annotationTest")
public class TaskB {

	@Input
	public int a;
	
	@Output
	public int b;
	
	@Operation
	public void boog() {
		b = a * 2;
	}
}
