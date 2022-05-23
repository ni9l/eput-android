package eput.protocol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

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
import eput.protocol.properties.FloatProperty;
import eput.protocol.properties.Int16Property;
import eput.protocol.properties.Int32Property;
import eput.protocol.properties.Int64Property;
import eput.protocol.properties.Int8Property;
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
import eput.protocol.properties.ZonedDateTimeProperty;

public final class TestUtil {
    public static void assertIsDivider(BaseItem prop) {
        assertEquals(Type.DIVIDER, prop.getType());
        assertTrue(prop instanceof Divider);
    }

    public static void assertIsHeader(BaseItem prop, String id) {
        assertEquals(Type.HEADER, prop.getType());
        assertTrue(prop instanceof Header);
        Header h = (Header) prop;
        assertEquals(id, h.id.getId());
    }

    public static void assertIsUInt8(BaseItem prop, String id, BigInteger value) {
        assertIsUInt8(prop, id, value, UInt8Property.DEF_MIN, UInt8Property.DEF_MAX, UInt8Property.DEF_STP, Type.NumberContentType.NONE);
    }

    public static void assertIsUInt8(BaseItem prop, String id, BigInteger value, BigInteger min, BigInteger max, BigInteger step, int type) {
        assertEquals(Type.UINT8_T, prop.getType());
        assertTrue(prop instanceof UInt8Property);
        UInt8Property u8 = (UInt8Property) prop;
        assertEquals(id, u8.id.getId());
        assertEquals(min, u8.getMinValue());
        assertEquals(max, u8.getMaxValue());
        assertEquals(step, u8.getStepSize());
        assertEquals(type, u8.getContentType());
        assertEquals(value, u8.getValue());
    }

    public static void assertIsUInt16(BaseItem prop, String id, BigInteger value) {
        assertEquals(Type.UINT16_T, prop.getType());
        assertTrue(prop instanceof UInt16Property);
        UInt16Property u = (UInt16Property) prop;
        assertEquals(id, u.id.getId());
        assertEquals(UInt16Property.DEF_MIN, u.getMinValue());
        assertEquals(UInt16Property.DEF_MAX, u.getMaxValue());
        assertEquals(UInt16Property.DEF_STP, u.getStepSize());
        assertEquals(Type.NumberContentType.NONE, u.getContentType());
        assertEquals(value, u.getValue());
    }

    public static void assertIsUInt32(BaseItem prop, String id, BigInteger value) {
        assertIsUInt32(prop, id, value, UInt32Property.DEF_MIN, UInt32Property.DEF_MAX, UInt32Property.DEF_STP, Type.NumberContentType.NONE);
    }

    public static void assertIsUInt32(BaseItem prop, String id, BigInteger value, BigInteger min, BigInteger max, BigInteger step, int type) {
        assertEquals(Type.UINT32_T, prop.getType());
        assertTrue(prop instanceof UInt32Property);
        UInt32Property u = (UInt32Property) prop;
        assertEquals(id, u.id.getId());
        assertEquals(min, u.getMinValue());
        assertEquals(max, u.getMaxValue());
        assertEquals(step, u.getStepSize());
        assertEquals(type, u.getContentType());
        assertEquals(value, u.getValue());
    }

    public static void assertIsUInt64(BaseItem prop, String id, BigInteger value) {
        assertEquals(Type.UINT64_T, prop.getType());
        assertTrue(prop instanceof UInt64Property);
        UInt64Property u = (UInt64Property) prop;
        assertEquals(id, u.id.getId());
        assertEquals(UInt64Property.DEF_MIN, u.getMinValue());
        assertEquals(UInt64Property.DEF_MAX, u.getMaxValue());
        assertEquals(UInt64Property.DEF_STP, u.getStepSize());
        assertEquals(Type.NumberContentType.NONE, u.getContentType());
        assertEquals(value, u.getValue());
    }

    public static void assertIsInt8(BaseItem prop, String id, BigInteger value) {
        assertEquals(Type.INT8_T, prop.getType());
        assertTrue(prop instanceof Int8Property);
        Int8Property u = (Int8Property) prop;
        assertEquals(id, u.id.getId());
        assertEquals(Int8Property.DEF_MIN, u.getMinValue());
        assertEquals(Int8Property.DEF_MAX, u.getMaxValue());
        assertEquals(Int8Property.DEF_STP, u.getStepSize());
        assertEquals(Type.NumberContentType.NONE, u.getContentType());
        assertEquals(value, u.getValue());
    }

