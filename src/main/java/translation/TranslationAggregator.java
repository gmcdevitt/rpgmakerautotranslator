package translation;

import java.util.ArrayList;
import java.util.List;

public class TranslationAggregator {
    private final List<Translation> translations;
    private StringBuilder builder = new StringBuilder();

    public TranslationAggregator() {
        this.translations = new ArrayList<>();
    }

    public void appendCurrent(String line) {
        builder.append(line);
    }

    public String finishCurrent() {
        String translation = builder.toString();
        builder = new StringBuilder();
        return translation;
    }

    public void add(Translation translation) {
        translations.add(translation);
    }

    public List<Translation> getTranslations() {
        return translations;
    }

}
