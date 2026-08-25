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
// Generated from /Users/jon/Projects/github/openrewrite/rewrite-cobol/.claude/worktrees/oraweb/src/main/antlr-linkedit/LinkEditLexer.g4 by ANTLR 4.13.2
package org.openrewrite.linkedit.internal.grammar;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue", "this-escape"})
public class LinkEditLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.13.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		UTF_8_BOM=1, WS=2, EOL=3, CARD=4, CARD_CONTINUATION=5, COMMENT_CARD=6, 
		STRINGLITERAL=7, TEXT=8;
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
			"LF", "CR", "FORM_FEED", "INSIDE_WS", "INSIDE_EOL", "STRINGLITERAL", 
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
			"STRINGLITERAL", "TEXT"
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


	public LinkEditLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "LinkEditLexer.g4"; }

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
		"\u0004\u0000\b\u0082\u0006\uffff\uffff\u0006\uffff\uffff\u0002\u0000\u0007"+
		"\u0000\u0002\u0001\u0007\u0001\u0002\u0002\u0007\u0002\u0002\u0003\u0007"+
		"\u0003\u0002\u0004\u0007\u0004\u0002\u0005\u0007\u0005\u0002\u0006\u0007"+
		"\u0006\u0002\u0007\u0007\u0007\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n"+
		"\u0007\n\u0002\u000b\u0007\u000b\u0002\f\u0007\f\u0001\u0000\u0001\u0000"+
		"\u0001\u0000\u0001\u0000\u0001\u0001\u0004\u0001\"\b\u0001\u000b\u0001"+
		"\f\u0001#\u0001\u0001\u0001\u0001\u0001\u0002\u0003\u0002)\b\u0002\u0001"+
		"\u0002\u0001\u0002\u0003\u0002-\b\u0002\u0001\u0002\u0001\u0002\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001"+
		"\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001"+
		"\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0005\u0001"+
		"\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001"+
		"\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0005"+
		"\u0005[\b\u0005\n\u0005\f\u0005^\t\u0005\u0001\u0005\u0001\u0005\u0001"+
		"\u0006\u0001\u0006\u0001\u0007\u0001\u0007\u0001\b\u0001\b\u0001\t\u0001"+
		"\t\u0001\t\u0001\t\u0001\t\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001"+
		"\n\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0005\u000bw\b\u000b"+
		"\n\u000b\f\u000bz\t\u000b\u0001\u000b\u0001\u000b\u0001\f\u0004\f\u007f"+
		"\b\f\u000b\f\f\f\u0080\u0000\u0000\r\u0002\u0001\u0004\u0002\u0006\u0003"+
		"\b\u0004\n\u0005\f\u0006\u000e\u0000\u0010\u0000\u0012\u0000\u0014\u0000"+
		"\u0016\u0000\u0018\u0007\u001a\b\u0002\u0000\u0001\u0004\u0003\u0000\t"+
		"\t\f\f  \u0002\u0000\n\n\r\r\u0003\u0000\n\n\r\r\'\'\u0004\u0000\t\n\f"+
		"\r  \'\'\u0084\u0000\u0002\u0001\u0000\u0000\u0000\u0000\u0004\u0001\u0000"+
		"\u0000\u0000\u0000\u0006\u0001\u0000\u0000\u0000\u0000\b\u0001\u0000\u0000"+
		"\u0000\u0000\n\u0001\u0000\u0000\u0000\u0000\f\u0001\u0000\u0000\u0000"+
		"\u0001\u0014\u0001\u0000\u0000\u0000\u0001\u0016\u0001\u0000\u0000\u0000"+
		"\u0001\u0018\u0001\u0000\u0000\u0000\u0001\u001a\u0001\u0000\u0000\u0000"+
		"\u0002\u001c\u0001\u0000\u0000\u0000\u0004!\u0001\u0000\u0000\u0000\u0006"+
		",\u0001\u0000\u0000\u0000\b0\u0001\u0000\u0000\u0000\n<\u0001\u0000\u0000"+
		"\u0000\fM\u0001\u0000\u0000\u0000\u000ea\u0001\u0000\u0000\u0000\u0010"+
		"c\u0001\u0000\u0000\u0000\u0012e\u0001\u0000\u0000\u0000\u0014g\u0001"+
		"\u0000\u0000\u0000\u0016l\u0001\u0000\u0000\u0000\u0018r\u0001\u0000\u0000"+
		"\u0000\u001a~\u0001\u0000\u0000\u0000\u001c\u001d\u0005\u8000\ufeff\u0000"+
		"\u0000\u001d\u001e\u0001\u0000\u0000\u0000\u001e\u001f\u0006\u0000\u0000"+
		"\u0000\u001f\u0003\u0001\u0000\u0000\u0000 \"\u0007\u0000\u0000\u0000"+
		"! \u0001\u0000\u0000\u0000\"#\u0001\u0000\u0000\u0000#!\u0001\u0000\u0000"+
		"\u0000#$\u0001\u0000\u0000\u0000$%\u0001\u0000\u0000\u0000%&\u0006\u0001"+
		"\u0001\u0000&\u0005\u0001\u0000\u0000\u0000\')\u0003\u0010\u0007\u0000"+
		"(\'\u0001\u0000\u0000\u0000()\u0001\u0000\u0000\u0000)*\u0001\u0000\u0000"+
		"\u0000*-\u0003\u000e\u0006\u0000+-\u0003\u0012\b\u0000,(\u0001\u0000\u0000"+
		"\u0000,+\u0001\u0000\u0000\u0000-.\u0001\u0000\u0000\u0000./\u0006\u0002"+
		"\u0001\u0000/\u0007\u0001\u0000\u0000\u000001\u0005^\u0000\u000012\u0005"+
		"^\u0000\u000023\u0005C\u0000\u000034\u0005A\u0000\u000045\u0005R\u0000"+
		"\u000056\u0005D\u0000\u000067\u0005^\u0000\u000078\u0005^\u0000\u0000"+
		"89\u0001\u0000\u0000\u00009:\u0006\u0003\u0001\u0000:;\u0006\u0003\u0002"+
		"\u0000;\t\u0001\u0000\u0000\u0000<=\u0005^\u0000\u0000=>\u0005^\u0000"+
		"\u0000>?\u0005C\u0000\u0000?@\u0005A\u0000\u0000@A\u0005R\u0000\u0000"+
		"AB\u0005D\u0000\u0000BC\u0005_\u0000\u0000CD\u0005C\u0000\u0000DE\u0005"+
		"O\u0000\u0000EF\u0005N\u0000\u0000FG\u0005T\u0000\u0000GH\u0005^\u0000"+
		"\u0000HI\u0005^\u0000\u0000IJ\u0001\u0000\u0000\u0000JK\u0006\u0004\u0001"+
		"\u0000KL\u0006\u0004\u0002\u0000L\u000b\u0001\u0000\u0000\u0000MN\u0005"+
		"^\u0000\u0000NO\u0005^\u0000\u0000OP\u0005C\u0000\u0000PQ\u0005O\u0000"+
		"\u0000QR\u0005M\u0000\u0000RS\u0005M\u0000\u0000ST\u0005E\u0000\u0000"+
		"TU\u0005N\u0000\u0000UV\u0005T\u0000\u0000VW\u0005^\u0000\u0000WX\u0005"+
		"^\u0000\u0000X\\\u0001\u0000\u0000\u0000Y[\b\u0001\u0000\u0000ZY\u0001"+
		"\u0000\u0000\u0000[^\u0001\u0000\u0000\u0000\\Z\u0001\u0000\u0000\u0000"+
		"\\]\u0001\u0000\u0000\u0000]_\u0001\u0000\u0000\u0000^\\\u0001\u0000\u0000"+
		"\u0000_`\u0006\u0005\u0001\u0000`\r\u0001\u0000\u0000\u0000ab\u0005\n"+
		"\u0000\u0000b\u000f\u0001\u0000\u0000\u0000cd\u0005\r\u0000\u0000d\u0011"+
		"\u0001\u0000\u0000\u0000ef\u0005\f\u0000\u0000f\u0013\u0001\u0000\u0000"+
		"\u0000gh\u0003\u0004\u0001\u0000hi\u0001\u0000\u0000\u0000ij\u0006\t\u0003"+
		"\u0000jk\u0006\t\u0001\u0000k\u0015\u0001\u0000\u0000\u0000lm\u0003\u0006"+
		"\u0002\u0000mn\u0001\u0000\u0000\u0000no\u0006\n\u0004\u0000op\u0006\n"+
		"\u0001\u0000pq\u0006\n\u0005\u0000q\u0017\u0001\u0000\u0000\u0000rx\u0005"+
		"\'\u0000\u0000sw\b\u0002\u0000\u0000tu\u0005\'\u0000\u0000uw\u0005\'\u0000"+
		"\u0000vs\u0001\u0000\u0000\u0000vt\u0001\u0000\u0000\u0000wz\u0001\u0000"+
		"\u0000\u0000xv\u0001\u0000\u0000\u0000xy\u0001\u0000\u0000\u0000y{\u0001"+
		"\u0000\u0000\u0000zx\u0001\u0000\u0000\u0000{|\u0005\'\u0000\u0000|\u0019"+
		"\u0001\u0000\u0000\u0000}\u007f\b\u0003\u0000\u0000~}\u0001\u0000\u0000"+
		"\u0000\u007f\u0080\u0001\u0000\u0000\u0000\u0080~\u0001\u0000\u0000\u0000"+
		"\u0080\u0081\u0001\u0000\u0000\u0000\u0081\u001b\u0001\u0000\u0000\u0000"+
		"\t\u0000\u0001#(,\\vx\u0080\u0006\u0006\u0000\u0000\u0000\u0001\u0000"+
		"\u0005\u0001\u0000\u0007\u0002\u0000\u0007\u0003\u0000\u0004\u0000\u0000";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}