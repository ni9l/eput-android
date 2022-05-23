package eput.protocol.modifiers;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;

import eput.protocol.NamedItem;
import eput.protocol.Type;

public class Header extends NamedItem {
    public Header(ByteBuffer meta) {
        super(meta);
    }

    @Override
    public int getType() {
        return Type.HEADER;
    }

    @Override
    public List<Byte> serializeData() {
        return Collections.emptyList();
    }
}
