package io.darkinno.hj1239.sdk.model.enums;

/**
 * HJ 1239.3-2021 Table 4 — Message type flags within real-time data body.
 */
public enum MessageType {

    OBD_ENGINE_DATA(0x01, "发动机系统OBD信息"),
    ENGINE_DPF_SCR_DATA(0x02, "DPF/SCR发动机信息"),
    ENGINE_TWC_NOX_DATA(0x03, "TWC/NOx发动机信息"),
    ENGINE_HYBRID_DATA(0x04, "混合动力信息"),
    ENGINE_TWC_NOX2_DATA(0x05, "三元/NOx发动机信息"),
    EMISSION_CHECK_DATA(0x80, "排放检查强制要求数据");

    private final int code;
    private final String description;

    MessageType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() { return code; }
    public String getDescription() { return description; }

    public static MessageType fromCode(int code) {
        for (MessageType t : VALUES) {
            if (t.code == code) return t;
        }
        return null;
    }

    private static final MessageType[] VALUES = values();
}
