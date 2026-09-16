package nz.ac.aut.comp713.customer_service.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import nz.ac.aut.comp713.customer_service.service.CustomerService;

@Controller
public class CustomerPageController {

    private final CustomerService customerService;

    public CustomerPageController(
            CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/customers")
    public String getCustomersPage(Model model) {
        model.addAttribute(
                "customers",
                customerService.getAllCustomers());

        return "customers";
    }
}
