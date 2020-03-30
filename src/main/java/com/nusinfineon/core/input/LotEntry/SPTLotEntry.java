package com.nusinfineon.core.input.LotEntry;

public class SPTLotEntry extends LotEntry {
    private Double processTime;

    public SPTLotEntry(String lot, String product, Double lotSize, String productionLocation, Double period,
                       Double processTime) {
        super(lot, product, lotSize, productionLocation, period);
        this.processTime = processTime;
    }

    public Double getComparable() {
        return processTime;
    }
}
