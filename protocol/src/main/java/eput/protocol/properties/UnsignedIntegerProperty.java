package eput.protocol.properties;

import java.nio.ByteBuffer;

public abstract class UnsignedIntegerProperty extends IntegerProperty {
    protected UnsignedIntegerProperty(int size, ByteBuffer meta, ByteBuffer data) {
        super(false, size, meta, data);
    }
}
