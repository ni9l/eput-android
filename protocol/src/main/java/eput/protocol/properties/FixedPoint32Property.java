package eput.protocol.properties;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.List;

import eput.protocol.Type;
import eput.protocol.Util;

public class FixedPoint32Property extends FixedPointProperty {
    public FixedPoint32Property(ByteBuffer meta, ByteBuffer data) {
        super(meta);
        // BigDecimal = unscaled * 10 ^ -scale
        int scale = meta.getInt();
        byte flags = meta.get();
        if ((flags & 0b00000001) > 0) {
            int unscaledMin = meta.getInt();
            this.minValue = BigDecimal.valueOf(unscaledMin, scale);
        } else {
            this.minValue = BigDecimal.valueOf(Integer.MIN_VALUE, scale);
        }
        if ((flags & 0b00000010) > 0) {
            int unscaledMax = meta.getInt();
            this.maxValue = BigDecimal.valueOf(unscaledMax, scale);
        } else {
            this.maxValue = BigDecimal.valueOf(Integer.MAX_VALUE, scale);
        }
        if ((flags & 0b00001000) > 0) {
            this.contentType = meta.get() & 0xFF;
        } else {
            this.contentType = Type.NumberContentType.NONE;
        }
        if ((flags & 0b00010000) > 0) {
            this.contentTypeDefault = meta.get() & 0xFF;
        } else {
            this.contentTypeDefault = 0;
        }
        int unscaledVal = data.getInt();
        this.value = BigDecimal.valueOf(unscaledVal, scale);
    }

    @Override
    public int getType() {
        return Type.FIXP32;
    }

    @Override
    public List<Byte> serializeData() {
        BigInteger unscaled = value.unscaledValue();
        int unscaledInt = unscaled.intValue();
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(unscaledInt);
        return Util.bufferToList(buffer);
    }
}
