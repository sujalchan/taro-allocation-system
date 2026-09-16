package nz.ac.aut.comp713.customer_service.controller;

import java.util.List;

import jakarta.validation.Validator;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import nz.ac.aut.comp713.customer_service.dto.CustomerRequest;
import nz.ac.aut.comp713.customer_service.exception.CustomerAlreadyExistsException;
import nz.ac.aut.comp713.customer_service.service.CustomerService;

@Controller
public class CustomerPageController {

    private final CustomerService customerService;
    private final Validator validator;

    public CustomerPageController(
            CustomerService customerService,
            Validator validator) {

        this.customerService = customerService;
        this.validator = validator;
    }

    @GetMapping("/customers")
    public String getCustomersPage(Model model) {

        model.addAttribute(
                "customers",
                customerService.getAllCustomers());

        return "customers";
    }

    @GetMapping("/customers/new")
    public String getCreateCustomerPage(Model model) {

        model.addAttribute("active", true);

        return "customer-form";
    }

    @PostMapping("/customers/new")
    public String createCustomer(
            @RequestParam String name,
            @RequestParam(required = false) String contactName,
            @RequestParam(required = false) String phone,
            @RequestParam(defaultValue = "false") boolean active,
            Model model) {

        CustomerRequest request = new CustomerRequest(
                name,
                contactName,
                phone,
                active);

        // run the same validation rules used by the API
        var violations = validator.validate(request);

        if (!violations.isEmpty()) {

            List<String> errors = violations.stream()
                    .map(violation -> violation.getMessage())
                    .toList();

            addFormValues(
                    model,
                    name,
                    contactName,
                    phone,
                    active);

            model.addAttribute("errors", errors);

            return "customer-form";
        }

        try {

            customerService.createCustomer(request);

        } catch (CustomerAlreadyExistsException exception) {

            addFormValues(
                    model,
                    name,
                    contactName,
                    phone,
                    active);

            model.addAttribute(
                    "errors",
                    List.of(exception.getMessage()));

            return "customer-form";
        }

        return "redirect:/customers";
    }

    private void addFormValues(
            Model model,
            String name,
            String contactName,
            String phone,
            boolean active) {

        model.addAttribute("name", name);
        model.addAttribute("contactName", contactName);
        model.addAttribute("phone", phone);
        model.addAttribute("active", active);
    }
}