package parsers;

import translation.Translation;
import translation.translators.Translator;

import java.nio.file.Path;
import java.util.List;

public interface PatchFileParser {
    List<Translation> parse(Path input, Path output, Translator translator);
}
