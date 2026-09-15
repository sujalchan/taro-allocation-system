package nz.ac.aut.comp713.allocation_service.client;

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