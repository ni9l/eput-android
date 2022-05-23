package eput.protocol.properties;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import eput.protocol.NamedItem;
import eput.protocol.Type;
import eput.protocol.Util;

public class DateTimeProperty extends NamedItem {
    protected final ZoneId zone = ZoneId.systemDefault();
    protected LocalDateTime value;

    public DateTimeProperty(ByteBuffer meta, ByteBuffer data) {
        super(meta);
        long timestamp = data.getLong();
        Instant time = Instant.ofEpochMilli(timestamp);
        ZonedDateTime zonedDateTime = time.atZone(zone);
        value = zonedDateTime.toLocalDateTime();
    }

    @Override
    public int getType() {
        return Type.DATE_TIME;
    }

    @Override
    public List<Byte> serializeData() {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES)
                .order(ByteOrder.BIG_ENDIAN);
        LocalDateTime d = value;
        long timestamp = d.atZone(zone).toInstant().toEpochMilli();
        buffer.putLong(timestamp);
        return Util.bufferToList(buffer);
    }

    public void setValue(LocalDateTime value) {
        this.value = value;
    }

    public void setValue(Instant instant, int hours, int minutes) {
        this.value = LocalDateTime
                .ofInstant(instant, zone)
                .withHour(hours)
                .withMinute(minutes);
    }

    public LocalDateTime getValue() {
        return value;
    }

    public Instant getValueAsInstant() {
        return value.atZone(zone).toInstant();
    }
}
