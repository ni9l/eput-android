package eput.protocol.properties;

import java.math.BigDecimal;
import java.nio.ByteBuffer;

public abstract class FixedPointProperty extends NumberProperty {
    protected int contentType;
    protected int contentTypeDefault;
    BigDecimal minValue;
    BigDecimal maxValue;
    BigDecimal value;

    protected FixedPointProperty(ByteBuffer meta) {
        super(meta);
    }

    public BigDecimal getMaxValue() {
        return maxValue;
    }

    public BigDecimal getMinValue() {
        return minValue;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        if (value.compareTo(getMinValue()) >= 0 && value.compareTo(getMaxValue()) <= 0) {
            this.value = value;
        } else {
            throw new NumberFormatException(
                    String.format(
                            "Number %g must be in range %g to %g",
                            value,
                            getMinValue(),
                            getMaxValue()));
        }
    }

    public int getContentType() {
        return contentType;
    }

    public int getContentTypeDefault() {
        return contentTypeDefault;
    }
}
