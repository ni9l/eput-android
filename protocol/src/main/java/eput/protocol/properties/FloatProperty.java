package eput.protocol.properties;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import eput.protocol.Type;

public class FloatProperty extends NumberProperty {
    public static final float DEF_MIN = -Float.MAX_VALUE;
    public static final float DEF_MAX = Float.MAX_VALUE;

    protected final int contentType;
    protected final int contentTypeDefault;
    private final float maxValue;
    private final float minValue;

    public FloatProperty(ByteBuffer meta, ByteBuffer data) {
        super(meta);
        byte flags = meta.get();
        if ((flags & 0b00000001) > 0) {
            this.minValue = meta.getFloat();
        } else {
            this.minValue = DEF_MIN;
        }
        if ((flags & 0b00000010) > 0) {
            this.maxValue = meta.getFloat();
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
        binaryValue = new byte[4];
        data.get(binaryValue);
    }

    @Override
    public int getType() {
        return Type.FLOAT;
    }

    public float getMaxValue() {
        return maxValue;
    }

    public float getMinValue() {
        return minValue;
    }

    public void setValue(float value) {
        if (Float.compare(getMinValue(), value) <= 0 && Float.compare(getMaxValue(), value) >= 0) {
            ByteBuffer buf = ByteBuffer.wrap(binaryValue).order(ByteOrder.BIG_ENDIAN);
            buf.putFloat(value);
        } else {
            throw new NumberFormatException(
                    String.format(
                            "Number %f must be in range %f to %f",
                            value,
                            getMinValue(),
                            getMaxValue()));
        }
    }

    public float getValue() {
        return ByteBuffer.wrap(binaryValue).order(ByteOrder.BIG_ENDIAN).getFloat();
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
