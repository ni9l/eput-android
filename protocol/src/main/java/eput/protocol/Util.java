package eput.protocol;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eput.protocol.modifiers.Divider;
import eput.protocol.modifiers.Header;
import eput.protocol.properties.ArrayProperty;
import eput.protocol.properties.BoolProperty;
import eput.protocol.properties.DateProperty;
import eput.protocol.properties.DateRangeProperty;
import eput.protocol.properties.DateTimeProperty;
import eput.protocol.properties.DateTimeRangeProperty;
import eput.protocol.properties.DoubleNumberListProperty;
import eput.protocol.properties.DoubleProperty;
import eput.protocol.properties.FixedPoint32Property;
import eput.protocol.properties.FixedPoint64Property;
import eput.protocol.properties.FloatProperty;
import eput.protocol.properties.Int16Property;
import eput.protocol.properties.Int32Property;
import eput.protocol.properties.Int64Property;
import eput.protocol.properties.Int8Property;
import eput.protocol.properties.IntNumberListProperty;
import eput.protocol.properties.LanguageProperty;
import eput.protocol.properties.NOutOfMProperty;
import eput.protocol.properties.OneOutOfMProperty;
import eput.protocol.properties.StrAsciiProperty;
import eput.protocol.properties.StrEmailProperty;
import eput.protocol.properties.StrPasswordProperty;
import eput.protocol.properties.StrPhoneProperty;
import eput.protocol.properties.StrUriProperty;
import eput.protocol.properties.StrUtf8Property;
import eput.protocol.properties.TimeProperty;
import eput.protocol.properties.TimeRangeProperty;
import eput.protocol.properties.UInt16Property;
import eput.protocol.properties.UInt32Property;
import eput.protocol.properties.UInt64Property;
import eput.protocol.properties.UInt8Property;
import eput.protocol.properties.UserDefinedProperty;
import eput.protocol.properties.ZonedDateTimeProperty;

public class Util {
    public static List<Byte> bufferToList(ByteBuffer buffer) {
        ArrayList<Byte> lis = new ArrayList<>(buffer.capacity());
        for (byte b : buffer.array()) {
            lis.add(b);
        }
        return lis;
    }

    public static String getAsciiString(ByteBuffer buffer) {
        int start = buffer.position();
        int len = 0;
        while (buffer.get() != 0) {
            len += 1;
        }
        if (len > 0) {
            buffer.position(start);
            byte[] str = new byte[len];
            buffer.get(str);
            buffer.get();
            return new String(str, StandardCharsets.US_ASCII);
        } else {
            return null;
        }
    }

    public static String getUtf8String(ByteBuffer buffer) {
        int start = buffer.position();
        int len = 0;
        while (buffer.get() != 0) {
            len += 1;
        }
        if (len > 0) {
            buffer.position(start);
            byte[] str = new byte[len];
            buffer.get(str);
            buffer.get();
            return new String(str, StandardCharsets.UTF_8);
        } else {
            return null;
        }
    }

    public static void addPropertyTranslation(
            BaseItem prop,
            String language,
            ByteBuffer meta
    ) {
        if (prop instanceof NamedItem) {
            String translatedId = Util.getUtf8String(meta);
            if (translatedId != null) {
                ((NamedItem) prop).addDisplayName(language, translatedId);
            }
        }
        if (prop instanceof SubEntryProperty) {
            SubEntryProperty p = (SubEntryProperty) prop;
            p.addSubEntryTranslations(language, meta);
        }
    }

    public static Map<Integer, List<Integer>> readDependencyMap(ByteBuffer meta) {
        Map<Integer, List<Integer>> dependencyMap = new HashMap<>();
        int depCount = meta.get() & 0xFF;
        for (int i = 0; i < depCount; i++) {
            int entryIndex = meta.get() & 0xFF;
            int idCount = meta.get() & 0xFF;
            List<Integer> idIndexes = new ArrayList<>(idCount);
            for (int j = 0; j < idCount; j++) {
                int depIndex = 0;
                depIndex = depIndex + meta.get();
                depIndex = (depIndex << 8) + meta.get();
                idIndexes.add(depIndex);
            }
            dependencyMap.put(entryIndex, idIndexes);
        }
        return dependencyMap;
    }

