package writers;

import writers.impl.DefaultWriter;

import java.nio.file.Path;

public class PatchFileWriterFactory {

    public PatchFileWriter create(Path path) {
        return new DefaultWriter(path);
    }
}
