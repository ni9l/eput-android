package eput.protocol.properties;

import java.nio.ByteBuffer;

import eput.protocol.Type;

public class StrPhoneProperty extends StrAsciiProperty {
    public StrPhoneProperty(ByteBuffer meta, ByteBuffer data) {
        super(meta, data);
    }

    @Override
    public int getType() {
        return Type.STR_PHONE;
    }
}
