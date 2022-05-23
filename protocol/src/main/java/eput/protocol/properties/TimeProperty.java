package eput.protocol.properties;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.LocalTime;
import java.util.List;

import eput.protocol.NamedItem;
import eput.protocol.Type;
import eput.protocol.Util;

public class TimeProperty extends NamedItem {
    private LocalTime value;

    public TimeProperty(ByteBuffer meta, ByteBuffer data) {
        super(meta);
        int hours = data.get() & 0xFF;
        int minutes = data.get() & 0xFF;
        int seconds = data.get() & 0xFF;
        value = LocalTime.of(hours, minutes, seconds);
    }

    @Override
    public int getType() {
        return Type.TIME;
    }

    @Override
    public List<Byte> serializeData() {
        ByteBuffer buffer = ByteBuffer.allocate(3).order(ByteOrder.BIG_ENDIAN);
        buffer.put((byte) value.getHour());
        buffer.put((byte) value.getMinute());
        buffer.put((byte) value.getSecond());
        return Util.bufferToList(buffer);
    }

    public LocalTime getValue() {
        return value;
    }

    public void setValue(LocalTime value) {
        this.value = value;
    }
}
