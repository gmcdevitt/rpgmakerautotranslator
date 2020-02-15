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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatchFileV3Translator implements PatchFileParser {

    private static final String header = "> RPGMAKER TRANS PATCH FILE VERSION 3.2";
    private static final String beginMarker = "> BEGIN STRING";
    private static final String contextMarker = "> CONTEXT: ";
    private static final String endMarker = "> END STRING";

    private boolean encounteredHeader = false;
    private boolean encounteredBegin = false;

    private final Translator translator;

    private static final String LINE_FEED = "\n";
    private static final int MAX_LINE_LENGTH = 80;

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
                    printWriter.print(line + LINE_FEED);
                } else if (encounteredHeader && line.equals(beginMarker)) {
                    // We've found a string
                    encounteredBegin = true;
                    printWriter.print(line + LINE_FEED);
                } else if (encounteredBegin && StringUtils.isNotBlank(line) && !line.startsWith(contextMarker)) {
                    // Here we go (add lines until we find the end marker)
                    aggregator.appendCurrent(line);
                    printWriter.print(line + LINE_FEED);
                } else if (line.startsWith(contextMarker)) {
                    // We are done loading that translation and can begin searching for the end marker
                    printWriter.print(line + LINE_FEED);
                } else if (StringUtils.isBlank(line)) {
                    // Here is where we will insert our translated text
                    String text = aggregator.finishCurrent();
                    Translation translation = new Translation(text, translator.translate(text));
                    aggregator.add(translation);
                    List<String> pieces = cutString(translation.getTranslation(), MAX_LINE_LENGTH);
                    if (pieces != null) {
                        for (String piece : pieces) {
                            printWriter.print(piece + LINE_FEED);
                        }
                    }
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

    protected List<String> cutString(String text, int lineLengthMax) {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        List<String> res = new ArrayList<>();

        Pattern p = Pattern.compile(String.format("(\\b.{1,%s})\\b\\W?", lineLengthMax - 1));
        Matcher m = p.matcher(text);

        while(m.find()) {
            res.add(StringUtils.trimToEmpty(m.group()));
        }
        return res;
    }
}
