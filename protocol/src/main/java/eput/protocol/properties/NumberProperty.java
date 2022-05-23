package eput.protocol.properties;

import java.nio.ByteBuffer;
import java.util.List;

import eput.protocol.NamedItem;
import eput.protocol.Util;

public abstract class NumberProperty extends NamedItem {
    protected byte[] binaryValue;

    protected NumberProperty(ByteBuffer meta) {
        super(meta);
    }

    @Override
    public List<Byte> serializeData() {
        return Util.bufferToList(ByteBuffer.wrap(binaryValue));
    }

    public abstract int getContentType();

    public abstract int getContentTypeDefault();
}
