package msdingfield.easyflowexample.model;

public class StockBalance {
	private final String symbol;
	private final Integer quantity;
	
	public StockBalance(final String symbol, final Integer quantity) {
		this.symbol = symbol;
		this.quantity = quantity;
	}

	public String getSymbol() {
		return symbol;
	}
	
	public Integer getQuantity() {
		return quantity;
	}
}
