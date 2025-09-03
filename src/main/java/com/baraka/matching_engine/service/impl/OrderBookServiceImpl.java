package com.baraka.matching_engine.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.PriorityQueue;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.baraka.matching_engine.model.Order;
import com.baraka.matching_engine.model.Trade;
import com.baraka.matching_engine.service.OrderBookService;
import com.baraka.matching_engine.util.Direction;

@Service
public class OrderBookServiceImpl implements OrderBookService {
	
	private final AtomicLong orderIdGenerator = new AtomicLong(0);
	private final ConcurrentHashMap<Long, Order> orders = new ConcurrentHashMap<>();

	private final TreeMap<BigDecimal, PriorityQueue<Order>> buyOrders = new TreeMap<>(Collections.reverseOrder());
	private final TreeMap<BigDecimal, PriorityQueue<Order>> sellOrders = new TreeMap<>();

	@Override
	public synchronized Order placeOrder(String asset, BigDecimal amount, BigDecimal price, String direction) {
		
		long orderId = orderIdGenerator.getAndIncrement();
		Order order = new Order(orderId, asset, price, amount, direction, new ArrayList<>(), amount);
		//Place all orders in orders Map based on OrderId
		orders.put(orderId, order);
		
		//Match the order
		matchOrder(order);
		
		if (order.getPendingAmount().compareTo(BigDecimal.ZERO) > 0) {
			TreeMap<BigDecimal, PriorityQueue<Order>> targetOrderBook = order.getDirection().equals(Direction.BUY.name()) ? buyOrders: sellOrders;
			targetOrderBook.computeIfAbsent(order.getPrice(), k -> new PriorityQueue<>((o1,o2)->o1.getTimestamp().compareTo(o2.getTimestamp()))).offer(order);
		}
		return order;
	}

	private void matchOrder(Order order) {
		//For Buy order pick SellOrderBook and Sell order pick BuyOrderBook
		TreeMap<BigDecimal, PriorityQueue<Order>> oppositeOrders = order.getDirection().equals(Direction.BUY.name())? sellOrders: buyOrders;
		BigDecimal orderPrice = order.getPrice();

		// Match order (for BUY: price <= orderPrice, for SELL: price >= orderPrice)
		NavigableMap<BigDecimal, PriorityQueue<Order>> matchingPriceLevels = order.getDirection().equals(Direction.BUY.name())
				? oppositeOrders.headMap(orderPrice,true)
				: oppositeOrders.tailMap(orderPrice,true);

		matchingPriceLevels.entrySet().stream()
				.sorted(order.getDirection().equals(Direction.BUY.name()) ? Comparator.comparing(Map.Entry::getKey)
						: Comparator.comparing(Map.Entry::getKey, Comparator.reverseOrder()))
				.takeWhile(entry -> order.getPendingAmount().compareTo(BigDecimal.ZERO) > 0)
				.flatMap(entry ->entry.getValue().stream().filter(ord->ord.getAsset().equals(order.getAsset()))) //Filter based on asset 
				.collect(Collectors.toList())
				.forEach(matchingOrder -> {
					if(order.getPendingAmount().compareTo(BigDecimal.ZERO) > 0) {
					BigDecimal tradeAmount = order.getPendingAmount().min(matchingOrder.getPendingAmount());
					BigDecimal tradePrice = matchingOrder.getPrice();

					// Create trades for both Buy and Sell orders
					Trade newTrade = new Trade(matchingOrder.getId(), tradeAmount, tradePrice);
					Trade counterTrade = new Trade(order.getId(), tradeAmount, tradePrice);

					order.addTrade(newTrade);
					matchingOrder.addTrade(counterTrade);
					}});
	}

	@Override
	public Order getOrder(long orderId) {
		//return from orders based on Id
		return orders.get(orderId);
	}

}
