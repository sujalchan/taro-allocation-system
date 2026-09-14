package nz.ac.aut.comp713.customer_service.service;

import java.util.List;

import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import nz.ac.aut.comp713.customer_service.dto.CustomerRequest;
import nz.ac.aut.comp713.customer_service.dto.CustomerResponse;
import nz.ac.aut.comp713.customer_service.exception.CustomerAlreadyExistsException;
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
    @Transactional
    public CustomerResponse createCustomer(CustomerRequest request) {

        String name = request.name();
        String normalizedName = name.toLowerCase().replaceAll("\\s+", "");

        Customer customer = new Customer();
        customer.setName(name);
        customer.setNormalizedName(normalizedName);
        customer.setContactName(request.contactName());
        customer.setPhone(request.phone());

        // Set the active status if provided in the request
        if (request.active() != null) {
            customer.setActive(request.active());
        }

        // Save the customer entity to the database
        try {
            Customer savedCustomer = customerRepository.saveAndFlush(customer);
            return toResponse(savedCustomer);
        } catch (JpaSystemException e) {
            throw new CustomerAlreadyExistsException(name);
        }
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
