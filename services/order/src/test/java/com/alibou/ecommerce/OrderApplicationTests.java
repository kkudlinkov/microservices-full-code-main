package com.alibou.ecommerce;

import com.alibou.ecommerce.customer.Customer;
import com.alibou.ecommerce.customer.CustomerClient;
import com.alibou.ecommerce.exception.BusinessException;
import com.alibou.ecommerce.kafka.OrderConfirmation;
import com.alibou.ecommerce.kafka.OrderProducer;
import com.alibou.ecommerce.order.*;
import com.alibou.ecommerce.orderline.OrderLineRequest;
import com.alibou.ecommerce.orderline.OrderLineService;
import com.alibou.ecommerce.payment.PaymentClient;
import com.alibou.ecommerce.payment.PaymentRequest;
import com.alibou.ecommerce.product.ProductClient;
import com.alibou.ecommerce.product.PurchaseRequest;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderServiceTest {

	@Mock
	private OrderRepository repository;

	@Mock
	private OrderMapper mapper;

	@Mock
	private CustomerClient customerClient;

	@Mock
	private PaymentClient paymentClient;

	@Mock
	private ProductClient productClient;

	@Mock
	private OrderLineService orderLineService;

	@Mock
	private OrderProducer orderProducer;

	@InjectMocks
	private OrderService orderService;

	private OrderRequest orderRequest;
	private Order order;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		orderRequest = new OrderRequest(
				null, // ID будет сгенерирован при создании
				"ref123",
				BigDecimal.valueOf(100),
				PaymentMethod.CREDIT_CARD,
				"CUST123",
				List.of(new PurchaseRequest(1, 2))
		);
		order = new Order(1, "ref123", BigDecimal.valueOf(100), PaymentMethod.CREDIT_CARD, "CUST123", null, null, null);
	}

	@Test
	void createOrder_ShouldThrowBusinessException_WhenCustomerNotFound() {
		when(customerClient.findCustomerById("CUST123")).thenReturn(Optional.empty());

		assertThrows(BusinessException.class, () -> orderService.createOrder(orderRequest));
	}

	@Test
	void findAllOrders_ShouldReturnListOfOrderResponses() {
		when(repository.findAll()).thenReturn(List.of(order));
		when(mapper.fromOrder(order)).thenReturn(new OrderResponse(1, "ref123", BigDecimal.valueOf(100), PaymentMethod.CREDIT_CARD, "CUST123"));

		List<OrderResponse> responses = orderService.findAllOrders();

		assertEquals(1, responses.size());
		assertEquals("ref123", responses.get(0).reference());
		assertEquals(BigDecimal.valueOf(100), responses.get(0).amount());
		assertEquals(PaymentMethod.CREDIT_CARD, responses.get(0).paymentMethod());
		assertEquals("CUST123", responses.get(0).customerId());
	}

	@Test
	void findById_ShouldReturnOrderResponse() {
		when(repository.findById(1)).thenReturn(Optional.of(order));
		when(mapper.fromOrder(order)).thenReturn(new OrderResponse(1, "ref123", BigDecimal.valueOf(100), PaymentMethod.CREDIT_CARD, "CUST123"));

		OrderResponse response = orderService.findById(1);

		assertEquals(1, response.id());
		assertEquals("ref123", response.reference());
		assertEquals(BigDecimal.valueOf(100), response.amount());
		assertEquals(PaymentMethod.CREDIT_CARD, response.paymentMethod());
		assertEquals("CUST123", response.customerId());
	}

	@Test
	void findById_ShouldThrowEntityNotFoundException() {
		when(repository.findById(1)).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class, () -> orderService.findById(1));
	}
}