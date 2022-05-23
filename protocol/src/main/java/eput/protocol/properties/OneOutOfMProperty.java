package eput.protocol.properties;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import eput.protocol.DependencyProperty;
import eput.protocol.NamedItem;
import eput.protocol.SubEntryProperty;
import eput.protocol.TranslateableText;
import eput.protocol.Type;
import eput.protocol.Util;

public class OneOutOfMProperty extends NamedItem implements SubEntryProperty, DependencyProperty {
    private final List<TranslateableText> entries;
    private final Map<Integer, List<Integer>> dependencyIndex;
    private final Set<String> dependencyIds;
    private final Map<Integer, List<String>> dependencyMap;
    private int selectedIndex;

    public OneOutOfMProperty(ByteBuffer meta, ByteBuffer data) {
        super(meta);
        int entryCount = meta.get() & 0xFF;
        entries = new ArrayList<>(entryCount);
        for (int i = 0; i < entryCount; i++) {
            String entryId = Util.getAsciiString(meta);
            entries.add(new TranslateableText(entryId));
        }
        dependencyIndex = Util.readDependencyMap(meta);
        dependencyIds = new HashSet<>();
        dependencyMap = new HashMap<>();
        selectedIndex = (data.get() & 0xFF) - 1; // 0 based indexing
    }

    @Override
    public int getType() {
        return Type.ONE_OUT_OF_M;
    }

    @Override
    public List<Byte> serializeData() {
        ArrayList<Byte> lis = new ArrayList<>(1);
        lis.add(Integer.valueOf((selectedIndex + 1) & 0xFF).byteValue());
        return lis;
    }

    public List<TranslateableText> getEntries() {
        return entries;
    }

    public Map<Integer, List<String>> getDependencyIdMap() {
        return dependencyMap;
    }

    public Set<String> getDependencyIds() {
        return dependencyIds;
    }

    public void resolveDependencies(List<String> ids) {
        for (Map.Entry<Integer, List<Integer>> entry : dependencyIndex.entrySet()) {
            List<String> entryIds = entry
                    .getValue()
                    .stream()
                    .map(ids::get)
                    .collect(Collectors.toList());
            dependencyIds.addAll(entryIds);
            dependencyMap.put(entry.getKey(), entryIds);
        }
    }

    public void setSelectedIndex(int index) {
        if (-2 < index && index < entries.size()) {
            selectedIndex = index;
        } else {
            throw new IllegalArgumentException("Index out of range");
        }
    }

    public int getSelectedIndex() {
        return selectedIndex;
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
