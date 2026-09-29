package io.veyralang.intellij;

import com.intellij.lang.Language;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class VeyraSyntaxHighlighterFactory extends SyntaxHighlighterFactory {
    @NotNull @Override public SyntaxHighlighter getSyntaxHighlighter(@Nullable Project project, @Nullable VirtualFile file) {
        return new VeyraSyntaxHighlighter();
    }
}
