package eput.protocol.properties;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import eput.protocol.BaseItem;
import eput.protocol.DependencyProperty;
import eput.protocol.NamedItem;
import eput.protocol.SubEntryProperty;
import eput.protocol.Type;
import eput.protocol.Util;

public class ArrayProperty extends NamedItem implements SubEntryProperty, DependencyProperty {
    private final int profileCount;
    private final List<List<BaseItem>> profiles;

    public ArrayProperty(ByteBuffer meta, ByteBuffer data) {
        super(meta);
        profileCount = meta.get() & 0xFF;
        profiles = new ArrayList<>(profileCount);
        int entryCount = meta.get() & 0xFF;
        int start = meta.position();
        for (int i = 0; i < profileCount; i++) {
            meta.position(start);
            List<BaseItem> entries = new ArrayList<>(entryCount);
            for (int j = 0; j < entryCount; j++) {
                BaseItem prop = Util.deserializeProperty(meta, data);
                if (prop != null) {
                    entries.add(prop);
                } else {
                    throw new IllegalArgumentException("End marker before end of array property");
                }
            }
            profiles.add(entries);
        }
    }

    @Override
    public int getType() {
        return Type.ARRAY;
    }

    @Override
    public List<Byte> serializeData() {
        ArrayList<Byte> lis = new ArrayList<>();
        for (List<BaseItem> profile : profiles) {
            for (BaseItem item : profile) {
                if (item instanceof NamedItem) {
                    lis.addAll(((NamedItem) item).serializeData());
                }
            }
        }
        return lis;
    }

    @Override
    public void addSubEntryTranslations(String language, ByteBuffer meta) {
        int pos = meta.position();
        for (List<BaseItem> profile : profiles) {
            meta.position(pos);
            for (BaseItem item : profile) {
                Util.addPropertyTranslation(item, language, meta);
            }
        }
    }

    public int getProfileCount() {
        return profileCount;
    }

    public List<BaseItem> getProfile(int index) {
        return profiles.get(index);
    }

    @Override
    public List<String> getSubEntryIds() {
        if (profileCount > 0) {
            return Util.getIds(profiles.get(0));
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public void resolveDependencies(List<String> ids) {
        for (List<BaseItem> profile : profiles) {
            for (BaseItem prop : profile) {
                if (prop instanceof DependencyProperty) {
                    ((DependencyProperty) prop).resolveDependencies(ids);
                }
            }
        }
    }
}
