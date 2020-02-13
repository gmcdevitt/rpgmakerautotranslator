package translation;

import org.apache.commons.lang3.tuple.ImmutablePair;

public class Translation {

    private final ImmutablePair<String, String> translation;

    public Translation(String original, String translation) {
        this.translation = new ImmutablePair<>(original, translation);
    }

    public String getOriginal() {
        return translation.getLeft();
    }

    public String getTranslation() {
        return translation.getRight();
    }

    public String toString() {
        return translation.toString();
    }
}
