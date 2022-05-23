package eput.protocol;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static eput.protocol.TestUtil.assertBoolDependencies;
import static eput.protocol.TestUtil.assertIsArray;
import static eput.protocol.TestUtil.assertIsBool;
import static eput.protocol.TestUtil.assertIsDate;
import static eput.protocol.TestUtil.assertIsDateRange;
import static eput.protocol.TestUtil.assertIsDateTime;
import static eput.protocol.TestUtil.assertIsDateTimeRange;
import static eput.protocol.TestUtil.assertIsDivider;
import static eput.protocol.TestUtil.assertIsDouble;
import static eput.protocol.TestUtil.assertIsFloat;
import static eput.protocol.TestUtil.assertIsHeader;
import static eput.protocol.TestUtil.assertIsInt16;
import static eput.protocol.TestUtil.assertIsInt32;
import static eput.protocol.TestUtil.assertIsInt64;
import static eput.protocol.TestUtil.assertIsInt8;
import static eput.protocol.TestUtil.assertIsLanguage;
import static eput.protocol.TestUtil.assertIsNOutOfM;
import static eput.protocol.TestUtil.assertIsNumberListDouble;
import static eput.protocol.TestUtil.assertIsOneOutOfM;
import static eput.protocol.TestUtil.assertIsStrAscii;
import static eput.protocol.TestUtil.assertIsStrMail;
import static eput.protocol.TestUtil.assertIsStrPhone;
import static eput.protocol.TestUtil.assertIsStrPwd;
import static eput.protocol.TestUtil.assertIsStrUri;
import static eput.protocol.TestUtil.assertIsStrUtf8;
import static eput.protocol.TestUtil.assertIsTime;
import static eput.protocol.TestUtil.assertIsTimeRange;
import static eput.protocol.TestUtil.assertIsUInt16;
import static eput.protocol.TestUtil.assertIsUInt32;
import static eput.protocol.TestUtil.assertIsUInt64;
import static eput.protocol.TestUtil.assertIsUInt8;
import static eput.protocol.TestUtil.assertIsZonedDateTime;
import static eput.protocol.TestUtil.assertNOutOfMDependencies;
import static eput.protocol.TestUtil.assertOneOutOfMDependencies;
import static eput.protocol.TestUtil.getResource;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

import eput.protocol.properties.FloatProperty;
import eput.protocol.properties.Int32Property;
import eput.protocol.properties.UInt32Property;


public class DeviceTest {
    @Before
    public void before() {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
    }

    @Test
    public void serializeTest() throws IOException, URISyntaxException {
        byte[] meta = getResource("test_meta.bin");
        byte[] data = getResource("test_data.bin");
        byte[] dataNoTimeStamp = Arrays.copyOfRange(data, 0, data.length - 8);
        Device dev = Device.deserialize(meta, data, true);
        byte[] ser = dev.serializeData();
        byte[] serNoTimeStamp = Arrays.copyOfRange(ser, 0, ser.length - 8);
        byte[] serTimeStamp = Arrays.copyOfRange(ser, ser.length - 8, ser.length);
        assertArrayEquals(dataNoTimeStamp, serNoTimeStamp);
        ByteBuffer buffer = ByteBuffer.wrap(serTimeStamp).order(ByteOrder.BIG_ENDIAN);
        long timestamp = buffer.getLong();
        Instant timestampInstant = Instant.ofEpochMilli(timestamp);
        assertNotEquals(dev.getDataWrittenTimestamp(), timestampInstant);
    }

