package msdingfield.easyflow.testsupport;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class TestExecutor implements Executor, TestRule {

	private ExecutorService executor = null;
	
	@Override
	public void execute(final Runnable runnable) {
		executor.execute(runnable);
	}

	@Override
	public Statement apply(final Statement stmt, final Description desc) {
		return new Statement(){

			@Override
			public void evaluate() throws Throwable {
				try {
					executor = Executors.newCachedThreadPool();
					stmt.evaluate();
				} finally {
					executor.shutdownNow();
				}
			}};
	}

}
