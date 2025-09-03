package com.baraka.matching_engine.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class Order {
	
	private final long id;
	private final String timestamp = Instant.now().toString();
	private final String asset;
	private final BigDecimal price;
	private final BigDecimal amount;
	private final String direction;
	private final List<Trade> trades;
	private BigDecimal pendingAmount;
	
	public void addTrade(Trade trade) {
		trades.add(trade);
		pendingAmount = pendingAmount.subtract(trade.getAmount());
		
	}

}