    public static List<String> getIds(List<BaseItem> properties) {
        List<String> ids = new ArrayList<>();
        properties.forEach((it) -> {
            if (it instanceof NamedItem) {
                ids.add(((NamedItem) it).getId());
            }
            if (it instanceof SubEntryProperty) {
                ids.addAll(((SubEntryProperty) it).getSubEntryIds());
            }
        });
        return ids;
    }

    public static List<BaseItem> deserializeProperties(ByteBuffer meta, ByteBuffer data) throws
            IllegalArgumentException, IllegalStateException {
        List<BaseItem> properties = new ArrayList<>();
        while (meta.remaining() != 0) {
            BaseItem prop = deserializeProperty(meta, data);
            if (prop != null) {
                properties.add(prop);
            } else {
                return properties;
            }
        }
        throw new IllegalArgumentException("Metadata buffer ended before end marker");
    }
    
    public static BaseItem deserializeProperty(ByteBuffer meta, ByteBuffer data) throws
            IllegalArgumentException, IllegalStateException {
        int propertyType = meta.get() & 0xFF;
        switch (propertyType) {
            case Type.DIVIDER:
                return new Divider();
            case Type.HEADER:
                return new Header(meta);
            case Type.ONE_OUT_OF_M:
                return new OneOutOfMProperty(meta, data);
            case Type.N_OUT_OF_M:
                return new NOutOfMProperty(meta, data);
            case Type.BOOL:
                return new BoolProperty(meta, data);
            case Type.ARRAY:
                return new ArrayProperty(meta, data);
            case Type.UINT8_T:
                return new UInt8Property(meta, data);
            case Type.UINT16_T:
                return new UInt16Property(meta, data);
            case Type.UINT32_T:
                return new UInt32Property(meta, data);
            case Type.UINT64_T:
                return new UInt64Property(meta, data);
            case Type.INT8_T:
                return new Int8Property(meta, data);
            case Type.INT16_T:
                return new Int16Property(meta, data);
            case Type.INT32_T:
                return new Int32Property(meta, data);
            case Type.INT64_T:
                return new Int64Property(meta, data);
            case Type.FLOAT:
                return new FloatProperty(meta, data);
            case Type.DOUBLE:
                return new DoubleProperty(meta, data);
            case Type.NUMBER_LIST_INT:
                return new IntNumberListProperty(meta, data);
            case Type.NUMBER_LIST_DOUBLE:
                return new DoubleNumberListProperty(meta, data);
            case Type.DATE:
                return new DateProperty(meta, data);
            case Type.DATE_TIME:
                return new DateTimeProperty(meta, data);
            case Type.TIME:
                return new TimeProperty(meta, data);
            case Type.ZONED_DATE_TIME:
                return new ZonedDateTimeProperty(meta, data);
            case Type.DATE_RANGE:
                return new DateRangeProperty(meta, data);
            case Type.DATE_TIME_RANGE:
                return new DateTimeRangeProperty(meta, data);
            case Type.TIME_RANGE:
                return new TimeRangeProperty(meta, data);
            case Type.STR_ASCII:
                return new StrAsciiProperty(meta, data);
            case Type.STR_UTF8:
                return new StrUtf8Property(meta, data);
            case Type.STR_EMAIL:
                return new StrEmailProperty(meta, data);
            case Type.STR_PHONE:
                return new StrPhoneProperty(meta, data);
            case Type.STR_URI:
                return new StrUriProperty(meta, data);
            case Type.STR_PASSWORD:
                return new StrPasswordProperty(meta, data);
            case Type.FIXP32:
                return new FixedPoint32Property(meta, data);
            case Type.FIXP64:
                return new FixedPoint64Property(meta, data);
            case Type.LANGUAGE:
                return new LanguageProperty(meta, data);
            case Type.METADATA_TRUNCATED:
                throw new IllegalStateException("Metadata incomplete");
            case Type.END:
                return null;
            default:
                if ((propertyType & 0b10000000) == 0) {
                    // User defined property
                    return new UserDefinedProperty(meta, data, propertyType);
                } else {
                    throw new IllegalArgumentException("Unknown property type: " + propertyType);
                }
        }
    }
}
