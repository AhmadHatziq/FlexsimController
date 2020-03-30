package com.nusinfineon.core.input.LotEntry;

public class MJLotEntry extends LotEntry {
    public MJLotEntry(String lot, String product, Double lotSize, String productionLocation, Double period) {
        super(lot, product, lotSize, productionLocation, period);
    }

    public Double getComparable() {
        return getLotSize();
    }
}
