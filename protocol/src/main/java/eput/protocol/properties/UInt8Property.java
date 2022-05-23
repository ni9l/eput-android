package eput.protocol.properties;

import java.math.BigInteger;
import java.nio.ByteBuffer;

import eput.protocol.Type;

public class UInt8Property extends UnsignedIntegerProperty {
    public static final BigInteger DEF_MIN = new BigInteger("0");
    public static final BigInteger DEF_MAX = new BigInteger("255");
    public static final BigInteger DEF_STP = BigInteger.valueOf(1);

    public UInt8Property(ByteBuffer meta, ByteBuffer data) {
        super(1, meta, data);
    }

    @Override
    public int getType() {
        return Type.UINT8_T;
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
