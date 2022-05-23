package eput.protocol.properties;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import eput.protocol.Type;

public class StrAsciiProperty extends StringProperty {
    private final CharsetEncoder encoder;

    public StrAsciiProperty(ByteBuffer meta, ByteBuffer data) {
        super(meta, data);
        this.encoder = StandardCharsets.US_ASCII.newEncoder();
    }

    @Override
    public int getType() {
        return Type.STR_ASCII;
    }

    @Override
    public void setValue(String value) {
        encoder.reset();
        if (encoder.canEncode(value)) {
            if (value.length() <= maxLength) {
                try {
                    ByteBuffer buffer = encoder.encode(CharBuffer.wrap(value));
                    binaryString = Arrays.copyOf(buffer.array(), maxLength);
                } catch (CharacterCodingException e) {
                    throw new RuntimeException("Invalid String");
                }
            } else {
                throw new IllegalArgumentException("String is too long");
            }
        } else {
            throw new IllegalArgumentException("String is not valid ASCII");
        }
    }

    @Override
    public String getValue() {
        return new String(binaryString, StandardCharsets.US_ASCII).trim();
    }
}
