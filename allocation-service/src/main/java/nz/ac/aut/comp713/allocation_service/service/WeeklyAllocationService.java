package nz.ac.aut.comp713.allocation_service.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import nz.ac.aut.comp713.allocation_service.client.CustomerClient;
import nz.ac.aut.comp713.allocation_service.client.CustomerResponse;
import nz.ac.aut.comp713.allocation_service.client.TaroTypeResponse;
import nz.ac.aut.comp713.allocation_service.dto.AllocationItemRequest;
import nz.ac.aut.comp713.allocation_service.dto.AllocationItemResponse;
import nz.ac.aut.comp713.allocation_service.dto.WeeklyAllocationRequest;
import nz.ac.aut.comp713.allocation_service.dto.WeeklyAllocationResponse;
import nz.ac.aut.comp713.allocation_service.exception.DuplicateTaroTypeException;
import nz.ac.aut.comp713.allocation_service.exception.WeeklyAllocationAlreadyExistsException;
import nz.ac.aut.comp713.allocation_service.exception.WeeklyAllocationNotFoundException;
import nz.ac.aut.comp713.allocation_service.model.AllocationItem;
import nz.ac.aut.comp713.allocation_service.model.WeeklyAllocation;
import nz.ac.aut.comp713.allocation_service.repository.AllocationItemRepository;
import nz.ac.aut.comp713.allocation_service.repository.WeeklyAllocationRepository;

@Service
public class WeeklyAllocationService {

    private final WeeklyAllocationRepository weeklyAllocationRepository;
    private final AllocationItemRepository allocationItemRepository;
    private final CustomerClient customerClient;

    public WeeklyAllocationService(
            WeeklyAllocationRepository weeklyAllocationRepository,
            AllocationItemRepository allocationItemRepository,
            CustomerClient customerClient) {

        this.weeklyAllocationRepository = weeklyAllocationRepository;
        this.allocationItemRepository = allocationItemRepository;
        this.customerClient = customerClient;
    }

    // get all weekly allocations
    public List<WeeklyAllocationResponse> getAllWeeklyAllocations() {
        return weeklyAllocationRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // get a weekly allocation by id
    public WeeklyAllocationResponse getWeeklyAllocationById(Long id) {
        WeeklyAllocation weeklyAllocation = weeklyAllocationRepository.findById(id)
                .orElseThrow(() -> new WeeklyAllocationNotFoundException(id));

        return toResponse(weeklyAllocation);
    }

    // create a new weekly allocation
    @Transactional
    public WeeklyAllocationResponse createWeeklyAllocation(
            WeeklyAllocationRequest request) {

        // confirm the customer exists in customer-service
        customerClient.getCustomer(request.customerId());

        // confirm every taro type exists and prevent duplicate taro types
        Set<Long> taroTypeIds = new HashSet<>();
        Map<Long, TaroTypeResponse> taroTypes = new HashMap<>();

        for (AllocationItemRequest item : request.allocationItems()) {

            if (!taroTypeIds.add(item.taroTypeId())) {
                throw new DuplicateTaroTypeException(item.taroTypeId());
            }

            TaroTypeResponse taroType = customerClient.getTaroType(item.taroTypeId());

            taroTypes.put(item.taroTypeId(), taroType);
        }

        // give a clear conflict response before attempting the insert
        if (weeklyAllocationRepository.existsByCustomerIdAndWeekStart(
                request.customerId(),
                request.weekStart())) {

            throw new WeeklyAllocationAlreadyExistsException(
                    request.customerId(),
                    request.weekStart());
        }

        WeeklyAllocation weeklyAllocation = new WeeklyAllocation();

        weeklyAllocation.setCustomerId(request.customerId());
        weeklyAllocation.setWeekStart(request.weekStart());

        WeeklyAllocation savedAllocation;

        try {
            savedAllocation = weeklyAllocationRepository.saveAndFlush(weeklyAllocation);

        } catch (DataIntegrityViolationException | JpaSystemException exception) {

            // the database constraint is the final protection against
            // concurrent duplicate allocation requests
            throw new WeeklyAllocationAlreadyExistsException(
                    request.customerId(),
                    request.weekStart());
        }

        // create each item and associate it with the saved weekly allocation
        List<AllocationItem> allocationItems = request.allocationItems()
                .stream()
                .map(itemRequest -> createAllocationItem(
                        savedAllocation,
                        itemRequest,
                        taroTypes.get(itemRequest.taroTypeId())))
                .toList();

        allocationItemRepository.saveAllAndFlush(allocationItems);

        return toResponse(savedAllocation);
    }

    // create an allocation item entity from its request dto
    private AllocationItem createAllocationItem(
            WeeklyAllocation weeklyAllocation,
            AllocationItemRequest request,
            TaroTypeResponse taroType) {

        AllocationItem allocationItem = new AllocationItem();

        allocationItem.setWeeklyAllocation(weeklyAllocation);
        allocationItem.setTaroTypeId(request.taroTypeId());
        allocationItem.setQuantity(request.quantity());

        if (request.pricePerKg() != null) {
            allocationItem.setPricePerKg(request.pricePerKg());
        } else {
            allocationItem.setPricePerKg(taroType.standardPrice());
        }

        return allocationItem;
    }

    // convert a weekly allocation entity into an enriched response dto
    private WeeklyAllocationResponse toResponse(
            WeeklyAllocation weeklyAllocation) {

        CustomerResponse customer = customerClient.getCustomer(weeklyAllocation.getCustomerId());

        List<AllocationItemResponse> allocationItemResponses = allocationItemRepository
                .findByWeeklyAllocationId(weeklyAllocation.getId())
                .stream()
                .map(this::toItemResponse)
                .toList();

        return new WeeklyAllocationResponse(
                weeklyAllocation.getId(),
                weeklyAllocation.getCustomerId(),
                customer.name(),
                weeklyAllocation.getWeekStart(),
                allocationItemResponses);
    }

    // convert an allocation item entity into an enriched response dto
    private AllocationItemResponse toItemResponse(
            AllocationItem allocationItem) {

        TaroTypeResponse taroType = customerClient.getTaroType(allocationItem.getTaroTypeId());

        return new AllocationItemResponse(
                allocationItem.getId(),
                allocationItem.getTaroTypeId(),
                taroType.name(),
                allocationItem.getQuantity(),
                allocationItem.getPricePerKg());
    }
}