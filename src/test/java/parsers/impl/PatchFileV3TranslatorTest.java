package parsers.impl;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Module;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import translation.Translation;
import translation.translators.Translator;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class PatchFileV3TranslatorTest {

    @Inject
    private Translator translator;

    @Before
    public void setUp() {
        this.translator = mock(Translator.class);
        when(translator.translate(anyString())).then(i -> i.getArgument(0, String.class));
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
        Assert.assertEquals(1, translations.size());
    }

    @Test
    public void canWriteSingleTranslation() throws IOException {
        String expected =
            "> RPGMAKER TRANS PATCH FILE VERSION 3.2\r\n" +
            "> BEGIN STRING\r\n" +
            "ホーイ市・駐屯地\r\n" +
            "> CONTEXT: Map101/display_name/ < UNTRANSLATED\r\n" +
            "ホーイ市・駐屯地\r\n" +
            "> END STRING\r\n" +
            "";
        // Given
        File patchFile = new File("src/test/resources/patchfiles/single.txt");
        StringWriter writer = new StringWriter();

        // When
        PatchFileV3Translator parser = new PatchFileV3Translator(translator);

        // Then
        parser.parse(new FileReader(patchFile), writer);
        Assert.assertEquals(expected, writer.toString());
    }
}
