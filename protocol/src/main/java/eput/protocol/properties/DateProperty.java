package eput.protocol.properties;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import eput.protocol.NamedItem;
import eput.protocol.Type;
import eput.protocol.Util;

public class DateProperty extends NamedItem {
    private final ZoneId zone = ZoneId.systemDefault();
    private LocalDate value;

    public DateProperty(ByteBuffer meta, ByteBuffer data) {
        super(meta);
        long timestamp = data.getLong();
        Instant time = Instant.ofEpochMilli(timestamp);
        ZonedDateTime zonedDateTime = time.atZone(zone);
        value = zonedDateTime.toLocalDate();
    }

    @Override
    public int getType() {
        return Type.DATE;
    }

    @Override
    public List<Byte> serializeData() {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES)
                .order(ByteOrder.BIG_ENDIAN);
        LocalDate d = value;
        long timestamp = d.atStartOfDay(zone).toInstant().toEpochMilli();
        buffer.putLong(timestamp);
        return Util.bufferToList(buffer);
    }

    public void setValue(LocalDate value) {
        this.value = value;
    }

    public void setValue(Instant value) {
        this.value = value.atZone(zone).toLocalDate();
    }

    public LocalDate getValue() {
        return value;
    }

    public Instant getValueAsInstant() {
        return value.atStartOfDay(zone).toInstant();
    }
}
