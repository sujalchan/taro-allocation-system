package nz.ac.aut.comp713.customer_service.service;

import java.util.List;

import org.springframework.stereotype.Service;

import nz.ac.aut.comp713.customer_service.dto.CustomerRequest;
import nz.ac.aut.comp713.customer_service.dto.CustomerResponse;
import nz.ac.aut.comp713.customer_service.exception.CustomerNotFoundException;
import nz.ac.aut.comp713.customer_service.model.Customer;
import nz.ac.aut.comp713.customer_service.repository.CustomerRepository;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    // Get all customers
    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // Get a customer by ID
    public CustomerResponse getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));

        return toResponse(customer);
    }

    // Create a new customer
    public CustomerResponse createCustomer(CustomerRequest request) {
        Customer customer = new Customer();
        customer.setName(customer.getName());
        customer.setContactName(customer.getContactName());
        customer.setPhone(customer.getPhone());

        // Set the active status if provided in the request
        if (request.active() != null) {
            customer.setActive(request.active());
        }

        // Save the customer entity to the database
        Customer savedCustomer = customerRepository.save(customer);
        return toResponse(savedCustomer);
    }

    // Helper method to convert Customer entity to CustomerResponse DTO
    private CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getName(),
                customer.getContactName(),
                customer.getPhone(),
                customer.isActive());
    }
}
