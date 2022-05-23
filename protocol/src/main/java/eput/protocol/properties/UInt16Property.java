package eput.protocol.properties;

import java.math.BigInteger;
import java.nio.ByteBuffer;

import eput.protocol.Type;

public class UInt16Property extends UnsignedIntegerProperty {
    public static final BigInteger DEF_MIN = new BigInteger("0");
    public static final BigInteger DEF_MAX = new BigInteger("65535");
    public static final BigInteger DEF_STP = BigInteger.valueOf(1);

    public UInt16Property(ByteBuffer meta, ByteBuffer data) {
        super(2, meta, data);
    }

    @Override
    public int getType() {
        return Type.UINT16_T;
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
