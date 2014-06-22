package msdingfield.easyflowexample.dal;

import java.util.Set;

import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

public class PortfolioDao {

	public ListenableFuture<Set<String>> getPortfolio(final String clientId) {
		final Set<String> portfolio = Sets.newHashSet("MSFT", "GOOG", "AAPL");
		final SettableFuture<Set<String>> future = SettableFuture.create();
		LatencySimulator.setWithLatency(future, portfolio, 500L);
		return future;
	}
	
	public ListenableFuture<Integer> getQuantity(final String clientId, final String symbol) {
		final SettableFuture<Integer> future = SettableFuture.create();
		Integer quantity = 0;
		switch(symbol) {
		case "MSFT": quantity = 100; break;
		case "GOOG": quantity = 5; break;
		case "AAPL": quantity = 15; break;
		}
		LatencySimulator.setWithLatency(future, quantity, 100L);
		return future;
	}
}
