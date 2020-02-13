package writers.impl;

import writers.PatchFileWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;

public class DefaultWriter implements PatchFileWriter {
    private PrintWriter writer;

    public DefaultWriter(Path path) {
        try {
            writer = new PrintWriter(new FileWriter(path.toString()));
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    @Override
    public void write(String line) {
        writer.println(line);
    }

    @Override
    public void close() {
        writer.close();
    }

    @Override
    public void writeMultiLine(String line) {
        writer.println(line);
    }
}
