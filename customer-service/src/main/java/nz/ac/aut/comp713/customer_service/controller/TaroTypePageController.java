package nz.ac.aut.comp713.customer_service.controller;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Validator;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import nz.ac.aut.comp713.customer_service.dto.TaroTypeRequest;
import nz.ac.aut.comp713.customer_service.exception.TaroTypeAlreadyExistsException;
import nz.ac.aut.comp713.customer_service.service.TaroTypeService;

@Controller
public class TaroTypePageController {

    private final TaroTypeService taroTypeService;
    private final Validator validator;

    public TaroTypePageController(
            TaroTypeService taroTypeService,
            Validator validator) {

        this.taroTypeService = taroTypeService;
        this.validator = validator;
    }

    // show all taro types
    @GetMapping("/taro-types")
    public String getTaroTypesPage(Model model) {

        model.addAttribute(
                "taroTypes",
                taroTypeService.getAllTaroTypes());

        return "taro-types";
    }

    // show create taro type form
    @GetMapping("/taro-types/new")
    public String getCreateTaroTypePage() {

        return "taro-type-form";
    }

    // create a taro type
    @PostMapping("/taro-types/new")
    public String createTaroType(
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String standardPrice,
            Model model) {

        BigDecimal parsedPrice;

        try {

            parsedPrice = parsePrice(standardPrice);

        } catch (NumberFormatException exception) {

            addCreateFormValues(
                    model,
                    name,
                    description,
                    standardPrice);

            model.addAttribute(
                    "errors",
                    List.of("Standard price must be a valid number"));

            return "taro-type-form";
        }

        TaroTypeRequest request = new TaroTypeRequest(
                name,
                description,
                parsedPrice);

        var violations = validator.validate(request);

        if (!violations.isEmpty()) {

            List<String> errors = violations.stream()
                    .map(violation -> violation.getMessage())
                    .toList();

            addCreateFormValues(
                    model,
                    name,
                    description,
                    standardPrice);

            model.addAttribute("errors", errors);

            return "taro-type-form";
        }

        try {

            taroTypeService.createTaroType(request);

        } catch (TaroTypeAlreadyExistsException exception) {

            addCreateFormValues(
                    model,
                    name,
                    description,
                    standardPrice);

            model.addAttribute(
                    "errors",
                    List.of(exception.getMessage()));

            return "taro-type-form";
        }

        return "redirect:/taro-types";
    }

    // show edit taro type form
    @GetMapping("/taro-types/{id}/edit")
    public String getEditTaroTypePage(
            @PathVariable Long id,
            Model model) {

        var taroType = taroTypeService.getTaroTypeById(id);

        model.addAttribute("taroTypeId", taroType.id());
        model.addAttribute("name", taroType.name());
        model.addAttribute("description", taroType.description());
        model.addAttribute("standardPrice", taroType.standardPrice());

        return "taro-type-edit-form";
    }

    // update a taro type
    @PostMapping("/taro-types/{id}/edit")
    public String updateTaroType(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String standardPrice,
            Model model) {

        BigDecimal parsedPrice;

        try {

            parsedPrice = parsePrice(standardPrice);

        } catch (NumberFormatException exception) {

            addEditFormValues(
                    model,
                    id,
                    name,
                    description,
                    standardPrice);

            model.addAttribute(
                    "errors",
                    List.of("Standard price must be a valid number"));

            return "taro-type-edit-form";
        }

        TaroTypeRequest request = new TaroTypeRequest(
                name,
                description,
                parsedPrice);

        var violations = validator.validate(request);

        if (!violations.isEmpty()) {

            List<String> errors = violations.stream()
                    .map(violation -> violation.getMessage())
                    .toList();

            addEditFormValues(
                    model,
                    id,
                    name,
                    description,
                    standardPrice);

            model.addAttribute("errors", errors);

            return "taro-type-edit-form";
        }

        try {

            taroTypeService.updateTaroType(id, request);

        } catch (TaroTypeAlreadyExistsException exception) {

            addEditFormValues(
                    model,
                    id,
                    name,
                    description,
                    standardPrice);

            model.addAttribute(
                    "errors",
                    List.of(exception.getMessage()));

            return "taro-type-edit-form";
        }

        return "redirect:/taro-types";
    }

    // convert the form price into a decimal
    private BigDecimal parsePrice(String standardPrice) {

        if (standardPrice == null || standardPrice.isBlank()) {
            return null;
        }

        return new BigDecimal(standardPrice);
    }

    // preserve values after a failed create
    private void addCreateFormValues(
            Model model,
            String name,
            String description,
            String standardPrice) {

        model.addAttribute("name", name);
        model.addAttribute("description", description);
        model.addAttribute("standardPrice", standardPrice);
    }

    // preserve values after a failed update
    private void addEditFormValues(
            Model model,
            Long taroTypeId,
            String name,
            String description,
            String standardPrice) {

        model.addAttribute("taroTypeId", taroTypeId);
        model.addAttribute("name", name);
        model.addAttribute("description", description);
        model.addAttribute("standardPrice", standardPrice);
    }
}