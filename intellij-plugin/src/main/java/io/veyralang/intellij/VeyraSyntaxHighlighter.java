package io.veyralang.intellij;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

public final class VeyraSyntaxHighlighter extends SyntaxHighlighterBase {
    static final TextAttributesKey KEYWORD=TextAttributesKey.createTextAttributesKey("VEYRA_KEYWORD",DefaultLanguageHighlighterColors.KEYWORD);
    static final TextAttributesKey STRING=TextAttributesKey.createTextAttributesKey("VEYRA_STRING",DefaultLanguageHighlighterColors.STRING);
    static final TextAttributesKey NUMBER=TextAttributesKey.createTextAttributesKey("VEYRA_NUMBER",DefaultLanguageHighlighterColors.NUMBER);
    static final TextAttributesKey COMMENT=TextAttributesKey.createTextAttributesKey("VEYRA_COMMENT",DefaultLanguageHighlighterColors.LINE_COMMENT);
    static final TextAttributesKey OP=TextAttributesKey.createTextAttributesKey("VEYRA_OPERATION",DefaultLanguageHighlighterColors.OPERATION_SIGN);
    static final TextAttributesKey IDENTIFIER=TextAttributesKey.createTextAttributesKey("VEYRA_IDENTIFIER",DefaultLanguageHighlighterColors.IDENTIFIER);
    @NotNull @Override public com.intellij.lexer.Lexer getHighlightingLexer(){return new VeyraEditorLexer();}
    @NotNull @Override public TextAttributesKey[] getTokenHighlights(IElementType type){
        if(type==VeyraEditorLexer.KEYWORD)return pack(KEYWORD);
        if(type==VeyraEditorLexer.STRING)return pack(STRING);
        if(type==VeyraEditorLexer.NUMBER)return pack(NUMBER);
        if(type==VeyraEditorLexer.COMMENT)return pack(COMMENT);
        if(type==VeyraEditorLexer.OPERATOR)return pack(OP);
        if(type==VeyraEditorLexer.IDENTIFIER)return pack(IDENTIFIER);
        return TextAttributesKey.EMPTY_ARRAY;
    }
}
