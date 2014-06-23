package msdingfield.easyflow.reflect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URLClassLoader;
import java.util.List;

import msdingfield.easyflow.reflect.classscannertest.TestClassA;
import msdingfield.easyflow.reflect.classscannertest.TestClassB;
import msdingfield.easyflow.reflect.classscannertest.nestedpackage.SubpackageClass;

import org.junit.Test;

import com.google.common.collect.Lists;

public class TestClassScannerTest {

	@Test
	public void testTopLevelClasses() {
		final List<Class<?>> matches = Lists.newArrayList(
				ClassScanner.from(
						(URLClassLoader)getClass().getClassLoader(), 
						"msdingfield.easyflow.reflect.classscannertest"));
		assertEquals(3, matches.size());
		assertTrue(matches.contains(TestClassA.class));
		assertTrue(matches.contains(TestClassB.class));
		assertTrue(matches.contains(SubpackageClass.class));
	}

	@Test
	public void testInnerClasses() {
		final List<Class<?>> matches = Lists.newArrayList(ClassScanner.from(TestClassA.class));
		assertEquals(2, matches.size());
		assertTrue(matches.contains(TestClassA.InnerClass.class));
		assertTrue(matches.contains(TestClassA.StaticInnerClass.class));
	}
}
