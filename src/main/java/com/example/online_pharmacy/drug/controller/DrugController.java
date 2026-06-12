package com.example.online_pharmacy.drug.controller;

import com.example.online_pharmacy.common.Result;
import com.example.online_pharmacy.drug.dto.DrugResponse;
import com.example.online_pharmacy.drug.service.DrugService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/drugs")
public class DrugController {

    private final DrugService drugService;

    public DrugController(DrugService drugService) {
        this.drugService = drugService;
    }

    /**
     * Searches on-sale drugs by optional keyword, category, and prescription flag.
     *
     * @param keyword optional keyword matched against drug name, manufacturer, and description
     * @param category optional exact drug category filter
     * @param prescriptionRequired optional prescription-drug filter
     * @param page zero-based page index
     * @param size page size, limited to 1 through 100
     * @return paged drug search result wrapped in the unified response body
     * @throws jakarta.validation.ConstraintViolationException if page or size validation fails
     */
    @GetMapping("/search")
    public Result<Page<DrugResponse>> searchDrugs(@RequestParam(required = false) String keyword,
                                                  @RequestParam(required = false) String category,
                                                  @RequestParam(required = false) Boolean prescriptionRequired,
                                                  @RequestParam(defaultValue = "0") @Min(0) int page,
                                                  @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        return Result.success(drugService.searchDrugs(keyword, category, prescriptionRequired, pageable));
    }

    /**
     * Gets detail information for a single drug.
     *
     * @param id drug id
     * @return drug detail wrapped in the unified response body
     * @throws jakarta.validation.ConstraintViolationException if id is not positive
     * @throws com.example.online_pharmacy.common.BusinessException if the drug does not exist
     */
    @GetMapping("/{id}")
    public Result<DrugResponse> getDrugDetail(@PathVariable @Positive Long id) {
        return Result.success(drugService.getDrugDetail(id));
    }
}
