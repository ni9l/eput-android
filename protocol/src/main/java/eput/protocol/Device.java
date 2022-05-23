package eput.protocol;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.DeflaterInputStream;
import java.util.zip.InflaterInputStream;

import eput.protocol.properties.UserDefinedProperty;

public class Device {
    private final int type;
    private final String name;
    private final byte[] ids;
    private final Instant dataWrittenTimestamp;
    private final List<String> availableLanguages;
    private final List<BaseItem> properties;

    private Device(
            int type,
            String name,
            byte[] ids,
            Instant dataWrittenTimestamp,
            List<BaseItem> properties,
            List<String> availableLanguages
    ) {
        this.type = type;
        this.name = name;
        this.ids = ids;
        this.dataWrittenTimestamp = dataWrittenTimestamp;
        this.properties = properties;
        this.availableLanguages = availableLanguages;
    }

    public int getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public byte[] getIds() {
        return ids;
    }

    public Instant getDataWrittenTimestamp() {
        return dataWrittenTimestamp;
    }

    public List<String> getAvailableLanguages() {
        return availableLanguages;
    }

    public List<BaseItem> getProperties() {
        return properties;
    }

    public byte[] serializeData() {
        ArrayList<Byte> bytes = new ArrayList<>();
        for (BaseItem prop : properties) {
            if (prop instanceof NamedItem) {
                bytes.addAll(((NamedItem) prop).serializeData());
            } else if (prop instanceof UserDefinedProperty) {
                byte[] rawData = ((UserDefinedProperty) prop).getRawData();
                bytes.addAll(Util.bufferToList(ByteBuffer.wrap(rawData)));
            }
        }
        Instant timestamp = Instant.now();
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES).order(ByteOrder.BIG_ENDIAN);
        buffer.putLong(timestamp.toEpochMilli());
        bytes.addAll(Util.bufferToList(buffer));
        byte[] arr = new byte[bytes.size()];
        Iterator<Byte> iterator = bytes.iterator();
        for (int i = 0; i < arr.length; i++) {
            arr[i] = iterator.next();
        }
        return arr;
    }

    public static Device deserialize(byte[] meta, byte[] data, boolean compressed) throws
            IllegalArgumentException, IllegalStateException {
        byte[] plainMetadata;
        if (compressed) {
            plainMetadata = decompress(meta);
        } else {
            plainMetadata = meta;
        }
        ByteBuffer metaBuffer = ByteBuffer.wrap(plainMetadata).order(ByteOrder.BIG_ENDIAN);
        ByteBuffer dataBuffer = ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN);

        int deviceType = metaBuffer.get() & 0xFF;
        byte[] deviceIds = new byte[8];
        metaBuffer.get(deviceIds);
        String deviceName = Util.getAsciiString(metaBuffer);
        if (deviceName == null) {
            deviceName = "";
        }
        List<BaseItem> properties = Util.deserializeProperties(metaBuffer, dataBuffer);
        Instant timestamp = getDataWrittenTimestamp(dataBuffer);
        List<String> languages = new ArrayList<>();
        while (metaBuffer.remaining() != 0) {
            String langCode = Util.getAsciiString(metaBuffer);
            if (langCode == null) {
                break;
            }
            languages.add(langCode);
            for (BaseItem item : properties) {
                Util.addPropertyTranslation(item, langCode, metaBuffer);
            }
        }
        List<String> ids = Util.getIds(properties);
        for (BaseItem prop : properties) {
            if (prop instanceof DependencyProperty) {
                ((DependencyProperty) prop).resolveDependencies(ids);
            }
        }
        return new Device(deviceType, deviceName, deviceIds, timestamp, properties, languages);
    }

    public static byte[] decompress(byte[] meta) {
        InflaterInputStream input = new InflaterInputStream(new ByteArrayInputStream(meta));
        ByteArrayOutputStream output = new ByteArrayOutputStream(1024);
        byte[] buffer = new byte[1024];
        try {
            while (true) {
                int read = input.read(buffer);
                if (read == -1) break;
                output.write(buffer, 0, read);
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Ignore
        }
        return output.toByteArray();
    }

    public static byte[] compress(byte[] meta) {
        Deflater def = new Deflater(Deflater.BEST_COMPRESSION);
        DeflaterInputStream input = new DeflaterInputStream(new ByteArrayInputStream(meta), def);
        ByteArrayOutputStream output = new ByteArrayOutputStream(1024);
        byte[] buffer = new byte[1024];
        try {
            while (true) {
                int read = input.read(buffer);
                if (read == -1) break;
                output.write(buffer, 0, read);
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Ignore
        }
        return output.toByteArray();
    }

    private static Instant getDataWrittenTimestamp(ByteBuffer data) throws
            IllegalArgumentException {
        if (data.remaining() != 8) {
            throw new IllegalArgumentException("Data ended before timestamp");
        } else {
            long timestamp = data.getLong();
            return Instant.ofEpochMilli(timestamp);
        }
    }

    public static byte[] getDeviceIds(byte[] meta, boolean compressed) {
        byte[] plainMetadata;
        if (compressed) {
            plainMetadata = decompress(meta);
        } else {
            plainMetadata = meta;
        }
        ByteBuffer metaBuffer = ByteBuffer.wrap(plainMetadata).order(ByteOrder.BIG_ENDIAN);
        byte[] deviceIds = new byte[8];
        metaBuffer.get(deviceIds);
        return deviceIds;
    }
}
