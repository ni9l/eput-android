package eput.protocol.properties;

import java.nio.ByteBuffer;

public abstract class SignedIntegerProperty extends IntegerProperty {
    protected SignedIntegerProperty(int size, ByteBuffer meta, ByteBuffer data) {
        super(true, size, meta, data);
    }
}
