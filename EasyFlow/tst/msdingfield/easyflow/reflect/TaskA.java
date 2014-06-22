package msdingfield.easyflow.reflect;

import msdingfield.easyflow.annotations.Input;
import msdingfield.easyflow.annotations.Operation;
import msdingfield.easyflow.annotations.Output;
import msdingfield.easyflow.annotations.Scope;


@Scope("annotationTest")
public class TaskA {

	@Input
	public int input;
	
	@Output
	public int a;
	
	@Operation
	public void execute() {
		a = input + 2;
	}
}
