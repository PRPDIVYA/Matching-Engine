package com.baraka.matching_engine.dto;

import java.math.BigDecimal;

import com.baraka.matching_engine.util.Direction;
import com.baraka.matching_engine.util.ValidateDirection;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderRequest {

	@NotBlank(message = "asset cannot be blank.")
	private String asset;
	@NotNull(message = "Invalid price.Price must be greater than 0.0")
	@DecimalMin(value = "0.0", inclusive = false, message = "Invalid price.Price must be greater than 0.0")
	private BigDecimal price;
	@NotNull(message = "Invalid amount.Amount must be greater than 0.0")
	@DecimalMin(value = "0.0", inclusive = false, message = "Invalid amount.Amount must be greater than 0.0")
	private BigDecimal amount;
	@ValidateDirection(enumClass = Direction.class, message = "Invalid direction value.Must be either BUY or SELL")
	private String direction;
}
