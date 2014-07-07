package msdingfield.easyflowexample.dal;

import java.util.Set;

import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

public class LastViewedDao {
	
	public ListenableFuture<Set<String>> getLastViewedStocks(final String customerId) {
		final SettableFuture<Set<String>> future = SettableFuture.create();
		LatencySimulator.setWithLatency(future, Sets.newHashSet("T", "MMM"),  750L);
		return future;
	}
}
