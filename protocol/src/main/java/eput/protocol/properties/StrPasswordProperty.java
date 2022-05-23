package eput.protocol.properties;

import java.nio.ByteBuffer;

import eput.protocol.Type;

public class StrPasswordProperty extends StrUtf8Property {
    public StrPasswordProperty(ByteBuffer meta, ByteBuffer data) {
        super(meta, data);
    }

    @Override
    public int getType() {
        return Type.STR_PASSWORD;
    }
}
