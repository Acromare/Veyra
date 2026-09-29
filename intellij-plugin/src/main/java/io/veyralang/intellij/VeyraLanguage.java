package io.veyralang.intellij;

import com.intellij.lang.Language;

public final class VeyraLanguage extends Language {
    public static final VeyraLanguage INSTANCE = new VeyraLanguage();
    private VeyraLanguage() { super("Veyra"); }
}
