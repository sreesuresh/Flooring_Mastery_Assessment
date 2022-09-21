package org.example.dto;

import java.math.BigDecimal;


public class Tax {
    private final String stateAbbreviation, stateName;
    private final BigDecimal taxRate;

    public Tax(String stateAbbrev, String stateName, BigDecimal taxRate) {
        this.stateAbbreviation = stateAbbrev;
        this.stateName = stateName;
        this.taxRate = taxRate;
    }

    public String getStateAbbrev() {
        return stateAbbreviation;
    }

    public String getStateName() {
        return stateName;
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }

}