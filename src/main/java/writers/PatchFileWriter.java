package writers;

import java.io.Closeable;

public interface PatchFileWriter extends Closeable {
    void write(String line);
    void writeMultiLine(String translation);
    void close();
}
