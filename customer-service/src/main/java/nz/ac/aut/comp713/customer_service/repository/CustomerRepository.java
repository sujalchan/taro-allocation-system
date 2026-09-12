package nz.ac.aut.comp713.customer_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import nz.ac.aut.comp713.customer_service.model.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
