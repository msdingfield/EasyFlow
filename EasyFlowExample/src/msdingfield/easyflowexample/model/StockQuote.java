package msdingfield.easyflowexample.model;

public class StockQuote {

	private final String symbol;
	private final Double amount;
	
	public StockQuote(final String symbol, final Double amount) {
		this.symbol = symbol;
		this.amount = amount;
	}

	public String getSymbol() {
		return symbol;
	}

	public Double getAmount() {
		return amount;
	}

}
