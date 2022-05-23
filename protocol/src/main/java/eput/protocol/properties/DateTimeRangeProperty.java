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

public class DateTimeRangeProperty extends NamedItem {
    private final ZoneId zone = ZoneId.systemDefault();
    private LocalDateTime valueFrom;
    private LocalDateTime valueTo;

    public DateTimeRangeProperty(ByteBuffer meta, ByteBuffer data) {
        super(meta);
        long fromTimestamp = data.getLong();
        long toTimestamp = data.getLong();
        Instant fromTime = Instant.ofEpochMilli(fromTimestamp);
        ZonedDateTime fromZonedDateTime = fromTime.atZone(zone);
        Instant toTime = Instant.ofEpochMilli(toTimestamp);
        ZonedDateTime toZonedDateTime = toTime.atZone(zone);
        valueFrom = fromZonedDateTime.toLocalDateTime();
        valueTo = toZonedDateTime.toLocalDateTime();
    }

    @Override
    public int getType() {
        return Type.DATE_TIME_RANGE;
    }

    @Override
    public List<Byte> serializeData() {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES * 2)
                .order(ByteOrder.BIG_ENDIAN);
        long fromTimestamp = valueFrom.atZone(zone).toInstant().toEpochMilli();
        long toTimestamp = valueTo.atZone(zone).toInstant().toEpochMilli();
        buffer.putLong(fromTimestamp);
        buffer.putLong(toTimestamp);
        return Util.bufferToList(buffer);
    }

    public LocalDateTime getValueFrom() {
        return valueFrom;
    }

    public Instant getValueFromAsInstant() {
        return valueFrom.atZone(zone).toInstant();
    }

    public void setValueFrom(LocalDateTime valueFrom) {
        this.valueFrom = valueFrom;
    }

    public void setValueFrom(Instant instant, int hours, int minutes) {
        this.valueFrom = LocalDateTime
                .ofInstant(instant, zone)
                .withHour(hours)
                .withMinute(minutes);
    }

    public LocalDateTime getValueTo() {
        return valueTo;
    }

    public Instant getValueToAsInstant() {
        return valueTo.atZone(zone).toInstant();
    }

    public void setValueTo(LocalDateTime valueTo) {
        this.valueTo = valueTo;
    }

    public void setValueTo(Instant instant, int hours, int minutes) {
        this.valueTo = LocalDateTime
                .ofInstant(instant, zone)
                .withHour(hours)
                .withMinute(minutes);
    }
}
