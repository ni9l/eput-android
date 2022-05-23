package eput.protocol.properties;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;

import eput.protocol.Type;

public abstract class IntegerProperty extends NumberProperty {
    protected final int dataSize;
    protected final boolean isSigned;
    protected final int contentType;
    protected final int contentTypeDefault;
    private final BigInteger maxValue;
    private final BigInteger minValue;
    private final BigInteger stepSize;

    public IntegerProperty(boolean signed, int size, ByteBuffer meta, ByteBuffer data) {
        super(meta);
        dataSize = size;
        isSigned = signed;
        byte flags = meta.get();
        if ((flags & 0b00000001) > 0) {
            byte[] arr = new byte[dataSize];
            meta.get(arr);
            if (isSigned) {
                this.minValue = new BigInteger(arr);
            } else {
                this.minValue = new BigInteger(1, arr);
            }
        } else {
            this.minValue = getDefaultMinValue();
        }
        if ((flags & 0b00000010) > 0) {
            byte[] arr = new byte[dataSize];
            meta.get(arr);
            if (isSigned) {
                this.maxValue = new BigInteger(arr);
            } else {
                this.maxValue = new BigInteger(1, arr);
            }
        } else {
            this.maxValue = getDefaultMaxValue();
        }
        if ((flags & 0b00000100) > 0) {
            byte[] arr = new byte[dataSize];
            meta.get(arr);
            if (isSigned) {
                this.stepSize = new BigInteger(arr);
            } else {
                this.stepSize = new BigInteger(1, arr);
            }
        } else {
            this.stepSize = getDefaultStepSize();
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
        binaryValue = new byte[dataSize];
        data.get(binaryValue);
    }

    public BigInteger getMaxValue() {
        return maxValue;
    }

    public BigInteger getMinValue() {
        return minValue;
    }

    public BigInteger getStepSize() {
        return stepSize;
    }

    public void setValue(BigInteger value) {
        if (value.compareTo(getMinValue()) >= 0 &&
                value.compareTo(getMaxValue()) <= 0 &&
                value.mod(stepSize).equals(BigInteger.ZERO)) {
            byte[] arr = value.toByteArray();
            binaryValue = new byte[dataSize];
            if (isSigned) {
                byte sign = (byte) ((arr[0] & 0x80) == 0 ? 0x00 : 0xFF);
                Arrays.fill(binaryValue, sign);
            }
            System.arraycopy(
                    arr,
                    0,
                    binaryValue,
                    binaryValue.length - arr.length,
                    arr.length
            );
        } else {
            throw new NumberFormatException(
                    String.format(
                            "Number %d must be in range %d to %d",
                            value,
                            getMinValue(),
                            getMaxValue()));
        }
    }

    public BigInteger getValue() {
        if (isSigned) {
            return new BigInteger(binaryValue);
        } else {
            return new BigInteger(1, binaryValue);
        }
    }

    @Override
    public int getContentType() {
        return contentType;
    }

    @Override
    public int getContentTypeDefault() {
        return contentTypeDefault;
    }

    protected abstract BigInteger getDefaultMinValue();

    protected abstract BigInteger getDefaultMaxValue();

    protected abstract BigInteger getDefaultStepSize();
}
