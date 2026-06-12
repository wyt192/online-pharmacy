package com.example.online_pharmacy.drug.service;

import com.example.online_pharmacy.common.BusinessException;
import com.example.online_pharmacy.drug.dto.DrugResponse;
import com.example.online_pharmacy.drug.entity.Drug;
import com.example.online_pharmacy.drug.entity.DrugStatus;
import com.example.online_pharmacy.drug.repository.DrugRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class DrugService {

    private final DrugRepository drugRepository;

    public DrugService(DrugRepository drugRepository) {
        this.drugRepository = drugRepository;
    }

    /**
     * Searches on-sale drugs using dynamic query conditions.
     *
     * @param keyword optional keyword matched against drug name, manufacturer, and description
     * @param category optional exact category filter
     * @param prescriptionRequired optional prescription-drug filter
     * @param pageable pagination and sorting information
     * @return page of drug response DTOs
     */
    public Page<DrugResponse> searchDrugs(String keyword,
                                          String category,
                                          Boolean prescriptionRequired,
                                          Pageable pageable) {
        Specification<Drug> specification = buildSearchSpecification(keyword, category, prescriptionRequired);
        return drugRepository.findAll(specification, pageable).map(DrugResponse::from);
    }

    /**
     * Loads a drug by id and converts it to a response DTO.
     *
     * @param id drug id
     * @return drug detail response
     * @throws BusinessException if the drug does not exist
     */
    public DrugResponse getDrugDetail(Long id) {
        Drug drug = drugRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Drug not found"));
        return DrugResponse.from(drug);
    }

    private Specification<Drug> buildSearchSpecification(String keyword,
                                                         String category,
                                                         Boolean prescriptionRequired) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("status"), DrugStatus.ON_SALE));

            if (StringUtils.hasText(keyword)) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                Predicate nameLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), pattern);
                Predicate manufacturerLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("manufacturer")), pattern);
                Predicate descriptionLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), pattern);
                predicates.add(criteriaBuilder.or(nameLike, manufacturerLike, descriptionLike));
            }

            if (StringUtils.hasText(category)) {
                predicates.add(criteriaBuilder.equal(root.get("category"), category.trim()));
            }

            if (prescriptionRequired != null) {
                predicates.add(criteriaBuilder.equal(root.get("prescriptionRequired"), prescriptionRequired));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
