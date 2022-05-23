package eput.protocol;

public final class DeviceType {
    public static final int CUSTOM          = 0b00000000;
    public static final int CUSTOM_NO_TRUNCATE = 0b10000000;
    public static final int LIGHT           = 0b00000001;
    public static final int WASHING_MACHINE = 0b00000010;
    public static final int HEATER          = 0b00000011;
}
