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

public class DateRangeProperty extends NamedItem {
    private final ZoneId zone = ZoneId.systemDefault();
    private LocalDate valueFrom;
    private LocalDate valueTo;

    public DateRangeProperty(ByteBuffer meta, ByteBuffer data) {
        super(meta);
        long fromTimestamp = data.getLong();
        long toTimestamp = data.getLong();
        Instant fromTime = Instant.ofEpochMilli(fromTimestamp);
        ZonedDateTime fromZonedDateTime = fromTime.atZone(zone);
        Instant toTime = Instant.ofEpochMilli(toTimestamp);
        ZonedDateTime toZonedDateTime = toTime.atZone(zone);
        valueFrom = fromZonedDateTime.toLocalDate();
        valueTo = toZonedDateTime.toLocalDate();
    }

    @Override
    public int getType() {
        return Type.DATE_RANGE;
    }

    @Override
    public List<Byte> serializeData() {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES * 2)
                .order(ByteOrder.BIG_ENDIAN);
        long fromTimestamp = valueFrom.atStartOfDay(zone).toInstant().toEpochMilli();
        long toTimestamp = valueTo.atStartOfDay(zone).toInstant().toEpochMilli();
        buffer.putLong(fromTimestamp);
        buffer.putLong(toTimestamp);
        return Util.bufferToList(buffer);
    }

    public LocalDate getValueFrom() {
        return valueFrom;
    }

    public Instant getValueFromAsInstant() {
        return valueFrom.atStartOfDay(zone).toInstant();
    }

    public void setValueFrom(LocalDate valueFrom) {
        this.valueFrom = valueFrom;
    }

    public void setValueFrom(Instant value) {
        this.valueFrom = value.atZone(zone).toLocalDate();
    }

    public LocalDate getValueTo() {
        return valueTo;
    }

    public Instant getValueToAsInstant() {
        return valueTo.atStartOfDay(zone).toInstant();
    }

    public void setValueTo(LocalDate valueTo) {
        this.valueTo = valueTo;
    }

    public void setValueTo(Instant value) {
        this.valueTo = value.atZone(zone).toLocalDate();
    }
}
