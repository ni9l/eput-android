package eput.protocol.modifiers;

import eput.protocol.BaseItem;
import eput.protocol.Type;

public class Divider implements BaseItem {
    @Override
    public int getType() {
        return Type.DIVIDER;
    }
}
