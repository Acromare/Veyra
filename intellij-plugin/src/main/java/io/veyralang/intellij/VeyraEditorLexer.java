package io.veyralang.intellij;

import com.intellij.lexer.LexerBase;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.TokenType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class VeyraEditorLexer extends LexerBase {
    static final IElementType KEYWORD=new IElementType("VEYRA_KEYWORD",VeyraLanguage.INSTANCE);
    static final IElementType STRING=new IElementType("VEYRA_STRING",VeyraLanguage.INSTANCE);
    static final IElementType NUMBER=new IElementType("VEYRA_NUMBER",VeyraLanguage.INSTANCE);
    static final IElementType COMMENT=new IElementType("VEYRA_COMMENT",VeyraLanguage.INSTANCE);
    static final IElementType OPERATOR=new IElementType("VEYRA_OPERATOR",VeyraLanguage.INSTANCE);
    static final IElementType IDENTIFIER=new IElementType("VEYRA_IDENTIFIER",VeyraLanguage.INSTANCE);
    private static final java.util.Set<String> KEYWORDS=java.util.Set.of("function","rule","let","var","if","elif","else","return","emit","true","false","null");
    private CharSequence buffer=""; private int endOffset,tokenStart,tokenEnd; private IElementType tokenType;
    @Override public void start(@NotNull CharSequence buffer,int startOffset,int endOffset,int initialState){this.buffer=buffer;this.endOffset=endOffset;this.tokenStart=startOffset;this.tokenEnd=startOffset;advance();}
    @Nullable @Override public IElementType getTokenType(){return tokenType;}
    @Override public int getTokenStart(){return tokenStart;}
    @Override public int getTokenEnd(){return tokenEnd;}
    @Override public int getState(){return 0;}
    @Override public void advance(){tokenStart=tokenEnd;if(tokenStart>=endOffset){tokenType=null;return;}char c=buffer.charAt(tokenStart);int i=tokenStart+1;
        if(Character.isWhitespace(c)){while(i<endOffset&&Character.isWhitespace(buffer.charAt(i)))i++;tokenType=TokenType.WHITE_SPACE;}
        else if(c=='/'&&i<endOffset&&buffer.charAt(i)=='/'){while(i<endOffset&&buffer.charAt(i)!='\n'&&buffer.charAt(i)!='\r')i++;tokenType=COMMENT;}
        else if(c=='"'){boolean escaped=false;while(i<endOffset){char n=buffer.charAt(i++);if(n=='"'&&!escaped)break;if(n=='\\'&&!escaped)escaped=true;else escaped=false;}tokenType=STRING;}
        else if(Character.isJavaIdentifierStart(c)){while(i<endOffset&&Character.isJavaIdentifierPart(buffer.charAt(i)))i++;String word=buffer.subSequence(tokenStart,i).toString();tokenType=KEYWORDS.contains(word)?KEYWORD:IDENTIFIER;}
        else if(Character.isDigit(c)){while(i<endOffset&&(Character.isDigit(buffer.charAt(i))||buffer.charAt(i)=='.'))i++;tokenType=NUMBER;}
        else if("+-*/%=!><&|?.".indexOf(c)>=0){while(i<endOffset&&"=|&?".indexOf(buffer.charAt(i))>=0)i++;tokenType=OPERATOR;}
        else tokenType=TokenType.BAD_CHARACTER;
        tokenEnd=i;
    }
    @NotNull @Override public CharSequence getBufferSequence(){return buffer;}
    @Override public int getBufferEnd(){return endOffset;}
}
