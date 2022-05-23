package eput.protocol.properties;

import java.nio.ByteBuffer;
import java.util.List;

import eput.protocol.NamedItem;
import eput.protocol.Util;

public abstract class StringProperty extends NamedItem {
    protected byte[] binaryString;
    protected final int maxLength;

    protected StringProperty(ByteBuffer meta, ByteBuffer data) {
        super(meta);
        this.maxLength = (meta.get() & 0xFF) - 1; // Not counting trailing 0
        this.binaryString = new byte[maxLength];
        data.get(binaryString);
    }

    @Override
    public List<Byte> serializeData() {
        return Util.bufferToList(ByteBuffer.wrap(binaryString));
    }

    public abstract void setValue(String value);

    public abstract String getValue();

    public int getMaxLength() {
        return maxLength;
    }
}
