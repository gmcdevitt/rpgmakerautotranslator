package parsers.impl;

import com.google.inject.Inject;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.*;
import parsers.PatchFileParser;
import translation.Translation;
import translation.TranslationAggregator;
import translation.translators.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.*;

public class PatchFileV3Translator implements PatchFileParser {

    private static final String header = "> RPGMAKER TRANS PATCH FILE VERSION 3.2";
    private static final String beginMarker = "> BEGIN STRING";
    private static final String contextMarker = "> CONTEXT: ";
    private static final String endMarker = "> END STRING";

    private boolean encounteredHeader = false;
    private boolean encounteredBegin = false;

    private final Translator translator;

    @Inject
    protected PatchFileV3Translator(Translator translator){
        this.translator = translator;
    }

    @Override
    public List<Translation> parse(Reader reader, Writer writer) {
        TranslationAggregator aggregator = new TranslationAggregator();
        String line = null;
        try (BufferedReader bufferedReader = IOUtils.buffer(reader);
                PrintWriter printWriter = new PrintWriter(writer)) {
            while ((line = bufferedReader.readLine()) != null) {
                if (line.equals(header)) {
                    // Basically, validate the file
                    encounteredHeader = true;
                    printWriter.println(line);
                } else if (encounteredHeader && line.equals(beginMarker)) {
                    // We've found a string
                    encounteredBegin = true;
                    printWriter.println(line);
                } else if (encounteredBegin && StringUtils.isNotBlank(line) && !line.startsWith(contextMarker)) {
                    // Here we go (add lines until we find the end marker)
                    aggregator.appendCurrent(line);
                    printWriter.println(line);
                } else if (line.startsWith(contextMarker)) {
                    // We are done loading that translation and can begin searching for the end marker
                    printWriter.println(line);
                } else if (StringUtils.isBlank(line)) {
                    // Here is where we will insert our translated text
                    String text = aggregator.finishCurrent();
                    Translation translation = new Translation(text, translator.translate(text));
                    aggregator.add(translation);
                    printWriter.println(translation.getTranslation());
                } else if (line.equals(endMarker)) {
                    // We are totally done with this translation (Hooray!)
                    encounteredBegin = false;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(String.format("Error while parsing line '%s'", line), e);
        }
        return aggregator.getTranslations();
    }
}
