package eput.protocol;

import java.nio.ByteBuffer;
import java.util.List;

public abstract class NamedItem implements BaseItem {
    protected final TranslateableText id;

    protected NamedItem(ByteBuffer meta) {
        this.id = new TranslateableText(Util.getAsciiString(meta));
    }

    public String getId() {
        return id.getId();
    }

    public String getDisplayName(String language) {
        return id.getTranslated(language);
    }

    public void addDisplayName(String language, String name) {
        id.addTranslation(language, name);
    }

    public abstract List<Byte> serializeData();
}
