package eput.protocol.properties;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.LocalTime;
import java.util.List;

import eput.protocol.NamedItem;
import eput.protocol.Type;
import eput.protocol.Util;

public class TimeRangeProperty extends NamedItem {
    private LocalTime valueFrom;
    private LocalTime valueTo;

    public TimeRangeProperty(ByteBuffer meta, ByteBuffer data) {
        super(meta);
        int fromHours = data.get() & 0xFF;
        int fromMinutes = data.get() & 0xFF;
        int fromSeconds = data.get() & 0xFF;
        int toHours = data.get() & 0xFF;
        int toMinutes = data.get() & 0xFF;
        int toSeconds = data.get() & 0xFF;
        valueFrom = LocalTime.of(fromHours, fromMinutes, fromSeconds);
        valueTo = LocalTime.of(toHours, toMinutes, toSeconds);
    }

    @Override
    public int getType() {
        return Type.TIME_RANGE;
    }

    @Override
    public List<Byte> serializeData() {
        ByteBuffer buffer = ByteBuffer.allocate(6).order(ByteOrder.BIG_ENDIAN);
        buffer.put((byte) valueFrom.getHour());
        buffer.put((byte) valueFrom.getMinute());
        buffer.put((byte) valueFrom.getSecond());
        buffer.put((byte) valueTo.getHour());
        buffer.put((byte) valueTo.getMinute());
        buffer.put((byte) valueTo.getSecond());
        return Util.bufferToList(buffer);
    }

    public LocalTime getValueFrom() {
        return valueFrom;
    }

    public void setValueFrom(LocalTime valueFrom) {
        this.valueFrom = valueFrom;
    }

    public LocalTime getValueTo() {
        return valueTo;
    }

    public void setValueTo(LocalTime valueTo) {
        this.valueTo = valueTo;
    }
}
