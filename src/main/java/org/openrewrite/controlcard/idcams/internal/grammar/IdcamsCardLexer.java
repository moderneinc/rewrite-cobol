/*
 * Copyright 2026 the original author or authors.
 * <p>
 * Licensed under the Moderne Source Available License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://docs.moderne.io/licensing/moderne-source-available-license
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// Generated from /Users/jon/Projects/github/openrewrite/rewrite-cobol/.claude/worktrees/oraweb/src/main/antlr-idcams/IdcamsCardLexer.g4 by ANTLR 4.13.2
package org.openrewrite.controlcard.idcams.internal.grammar;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue", "this-escape"})
public class IdcamsCardLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.13.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		UTF_8_BOM=1, WS=2, EOL=3, CARD=4, CARD_CONTINUATION=5, COMMENT_CARD=6, 
		COMMENT=7, STRINGLITERAL=8, TEXT=9;
	public static final int
		INSIDE_CARD=1;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE", "INSIDE_CARD"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"UTF_8_BOM", "WS", "EOL", "CARD", "CARD_CONTINUATION", "COMMENT_CARD", 
			"LF", "CR", "FORM_FEED", "INSIDE_WS", "INSIDE_EOL", "COMMENT", "STRINGLITERAL", 
			"TEXT"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'\\uFEFF'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "UTF_8_BOM", "WS", "EOL", "CARD", "CARD_CONTINUATION", "COMMENT_CARD", 
			"COMMENT", "STRINGLITERAL", "TEXT"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}


	public IdcamsCardLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "IdcamsCardLexer.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\u0004\u0000\t\u0092\u0006\uffff\uffff\u0006\uffff\uffff\u0002\u0000\u0007"+
		"\u0000\u0002\u0001\u0007\u0001\u0002\u0002\u0007\u0002\u0002\u0003\u0007"+
		"\u0003\u0002\u0004\u0007\u0004\u0002\u0005\u0007\u0005\u0002\u0006\u0007"+
		"\u0006\u0002\u0007\u0007\u0007\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n"+
		"\u0007\n\u0002\u000b\u0007\u000b\u0002\f\u0007\f\u0002\r\u0007\r\u0001"+
		"\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0001\u0004\u0001$\b"+
		"\u0001\u000b\u0001\f\u0001%\u0001\u0001\u0001\u0001\u0001\u0002\u0003"+
		"\u0002+\b\u0002\u0001\u0002\u0001\u0002\u0003\u0002/\b\u0002\u0001\u0002"+
		"\u0001\u0002\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003"+
		"\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003"+
		"\u0001\u0003\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004"+
		"\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004"+
		"\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004"+
		"\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005"+
		"\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005"+
		"\u0001\u0005\u0005\u0005]\b\u0005\n\u0005\f\u0005`\t\u0005\u0001\u0005"+
		"\u0001\u0005\u0001\u0006\u0001\u0006\u0001\u0007\u0001\u0007\u0001\b\u0001"+
		"\b\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\n\u0001\n\u0001\n\u0001"+
		"\n\u0001\n\u0001\n\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0005"+
		"\u000by\b\u000b\n\u000b\f\u000b|\t\u000b\u0001\u000b\u0001\u000b\u0001"+
		"\u000b\u0001\u000b\u0001\u000b\u0001\f\u0001\f\u0001\f\u0001\f\u0005\f"+
		"\u0087\b\f\n\f\f\f\u008a\t\f\u0001\f\u0001\f\u0001\r\u0004\r\u008f\b\r"+
		"\u000b\r\f\r\u0090\u0001z\u0000\u000e\u0002\u0001\u0004\u0002\u0006\u0003"+
		"\b\u0004\n\u0005\f\u0006\u000e\u0000\u0010\u0000\u0012\u0000\u0014\u0000"+
		"\u0016\u0000\u0018\u0007\u001a\b\u001c\t\u0002\u0000\u0001\u0004\u0003"+
		"\u0000\t\t\f\f  \u0002\u0000\n\n\r\r\u0003\u0000\n\n\r\r\'\'\u0004\u0000"+
		"\t\n\f\r  \'\'\u0095\u0000\u0002\u0001\u0000\u0000\u0000\u0000\u0004\u0001"+
		"\u0000\u0000\u0000\u0000\u0006\u0001\u0000\u0000\u0000\u0000\b\u0001\u0000"+
		"\u0000\u0000\u0000\n\u0001\u0000\u0000\u0000\u0000\f\u0001\u0000\u0000"+
		"\u0000\u0001\u0014\u0001\u0000\u0000\u0000\u0001\u0016\u0001\u0000\u0000"+
		"\u0000\u0001\u0018\u0001\u0000\u0000\u0000\u0001\u001a\u0001\u0000\u0000"+
		"\u0000\u0001\u001c\u0001\u0000\u0000\u0000\u0002\u001e\u0001\u0000\u0000"+
		"\u0000\u0004#\u0001\u0000\u0000\u0000\u0006.\u0001\u0000\u0000\u0000\b"+
		"2\u0001\u0000\u0000\u0000\n>\u0001\u0000\u0000\u0000\fO\u0001\u0000\u0000"+
		"\u0000\u000ec\u0001\u0000\u0000\u0000\u0010e\u0001\u0000\u0000\u0000\u0012"+
		"g\u0001\u0000\u0000\u0000\u0014i\u0001\u0000\u0000\u0000\u0016n\u0001"+
		"\u0000\u0000\u0000\u0018t\u0001\u0000\u0000\u0000\u001a\u0082\u0001\u0000"+
		"\u0000\u0000\u001c\u008e\u0001\u0000\u0000\u0000\u001e\u001f\u0005\u8000"+
		"\ufeff\u0000\u0000\u001f \u0001\u0000\u0000\u0000 !\u0006\u0000\u0000"+
		"\u0000!\u0003\u0001\u0000\u0000\u0000\"$\u0007\u0000\u0000\u0000#\"\u0001"+
		"\u0000\u0000\u0000$%\u0001\u0000\u0000\u0000%#\u0001\u0000\u0000\u0000"+
		"%&\u0001\u0000\u0000\u0000&\'\u0001\u0000\u0000\u0000\'(\u0006\u0001\u0001"+
		"\u0000(\u0005\u0001\u0000\u0000\u0000)+\u0003\u0010\u0007\u0000*)\u0001"+
		"\u0000\u0000\u0000*+\u0001\u0000\u0000\u0000+,\u0001\u0000\u0000\u0000"+
		",/\u0003\u000e\u0006\u0000-/\u0003\u0012\b\u0000.*\u0001\u0000\u0000\u0000"+
		".-\u0001\u0000\u0000\u0000/0\u0001\u0000\u0000\u000001\u0006\u0002\u0001"+
		"\u00001\u0007\u0001\u0000\u0000\u000023\u0005^\u0000\u000034\u0005^\u0000"+
		"\u000045\u0005C\u0000\u000056\u0005A\u0000\u000067\u0005R\u0000\u0000"+
		"78\u0005D\u0000\u000089\u0005^\u0000\u00009:\u0005^\u0000\u0000:;\u0001"+
		"\u0000\u0000\u0000;<\u0006\u0003\u0001\u0000<=\u0006\u0003\u0002\u0000"+
		"=\t\u0001\u0000\u0000\u0000>?\u0005^\u0000\u0000?@\u0005^\u0000\u0000"+
		"@A\u0005C\u0000\u0000AB\u0005A\u0000\u0000BC\u0005R\u0000\u0000CD\u0005"+
		"D\u0000\u0000DE\u0005_\u0000\u0000EF\u0005C\u0000\u0000FG\u0005O\u0000"+
		"\u0000GH\u0005N\u0000\u0000HI\u0005T\u0000\u0000IJ\u0005^\u0000\u0000"+
		"JK\u0005^\u0000\u0000KL\u0001\u0000\u0000\u0000LM\u0006\u0004\u0001\u0000"+
		"MN\u0006\u0004\u0002\u0000N\u000b\u0001\u0000\u0000\u0000OP\u0005^\u0000"+
		"\u0000PQ\u0005^\u0000\u0000QR\u0005C\u0000\u0000RS\u0005O\u0000\u0000"+
		"ST\u0005M\u0000\u0000TU\u0005M\u0000\u0000UV\u0005E\u0000\u0000VW\u0005"+
		"N\u0000\u0000WX\u0005T\u0000\u0000XY\u0005^\u0000\u0000YZ\u0005^\u0000"+
		"\u0000Z^\u0001\u0000\u0000\u0000[]\b\u0001\u0000\u0000\\[\u0001\u0000"+
		"\u0000\u0000]`\u0001\u0000\u0000\u0000^\\\u0001\u0000\u0000\u0000^_\u0001"+
		"\u0000\u0000\u0000_a\u0001\u0000\u0000\u0000`^\u0001\u0000\u0000\u0000"+
		"ab\u0006\u0005\u0001\u0000b\r\u0001\u0000\u0000\u0000cd\u0005\n\u0000"+
		"\u0000d\u000f\u0001\u0000\u0000\u0000ef\u0005\r\u0000\u0000f\u0011\u0001"+
		"\u0000\u0000\u0000gh\u0005\f\u0000\u0000h\u0013\u0001\u0000\u0000\u0000"+
		"ij\u0003\u0004\u0001\u0000jk\u0001\u0000\u0000\u0000kl\u0006\t\u0003\u0000"+
		"lm\u0006\t\u0001\u0000m\u0015\u0001\u0000\u0000\u0000no\u0003\u0006\u0002"+
		"\u0000op\u0001\u0000\u0000\u0000pq\u0006\n\u0004\u0000qr\u0006\n\u0001"+
		"\u0000rs\u0006\n\u0005\u0000s\u0017\u0001\u0000\u0000\u0000tu\u0005/\u0000"+
		"\u0000uv\u0005*\u0000\u0000vz\u0001\u0000\u0000\u0000wy\b\u0001\u0000"+
		"\u0000xw\u0001\u0000\u0000\u0000y|\u0001\u0000\u0000\u0000z{\u0001\u0000"+
		"\u0000\u0000zx\u0001\u0000\u0000\u0000{}\u0001\u0000\u0000\u0000|z\u0001"+
		"\u0000\u0000\u0000}~\u0005*\u0000\u0000~\u007f\u0005/\u0000\u0000\u007f"+
		"\u0080\u0001\u0000\u0000\u0000\u0080\u0081\u0006\u000b\u0001\u0000\u0081"+
		"\u0019\u0001\u0000\u0000\u0000\u0082\u0088\u0005\'\u0000\u0000\u0083\u0087"+
		"\b\u0002\u0000\u0000\u0084\u0085\u0005\'\u0000\u0000\u0085\u0087\u0005"+
		"\'\u0000\u0000\u0086\u0083\u0001\u0000\u0000\u0000\u0086\u0084\u0001\u0000"+
		"\u0000\u0000\u0087\u008a\u0001\u0000\u0000\u0000\u0088\u0086\u0001\u0000"+
		"\u0000\u0000\u0088\u0089\u0001\u0000\u0000\u0000\u0089\u008b\u0001\u0000"+
		"\u0000\u0000\u008a\u0088\u0001\u0000\u0000\u0000\u008b\u008c\u0005\'\u0000"+
		"\u0000\u008c\u001b\u0001\u0000\u0000\u0000\u008d\u008f\b\u0003\u0000\u0000"+
		"\u008e\u008d\u0001\u0000\u0000\u0000\u008f\u0090\u0001\u0000\u0000\u0000"+
		"\u0090\u008e\u0001\u0000\u0000\u0000\u0090\u0091\u0001\u0000\u0000\u0000"+
		"\u0091\u001d\u0001\u0000\u0000\u0000\n\u0000\u0001%*.^z\u0086\u0088\u0090"+
		"\u0006\u0006\u0000\u0000\u0000\u0001\u0000\u0005\u0001\u0000\u0007\u0002"+
		"\u0000\u0007\u0003\u0000\u0004\u0000\u0000";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}