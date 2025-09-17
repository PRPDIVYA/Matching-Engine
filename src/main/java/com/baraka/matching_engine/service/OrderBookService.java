package com.baraka.matching_engine.service;

import java.math.BigDecimal;

import com.baraka.matching_engine.model.Order;

public interface OrderBookService {

	public  Order placeOrder(String asset, BigDecimal amount,BigDecimal price , String direction);
	public Order getOrder(long orderId);
	public String cancelOrder(long orderId);
}
