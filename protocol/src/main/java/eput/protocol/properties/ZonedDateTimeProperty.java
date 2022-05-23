package eput.protocol.properties;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

import eput.protocol.NamedItem;
import eput.protocol.Type;
import eput.protocol.Util;

public class ZonedDateTimeProperty extends NamedItem {
    private OffsetDateTime value;

    public ZonedDateTimeProperty(ByteBuffer meta, ByteBuffer data) {
        super(meta);
        long timestamp = data.getLong();
        short offsetMinutes = data.getShort();
        Instant time = Instant.ofEpochMilli(timestamp);
        ZoneOffset offset = ZoneOffset.ofHoursMinutes(
                offsetMinutes / 60,
                offsetMinutes % 60);
        value = OffsetDateTime.ofInstant(time, offset);
    }

    @Override
    public int getType() {
        return Type.ZONED_DATE_TIME;
    }

    @Override
    public List<Byte> serializeData() {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES + Short.BYTES)
                .order(ByteOrder.BIG_ENDIAN);
        OffsetDateTime d = value;
        long timestamp = d.toInstant().toEpochMilli();
        int offsetSeconds = d.getOffset().getTotalSeconds();
        short offset = (short) (offsetSeconds / 60);
        buffer.putLong(timestamp);
        buffer.putShort(offset);
        return Util.bufferToList(buffer);
    }

    public void setValue(OffsetDateTime value) {
        this.value = value;
    }

    public void setValue(ZoneId zone, Instant instant, int hours, int minutes) {
        this.value = OffsetDateTime
                .ofInstant(instant, zone)
                .withHour(hours)
                .withMinute(minutes);
    }

    public void setOffset(ZoneOffset offset) {
        this.value = this.value.withOffsetSameInstant(offset);
    }

    public OffsetDateTime getValue() {
        return value;
    }

    public Instant getValueAsInstant() {
        return value.toInstant();
    }
}
