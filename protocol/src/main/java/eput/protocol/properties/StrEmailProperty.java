package eput.protocol.properties;

import java.nio.ByteBuffer;

import eput.protocol.Type;

public class StrEmailProperty extends StrUtf8Property {
    public StrEmailProperty(ByteBuffer meta, ByteBuffer data) {
        super(meta, data);
    }

    @Override
    public int getType() {
        return Type.STR_EMAIL;
    }
}
