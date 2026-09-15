package nz.ac.aut.comp713.customer_service.service;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import nz.ac.aut.comp713.customer_service.dto.TaroTypeRequest;
import nz.ac.aut.comp713.customer_service.dto.TaroTypeResponse;
import nz.ac.aut.comp713.customer_service.exception.TaroTypeAlreadyExistsException;
import nz.ac.aut.comp713.customer_service.exception.TaroTypeNotFoundException;
import nz.ac.aut.comp713.customer_service.model.TaroType;
import nz.ac.aut.comp713.customer_service.repository.TaroTypeRepository;

@Service
public class TaroTypeService {

    private final TaroTypeRepository taroTypeRepository;

    public TaroTypeService(TaroTypeRepository taroTypeRepository) {
        this.taroTypeRepository = taroTypeRepository;
    }

    // get all taro types
    public List<TaroTypeResponse> getAllTaroTypes() {
        return taroTypeRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // get a taro type by id
    public TaroTypeResponse getTaroTypeById(Long id) {
        TaroType taroType = taroTypeRepository.findById(id)
                .orElseThrow(() -> new TaroTypeNotFoundException(id));

        return toResponse(taroType);
    }

    // create a new taro type
    @Transactional
    public TaroTypeResponse createTaroType(TaroTypeRequest request) {
        String name = request.name();
        String normalizedName = normalizeName(name);

        TaroType taroType = new TaroType();

        taroType.setName(name);
        taroType.setNormalizedName(normalizedName);
        taroType.setDescription(request.description());
        taroType.setStandardPrice(request.standardPrice());

        try {
            TaroType savedTaroType = taroTypeRepository.saveAndFlush(taroType);
            return toResponse(savedTaroType);
        } catch (DataIntegrityViolationException e) {
            throw new TaroTypeAlreadyExistsException(name);
        }
    }

    // update an existing taro type
    @Transactional
    public TaroTypeResponse updateTaroType(Long id, TaroTypeRequest request) {
        TaroType taroType = taroTypeRepository.findById(id)
                .orElseThrow(() -> new TaroTypeNotFoundException(id));

        String name = request.name();
        String normalizedName = normalizeName(name);

        taroType.setName(name);
        taroType.setNormalizedName(normalizedName);
        taroType.setDescription(request.description());
        taroType.setStandardPrice(request.standardPrice());

        try {
            TaroType savedTaroType = taroTypeRepository.saveAndFlush(taroType);
            return toResponse(savedTaroType);
        } catch (DataIntegrityViolationException e) {
            throw new TaroTypeAlreadyExistsException(name);
        }
    }

    // normalize taro type names for duplicate detection
    private String normalizeName(String name) {
        return name.toLowerCase().replaceAll("\\s+", "");
    }

    // convert taro type entity to response dto
    private TaroTypeResponse toResponse(TaroType taroType) {
        return new TaroTypeResponse(
                taroType.getId(),
                taroType.getName(),
                taroType.getDescription(),
                taroType.getStandardPrice());
    }
}