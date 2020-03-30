package com.nusinfineon.core.input.LotEntry;

public class GenericLotEntry extends LotEntry {

    public GenericLotEntry(String lot, String product, Double lotSize, String productionLocation, Double period) {
        super(lot, product, lotSize, productionLocation, period);
    }

    public Double getComparable() {
        return 0.0;
    }
}
