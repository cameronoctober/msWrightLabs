package com.wrightlabs.marketplace.catalog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchCriteria {

    private String keyword;
    private String grade;
    private String subject;
    private String resourceType;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Set<String> tags;
    private String sortBy; // newest, price_asc, price_desc, rating, popularity
}
