package com.alibou.ecommerce;

import com.alibou.ecommerce.exception.ProductPurchaseException;
import com.alibou.ecommerce.product.*;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProductServiceTest {

	@Mock
	private ProductRepository repository;

	@Mock
	private ProductMapper mapper;

	@InjectMocks
	private ProductService productService;

	private Product product;
	private ProductRequest productRequest;
	private ProductResponse productResponse;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		product = new Product(1, "Test Product", "Description", 10, BigDecimal.valueOf(100), null);
		productRequest = new ProductRequest(
				null, // ID будет сгенерирован при создании
				"Test Product",
				"Description",
				10,
				BigDecimal.valueOf(100),
				null // categoryId может быть null, если не используется
		);
		productResponse = new ProductResponse(
				1,
				"Test Product",
				"Description",
				10,
				BigDecimal.valueOf(100),
				null, // categoryId
				null, // categoryName
				null  // categoryDescription
		);
	}

	@Test
	void createProduct_ShouldReturnProductId() {
		when(mapper.toProduct(productRequest)).thenReturn(product);
		when(repository.save(product)).thenReturn(product);

		Integer productId = productService.createProduct(productRequest);

		assertEquals(1, productId);
		verify(repository).save(product);
	}

	@Test
	void findById_ShouldReturnProductResponse() {
		when(repository.findById(1)).thenReturn(Optional.of(product));
		when(mapper.toProductResponse(product)).thenReturn(productResponse);

		ProductResponse response = productService.findById(1);

		assertEquals(productResponse, response);
	}

	@Test
	void findById_ShouldThrowEntityNotFoundException() {
		when(repository.findById(1)).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class, () -> productService.findById(1));
	}

	@Test
	void findAll_ShouldReturnListOfProductResponses() {
		when(repository.findAll()).thenReturn(Arrays.asList(product));
		when(mapper.toProductResponse(product)).thenReturn(productResponse);

		List<ProductResponse> responses = productService.findAll();

		assertEquals(1, responses.size());
		assertEquals(productResponse, responses.get(0));
	}

	@Test
	void purchaseProducts_ShouldThrowProductPurchaseException_WhenInsufficientStock() {
		ProductPurchaseRequest purchaseRequest = new ProductPurchaseRequest(1, 15);
		List<ProductPurchaseRequest> requests = Collections.singletonList(purchaseRequest);
		when(repository.findAllByIdInOrderById(Arrays.asList(1))).thenReturn(Collections.singletonList(product));

		assertThrows(ProductPurchaseException.class, () -> productService.purchaseProducts(requests));
	}

	@Test
	void purchaseProducts_ShouldThrowProductPurchaseException_WhenProductNotFound() {
		ProductPurchaseRequest purchaseRequest = new ProductPurchaseRequest(1, 5);
		List<ProductPurchaseRequest> requests = Collections.singletonList(purchaseRequest);
		when(repository.findAllByIdInOrderById(Arrays.asList(1))).thenReturn(Collections.emptyList());

		assertThrows(ProductPurchaseException.class, () -> productService.purchaseProducts(requests));
	}
}
