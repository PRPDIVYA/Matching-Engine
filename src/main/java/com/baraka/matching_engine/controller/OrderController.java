package com.baraka.matching_engine.controller;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baraka.matching_engine.dto.OrderRequest;
import com.baraka.matching_engine.dto.OrderResponse;
import com.baraka.matching_engine.model.Order;
import com.baraka.matching_engine.service.OrderBookService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/orders")
@AllArgsConstructor
public class OrderController {
	
	@Autowired
	private OrderBookService orderBookService;
	@Autowired
	private ObjectMapper objectMapper;



	@PostMapping
	public ResponseEntity<OrderResponse> placeOrder(@RequestBody @Valid OrderRequest request){
		Order order = orderBookService.placeOrder(request.getAsset(),request.getAmount(),request.getPrice(),request.getDirection());
		return ResponseEntity.ok(objectMapper.convertValue(order, OrderResponse.class));
	}

	
	@GetMapping("/{orderId}")
	public ResponseEntity<OrderResponse> getOrder(@PathVariable long orderId){
		Order order = orderBookService.getOrder(orderId);
		if(Objects.isNull(order))
			return ResponseEntity.notFound().build();
		return ResponseEntity.ok(objectMapper.convertValue(order, OrderResponse.class));
	} 
	
	@PostMapping("/cancelOrder/{orderId}")
	public ResponseEntity<String> cancelrder(@PathVariable long orderId){
		return ResponseEntity.ok(orderBookService.cancelOrder(orderId));
	}
}
