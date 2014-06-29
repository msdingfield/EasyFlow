package msdingfield.easyflow.reflect;

import msdingfield.easyflow.reflect.ClassOperationTest.TestOperation;
import msdingfield.easyflow.reflect.support.InvalidOperationBindingException;

import org.junit.Test;

public class ClassOperationValidationTest {

	/////////////////////////////////////////
	public interface InterfaceOperation {
		void op();
	}
	
	@Test(expected=InvalidOperationBindingException.class)
	public void testInterfaceOperation() throws NoSuchMethodException {
		makeOp(TestOperation.class);
	}
	
	//////////////////////////////////////////
	private static class PrivateOperation {
		public void op() {}
	}
	
	@Test(expected=InvalidOperationBindingException.class)
	public void testPrivateOperation() throws NoSuchMethodException {
		makeOp(PrivateOperation.class);
	}

	////////////////////////////////////////////
	public static class ConstructorWithParameter {
		public ConstructorWithParameter(int n) {}
		public void op() {}
	}
	
	@Test(expected=InvalidOperationBindingException.class)
	public void testConstructorWithParameter() throws NoSuchMethodException, SecurityException {
		ClassOperation op = new ClassOperation();
		op.setOperationClass(ConstructorWithParameter.class);
		op.setConstructor(ConstructorWithParameter.class.getDeclaredConstructor(int.class));
		op.setOperationMethod(ConstructorWithParameter.class.getDeclaredMethod("op"));
		new ClassOperationProxy(op);
	}
	
	///////////////////////////////////////////////
	public static class OperationWithParameter {
		public void op(int n) {}
	}
	
	@Test(expected=InvalidOperationBindingException.class)
	public void testOperationWithParameter() throws NoSuchMethodException, SecurityException {
		ClassOperation op = new ClassOperation();
		op.setOperationClass(OperationWithParameter.class);
		op.setConstructor(OperationWithParameter.class.getDeclaredConstructor());
		op.setOperationMethod(OperationWithParameter.class.getDeclaredMethod("op", int.class));
		new ClassOperationProxy(op);
	}
	
	/////////////////////////////////////////
	private void makeOp(final Class<?> clazz)
			throws NoSuchMethodException {
		ClassOperation op = new ClassOperation();
		op.setOperationClass(clazz);
		op.setConstructor(clazz.getDeclaredConstructor());
		op.setOperationMethod(clazz.getDeclaredMethod("op"));
		new ClassOperationProxy(op);
	}
}
