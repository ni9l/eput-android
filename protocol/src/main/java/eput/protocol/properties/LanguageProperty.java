package eput.protocol.properties;

import java.nio.ByteBuffer;

import eput.protocol.Type;

public class LanguageProperty extends OneOutOfMProperty {
    public LanguageProperty(ByteBuffer meta, ByteBuffer data) {
        super(meta, data);
    }

    @Override
    public int getType() {
        return Type.LANGUAGE;
    }
}
