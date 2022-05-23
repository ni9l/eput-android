package eput.protocol.properties;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import eput.protocol.Type;
import eput.protocol.Util;

public class DoubleNumberListProperty extends NumberListProperty {
    private final List<Double> entries;

    public DoubleNumberListProperty(ByteBuffer meta, ByteBuffer data) {
        super(meta);
        byte[] entryCountArr = new byte[2];
        meta.get(entryCountArr);
        BigInteger entryCount = new BigInteger(1, entryCountArr);
        entries = new ArrayList<>(entryCount.intValue());
        for (int i = 0; i < entryCount.intValue(); i++) {
            entries.add(meta.getDouble());
        }
        selectedIndex = entries.indexOf(data.getDouble());
    }

    @Override
    public int getType() {
        return Type.NUMBER_LIST_DOUBLE;
    }

    @Override
    public List<Byte> serializeData() {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putDouble(entries.get(selectedIndex));
        return Util.bufferToList(buffer);
    }

    @Override
    public int getEntryCount() {
        return entries.size();
    }

    @Override
    public List<String> getStringEntries() {
        return entries.stream().map((it) -> Double.toString(it)).collect(Collectors.toList());
    }

    @Override
    public String getSelectedStringValue() {
        return Double.toString(entries.get(selectedIndex));
    }

    public List<Double> getEntries() {
        return entries;
    }

    public double getSelectedValue() {
        return entries.get(selectedIndex);
    }
}