    public static void assertIsInt16(BaseItem prop, String id, BigInteger value) {
        assertEquals(Type.INT16_T, prop.getType());
        assertTrue(prop instanceof Int16Property);
        Int16Property u = (Int16Property) prop;
        assertEquals(id, u.id.getId());
        assertEquals(Int16Property.DEF_MIN, u.getMinValue());
        assertEquals(Int16Property.DEF_MAX, u.getMaxValue());
        assertEquals(Int16Property.DEF_STP, u.getStepSize());
        assertEquals(Type.NumberContentType.NONE, u.getContentType());
        assertEquals(value, u.getValue());
    }

    public static void assertIsInt32(BaseItem prop, String id, BigInteger value) {
        assertIsInt32(prop, id, value, Int32Property.DEF_MIN, Int32Property.DEF_MAX, Int32Property.DEF_STP, Type.NumberContentType.NONE);
    }

    public static void assertIsInt32(BaseItem prop, String id, BigInteger value, BigInteger min, BigInteger max, BigInteger step, int type) {
        assertEquals(Type.INT32_T, prop.getType());
        assertTrue(prop instanceof Int32Property);
        Int32Property u = (Int32Property) prop;
        assertEquals(id, u.id.getId());
        assertEquals(min, u.getMinValue());
        assertEquals(max, u.getMaxValue());
        assertEquals(step, u.getStepSize());
        assertEquals(type, u.getContentType());
        assertEquals(value, u.getValue());
    }

    public static void assertIsInt64(BaseItem prop, String id, BigInteger value) {
        assertEquals(Type.INT64_T, prop.getType());
        assertTrue(prop instanceof Int64Property);
        Int64Property u = (Int64Property) prop;
        assertEquals(id, u.id.getId());
        assertEquals(Int64Property.DEF_MIN, u.getMinValue());
        assertEquals(Int64Property.DEF_MAX, u.getMaxValue());
        assertEquals(Int64Property.DEF_STP, u.getStepSize());
        assertEquals(Type.NumberContentType.NONE, u.getContentType());
        assertEquals(value, u.getValue());
    }

    public static void assertIsFloat(BaseItem prop, String id, float value) {
        assertIsFloat(prop, id, value, FloatProperty.DEF_MIN, FloatProperty.DEF_MAX, Type.NumberContentType.NONE);
    }

    public static void assertIsFloat(BaseItem prop, String id, float value, float min, float max, int type) {
        assertEquals(Type.FLOAT, prop.getType());
        assertTrue(prop instanceof FloatProperty);
        FloatProperty u = (FloatProperty) prop;
        assertEquals(id, u.id.getId());
        assertEquals(min, u.getMinValue(), 0.000001);
        assertEquals(max, u.getMaxValue(), 0.000001);
        assertEquals(type, u.getContentType());
        assertEquals(value, u.getValue(), 0.000001);
    }

    public static void assertIsDouble(BaseItem prop, String id, double value) {
        assertEquals(Type.DOUBLE, prop.getType());
        assertTrue(prop instanceof DoubleProperty);
        DoubleProperty u = (DoubleProperty) prop;
        assertEquals(id, u.id.getId());
        assertEquals(DoubleProperty.DEF_MIN, u.getMinValue(), 0.000001);
        assertEquals(DoubleProperty.DEF_MAX, u.getMaxValue(), 0.000001);
        assertEquals(Type.NumberContentType.NONE, u.getContentType());
        assertEquals(value, u.getValue(), 0.000001);
    }

    public static void assertIsNumberListDouble(BaseItem prop, String id, double value, Double... entries) {
        assertEquals(Type.NUMBER_LIST_DOUBLE, prop.getType());
        assertTrue(prop instanceof DoubleNumberListProperty);
        DoubleNumberListProperty u = (DoubleNumberListProperty) prop;
        assertEquals(id, u.id.getId());
        assertEquals(value, u.getSelectedValue(), 0.000001);
        assertEquals(entries.length, u.getEntries().size());
        for (int i = 0; i < entries.length; i++) {
            assertEquals(entries[i], u.getEntries().get(i));
        }
    }

    public static void assertIsDate(BaseItem prop, String id, LocalDate value) {
        assertEquals(Type.DATE, prop.getType());
        assertTrue(prop instanceof DateProperty);
        DateProperty u = (DateProperty) prop;
        assertEquals(id, u.id.getId());
        assertEquals(value, u.getValue());
    }

