package eput.protocol.properties;

import java.math.BigInteger;
import java.nio.ByteBuffer;

import eput.protocol.Type;

public class Int64Property extends SignedIntegerProperty {
    public static final BigInteger DEF_MIN = new BigInteger("-9223372036854775808");
    public static final BigInteger DEF_MAX = new BigInteger("9223372036854775807");
    public static final BigInteger DEF_STP = BigInteger.valueOf(1);

    public Int64Property(ByteBuffer meta, ByteBuffer data) {
        super(8, meta, data);
    }

    @Override
    public int getType() {
        return Type.INT64_T;
    }

    @Override
    protected BigInteger getDefaultMinValue() {
        return DEF_MIN;
    }

    @Override
    protected BigInteger getDefaultMaxValue() {
        return DEF_MAX;
    }

    @Override
    protected BigInteger getDefaultStepSize() {
        return DEF_STP;
    }
}
