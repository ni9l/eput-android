package eput.protocol;

import java.nio.ByteBuffer;
import java.util.List;

public interface SubEntryProperty {
    void addSubEntryTranslations(String language, ByteBuffer meta);

    List<String> getSubEntryIds();
}
