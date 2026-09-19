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
    public List<CustomerResponse> getAllCustomers(String search) {
        return customerRepository.findAll()
                .stream()
                .filter(customer -> matchesSearch(customer, search))
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

    // Update an existing customer
    @Transactional
    public CustomerResponse updateCustomer(Long id, CustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));

        String name = request.name();
        String normalizedName = name.toLowerCase().replaceAll("\\s+", "");

        customer.setName(name);
        customer.setNormalizedName(normalizedName);
        customer.setContactName(request.contactName());
        customer.setPhone(request.phone());

        // Set the active status if provided in the request
        if (request.active() != null) {
            customer.setActive(request.active());
        }

        // Save the updated customer entity to the database
        try {
            Customer updatedCustomer = customerRepository.saveAndFlush(customer);
            return toResponse(updatedCustomer);
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

    // Helper method for search query
    private boolean matchesSearch(Customer customer, String search) {
        if (search == null || search.isBlank()) {
            return true;
        }

        String query = search.trim().toLowerCase();

        return customer.getName().toLowerCase().contains(query)
                || (customer.getContactName() != null
                        && customer.getContactName().toLowerCase().contains(query))
                || (customer.getPhone() != null
                        && customer.getPhone().toLowerCase().contains(query));
    }
}
