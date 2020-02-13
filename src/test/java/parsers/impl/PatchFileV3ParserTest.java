package parsers.impl;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Module;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import translation.Translation;
import writers.MockFileWriter;
import translation.MimicTranslator;
import writers.PatchFileWriterFactory;
import writers.impl.DefaultWriter;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.any;

@RunWith(MockitoJUnitRunner.class)
public class PatchFileV3ParserTest {

    private MockFileWriter writer = spy(new MockFileWriter());

    @Inject
    private PatchFileWriterFactory writerFactory;

    @Before
    public void setUp() {
        this.writerFactory = mock(PatchFileWriterFactory.class);
        Guice.createInjector(getTestModule()).injectMembers(this);
    }

    private Module getTestModule() {
        return new AbstractModule() {
            @Override
            protected void configure() {
                bind(PatchFileWriterFactory.class).toInstance(writerFactory);
            }
        };
    }

    @Test
    public void canParseSingleTranslation() {
        // Given
        doReturn(writer).when(writerFactory).create(any());
        Path patchFile = Paths.get("src/test/resources/patchfiles/single.txt");

        // When
        PatchFileV3Parser parser = new PatchFileV3Parser(writerFactory);

        // Then
        List<Translation> translations = parser.parse(patchFile, null, new MimicTranslator());
        Assert.assertEquals(1, translations.size());
    }

    @Test
    public void canWriteSingleTranslation() throws IOException {
        // Given
        Path patchFile = Paths.get("src/test/resources/patchfiles/single.txt");
        Path outputFile = Paths.get("src/test/resources/patchfiles/out/_single_out.txt");
        Path expectedFile = Paths.get("src/test/resources/patchfiles/single_expected.txt");
        doReturn(new DefaultWriter(outputFile)).when(writerFactory).create(outputFile);

        // When
        PatchFileV3Parser parser = new PatchFileV3Parser(writerFactory);

        // Then
        parser.parse(patchFile, outputFile, new MimicTranslator());
        Assert.assertTrue(FileUtils.contentEquals(expectedFile.toFile(), outputFile.toFile()));
    }
}
