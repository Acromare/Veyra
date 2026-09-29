package io.veyralang.intellij;

import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import org.jetbrains.annotations.NotNull;

public final class VeyraFileTypeFactory extends FileTypeFactory {
    @Override public void createFileTypes(@NotNull FileTypeConsumer consumer) {
        consumer.consume(VeyraFileType.INSTANCE, "veyra");
    }
}
