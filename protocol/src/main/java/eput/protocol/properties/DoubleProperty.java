package eput.protocol.properties;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import eput.protocol.Type;

public class DoubleProperty extends NumberProperty {
    public static final double DEF_MIN = -Double.MIN_VALUE;
    public static final double DEF_MAX = Double.MAX_VALUE;

    protected final int contentType;
    protected final int contentTypeDefault;
    private final double maxValue;
    private final double minValue;

    public DoubleProperty(ByteBuffer meta, ByteBuffer data) {
        super(meta);
        byte flags = meta.get();
        if ((flags & 0b00000001) > 0) {
            this.minValue = meta.getDouble();
        } else {
            this.minValue = DEF_MIN;
        }
        if ((flags & 0b00000010) > 0) {
            this.maxValue = meta.getDouble();
        } else {
            this.maxValue = DEF_MAX;
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
        binaryValue = new byte[8];
        data.get(binaryValue);
    }

    @Override
    public int getType() {
        return Type.DOUBLE;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public double getMinValue() {
        return minValue;
    }

    public void setValue(double value) {
        if (Double.compare(getMinValue(), value) <= 0 && Double.compare(getMaxValue(), value) >= 0) {
            ByteBuffer buf = ByteBuffer.wrap(binaryValue).order(ByteOrder.BIG_ENDIAN);
            buf.putDouble(value);
        } else {
            throw new NumberFormatException(
                    String.format(
                            "Number %f must be in range %f to %f",
                            value,
                            getMinValue(),
                            getMaxValue()));
        }
    }

    public double getValue() {
        return ByteBuffer.wrap(binaryValue).order(ByteOrder.BIG_ENDIAN).getDouble();
    }

    @Override
    public int getContentType() {
        return contentType;
    }

    @Override
    public int getContentTypeDefault() {
        return contentTypeDefault;
    }
}
