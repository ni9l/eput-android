package eput.protocol.properties;

import java.nio.ByteBuffer;

import eput.protocol.Type;

public class StrUriProperty extends StrUtf8Property {
    public StrUriProperty(ByteBuffer meta, ByteBuffer data) {
        super(meta, data);
    }

    @Override
    public int getType() {
        return Type.STR_URI;
    }
}
