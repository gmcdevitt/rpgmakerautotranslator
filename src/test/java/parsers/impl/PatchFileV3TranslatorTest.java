package parsers.impl;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Module;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import translation.Translation;
import translation.translators.Translator;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class PatchFileV3TranslatorTest {

    private final int LINE_LIMIT = 80;

    @Inject
    private Translator translator;

    @Before
    public void setUp() {
        this.translator = mock(Translator.class);
        Guice.createInjector(getTestModule()).injectMembers(this);
    }

    private Module getTestModule() {
        return new AbstractModule() {
            @Override
            protected void configure() {
                bind(Translator.class).toInstance(translator);
            }
        };
    }

    @Test
    public void canParseSingleTranslation() throws FileNotFoundException {
        // Given
        File patchFile = new File("src/test/resources/patchfiles/single.txt");

        // When
        PatchFileV3Translator parser = new PatchFileV3Translator(translator);

        // Then
        List<Translation> translations = parser.parse(new FileReader(patchFile), new StringWriter());
        assertEquals(1, translations.size());
    }

    @Test
    public void canWriteSingleTranslation() throws IOException {
        String expected =
            "> RPGMAKER TRANS PATCH FILE VERSION 3.2\n" +
            "> BEGIN STRING\n" +
            "ホーイ市・駐屯地\n" +
            "> CONTEXT: Map101/display_name/ < UNTRANSLATED\n" +
            "ホーイ市・駐屯地\n" +
            "> END STRING\n" +
            "";
        // Given
        File patchFile = new File("src/test/resources/patchfiles/single.txt");
        StringWriter writer = new StringWriter();

        // When
        when(translator.translate(anyString())).then(i -> i.getArgument(0, String.class));
        PatchFileV3Translator parser = new PatchFileV3Translator(translator);

        // Then
        parser.parse(new FileReader(patchFile), writer);
        assertEquals(expected, writer.toString());
    }

    @Test
    public void canKeepShortSentenceWhole() {
        String shortSentence = "This, is a short sentence";

        List<String> res = new PatchFileV3Translator(translator).cutString(shortSentence, LINE_LIMIT);

        assertTrue(res.contains(shortSentence));
        assertEquals(1, res.size());
    }

    @Test
    public void canCutLongSentenceIntoPieces() {
        String longSentence =
                "It seemed to Wanda that her daughter had more than enough crayons, they were" +
                " strewn across the bedroom floor and some of them were broken, and, worse still," +
                " someone had used the stub of a red crayon to mark a sinister smiley face on the" +
                " wall.";

        List<String> res =  new PatchFileV3Translator(translator).cutString(longSentence, LINE_LIMIT);

        assertEquals(4, res.size());
        assertTrue(res.contains("It seemed to Wanda that her daughter had more than enough crayons, they were"));
        assertTrue(res.contains("strewn across the bedroom floor and some of them were broken, and, worse still,"));
        assertTrue(res.contains("someone had used the stub of a red crayon to mark a sinister smiley face on the"));
        assertTrue(res.contains("wall."));

        for (String piece : res) {
            assertTrue(piece.length() < LINE_LIMIT);
        }
    }

    @Test
    public void canWriteLongSentencePieces() throws FileNotFoundException {
        File patchFile = new File("src/test/resources/patchfiles/single.txt");
        String longSentence =
            "It seemed to Wanda that her daughter had more than enough crayons, they were" +
            " strewn across the bedroom floor and some of them were broken, and, worse still," +
            " someone had used the stub of a red crayon to mark a sinister smiley face on the" +
            " wall.";
        List<String> pieces = Arrays.asList(
            "It seemed to Wanda that her daughter had more than enough crayons, they were",
            "strewn across the bedroom floor and some of them were broken, and, worse still,",
            "someone had used the stub of a red crayon to mark a sinister smiley face on the",
            "wall."
        );
        String expected =
            "> RPGMAKER TRANS PATCH FILE VERSION 3.2\n" +
            "> BEGIN STRING\n" +
            "ホーイ市・駐屯地\n" +
            "> CONTEXT: Map101/display_name/ < UNTRANSLATED\n" +
            "It seemed to Wanda that her daughter had more than enough crayons, they were\n" +
            "strewn across the bedroom floor and some of them were broken, and, worse still,\n" +
            "someone had used the stub of a red crayon to mark a sinister smiley face on the\n" +
            "wall.\n" +
            "> END STRING\n" +
            "";
        StringWriter writer = new StringWriter();
        when(translator.translate(anyString())).thenReturn(longSentence);
        PatchFileV3Translator parser = Mockito.spy(new PatchFileV3Translator(translator));
        when(parser.cutString(longSentence, LINE_LIMIT)).thenReturn(pieces);
        parser.parse(new FileReader(patchFile), writer);
        assertEquals(expected, writer.toString());
    }
}