    public static void assertIsDateTime(BaseItem prop, String id, LocalDateTime value) {
        assertEquals(Type.DATE_TIME, prop.getType());
        assertTrue(prop instanceof DateTimeProperty);
        DateTimeProperty u = (DateTimeProperty) prop;
        assertEquals(id, u.id.getId());
        assertEquals(value, u.getValue());
    }

    public static void assertIsTime(BaseItem prop, String id, LocalTime value) {
        assertEquals(Type.TIME, prop.getType());
        assertTrue(prop instanceof TimeProperty);
        TimeProperty u = (TimeProperty) prop;
        assertEquals(id, u.id.getId());
        assertEquals(value, u.getValue());
    }

    public static void assertIsZonedDateTime(BaseItem prop, String id, OffsetDateTime value) {
        assertEquals(Type.ZONED_DATE_TIME, prop.getType());
        assertTrue(prop instanceof ZonedDateTimeProperty);
        ZonedDateTimeProperty u = (ZonedDateTimeProperty) prop;
        assertEquals(id, u.id.getId());
        assertEquals(value, u.getValue());
    }

    public static void assertIsDateRange(BaseItem prop, String id, LocalDate valueFrom, LocalDate valueTo) {
        assertEquals(Type.DATE_RANGE, prop.getType());
        assertTrue(prop instanceof DateRangeProperty);
        DateRangeProperty u = (DateRangeProperty) prop;
        assertEquals(id, u.id.getId());
        assertEquals(valueFrom, u.getValueFrom());
        assertEquals(valueTo, u.getValueTo());
    }

    public static void assertIsDateTimeRange(BaseItem prop, String id, LocalDateTime valueFrom, LocalDateTime valueTo) {
        assertEquals(Type.DATE_TIME_RANGE, prop.getType());
        assertTrue(prop instanceof DateTimeRangeProperty);
        DateTimeRangeProperty u = (DateTimeRangeProperty) prop;
        assertEquals(id, u.id.getId());
        assertEquals(valueFrom, u.getValueFrom());
        assertEquals(valueTo, u.getValueTo());
    }

    public static void assertIsTimeRange(BaseItem prop, String id, LocalTime valueFrom, LocalTime valueTo) {
        assertEquals(Type.TIME_RANGE, prop.getType());
        assertTrue(prop instanceof TimeRangeProperty);
        TimeRangeProperty u = (TimeRangeProperty) prop;
        assertEquals(id, u.id.getId());
        assertEquals(valueFrom, u.getValueFrom());
        assertEquals(valueTo, u.getValueTo());
    }

    public static void assertIsStrAscii(BaseItem prop, String id, String value) {
        assertEquals(Type.STR_ASCII, prop.getType());
        assertTrue(prop instanceof StrAsciiProperty);
        StrAsciiProperty u = (StrAsciiProperty) prop;
        assertEquals(id, u.id.getId());
        assertEquals(value, u.getValue());
    }

    public static void assertIsStrUtf8(BaseItem prop, String id, String value) {
        assertEquals(Type.STR_UTF8, prop.getType());
        assertTrue(prop instanceof StrUtf8Property);
        StrUtf8Property u = (StrUtf8Property) prop;
        assertEquals(id, u.id.getId());
        assertEquals(value, u.getValue());
    }

    public static void assertIsStrMail(BaseItem prop, String id, String value) {
        assertEquals(Type.STR_EMAIL, prop.getType());
        assertTrue(prop instanceof StrEmailProperty);
        StrEmailProperty u = (StrEmailProperty) prop;
        assertEquals(id, u.id.getId());
        assertEquals(value, u.getValue());
    }

    public static void assertIsStrPhone(BaseItem prop, String id, String value) {
        assertEquals(Type.STR_PHONE, prop.getType());
        assertTrue(prop instanceof StrPhoneProperty);
        StrPhoneProperty u = (StrPhoneProperty) prop;
        assertEquals(id, u.id.getId());
        assertEquals(value, u.getValue());
    }

    public static void assertIsStrUri(BaseItem prop, String id, String value) {
        assertEquals(Type.STR_URI, prop.getType());
        assertTrue(prop instanceof StrUriProperty);
        StrUriProperty u = (StrUriProperty) prop;
        assertEquals(id, u.id.getId());
        assertEquals(value, u.getValue());
    }

    public static void assertIsStrPwd(BaseItem prop, String id, String value) {
        assertEquals(Type.STR_PASSWORD, prop.getType());
        assertTrue(prop instanceof StrPasswordProperty);
        StrPasswordProperty u = (StrPasswordProperty) prop;
        assertEquals(id, u.id.getId());
        assertEquals(value, u.getValue());
    }

