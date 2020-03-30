package com.nusinfineon.core.input.LotEntry;

public abstract class LotEntry implements Comparable<LotEntry> {
    private String lot;
    private String product;
    private Double lotSize;
    private String productionLocation;
    private Double period;

    public LotEntry(String lot, String product, Double lotSize, String productionLocation, Double period) {
        this.lot = lot;
        this.product = product;
        this.lotSize = lotSize;
        this.productionLocation = productionLocation;
        this.period = period;
    }

    public String getLot() {
        return lot;
    }

    public String getProduct() {
        return product;
    }

    public Double getLotSize() {
        return lotSize;
    }

    public String getProductionLocation() {
        return productionLocation;
    }

    public Double getPeriod() {
        return period;
    }

    public abstract Double getComparable();

    @Override
    public int compareTo(LotEntry o) {
        return this.getComparable().compareTo(o.getComparable());
    }
}
