package nz.ac.aut.comp713.allocation_service.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import nz.ac.aut.comp713.allocation_service.dto.WeeklyAllocationRequest;
import nz.ac.aut.comp713.allocation_service.dto.WeeklyAllocationResponse;
import nz.ac.aut.comp713.allocation_service.service.WeeklyAllocationService;

@RestController
@RequestMapping("/api/v1/allocations")
public class WeeklyAllocationController {

    private final WeeklyAllocationService weeklyAllocationService;

    public WeeklyAllocationController(
            WeeklyAllocationService weeklyAllocationService) {

        this.weeklyAllocationService = weeklyAllocationService;
    }

    // get all weekly allocations
    @GetMapping
    public ResponseEntity<List<WeeklyAllocationResponse>> getAllWeeklyAllocations() {

        return ResponseEntity.ok(
                weeklyAllocationService.getAllWeeklyAllocations());
    }

    // get a weekly allocation by id
    @GetMapping("/{id}")
    public ResponseEntity<WeeklyAllocationResponse> getWeeklyAllocationById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                weeklyAllocationService.getWeeklyAllocationById(id));
    }

    // create a new weekly allocation
    @PostMapping
    public ResponseEntity<WeeklyAllocationResponse> createWeeklyAllocation(
            @Valid @RequestBody WeeklyAllocationRequest request) {

        WeeklyAllocationResponse response = weeklyAllocationService.createWeeklyAllocation(request);

        URI location = URI.create(
                "/api/v1/allocations/" + response.id());

        return ResponseEntity
                .created(location)
                .body(response);
    }
}