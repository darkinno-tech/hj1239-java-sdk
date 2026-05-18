package io.darkinno.hj1239.sdk.model.enums;

public enum EmissionStandard {

    CHINA_III(0x01, "国三"),
    CHINA_IV(0x02, "国四"),
    CHINA_V(0x03, "国五"),
    CHINA_VI_A(0x04, "国六a"),
    CHINA_VI_B(0x05, "国六b"),
    UNKNOWN(0xFF, "未知");

    private final int code;
    private final String description;

    EmissionStandard(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static EmissionStandard fromCode(int code) {
        for (EmissionStandard std : VALUES) {
            if (std.code == code) {
                return std;
            }
        }
        return UNKNOWN;
    }

    private static final EmissionStandard[] VALUES = values();
}
