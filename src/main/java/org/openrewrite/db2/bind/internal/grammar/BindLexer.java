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
// Generated from /Users/jon/Projects/github/openrewrite/rewrite-cobol/.claude/worktrees/oraweb/src/main/antlr-bind/BindLexer.g4 by ANTLR 4.13.2
package org.openrewrite.db2.bind.internal.grammar;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue", "this-escape"})
public class BindLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.13.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		UTF_8_BOM=1, WS=2, EOL=3, CARD=4, CARD_CONTINUATION=5, STRINGLITERAL=6, 
		TEXT=7;
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
			"UTF_8_BOM", "WS", "EOL", "CARD", "CARD_CONTINUATION", "LF", "CR", "FORM_FEED", 
			"INSIDE_WS", "INSIDE_EOL", "STRINGLITERAL", "TEXT"
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
			null, "UTF_8_BOM", "WS", "EOL", "CARD", "CARD_CONTINUATION", "STRINGLITERAL", 
			"TEXT"
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


	public BindLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "BindLexer.g4"; }

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
		"\u0004\u0000\u0007l\u0006\uffff\uffff\u0006\uffff\uffff\u0002\u0000\u0007"+
		"\u0000\u0002\u0001\u0007\u0001\u0002\u0002\u0007\u0002\u0002\u0003\u0007"+
		"\u0003\u0002\u0004\u0007\u0004\u0002\u0005\u0007\u0005\u0002\u0006\u0007"+
		"\u0006\u0002\u0007\u0007\u0007\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n"+
		"\u0007\n\u0002\u000b\u0007\u000b\u0001\u0000\u0001\u0000\u0001\u0000\u0001"+
		"\u0000\u0001\u0001\u0004\u0001 \b\u0001\u000b\u0001\f\u0001!\u0001\u0001"+
		"\u0001\u0001\u0001\u0002\u0003\u0002\'\b\u0002\u0001\u0002\u0001\u0002"+
		"\u0003\u0002+\b\u0002\u0001\u0002\u0001\u0002\u0001\u0003\u0001\u0003"+
		"\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003"+
		"\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0004\u0001\u0004"+
		"\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004"+
		"\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004"+
		"\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0005\u0001\u0005\u0001\u0006"+
		"\u0001\u0006\u0001\u0007\u0001\u0007\u0001\b\u0001\b\u0001\b\u0001\b\u0001"+
		"\b\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\n\u0001\n\u0001"+
		"\n\u0001\n\u0005\na\b\n\n\n\f\nd\t\n\u0001\n\u0001\n\u0001\u000b\u0004"+
		"\u000bi\b\u000b\u000b\u000b\f\u000bj\u0000\u0000\f\u0002\u0001\u0004\u0002"+
		"\u0006\u0003\b\u0004\n\u0005\f\u0000\u000e\u0000\u0010\u0000\u0012\u0000"+
		"\u0014\u0000\u0016\u0006\u0018\u0007\u0002\u0000\u0001\u0003\u0003\u0000"+
		"\t\t\f\f  \u0003\u0000\n\n\r\r\'\'\u0004\u0000\t\n\f\r  \'\'m\u0000\u0002"+
		"\u0001\u0000\u0000\u0000\u0000\u0004\u0001\u0000\u0000\u0000\u0000\u0006"+
		"\u0001\u0000\u0000\u0000\u0000\b\u0001\u0000\u0000\u0000\u0000\n\u0001"+
		"\u0000\u0000\u0000\u0001\u0012\u0001\u0000\u0000\u0000\u0001\u0014\u0001"+
		"\u0000\u0000\u0000\u0001\u0016\u0001\u0000\u0000\u0000\u0001\u0018\u0001"+
		"\u0000\u0000\u0000\u0002\u001a\u0001\u0000\u0000\u0000\u0004\u001f\u0001"+
		"\u0000\u0000\u0000\u0006*\u0001\u0000\u0000\u0000\b.\u0001\u0000\u0000"+
		"\u0000\n:\u0001\u0000\u0000\u0000\fK\u0001\u0000\u0000\u0000\u000eM\u0001"+
		"\u0000\u0000\u0000\u0010O\u0001\u0000\u0000\u0000\u0012Q\u0001\u0000\u0000"+
		"\u0000\u0014V\u0001\u0000\u0000\u0000\u0016\\\u0001\u0000\u0000\u0000"+
		"\u0018h\u0001\u0000\u0000\u0000\u001a\u001b\u0005\u8000\ufeff\u0000\u0000"+
		"\u001b\u001c\u0001\u0000\u0000\u0000\u001c\u001d\u0006\u0000\u0000\u0000"+
		"\u001d\u0003\u0001\u0000\u0000\u0000\u001e \u0007\u0000\u0000\u0000\u001f"+
		"\u001e\u0001\u0000\u0000\u0000 !\u0001\u0000\u0000\u0000!\u001f\u0001"+
		"\u0000\u0000\u0000!\"\u0001\u0000\u0000\u0000\"#\u0001\u0000\u0000\u0000"+
		"#$\u0006\u0001\u0001\u0000$\u0005\u0001\u0000\u0000\u0000%\'\u0003\u000e"+
		"\u0006\u0000&%\u0001\u0000\u0000\u0000&\'\u0001\u0000\u0000\u0000\'(\u0001"+
		"\u0000\u0000\u0000(+\u0003\f\u0005\u0000)+\u0003\u0010\u0007\u0000*&\u0001"+
		"\u0000\u0000\u0000*)\u0001\u0000\u0000\u0000+,\u0001\u0000\u0000\u0000"+
		",-\u0006\u0002\u0001\u0000-\u0007\u0001\u0000\u0000\u0000./\u0005^\u0000"+
		"\u0000/0\u0005^\u0000\u000001\u0005C\u0000\u000012\u0005A\u0000\u0000"+
		"23\u0005R\u0000\u000034\u0005D\u0000\u000045\u0005^\u0000\u000056\u0005"+
		"^\u0000\u000067\u0001\u0000\u0000\u000078\u0006\u0003\u0001\u000089\u0006"+
		"\u0003\u0002\u00009\t\u0001\u0000\u0000\u0000:;\u0005^\u0000\u0000;<\u0005"+
		"^\u0000\u0000<=\u0005C\u0000\u0000=>\u0005A\u0000\u0000>?\u0005R\u0000"+
		"\u0000?@\u0005D\u0000\u0000@A\u0005_\u0000\u0000AB\u0005C\u0000\u0000"+
		"BC\u0005O\u0000\u0000CD\u0005N\u0000\u0000DE\u0005T\u0000\u0000EF\u0005"+
		"^\u0000\u0000FG\u0005^\u0000\u0000GH\u0001\u0000\u0000\u0000HI\u0006\u0004"+
		"\u0001\u0000IJ\u0006\u0004\u0002\u0000J\u000b\u0001\u0000\u0000\u0000"+
		"KL\u0005\n\u0000\u0000L\r\u0001\u0000\u0000\u0000MN\u0005\r\u0000\u0000"+
		"N\u000f\u0001\u0000\u0000\u0000OP\u0005\f\u0000\u0000P\u0011\u0001\u0000"+
		"\u0000\u0000QR\u0003\u0004\u0001\u0000RS\u0001\u0000\u0000\u0000ST\u0006"+
		"\b\u0003\u0000TU\u0006\b\u0001\u0000U\u0013\u0001\u0000\u0000\u0000VW"+
		"\u0003\u0006\u0002\u0000WX\u0001\u0000\u0000\u0000XY\u0006\t\u0004\u0000"+
		"YZ\u0006\t\u0001\u0000Z[\u0006\t\u0005\u0000[\u0015\u0001\u0000\u0000"+
		"\u0000\\b\u0005\'\u0000\u0000]a\b\u0001\u0000\u0000^_\u0005\'\u0000\u0000"+
		"_a\u0005\'\u0000\u0000`]\u0001\u0000\u0000\u0000`^\u0001\u0000\u0000\u0000"+
		"ad\u0001\u0000\u0000\u0000b`\u0001\u0000\u0000\u0000bc\u0001\u0000\u0000"+
		"\u0000ce\u0001\u0000\u0000\u0000db\u0001\u0000\u0000\u0000ef\u0005\'\u0000"+
		"\u0000f\u0017\u0001\u0000\u0000\u0000gi\b\u0002\u0000\u0000hg\u0001\u0000"+
		"\u0000\u0000ij\u0001\u0000\u0000\u0000jh\u0001\u0000\u0000\u0000jk\u0001"+
		"\u0000\u0000\u0000k\u0019\u0001\u0000\u0000\u0000\b\u0000\u0001!&*`bj"+
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