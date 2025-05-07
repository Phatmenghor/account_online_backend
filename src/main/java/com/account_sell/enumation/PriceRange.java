package com.account_sell.enumation;

import lombok.Getter;

@Getter
public enum PriceRange {
    DEFAULT(10.0, 0, 10000),
    LOW_20(20.0, 10000, 50000),
    MID_50(50.0, 50000, 100000),
    MID_100(100.0, 100000, 500000),
    MID_500(500.0, 500000, 1000000),
    HIGH_1000(1000.0, 1000000, 1500000),
    HIGH_1500(1500.0, 1500000, 2500000),
    HIGH_2500(2500.0, 2500000, 3000000),
    HIGH_3000(3000.0, 3000000, 5000000),
    PREMIUM_5000(5000.0, 5000000, 10000000),
    PREMIUM_10000(10000.0, 10000000, Integer.MAX_VALUE);

    private final double price;
    private final int minRange;
    private final int maxRange;

    PriceRange(double price, int minRange, int maxRange) {
        this.price = price;
        this.minRange = minRange;
        this.maxRange = maxRange;
    }

    /**
     * Find the appropriate price range based on the price value
     *
     * @param price the price value
     * @return the matching price range
     */
    public static PriceRange fromPrice(double price) {
        for (PriceRange range : values()) {
            if (price == range.getPrice()) {
                return range;
            }
        }
        return DEFAULT;
    }

    /**
     * Get range description for this price
     *
     * @return formatted range string
     */
    public String getRangeDescription() {
        if (this == PREMIUM_10000) {
            return "> " + formatNumber(minRange);
        } else {
            return "> " + formatNumber(minRange) + " - " + formatNumber(maxRange);
        }
    }

    private String formatNumber(int number) {
        if (number >= 1000000) {
            return (number / 1000000) + ",000,000";
        } else if (number >= 1000) {
            return (number / 1000) + ",000";
        } else {
            return String.valueOf(number);
        }
    }
}