    @Test
    public void deserializeTest() throws IOException, URISyntaxException {
        byte[] meta = getResource("test_meta.bin");
        byte[] data = getResource("test_data.bin");
        Device dev = Device.deserialize(meta, data, true);

        assertEquals(DeviceType.CUSTOM, dev.getType());
        assertArrayEquals(DEV_IDS, dev.getIds());
        assertEquals("Property demo", dev.getName());

        List<BaseItem> properties = dev.getProperties();
        assertEquals(59, properties.size());
        assertIsHeader(properties.get(0), "integers_plain");
        assertIsUInt8(properties.get(1), "uint8_plain", BigInteger.valueOf(10));
        assertIsUInt16(properties.get(2), "uint16_plain", BigInteger.valueOf(20));
        assertIsUInt32(properties.get(3), "uint32_plain", BigInteger.valueOf(30));
        assertIsUInt64(properties.get(4), "uint64_plain", BigInteger.valueOf(40));
        assertIsInt8(properties.get(5), "int8_plain", BigInteger.valueOf(-10));
        assertIsInt16(properties.get(6), "int16_plain", BigInteger.valueOf(-20));
        assertIsInt32(properties.get(7), "int32_plain", BigInteger.valueOf(-30));
        assertIsInt64(properties.get(8), "int64_plain", BigInteger.valueOf(-40));
        assertIsFloat(properties.get(9), "float_plain", 101.101F);
        assertIsDouble(properties.get(10), "double_plain", 202.202D);
        assertIsNumberListDouble(properties.get(11), "number_list", 153.6, -34.0D, -10.7, -5.0, 0.0, 23.3, 64.0, 153.6, 365.0);
        assertIsDivider(properties.get(12));
        assertIsHeader(properties.get(13), "integers_min");
        assertIsUInt32(properties.get(14), "uint32_min", BigInteger.valueOf(510101), BigInteger.valueOf(70001), UInt32Property.DEF_MAX, UInt32Property.DEF_STP, Type.NumberContentType.NONE);
        assertIsInt32(properties.get(15), "int32_min", BigInteger.valueOf(-4365), BigInteger.valueOf(-32868), Int32Property.DEF_MAX, Int32Property.DEF_STP, Type.NumberContentType.NONE);
        assertIsFloat(properties.get(16), "float_min", 43534.654F, -23534.23F, FloatProperty.DEF_MAX, Type.NumberContentType.NONE);
        assertIsDivider(properties.get(17));
        assertIsHeader(properties.get(18), "integers_max");
        assertIsUInt32(properties.get(19), "uint32_max", BigInteger.valueOf(610101), UInt32Property.DEF_MIN, new BigInteger("4294964295"), UInt32Property.DEF_STP, Type.NumberContentType.NONE);
        assertIsInt32(properties.get(20), "int32_max", BigInteger.valueOf(23421), Int32Property.DEF_MIN, new BigInteger("2147423648"), Int32Property.DEF_STP, Type.NumberContentType.NONE);
        assertIsFloat(properties.get(21), "float_max", 1536.65F, FloatProperty.DEF_MIN, 321543.46F, Type.NumberContentType.NONE);
        assertIsDivider(properties.get(22));
        assertIsHeader(properties.get(23), "integers_all");
        assertIsUInt32(properties.get(24), "uint32_all", BigInteger.valueOf(610105), new BigInteger("70000"), new BigInteger("4294964295"), BigInteger.valueOf(5), Type.NumberContentType.NONE);
        assertIsInt32(properties.get(25), "int32_all", BigInteger.valueOf(23420), new BigInteger("-32860"), new BigInteger("2147423640"), BigInteger.valueOf(20), Type.NumberContentType.NONE);
        assertIsFloat(properties.get(26), "float_all", 1536.65F, -23534.23F, 321543.46F, Type.NumberContentType.NONE);
        assertIsDivider(properties.get(27));
        assertIsHeader(properties.get(28), "integers_content_type");
        assertIsInt32(properties.get(29), "int32_ct", BigInteger.valueOf(525465), new BigInteger("70000"), new BigInteger("2147433647"), BigInteger.valueOf(5), Type.NumberContentType.TIME);
        assertIsDivider(properties.get(30));
        assertIsHeader(properties.get(31), "dates");
        assertIsDate(properties.get(32), "date", LocalDate.parse("2022-01-25", DateTimeFormatter.ISO_LOCAL_DATE));
        assertIsDateTime(properties.get(33), "datetime", LocalDateTime.parse("2022-01-25T07:43:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        assertIsTime(properties.get(34), "time", LocalTime.parse("11:34:00", DateTimeFormatter.ISO_LOCAL_TIME));
        assertIsZonedDateTime(properties.get(35), "zoneddatetime", OffsetDateTime.parse("2022-01-25T12:23:00+02:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        assertIsDateRange(properties.get(36), "daterange", LocalDate.parse("2022-01-25", DateTimeFormatter.ISO_LOCAL_DATE), LocalDate.parse("2022-02-16", DateTimeFormatter.ISO_LOCAL_DATE));
        assertIsDateTimeRange(properties.get(37), "datetimerange", LocalDateTime.parse("2022-01-25T07:43:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME), LocalDateTime.parse("2022-03-16T16:32:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        assertIsTimeRange(properties.get(38), "timerange", LocalTime.parse("11:34:00", DateTimeFormatter.ISO_LOCAL_TIME), LocalTime.parse("16:13:00", DateTimeFormatter.ISO_LOCAL_TIME));
        assertIsDivider(properties.get(39));
        assertIsHeader(properties.get(40), "strings");
        assertIsStrAscii(properties.get(41), "ascii", "Test ASCII String");
        assertIsStrUtf8(properties.get(42), "utf8", "Test UTF-8 String ₠ ℃");
        assertIsStrMail(properties.get(43), "mail", "max.mustermann@test.de");
        assertIsStrPhone(properties.get(44), "phone", "0123456");
        assertIsStrUri(properties.get(45), "uri", "www.test.de");
        assertIsStrPwd(properties.get(46), "pwd", "test pwd");
        assertIsDivider(properties.get(47));
        assertIsHeader(properties.get(48), "other");
        assertIsOneOutOfM(properties.get(49), "oneofm", 1, "always", "sometimes", "never", "empty");
        assertIsNOutOfM(properties.get(50), "nofm", new int[]{1, 3}, "milk", "sugar", "eggs", "flour", "chocolate");
        assertIsBool(properties.get(51), "bool_single", true);
        assertIsArray(properties.get(52), "array", (props) -> {
            assertIsStrAscii(props.get(0), "profname", "");
            assertIsBool(props.get(1), "profactive", false);
            assertIsUInt8(props.get(2), "profval", BigInteger.valueOf(80), BigInteger.valueOf(20), BigInteger.valueOf(240), BigInteger.valueOf(20), Type.NumberContentType.NONE);
            return null;
        });
        assertIsLanguage(properties.get(53), "langpref", -1, "en_e", "de_e");
        assertIsDivider(properties.get(54));
        assertIsHeader(properties.get(55), "dependencies");
        assertIsBool(properties.get(56), "bool_dependency", true);
        assertBoolDependencies(properties.get(56), true, "uint8_plain", "int8_plain", "number_list");
        assertBoolDependencies(properties.get(56), false);
        assertIsOneOutOfM(properties.get(57), "oneofm_dependency", 3, "other_e", "dates_e", "integers_content_e", "all_e", "none_e");
        assertOneOutOfMDependencies(properties.get(57), 0, "oneofm", "nofm", "bool_single", "array");
        assertOneOutOfMDependencies(properties.get(57), 1, "date", "datetime", "zoneddatetime", "daterange", "datetimerange", "time", "timerange");
        assertOneOutOfMDependencies(properties.get(57), 2, "int32_ct");
        assertOneOutOfMDependencies(properties.get(57), 3, "oneofm", "nofm", "bool_single", "array", "date", "datetime", "zoneddatetime", "daterange", "datetimerange", "time", "timerange", "int32_ct");
        assertOneOutOfMDependencies(properties.get(57), 4);
        assertIsNOutOfM(properties.get(58), "nofm_dependency", new int[]{2}, "strings_ascii", "strings_utf8", "strings_pwd", "strings_other");
        assertNOutOfMDependencies(properties.get(58), 0, "ascii");
        assertNOutOfMDependencies(properties.get(58), 1, "utf8");
        assertNOutOfMDependencies(properties.get(58), 2, "pwd");
        assertNOutOfMDependencies(properties.get(58), 3, "mail", "phone", "uri");
    }

    private static final byte[] DEV_IDS = {
            (byte) 0xFE,
            (byte) 0xFE,
            (byte) 0xFE,
            (byte) 0x01,
            (byte) 0x01,
            (byte) 0x01,
            (byte) 0xFE,
            (byte) 0xFE
    };
}