package parsers.impl;

import com.google.inject.Inject;
import org.apache.commons.lang3.*;
import parsers.PatchFileParser;
import translation.Translation;
import translation.TranslationAggregator;
import translation.translators.*;
import writers.PatchFileWriter;
import writers.PatchFileWriterFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.*;

public class PatchFileV3Parser implements PatchFileParser {

    private static final String header = "> RPGMAKER TRANS PATCH FILE VERSION 3.2";
    private static final String beginMarker = "> BEGIN STRING";
    private static final String contextMarker = "> CONTEXT: ";
    private static final String endMarker = "> END STRING";

    private boolean encounteredHeader = false;
    private boolean encounteredBegin = false;

    private final PatchFileWriterFactory writerFactory;

    @Inject
    protected PatchFileV3Parser(PatchFileWriterFactory writerFactory) {
        this.writerFactory = writerFactory;
    }

    @Override
    public List<Translation> parse(Path input, Path output, Translator translator) {
        try (PatchFileWriter writer = this.writerFactory.create(output)) {
            Stream<String> lines = Files.lines(input);
            TranslationAggregator aggregator = new TranslationAggregator();
            lines.forEachOrdered(line -> {
                if (line.equals(header)) {
                    // Basically, validate the file
                    encounteredHeader = true;
                    writer.write(line);
                    return;
                }
                if (encounteredHeader && line.equals(beginMarker)) {
                    // We've found a string
                    encounteredBegin = true;
                    writer.write(line);
                    return;
                }
                if (encounteredBegin && StringUtils.isNotBlank(line) && !line.startsWith(contextMarker)) {
                    // Here we go (add lines until we find the end marker)
                    aggregator.appendCurrent(line);
                    writer.write(line);
                    return;
                }
                if (line.startsWith(contextMarker)) {
                    // We are done loading that translation and can begin searching for the end marker
                    writer.write(line);
                    return;
                }
                if (StringUtils.isBlank(line)) {
                    // Here is where we will insert our translated text
                    String text = aggregator.finishCurrent();
                    Translation translation = new Translation(text, translator.translate(text));
                    aggregator.add(translation);
                    writer.writeMultiLine(translation.getTranslation());
                    return;
                }
                if (line.equals(endMarker)) {
                    // We are totally done with this translation (Hooray!)
                    encounteredBegin = false;
                }
            });
            return aggregator.getTranslations();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
