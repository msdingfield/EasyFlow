package msdingfield.easyflow.reflect;

import msdingfield.easyflow.reflect.support.InvalidOperationBindingException;

import org.junit.Test;

public class ClassOperationValidationTest {

	/////////////////////////////////////////
	public static abstract class AbstractOperation {
		public static boolean isExecuted = false;
		public void op() {
			isExecuted = true;
		}
	}

	@Test(expected=InvalidOperationBindingException.class)
	public void testAbstractOperation() throws NoSuchMethodException {
		makeOp(AbstractOperation.class);
	}

	/////////////////////////////////////////
	public interface InterfaceOperation {
		void op();
	}

	@Test(expected=NoSuchMethodException.class)
	public void testInterfaceOperation() throws NoSuchMethodException {
		makeOp(InterfaceOperation.class);
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
		public ConstructorWithParameter(final int n) {}
		public void op() {}
	}

	@Test(expected=InvalidOperationBindingException.class)
	public void testConstructorWithParameter() throws NoSuchMethodException, SecurityException {
		final ClassOperation op = new ClassOperation();
		op.setOperationClass(ConstructorWithParameter.class);
		op.setConstructor(ConstructorWithParameter.class.getDeclaredConstructor(int.class));
		op.setOperationMethod(ConstructorWithParameter.class.getDeclaredMethod("op"));
		new ClassOperationProxy(op);
	}

	///////////////////////////////////////////////
	public static class OperationWithParameter {
		public void op(final int n) {}
	}

	@Test(expected=InvalidOperationBindingException.class)
	public void testOperationWithParameter() throws NoSuchMethodException, SecurityException {
		final ClassOperation op = new ClassOperation();
		op.setOperationClass(OperationWithParameter.class);
		op.setConstructor(OperationWithParameter.class.getDeclaredConstructor());
		op.setOperationMethod(OperationWithParameter.class.getDeclaredMethod("op", int.class));
		new ClassOperationProxy(op);
	}

	/////////////////////////////////////////
	private void makeOp(final Class<?> clazz)
			throws NoSuchMethodException {
		final ClassOperation op = new ClassOperation();
		op.setOperationClass(clazz);
		op.setConstructor(clazz.getDeclaredConstructor());
		op.setOperationMethod(clazz.getDeclaredMethod("op"));
		new ClassOperationProxy(op);
	}
}
