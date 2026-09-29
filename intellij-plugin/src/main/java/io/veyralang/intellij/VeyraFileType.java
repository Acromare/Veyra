package io.veyralang.intellij;

import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.util.IconLoader;
import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class VeyraFileType extends LanguageFileType {
    public static final VeyraFileType INSTANCE = new VeyraFileType();
    private VeyraFileType() { super(VeyraLanguage.INSTANCE); }
    @NotNull @Override public String getName() { return "Veyra"; }
    @NotNull @Override public String getDescription() { return "Veyra business script"; }
    @NotNull @Override public String getDefaultExtension() { return "veyra"; }
    @Nullable @Override public Icon getIcon() { return IconLoader.getIcon("/icons/veyra.svg", VeyraFileType.class); }
}
