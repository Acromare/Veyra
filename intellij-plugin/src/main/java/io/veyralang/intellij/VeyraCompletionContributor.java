package io.veyralang.intellij;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

/** Basic completion for Veyra's stable language vocabulary. */
public final class VeyraCompletionContributor extends CompletionContributor {
    private static final String[] KEYWORDS = {
            "function", "rule", "let", "var", "if", "elif", "else", "return", "emit"
    };
    private static final String[] TYPES = {"string", "int", "long", "double", "boolean", "list", "map"};
    private static final String[] VALUES = {"true", "false", "null"};

    public VeyraCompletionContributor() {
        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement().withLanguage(VeyraLanguage.INSTANCE),
                new CompletionProvider<>() {
                    @Override
                    protected void addCompletions(@NotNull CompletionParameters parameters,
                                                  @NotNull ProcessingContext context,
                                                  @NotNull CompletionResultSet result) {
                        add(result, KEYWORDS, "keyword");
                        add(result, TYPES, "type");
                        add(result, VALUES, "value");
                    }
                });
    }

    private static void add(CompletionResultSet result, String[] values, String type) {
        for (String value : values) {
            result.addElement(LookupElementBuilder.create(value).withTypeText(type));
        }
    }
}
