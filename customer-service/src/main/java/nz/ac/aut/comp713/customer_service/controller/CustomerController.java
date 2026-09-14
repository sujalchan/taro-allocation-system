package nz.ac.aut.comp713.customer_service.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;
import nz.ac.aut.comp713.customer_service.dto.CustomerRequest;
import nz.ac.aut.comp713.customer_service.dto.CustomerResponse;
import nz.ac.aut.comp713.customer_service.service.CustomerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import nz.ac.aut.comp713.customer_service.dto.ApiError;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public List<CustomerResponse> getAllCustomers() {
        return customerService.getAllCustomers();
    }

    @Operation(summary = "Retrieve a customer by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Customer retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Customer not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/{id}")
    public CustomerResponse getCustomerById(@PathVariable Long id) {
        return customerService.getCustomerById(id);
    }

    @Operation(summary = "Create a customer")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Customer created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid customer data", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Customer already exists", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(
            @Valid @RequestBody CustomerRequest request) {

        CustomerResponse created = customerService.createCustomer(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(created);
    }

    @Operation(summary = "Update an existing customer")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Customer updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid customer data", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Customer not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Customer name already exists", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    @PutMapping("/{id}")
    public CustomerResponse updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequest request) {

        return customerService.updateCustomer(id, request);
    }
}