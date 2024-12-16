package com.alibou.ecommerce;

import com.alibou.ecommerce.customer.*;
import com.alibou.ecommerce.exception.CustomerNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CustomerApplicationTests {

	@InjectMocks
	private CustomerService customerService;

	@Mock
	private CustomerRepository repository;

	@Mock
	private CustomerMapper mapper;

	private CustomerRequest customerRequest;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		customerRequest = new CustomerRequest("1", "John", "Doe", "john@example.com", new Address("Street 1", "House 2", "12345"));
	}

	@Test
	void createCustomer_success() {
		// Arrange
		Customer customer = Customer.builder()
				.id("1")
				.firstname("John")
				.email("john@example.com")
				.address(new Address("Street 1", "City", "Country"))
				.build();

		when(mapper.toCustomer(customerRequest)).thenReturn(customer);
		when(repository.save(customer)).thenReturn(customer);

		// Act
		String customerId = customerService.createCustomer(customerRequest);

		// Assert
		assertEquals("1", customerId);
		verify(repository, times(1)).save(any(Customer.class));
	}

	@Test
	void updateCustomer_success() {
		// Arrange
		Customer existingCustomer = Customer.builder()
				.id("1")
				.firstname("John")
				.lastname("Doe")
				.email("john@example.com")
				.address(new Address("Street 1", "House 2", "12345"))
				.build();

		CustomerRequest updateRequest = new CustomerRequest("1", "Jane", null, "jane@example.com", null);

		when(repository.findById("1")).thenReturn(Optional.of(existingCustomer));
		when(repository.save(any(Customer.class))).thenReturn(existingCustomer);

		// Act
		customerService.updateCustomer(updateRequest);

		// Assert
		assertEquals("Jane", existingCustomer.getFirstname());
		assertEquals("jane@example.com", existingCustomer.getEmail());
		verify(repository, times(1)).save(existingCustomer);
	}

	@Test
	void updateCustomer_customerNotFound() {
		// Arrange
		CustomerRequest updateRequest = new CustomerRequest("1", "Jane", null, "jane@example.com", null);

		when(repository.findById("1")).thenReturn(Optional.empty());

		// Act & Assert
		assertThrows(CustomerNotFoundException.class, () -> customerService.updateCustomer(updateRequest));
	}

	@Test
	void findAllCustomers_success() {
		// Arrange
		Customer customer1 = Customer.builder()
				.id("1")
				.firstname("John")
				.lastname("Doe")
				.email("john@example.com")
				.address(new Address("Street 1", "House 2", "12345"))
				.build();

		Customer customer2 = Customer.builder()
				.id("2")
				.firstname("Jane")
				.lastname("Doe")
				.email("jane@example.com")
				.address(new Address("Street 2", "House 3", "54321"))
				.build();

		List<Customer> customers = List.of(customer1, customer2);
		when(repository.findAll()).thenReturn(customers);
		when(mapper.fromCustomer(customer1)).thenReturn(new CustomerResponse("1", "John", "Doe", "john@example.com", new Address("Street 1", "House 2", "12345")));
		when(mapper.fromCustomer(customer2)).thenReturn(new CustomerResponse("2", "Jane", "Doe", "jane@example.com", new Address("Street 2", "House 3", "54321")));

		// Act
		List<CustomerResponse> customerResponses = customerService.findAllCustomers();

		// Assert
		assertEquals(2, customerResponses.size());
		assertEquals("John", customerResponses.get(0).firstname());
		assertEquals("Jane", customerResponses.get(1).firstname());
	}

	@Test
	void findById_success() {
		// Arrange
		Customer customer = Customer.builder()
				.id("1")
				.firstname("John")
				.lastname("Doe")
				.email("john@example.com")
				.address(new Address("Street 1", "House 2", "12345"))
				.build();

		when(repository.findById("1")).thenReturn(Optional.of(customer));
		when(mapper.fromCustomer(customer)).thenReturn(new CustomerResponse("1", "John", "Doe", "john@example.com", new Address("Street 1", "House 2", "12345")));

		// Act
		CustomerResponse customerResponse = customerService.findById("1");

		// Assert
		assertEquals("John", customerResponse.firstname());
	}

	@Test
	void findById_customerNotFound() {
		// Arrange
		when(repository.findById("1")).thenReturn(Optional.empty());

		// Act & Assert
		assertThrows(CustomerNotFoundException.class, () -> customerService.findById("1"));
	}

	@Test
	void deleteCustomer_success() {
		// Arrange
		String customerId = "1";

		// Act
		customerService.deleteCustomer(customerId);

		// Assert
		verify(repository, times(1)).deleteById(customerId);
	}
}
