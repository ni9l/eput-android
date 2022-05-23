package eput.protocol.properties;

import java.nio.ByteBuffer;

import eput.protocol.BaseItem;

public class UserDefinedProperty implements BaseItem {
    private final int rawType;
    private final byte[] rawMeta;
    private final byte[] rawData;

    public UserDefinedProperty(ByteBuffer meta, ByteBuffer data, int type) {
        rawType = type;
        int metaLength = meta.get() & 0xFF;
        int dataLength = meta.get() & 0xFF;
        rawMeta = new byte[metaLength];
        meta.get(rawMeta);
        rawData = new byte[dataLength];
        data.get(rawData);
    }

    @Override
    public int getType() {
        return rawType;
    }

    public byte[] getRawMeta() {
        return rawMeta;
    }

    public byte[] getRawData() {
        return rawData;
    }
}
