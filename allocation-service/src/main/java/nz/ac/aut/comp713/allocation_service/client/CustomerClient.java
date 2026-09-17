package nz.ac.aut.comp713.allocation_service.client;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import nz.ac.aut.comp713.allocation_service.exception.CustomerNotFoundException;
import nz.ac.aut.comp713.allocation_service.exception.CustomerServiceUnavailableException;
import nz.ac.aut.comp713.allocation_service.exception.TaroTypeNotFoundException;

@Component
public class CustomerClient {

    private final RestClient restClient;

    public CustomerClient(
            RestClient.Builder builder,
            @Value("${customer.service.url}") String baseUrl) {

        this.restClient = builder
                .baseUrl(baseUrl)
                .build();
    }

    // get all customers
    public List<CustomerResponse> getCustomers() {
        try {
            CustomerResponse[] customers = restClient.get()
                    .uri("/api/v1/customers")
                    .retrieve()
                    .body(CustomerResponse[].class);

            if (customers == null) {
                return List.of();
            }
            return Arrays.asList(customers);

        } catch (ResourceAccessException e) {
            throw new CustomerServiceUnavailableException();
        }
    }

    // get a customer by id
    public CustomerResponse getCustomer(Long customerId) {
        try {
            return restClient.get()
                    .uri("/api/v1/customers/{id}", customerId)
                    .retrieve()
                    .body(CustomerResponse.class);

        } catch (HttpClientErrorException.NotFound e) {
            throw new CustomerNotFoundException(customerId);

        } catch (ResourceAccessException e) {
            throw new CustomerServiceUnavailableException();
        }
    }

    // get all taro types
    public List<TaroTypeResponse> getTaroTypes() {
        try {
            TaroTypeResponse[] taroTypes = restClient.get()
                    .uri("/api/v1/taro-types")
                    .retrieve()
                    .body(TaroTypeResponse[].class);

            if (taroTypes == null) {
                return List.of();
            }

            return Arrays.asList(taroTypes);

        } catch (ResourceAccessException e) {
            throw new CustomerServiceUnavailableException();
        }
    }

    // get a taro type by id
    public TaroTypeResponse getTaroType(Long taroTypeId) {
        try {
            return restClient.get()
                    .uri("/api/v1/taro-types/{id}", taroTypeId)
                    .retrieve()
                    .body(TaroTypeResponse.class);

        } catch (HttpClientErrorException.NotFound e) {
            throw new TaroTypeNotFoundException(taroTypeId);

        } catch (ResourceAccessException e) {
            throw new CustomerServiceUnavailableException();
        }
    }
}