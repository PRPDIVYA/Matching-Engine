package com.baraka.matching_engine.model;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Getter
@NoArgsConstructor
public class Trade {
	
	private  long orderId;
	private  BigDecimal amount;
	private  BigDecimal price;

}
