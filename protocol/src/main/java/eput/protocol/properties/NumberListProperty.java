package eput.protocol.properties;

import java.nio.ByteBuffer;
import java.util.List;

import eput.protocol.NamedItem;

public abstract class NumberListProperty extends NamedItem {
    protected int selectedIndex;

    public NumberListProperty(ByteBuffer meta) {
        super(meta);
    }

    public void setSelectedIndex(int index) {
        if (-1 < index && index < getEntryCount()) {
            selectedIndex = index;
        } else {
            throw new IllegalArgumentException("Index out of range");
        }
    }
    public int getSelectedIndex() {
        return selectedIndex;
    }

    public abstract int getEntryCount();

    public abstract List<String> getStringEntries();

    public abstract String getSelectedStringValue();
}
