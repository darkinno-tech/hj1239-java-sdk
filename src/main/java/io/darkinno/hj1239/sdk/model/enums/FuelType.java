package io.darkinno.hj1239.sdk.model.enums;

public enum FuelType {

    DIESEL(0x01, "柴油"),
    GASOLINE(0x02, "汽油"),
    NATURAL_GAS(0x03, "天然气"),
    METHANOL(0x04, "甲醇"),
    ELECTRIC(0x05, "电动"),
    HYBRID(0x06, "混合动力"),
    OTHER(0xFF, "其他");

    private final int code;
    private final String description;

    FuelType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static FuelType fromCode(int code) {
        for (FuelType type : VALUES) {
            if (type.code == code) {
                return type;
            }
        }
        return OTHER;
    }

    private static final FuelType[] VALUES = values();
}
