package eput.protocol.properties;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import eput.protocol.DependencyProperty;
import eput.protocol.NamedItem;
import eput.protocol.Type;

public class BoolProperty extends NamedItem implements DependencyProperty {
    private byte binaryValue;
    private final List<Integer> dependencyTrueIndex;
    private final List<String> dependencyTrueIds;
    private final List<Integer> dependencyFalseIndex;
    private final List<String> dependencyFalseIds;

    public BoolProperty(ByteBuffer meta, ByteBuffer data) {
        super(meta);
        int depTrueCount = meta.get() & 0xFF;
        dependencyTrueIndex = new ArrayList<>(depTrueCount);
        dependencyTrueIds = new ArrayList<>(depTrueCount);
        for (int i = 0; i < depTrueCount; i++) {
            int depIndex = 0;
            depIndex = depIndex + meta.get();
            depIndex = (depIndex << 8) + meta.get();
            dependencyTrueIndex.add(depIndex);
        }
        int depFalseCount = meta.get() & 0xFF;
        dependencyFalseIndex = new ArrayList<>(depFalseCount);
        dependencyFalseIds = new ArrayList<>(depFalseCount);
        for (int i = 0; i < depFalseCount; i++) {
            int depIndex = 0;
            depIndex = depIndex + meta.get();
            depIndex = (depIndex << 8) + meta.get();
            dependencyFalseIndex.add(depIndex);
        }
        binaryValue = data.get();
    }

    @Override
    public int getType() {
        return Type.BOOL;
    }

    @Override
    public List<Byte> serializeData() {
        ArrayList<Byte> lis = new ArrayList<>(1);
        lis.add(binaryValue);
        return lis;
    }

    public void setValue(boolean value) {
        if (value) {
            binaryValue = 0x01;
        } else {
            binaryValue = 0x00;
        }
    }

    public boolean getValue() {
        return binaryValue != 0;
    }

    public List<String> getDependencyIds(boolean state) {
        return state ? dependencyTrueIds : dependencyFalseIds;
    }

    public void resolveDependencies(List<String> ids) {
        for (int i : dependencyTrueIndex) {
            dependencyTrueIds.add(ids.get(i));
        }
        for (int i : dependencyFalseIndex) {
            dependencyFalseIds.add(ids.get(i));
        }
    }
}
