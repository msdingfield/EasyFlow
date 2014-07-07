package msdingfield.easyflow.graph;


public class DiamondOperations {

	public static final AddOperation a = AddOperation.builder()
		.setName("a")
		.setConstant(2)
		.addInput("input")
		.addOutput("a")
		.newOperation();
	
	public static final AddOperation b1 = AddOperation.builder()
		.setName("b.1")
		.setConstant(4)
		.addInput("a")
		.addOutput("b.1")
		.newOperation();
	
	public static final AddOperation b2 = AddOperation.builder()
		.setName("b.2")
		.setConstant(8)
		.addInput("a")
		.addOutput("b.2")
		.newOperation();
	
	public static final AddOperation c = AddOperation.builder()
		.setName("c")
		.setConstant(16)
		.addInput("b.1")
		.addInput("b.2")
		.addOutput("c")
		.newOperation();
	
}
