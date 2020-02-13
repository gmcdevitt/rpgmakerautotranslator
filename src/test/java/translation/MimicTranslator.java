package translation;

import translation.translators.Translator;

public class MimicTranslator implements Translator {

    @Override
    public String translate(String text) {
        return text;
    }
}
