package nz.ac.aut.comp713.allocation_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import nz.ac.aut.comp713.allocation_service.model.WeeklyAllocation;

public interface WeeklyAllocationRepository extends JpaRepository<WeeklyAllocation, Long> {
}