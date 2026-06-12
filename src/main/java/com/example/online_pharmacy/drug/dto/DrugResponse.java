package com.example.online_pharmacy.drug.dto;

import com.example.online_pharmacy.drug.entity.Drug;
import com.example.online_pharmacy.drug.entity.DrugStatus;

import java.math.BigDecimal;

public class DrugResponse {

    private Long id;
    private String name;
    private String category;
    private String manufacturer;
    private String specification;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private Boolean prescriptionRequired;
    private DrugStatus status;

    public static DrugResponse from(Drug drug) {
        DrugResponse response = new DrugResponse();
        response.setId(drug.getId());
        response.setName(drug.getName());
        response.setCategory(drug.getCategory());
        response.setManufacturer(drug.getManufacturer());
        response.setSpecification(drug.getSpecification());
        response.setDescription(drug.getDescription());
        response.setPrice(drug.getPrice());
        response.setStockQuantity(drug.getStockQuantity());
        response.setPrescriptionRequired(drug.getPrescriptionRequired());
        response.setStatus(drug.getStatus());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getSpecification() {
        return specification;
    }

    public void setSpecification(String specification) {
        this.specification = specification;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public Boolean getPrescriptionRequired() {
        return prescriptionRequired;
    }

    public void setPrescriptionRequired(Boolean prescriptionRequired) {
        this.prescriptionRequired = prescriptionRequired;
    }

    public DrugStatus getStatus() {
        return status;
    }

    public void setStatus(DrugStatus status) {
        this.status = status;
    }
}
