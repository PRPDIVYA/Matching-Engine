package com.baraka.matching_engine.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.baraka.matching_engine.model.Order;
import com.baraka.matching_engine.model.Trade;
import com.baraka.matching_engine.service.impl.OrderBookServiceImpl;

public class OrderBookServiceTest {

	private OrderBookService orderBookService;

	@BeforeEach
	void setup() {
		orderBookService = new OrderBookServiceImpl();
	}

	@Test
	void testPlaceBuyOrder() {
		Order order = orderBookService.placeOrder("BTC", new BigDecimal("1.0"), new BigDecimal("43251.00"), "BUY");

		assertNotNull(order);
		assertEquals(0, order.getId());
		assertEquals("BTC", order.getAsset());
		assertEquals(new BigDecimal("43251.00"), order.getPrice());
		assertEquals(new BigDecimal("1.0"), order.getAmount());
		assertEquals(new BigDecimal("1.0"), order.getPendingAmount());
		assertTrue(order.getTrades().isEmpty());
		assertEquals("BUY", order.getDirection());
	}

	@Test
	void testPlaceSellOrderWithMatchingBuyOrder() {
		orderBookService.placeOrder("BTC", new BigDecimal("1.0"), new BigDecimal("43251.00"), "BUY");
		Order sellOrder = orderBookService.placeOrder("BTC", new BigDecimal("0.5"), new BigDecimal("43251.00"), "SELL");

		assertNotNull(sellOrder);
		assertEquals(1, sellOrder.getId());
		assertEquals(new BigDecimal("0.0"), sellOrder.getPendingAmount());
		assertEquals(1, sellOrder.getTrades().size());

		Trade trade = sellOrder.getTrades().get(0);
		assertEquals(0, trade.getOrderId());
		assertEquals(new BigDecimal("0.5"), trade.getAmount());
		assertEquals(new BigDecimal("43251.00"), trade.getPrice());

		Order buyOrder = orderBookService.getOrder(1);
		assertEquals(new BigDecimal("0.0"), buyOrder.getPendingAmount());
	}

	@Test
	void testPartialFillMultipleMatches() {
		orderBookService.placeOrder("BTC", new BigDecimal("0.2"), new BigDecimal("43250.00"), "SELL");
		orderBookService.placeOrder("BTC", new BigDecimal("0.3"), new BigDecimal("43251.00"), "SELL");
		Order buyOrder = orderBookService.placeOrder("BTC", new BigDecimal("0.4"), new BigDecimal("43251.00"), "BUY");

		assertEquals(new BigDecimal("0.0"), buyOrder.getPendingAmount());
		assertEquals(2, buyOrder.getTrades().size());

		Trade trade1 = buyOrder.getTrades().get(0);
		assertEquals(0, trade1.getOrderId());
		assertEquals(new BigDecimal("0.2"), trade1.getAmount());
		assertEquals(new BigDecimal("43250.00"), trade1.getPrice());

		Trade trade2 = buyOrder.getTrades().get(1);
		assertEquals(1, trade2.getOrderId());
		assertEquals(new BigDecimal("0.2"), trade2.getAmount());
		assertEquals(new BigDecimal("43251.00"), trade2.getPrice());
	}

	@Test
	void testFifoMatchingSamePrice() {
		orderBookService.placeOrder("BTC", new BigDecimal("0.3"), new BigDecimal("43251.00"), "SELL");
		orderBookService.placeOrder("BTC", new BigDecimal("0.3"), new BigDecimal("43251.00"), "SELL");
		Order buyOrder = orderBookService.placeOrder("BTC", new BigDecimal("0.4"), new BigDecimal("43251.00"), "BUY");

		assertEquals(2, buyOrder.getTrades().size());
		assertEquals(0, buyOrder.getTrades().get(0).getOrderId()); 
		assertEquals(1, buyOrder.getTrades().get(1).getOrderId()); 
	}

	@Test
	void testThreadSafety() throws InterruptedException {
		ExecutorService executor = Executors.newFixedThreadPool(10);
		for (int i = 0; i < 100; i++) {
			final int index = i;
			executor.submit(() -> {
				orderBookService.placeOrder("BTC", new BigDecimal("0.1"), new BigDecimal("43251.00"),
						index % 2 == 0 ? "BUY" : "SELL");
			});
		}
		executor.shutdown();
		executor.awaitTermination(10, TimeUnit.SECONDS);

		for (long i = 0; i < 100; i++) {
			Order order = orderBookService.getOrder(i);
			assertEquals(i, order.getId());
		}
	}
}
