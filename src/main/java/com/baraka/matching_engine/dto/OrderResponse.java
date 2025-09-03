package com.baraka.matching_engine.dto;

import java.math.BigDecimal;
import java.util.List;

import com.baraka.matching_engine.model.Trade;

import lombok.Data;

@Data
public class OrderResponse {

	private long id;
	private  String timestamp;
	private  String asset;
	private  BigDecimal price;
	private  BigDecimal amount;
	private  String direction;
	private  List<Trade> trades;
	private  BigDecimal pendingAmount;
}
