package eput.protocol.properties;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import eput.protocol.DependencyProperty;
import eput.protocol.NamedItem;
import eput.protocol.SubEntryProperty;
import eput.protocol.TranslateableText;
import eput.protocol.Type;
import eput.protocol.Util;

public class NOutOfMProperty extends NamedItem implements SubEntryProperty, DependencyProperty {
    private final List<TranslateableText> entries;
    private final Map<Integer, List<Integer>> dependencyIndex;
    private final Map<Integer, List<String>> dependencyMap;
    private final List<Boolean> flags;
    private final int byteCount;

    public NOutOfMProperty(ByteBuffer meta, ByteBuffer data) {
        super(meta);
        int entryCount = meta.get() & 0xFF;
        entries = new ArrayList<>(entryCount);
        for (int i = 0; i < entryCount; i++) {
            String entryId = Util.getAsciiString(meta);
            entries.add(new TranslateableText(entryId));
        }
        dependencyIndex = Util.readDependencyMap(meta);
        dependencyMap = new HashMap<>();
        byteCount = (int) Math.ceil((float) entryCount / 8);
        flags = new ArrayList<>(byteCount * 8);
        for (int i = 0; i < byteCount; i++) {
            byte bitField = data.get();
            for (int j = 0; j < 8; j++) {
                flags.add(((bitField >> j) & 0x01) == 1);
            }
        }
    }

    @Override
    public int getType() {
        return Type.N_OUT_OF_M;
    }

    @Override
    public List<Byte> serializeData() {
        ArrayList<Byte> lis = new ArrayList<>(flags.size());
        for (int i = 0; i < byteCount; i++) {
            byte bitField = 0;
            for (int j = 0; j < 8; j++) {
                if (flags.get((i * 8) + j)) {
                    bitField = Integer.valueOf(bitField | (0x01 << j)).byteValue();
                }
            }

            lis.add(bitField);
        }
        return lis;
    }

    public List<TranslateableText> getEntries() {
        return entries;
    }

    public Map<Integer, List<String>> getDependencyIdMap() {
        return dependencyMap;
    }

    public void resolveDependencies(List<String> ids) {
        for (Map.Entry<Integer, List<Integer>> entry : dependencyIndex.entrySet()) {
            List<String> dependencyIds = entry
                    .getValue()
                    .stream()
                    .map(ids::get)
                    .collect(Collectors.toList());
            dependencyMap.put(entry.getKey(), dependencyIds);
        }
    }

    public void setSelected(int index, boolean selected) {
        if (-1 < index && index < entries.size()) {
            flags.set(index, selected);
        } else {
            throw new IllegalArgumentException("Index out of range");
        }
    }

    public boolean isSelected(int index) {
        if (-1 < index && index < entries.size()) {
            return flags.get(index);
        } else {
            throw new IllegalArgumentException("Index out of range");
        }
    }

    public void addSubEntryTranslations(String language, ByteBuffer meta) {
        for (TranslateableText entry : entries) {
            String translatedId = Util.getUtf8String(meta);
            if (translatedId != null) {
                entry.addTranslation(language, translatedId);
            }
        }
    }

    @Override
    public List<String> getSubEntryIds() {
        return entries.stream().map(TranslateableText::getId).collect(Collectors.toList());
    }
}
