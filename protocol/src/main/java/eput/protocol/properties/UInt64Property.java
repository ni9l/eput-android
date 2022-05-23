package eput.protocol.properties;

import java.math.BigInteger;
import java.nio.ByteBuffer;

import eput.protocol.Type;

public class UInt64Property extends UnsignedIntegerProperty {
    public static final BigInteger DEF_MIN = new BigInteger("0");
    public static final BigInteger DEF_MAX = new BigInteger("18446744073709551615");
    public static final BigInteger DEF_STP = BigInteger.valueOf(1);

    public UInt64Property(ByteBuffer meta, ByteBuffer data) {
        super(8, meta, data);
    }

    @Override
    public int getType() {
        return Type.UINT64_T;
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
