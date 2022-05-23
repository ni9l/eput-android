package eput.protocol;

public final class Type {
    public static final int DIVIDER            = 0b10000000;
    public static final int HEADER             = 0b10000001;
    public static final int ONE_OUT_OF_M       = 0b10000010;
    public static final int N_OUT_OF_M         = 0b10000011;
    public static final int BOOL               = 0b10000100;
    public static final int ARRAY              = 0b10000101;
    public static final int UINT8_T            = 0b10000110;
    public static final int UINT16_T           = 0b10000111;
    public static final int UINT32_T           = 0b10001000;
    public static final int UINT64_T           = 0b10001001;
    public static final int INT8_T             = 0b10001010;
    public static final int INT16_T            = 0b10001011;
    public static final int INT32_T            = 0b10001100;
    public static final int INT64_T            = 0b10001101;
    public static final int FLOAT              = 0b10001110;
    public static final int DOUBLE             = 0b10001111;
    public static final int NUMBER_LIST_INT    = 0b10010000;
    public static final int NUMBER_LIST_DOUBLE = 0b10010001;
    public static final int DATE               = 0b10010010;
    public static final int DATE_TIME          = 0b10010011;
    public static final int TIME               = 0b10010100;
    public static final int ZONED_DATE_TIME    = 0b10010101;
    public static final int DATE_RANGE         = 0b10010111;
    public static final int DATE_TIME_RANGE    = 0b10011000;
    public static final int TIME_RANGE         = 0b10011001;
    public static final int STR_ASCII          = 0b10011010;
    public static final int STR_UTF8           = 0b10011011;
    public static final int STR_EMAIL          = 0b10011100;
    public static final int STR_PHONE          = 0b10011101;
    public static final int STR_URI            = 0b10011110;
    public static final int STR_PASSWORD       = 0b10011111;
    public static final int FIXP32             = 0b10100000;
    public static final int FIXP64             = 0b10100001;
    public static final int LANGUAGE           = 0b10100010;
    public static final int METADATA_TRUNCATED = 0b11111110;
    public static final int END                = 0b11111111;
    public static final class NumberContentType {
        public static final int NONE   = 0;
        public static final int TIME   = 1;
        public static final int WEIGHT = 2;
        public static final int LENGTH = 3;
    }
}
