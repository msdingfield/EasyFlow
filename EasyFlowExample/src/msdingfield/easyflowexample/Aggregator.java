package msdingfield.easyflowexample;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import msdingfield.easyflow.annotations.Aggregate;
import msdingfield.easyflow.annotations.Input;
import msdingfield.easyflow.annotations.Operation;
import msdingfield.easyflow.annotations.Output;
import msdingfield.easyflow.annotations.ForkOn;
import msdingfield.easyflow.annotations.Scope;
import msdingfield.easyflow.execution.DefaultExecutor;
import msdingfield.easyflow.execution.GroupTask;
import msdingfield.easyflow.execution.Task;
import msdingfield.easyflow.graph.FlowGraph;
import msdingfield.easyflow.graph.FlowGraphTaskBuilder;
import msdingfield.easyflow.reflect.ClassOperationFlowNode;
import msdingfield.easyflow.reflect.ClassPathScannerFlowGraphBuilder;
import msdingfield.easyflow.reflect.ClassOperation;
import msdingfield.easyflow.reflect.ClassOperationTaskFactory;
import msdingfield.easyflow.reflect.Context;
import msdingfield.easyflowexample.dal.LastViewedDao;
import msdingfield.easyflowexample.dal.PortfolioDao;
import msdingfield.easyflowexample.dal.StockQuoteDao;
import msdingfield.easyflowexample.model.StockBalance;
import msdingfield.easyflowexample.model.StockQuote;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ListenableFuture;

public class Aggregator {

	@Scope("equities")
	public static class GetPortfolio {
		
		private PortfolioDao dao = new PortfolioDao();
		
		@Input
		public String clientId;
		
		@Output
		public ListenableFuture<Set<String>> portfolioSymbols;
		
		@Operation
		public void doLookup() {
			portfolioSymbols = dao.getPortfolio(clientId);
		}
	}
	
	@Scope("equities")
	public static class GetPortfolioQuotes {
		private StockQuoteDao dao = new StockQuoteDao();
		
		@ForkOn
		@Input(port="portfolioSymbols")
		public String portfolioSymbol;
		
		@Aggregate
		@Output(port="portfolioQuotes")
		public ListenableFuture<StockQuote> portfolioQuote;
		
		@Operation
		public void begin() {
			portfolioQuote = dao.getCurrentQuote(portfolioSymbol);			
		}
	}
	
	@Scope("equities")
	public static class GetBalance {
		private PortfolioDao dao = new PortfolioDao();
		
		@Input
		public String clientId;
		
		@ForkOn
		@Input(port="portfolioSymbols")
		public String portfolioSymbol;
		
		@Aggregate
		@Output(port="portfolioBalances")
		public StockBalance portfolioBalance; 
		
		@Operation
		public void enact() {
			final ListenableFuture<Integer> quantity = dao.getQuantity(clientId, portfolioSymbol);
			Task.fork(quantity, new Runnable(){

				@Override
				public void run() {
					try {
						portfolioBalance = new StockBalance(portfolioSymbol, quantity.get());
					} catch (Exception e) {
						portfolioBalance = null;
					}
				}});
		}
	}

	@Scope("equities")
	public static class GetLastViewed {
		private LastViewedDao dao = new LastViewedDao();
		
		@Input
		public String clientId;
		
		@Output
		public ListenableFuture<Set<String>> viewedSymbols;
		
		@Operation
		public void enact() {
			viewedSymbols = dao.getLastViewedStocks(clientId);
		}
	}

	@Scope("equities")
	public static class GetLastViewedQuotes {
		private StockQuoteDao dao = new StockQuoteDao();
		
		@ForkOn
		@Input(port="viewedSymbols")
		public String viewedSymbol;
		
		@Aggregate
		@Output(port="viewedQuotes")
		public ListenableFuture<StockQuote> viewedQuote;
		
		@Operation
		public void enact() {
			viewedQuote = dao.getCurrentQuote(viewedSymbol);			
		}
	}
	
	@Scope("equities")
	public static class DisplayAll {
		
		@Input
		public Collection<String> portfolioSymbols;
		
		@Input
		public Collection<StockQuote> portfolioQuotes;
		
		@Input
		public Collection<StockBalance> portfolioBalances;
		
		@Input 
		public Collection<String> viewedSymbols;
		
		@Input
		public Collection<StockQuote> viewedQuotes;
		
		private static class Line {
			public Line(final String symbol) {
				this.symbol = symbol;
			}
			public String symbol = null;
			public Integer quantity = null;
			public Double value = null;
		}
		
		private static Line get(final Map<String,Line> lines, final String symbol) {
			if (!lines.containsKey(symbol)) {
				lines.put(symbol, new Line(symbol));
				
			}
			return lines.get(symbol);
		}
		
		@Operation
		public void enact() {
			final Map<String,Line> portfolioLines = Maps.newHashMap();
			for (final String symbol : portfolioSymbols) {
				get(portfolioLines, symbol);
			}
			for(final StockQuote quote : portfolioQuotes) {
				final String symbol = quote.getSymbol();
				final Double amount = quote.getAmount();
				final Line line = get(portfolioLines, symbol);
				line.value = amount;
			}
			for(final StockBalance bal : portfolioBalances) {
				get(portfolioLines, bal.getSymbol()).quantity = bal.getQuantity();
			}
			
			final Map<String,Line> viewLines = Maps.newHashMap();
			for (final String symbol : viewedSymbols) {
				get(viewLines, symbol);
			}
			for (final StockQuote quote : viewedQuotes) {
				get(viewLines, quote.getSymbol()).value = quote.getAmount();
			}
			
			for (final Line line : portfolioLines.values()) {
				System.out.printf("%s: QTY: %d VAL: %f\n", line.symbol, line.quantity, line.value);
			}
			
			for (final Line line : viewLines.values()) {
				System.out.printf("%s: VAL: %f\n", line.symbol, line.value);
			}
		}
	}

	public static void show(final String clientId) throws InterruptedException {
		final List<ClassOperation> operations = ClassPathScannerFlowGraphBuilder.loadOperationsOnClasspath("msdingfield.easyflowexample", "equities");
		final FlowGraph<ClassOperationFlowNode> system = new FlowGraph<ClassOperationFlowNode>(Sets.newHashSet(Lists.transform(operations, new Function<ClassOperation, ClassOperationFlowNode>(){
			@Override public ClassOperationFlowNode apply(ClassOperation arg0) {
				return new ClassOperationFlowNode(arg0);
			}})));
		final Context context = new Context();
		context.putPortValue("clientId", clientId);
		FlowGraphTaskBuilder
			.graph(system)
			.taskFactory(ClassOperationTaskFactory.get(context))
			.build()
			.schedule()
			.waitForCompletion();
	}
}
