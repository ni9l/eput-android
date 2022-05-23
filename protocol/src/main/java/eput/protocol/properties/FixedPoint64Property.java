package eput.protocol.properties;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.List;

import eput.protocol.Type;
import eput.protocol.Util;

public class FixedPoint64Property extends FixedPointProperty {
    public FixedPoint64Property(ByteBuffer meta, ByteBuffer data) {
        super(meta);
        // BigDecimal = unscaled * 10 ^ -scale
        int scale = meta.getInt();
        byte flags = meta.get();
        if ((flags & 0b00000001) > 0) {
            long unscaledMin = meta.getLong();
            this.minValue = BigDecimal.valueOf(unscaledMin, scale);
        } else {
            this.minValue = BigDecimal.valueOf(Long.MIN_VALUE, scale);
        }
        if ((flags & 0b00000010) > 0) {
            long unscaledMax = meta.getLong();
            this.maxValue = BigDecimal.valueOf(unscaledMax, scale);
        } else {
            this.maxValue = BigDecimal.valueOf(Long.MAX_VALUE, scale);
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
        long unscaledVal = data.getLong();
        this.value = BigDecimal.valueOf(unscaledVal, scale);
    }

    @Override
    public int getType() {
        return Type.FIXP64;
    }

    @Override
    public List<Byte> serializeData() {
        BigInteger unscaled = value.unscaledValue();
        long unscaledLong = unscaled.longValue();
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(unscaledLong);
        return Util.bufferToList(buffer);
    }
}
