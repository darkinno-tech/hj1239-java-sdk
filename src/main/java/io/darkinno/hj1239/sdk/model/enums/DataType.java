package io.darkinno.hj1239.sdk.model.enums;

/**
 * HJ 1239.3-2021 Table 1 / Table 17 — Command unit codes.
 */
public enum DataType {

    VEHICLE_LOGIN(0x01, "车辆登入"),
    REALTIME_DATA(0x02, "实时信息上报"),
    REPLENISH_DATA(0x03, "补传信息上报"),
    VEHICLE_LOGOUT(0x04, "车辆登出"),
    TERMINAL_TIME_SYNC(0x05, "终端校时"),
    EMISSION_CHECK(0x06, "排放检查数据"),
    PLATFORM_LOGIN(0x07, "企业平台登入"),
    PLATFORM_LOGOUT(0x08, "企业平台登出"),
    KEY_EXCHANGE(0x09, "密钥交换"),
    UNKNOWN(0xFF, "未知");

    private final int code;
    private final String description;

    DataType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static DataType fromCode(int code) {
        for (DataType type : VALUES) {
            if (type.code == code) {
                return type;
            }
        }
        return UNKNOWN;
    }

    private static final DataType[] VALUES = values();
}
