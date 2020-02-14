package parsers;

import translation.Translation;

import java.io.Reader;
import java.io.Writer;
import java.util.List;

public interface PatchFileParser {
    List<Translation> parse(Reader input, Writer output);
}