    public static void assertIsOneOutOfM(BaseItem prop, String id, int value, String... entries) {
        assertEquals(Type.ONE_OUT_OF_M, prop.getType());
        assertTrue(prop instanceof OneOutOfMProperty);
        OneOutOfMProperty u = (OneOutOfMProperty) prop;
        assertEquals(id, u.id.getId());
        assertEquals(value, u.getSelectedIndex());
        assertEquals(entries.length, u.getEntries().size());
        for (int i = 0; i < entries.length; i++) {
            assertEquals(entries[i], u.getEntries().get(i).getId());
        }
    }

    public static void assertIsNOutOfM(BaseItem prop, String id, int[] selected, String... entries) {
        assertEquals(Type.N_OUT_OF_M, prop.getType());
        assertTrue(prop instanceof NOutOfMProperty);
        NOutOfMProperty u = (NOutOfMProperty) prop;
        assertEquals(id, u.id.getId());
        for (int i : selected) {
            assertTrue(u.isSelected(i));
        }
        assertEquals(entries.length, u.getEntries().size());
        for (int i = 0; i < entries.length; i++) {
            assertEquals(entries[i], u.getEntries().get(i).getId());
        }
    }

    public static void assertIsBool(BaseItem prop, String id, boolean value) {
        assertEquals(Type.BOOL, prop.getType());
        assertTrue(prop instanceof BoolProperty);
        BoolProperty u = (BoolProperty) prop;
        assertEquals(id, u.id.getId());
        assertEquals(value, u.getValue());
    }

    public static void assertIsArray(BaseItem prop, String id, Function<List<BaseItem>, Void> contentAssertion) {
        assertEquals(Type.ARRAY, prop.getType());
        assertTrue(prop instanceof ArrayProperty);
        ArrayProperty u = (ArrayProperty) prop;
        assertEquals(id, u.id.getId());
        for (int i = 0; i < u.getProfileCount(); i++) {
            contentAssertion.apply(u.getProfile(i));
        }
    }

    public static void assertIsLanguage(BaseItem prop, String id, int value, String... entries) {
        assertEquals(Type.LANGUAGE, prop.getType());
        assertTrue(prop instanceof LanguageProperty);
        LanguageProperty u = (LanguageProperty) prop;
        assertEquals(id, u.id.getId());
        assertEquals(value, u.getSelectedIndex());
        assertEquals(entries.length, u.getEntries().size());
        for (int i = 0; i < entries.length; i++) {
            assertEquals(entries[i], u.getEntries().get(i).getId());
        }
    }

    public static void assertBoolDependencies(BaseItem prop, boolean state, String... entries) {
        assertTrue(prop instanceof BoolProperty);
        BoolProperty u = (BoolProperty) prop;
        List<String> dependencies = u.getDependencyIds(state);
        assertEquals(entries.length, dependencies.size());
        for (int i = 0; i < entries.length; i++) {
            assertEquals(entries[i], dependencies.get(i));
        }
    }

    public static void assertOneOutOfMDependencies(BaseItem prop, int state, String... entries) {
        assertTrue(prop instanceof OneOutOfMProperty);
        OneOutOfMProperty u = (OneOutOfMProperty) prop;
        Map<Integer, List<String>> depMap = u.getDependencyIdMap();
        assertTrue(depMap.containsKey(state));
        assertEquals(entries.length, depMap.get(state).size());
        for (int i = 0; i < entries.length; i++) {
            assertEquals(entries[i], depMap.get(state).get(i));
        }
    }

    public static void assertNOutOfMDependencies(BaseItem prop, int state, String... entries) {
        assertTrue(prop instanceof NOutOfMProperty);
        NOutOfMProperty u = (NOutOfMProperty) prop;
        Map<Integer, List<String>> depMap = u.getDependencyIdMap();
        assertTrue(depMap.containsKey(state));
        assertEquals(entries.length, depMap.get(state).size());
        for (int i = 0; i < entries.length; i++) {
            assertEquals(entries[i], depMap.get(state).get(i));
        }
    }

    public static byte[] getResource(String name) throws IOException, URISyntaxException {
        URL res = ClassLoader.getSystemClassLoader().getResource(name);
        if (res != null) {
            return Files.readAllBytes(Paths.get(res.toURI()));
        } else {
            return new byte[0];
        }
    }
}
