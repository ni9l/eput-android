package eput.protocol.properties;

import java.math.BigInteger;
import java.nio.ByteBuffer;

import eput.protocol.Type;

public class Int16Property extends SignedIntegerProperty {
    public static final BigInteger DEF_MIN = new BigInteger("-32768");
    public static final BigInteger DEF_MAX = new BigInteger("32767");
    public static final BigInteger DEF_STP = BigInteger.valueOf(1);

    public Int16Property(ByteBuffer meta, ByteBuffer data) {
        super(2, meta, data);
    }

    @Override
    public int getType() {
        return Type.INT16_T;
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
