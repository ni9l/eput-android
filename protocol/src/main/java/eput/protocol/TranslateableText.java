package eput.protocol;

import java.util.HashMap;
import java.util.Map;

public class TranslateableText {
    private final String id;
    private final Map<String, String> translations;

    public TranslateableText(String id) {
        this.id = id;
        this.translations = new HashMap<>();
    }

    public String getId() {
        return id;
    }

    public String getTranslated(String language) {
        if (language != null) {
            String text = translations.get(language);
            if (text != null) {
                return text;
            }
        }
        return id;
    }

    public void addTranslation(String language, String translation) {
        translations.put(language, translation);
    }

    @Override
    public String toString() {
        return "TranslateableText{ id='" + id + "' }";
    }
}
