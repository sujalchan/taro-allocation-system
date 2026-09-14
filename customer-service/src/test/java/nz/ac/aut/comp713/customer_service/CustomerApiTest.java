package nz.ac.aut.comp713.customer_service;

import static org.hamcrest.Matchers.hasSize;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import nz.ac.aut.comp713.customer_service.repository.CustomerRepository;

@SpringBootTest
@AutoConfigureMockMvc
class CustomerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerRepository customerRepository;

    @BeforeEach
    // Don't remove as the method is used.
    void resetDatabase() {
        customerRepository.deleteAll();
    }

    @Test
    void getAllCustomersReturnsEmptyList() throws Exception {
        mockMvc.perform(get("/api/v1/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void createCustomerReturnsCreatedCustomer() throws Exception {
        String requestBody = """
                {
                    "name": "Island Foods",
                    "contactName": "John",
                    "phone": "0211234567",
                    "active": true
                }
                """;

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.name").value("Island Foods"))
                .andExpect(jsonPath("$.contactName").value("John"))
                .andExpect(jsonPath("$.phone").value("0211234567"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    void getCustomerByIdReturnsCustomer() throws Exception {
        String requestBody = """
                {
                    "name": "Island Foods",
                    "contactName": "John",
                    "phone": "0211234567",
                    "active": true
                }
                """;

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());

        Long customerId = customerRepository.findAll().getFirst().getId();

        mockMvc.perform(get("/api/v1/customers/{id}", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(customerId))
                .andExpect(jsonPath("$.name").value("Island Foods"))
                .andExpect(jsonPath("$.contactName").value("John"))
                .andExpect(jsonPath("$.phone").value("0211234567"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void getUnknownCustomerReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/customers/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CUSTOMER_NOT_FOUND"))
                .andExpect(jsonPath("$.message")
                        .value("Customer not found with id: 999"))
                .andExpect(jsonPath("$.path")
                        .value("/api/v1/customers/999"));
    }

    @Test
    void blankCustomerNameReturnsBadRequest() throws Exception {
        String requestBody = """
                {
                    "name": "",
                    "contactName": "Sarah",
                    "phone": "0219876543",
                    "active": true
                }
                """;

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message")
                        .value("Customer name is required"))
                .andExpect(jsonPath("$.path")
                        .value("/api/v1/customers"));
    }

    @Test
    void duplicateCustomerReturnsConflict() throws Exception {
        String requestBody = """
                {
                    "name": "Island Foods",
                    "contactName": "John",
                    "phone": "0211234567",
                    "active": true
                }
                """;

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code")
                        .value("CUSTOMER_ALREADY_EXISTS"))
                .andExpect(jsonPath("$.message")
                        .value("Customer 'Island Foods' already exists."))
                .andExpect(jsonPath("$.path")
                        .value("/api/v1/customers"));

        // only one customer should exist
        org.junit.jupiter.api.Assertions.assertEquals(
                1,
                customerRepository.count()
        );
    }
}