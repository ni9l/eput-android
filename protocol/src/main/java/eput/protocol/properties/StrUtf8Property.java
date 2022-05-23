package eput.protocol.properties;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import eput.protocol.Type;

public class StrUtf8Property extends StringProperty {
    public StrUtf8Property(ByteBuffer meta, ByteBuffer data) {
        super(meta, data);
    }

    @Override
    public int getType() {
        return Type.STR_UTF8;
    }

    @Override
    public void setValue(String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        if (bytes.length <= maxLength) {
            binaryString = Arrays.copyOf(bytes, maxLength);
        } else {
            throw new IllegalArgumentException("String is too long");
        }
    }

    @Override
    public String getValue() {
        return new String(binaryString, StandardCharsets.UTF_8).trim();
    }
}
