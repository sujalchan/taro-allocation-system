package nz.ac.aut.comp713.allocation_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import nz.ac.aut.comp713.allocation_service.model.AllocationItem;
import java.util.List;

public interface AllocationItemRepository extends JpaRepository<AllocationItem, Long> {

    List<AllocationItem> findByWeeklyAllocationId(Long weeklyAllocationId);

    void deleteByWeeklyAllocationId(Long weeklyAllocationId);
}
