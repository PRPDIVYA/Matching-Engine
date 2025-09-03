package com.baraka.matching_engine.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.baraka.matching_engine.dto.OrderRequest;
import com.baraka.matching_engine.model.Order;
import com.baraka.matching_engine.model.Trade;
import com.baraka.matching_engine.service.OrderBookService;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
public class OrderControllerTest {

	private MockMvc mockMvc;

	private ObjectMapper objectMapper;

	private OrderBookService orderBookService;

	@BeforeEach
	void setup() {
		orderBookService = mock(OrderBookService.class);
		objectMapper = new ObjectMapper();
		mockMvc = MockMvcBuilders.standaloneSetup(new OrderController(orderBookService, objectMapper)).build();
	}

	@Test
	void testPlaceOrderSuccess() throws Exception {
		OrderRequest request = new OrderRequest();
		request.setAsset("BTC");
		request.setPrice(new BigDecimal("43251.00"));
		request.setAmount(new BigDecimal("1.0"));
		request.setDirection("SELL");

		Order order = new Order(1, "BTC", new BigDecimal("43251.00"), new BigDecimal("1.0"), "SELL", new ArrayList<>(),
				new BigDecimal("1.0"));
		when(orderBookService.placeOrder(any(), any(), any(), any())).thenReturn(order);

		mockMvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1)).andExpect(jsonPath("$.asset").value("BTC"))
				.andExpect(jsonPath("$.price").value(43251.00)).andExpect(jsonPath("$.amount").value(1.0))
				.andExpect(jsonPath("$.direction").value("SELL")).andExpect(jsonPath("$.pendingAmount").value(1.0))
				.andExpect(jsonPath("$.trades").isEmpty());
	}

	@Test
	void testPlaceOrderInvalidInput() throws Exception {
		OrderRequest request = new OrderRequest();
		request.setAsset(""); // Invalid asset
		request.setPrice(new BigDecimal("43251.00"));
		request.setAmount(new BigDecimal("1.0"));
		request.setDirection("SELL");

		mockMvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isBadRequest());
	}

	@Test
	void testGetOrderSuccess() throws Exception {
		// Prepare mock response
		Order order = new Order(1, "BTC", new BigDecimal("43251.00"), new BigDecimal("1.0"), "BUY", new ArrayList<>(),
				new BigDecimal("0.5"));
		order.getTrades().add(new Trade(2L, new BigDecimal("0.5"), new BigDecimal("43251.00")));
		when(orderBookService.getOrder(1L)).thenReturn(order);

		// Perform GET request
		mockMvc.perform(get("/orders/1")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.asset").value("BTC")).andExpect(jsonPath("$.price").value(43251.00))
				.andExpect(jsonPath("$.amount").value(1.0)).andExpect(jsonPath("$.pendingAmount").value(0.5))
				.andExpect(jsonPath("$.direction").value("BUY")).andExpect(jsonPath("$.trades[0].orderId").value(2))
				.andExpect(jsonPath("$.trades[0].amount").value(0.5))
				.andExpect(jsonPath("$.trades[0].price").value(43251.00));
		verify(orderBookService).getOrder(1);
	}

	@Test
	void testGetOrderNotFound() throws Exception {
		when(orderBookService.getOrder(1)).thenReturn(null);
		mockMvc.perform(get("/orders/1")).andExpect(status().isNotFound());
	}
}
