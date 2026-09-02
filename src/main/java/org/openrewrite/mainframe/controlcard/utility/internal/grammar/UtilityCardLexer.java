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
// Generated from /Users/jon/Projects/github/openrewrite/rewrite-cobol/.claude/worktrees/oraweb/src/main/antlr-utility/UtilityCardLexer.g4 by ANTLR 4.13.2
package org.openrewrite.mainframe.controlcard.utility.internal.grammar;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue", "this-escape"})
public class UtilityCardLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.13.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		UTF_8_BOM=1, WS=2, EOL=3, COMMENT_CARD=4, SEMICOLON=5, STRINGLITERAL=6, 
		TEXT=7;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"UTF_8_BOM", "WS", "EOL", "COMMENT_CARD", "SEMICOLON", "LF", "CR", "FORM_FEED", 
			"STRINGLITERAL", "TEXT"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'\\uFEFF'", null, null, null, "';'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "UTF_8_BOM", "WS", "EOL", "COMMENT_CARD", "SEMICOLON", "STRINGLITERAL", 
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


	public UtilityCardLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "UtilityCardLexer.g4"; }

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
		"\u0004\u0000\u0007U\u0006\uffff\uffff\u0002\u0000\u0007\u0000\u0002\u0001"+
		"\u0007\u0001\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004"+
		"\u0007\u0004\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007"+
		"\u0007\u0007\u0002\b\u0007\b\u0002\t\u0007\t\u0001\u0000\u0001\u0000\u0001"+
		"\u0000\u0001\u0000\u0001\u0001\u0004\u0001\u001b\b\u0001\u000b\u0001\f"+
		"\u0001\u001c\u0001\u0001\u0001\u0001\u0001\u0002\u0003\u0002\"\b\u0002"+
		"\u0001\u0002\u0001\u0002\u0003\u0002&\b\u0002\u0001\u0002\u0001\u0002"+
		"\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003"+
		"\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003"+
		"\u0001\u0003\u0005\u00037\b\u0003\n\u0003\f\u0003:\t\u0003\u0001\u0003"+
		"\u0001\u0003\u0001\u0004\u0001\u0004\u0001\u0005\u0001\u0005\u0001\u0006"+
		"\u0001\u0006\u0001\u0007\u0001\u0007\u0001\b\u0001\b\u0001\b\u0001\b\u0005"+
		"\bJ\b\b\n\b\f\bM\t\b\u0001\b\u0001\b\u0001\t\u0004\tR\b\t\u000b\t\f\t"+
		"S\u0000\u0000\n\u0001\u0001\u0003\u0002\u0005\u0003\u0007\u0004\t\u0005"+
		"\u000b\u0000\r\u0000\u000f\u0000\u0011\u0006\u0013\u0007\u0001\u0000\u0004"+
		"\u0003\u0000\t\t\f\f  \u0002\u0000\n\n\r\r\u0003\u0000\n\n\r\r\'\'\u0005"+
		"\u0000\t\n\f\r  \'\';;X\u0000\u0001\u0001\u0000\u0000\u0000\u0000\u0003"+
		"\u0001\u0000\u0000\u0000\u0000\u0005\u0001\u0000\u0000\u0000\u0000\u0007"+
		"\u0001\u0000\u0000\u0000\u0000\t\u0001\u0000\u0000\u0000\u0000\u0011\u0001"+
		"\u0000\u0000\u0000\u0000\u0013\u0001\u0000\u0000\u0000\u0001\u0015\u0001"+
		"\u0000\u0000\u0000\u0003\u001a\u0001\u0000\u0000\u0000\u0005%\u0001\u0000"+
		"\u0000\u0000\u0007)\u0001\u0000\u0000\u0000\t=\u0001\u0000\u0000\u0000"+
		"\u000b?\u0001\u0000\u0000\u0000\rA\u0001\u0000\u0000\u0000\u000fC\u0001"+
		"\u0000\u0000\u0000\u0011E\u0001\u0000\u0000\u0000\u0013Q\u0001\u0000\u0000"+
		"\u0000\u0015\u0016\u0005\u8000\ufeff\u0000\u0000\u0016\u0017\u0001\u0000"+
		"\u0000\u0000\u0017\u0018\u0006\u0000\u0000\u0000\u0018\u0002\u0001\u0000"+
		"\u0000\u0000\u0019\u001b\u0007\u0000\u0000\u0000\u001a\u0019\u0001\u0000"+
		"\u0000\u0000\u001b\u001c\u0001\u0000\u0000\u0000\u001c\u001a\u0001\u0000"+
		"\u0000\u0000\u001c\u001d\u0001\u0000\u0000\u0000\u001d\u001e\u0001\u0000"+
		"\u0000\u0000\u001e\u001f\u0006\u0001\u0001\u0000\u001f\u0004\u0001\u0000"+
		"\u0000\u0000 \"\u0003\r\u0006\u0000! \u0001\u0000\u0000\u0000!\"\u0001"+
		"\u0000\u0000\u0000\"#\u0001\u0000\u0000\u0000#&\u0003\u000b\u0005\u0000"+
		"$&\u0003\u000f\u0007\u0000%!\u0001\u0000\u0000\u0000%$\u0001\u0000\u0000"+
		"\u0000&\'\u0001\u0000\u0000\u0000\'(\u0006\u0002\u0001\u0000(\u0006\u0001"+
		"\u0000\u0000\u0000)*\u0005^\u0000\u0000*+\u0005^\u0000\u0000+,\u0005C"+
		"\u0000\u0000,-\u0005O\u0000\u0000-.\u0005M\u0000\u0000./\u0005M\u0000"+
		"\u0000/0\u0005E\u0000\u000001\u0005N\u0000\u000012\u0005T\u0000\u0000"+
		"23\u0005^\u0000\u000034\u0005^\u0000\u000048\u0001\u0000\u0000\u00005"+
		"7\b\u0001\u0000\u000065\u0001\u0000\u0000\u00007:\u0001\u0000\u0000\u0000"+
		"86\u0001\u0000\u0000\u000089\u0001\u0000\u0000\u00009;\u0001\u0000\u0000"+
		"\u0000:8\u0001\u0000\u0000\u0000;<\u0006\u0003\u0001\u0000<\b\u0001\u0000"+
		"\u0000\u0000=>\u0005;\u0000\u0000>\n\u0001\u0000\u0000\u0000?@\u0005\n"+
		"\u0000\u0000@\f\u0001\u0000\u0000\u0000AB\u0005\r\u0000\u0000B\u000e\u0001"+
		"\u0000\u0000\u0000CD\u0005\f\u0000\u0000D\u0010\u0001\u0000\u0000\u0000"+
		"EK\u0005\'\u0000\u0000FJ\b\u0002\u0000\u0000GH\u0005\'\u0000\u0000HJ\u0005"+
		"\'\u0000\u0000IF\u0001\u0000\u0000\u0000IG\u0001\u0000\u0000\u0000JM\u0001"+
		"\u0000\u0000\u0000KI\u0001\u0000\u0000\u0000KL\u0001\u0000\u0000\u0000"+
		"LN\u0001\u0000\u0000\u0000MK\u0001\u0000\u0000\u0000NO\u0005\'\u0000\u0000"+
		"O\u0012\u0001\u0000\u0000\u0000PR\b\u0003\u0000\u0000QP\u0001\u0000\u0000"+
		"\u0000RS\u0001\u0000\u0000\u0000SQ\u0001\u0000\u0000\u0000ST\u0001\u0000"+
		"\u0000\u0000T\u0014\u0001\u0000\u0000\u0000\b\u0000\u001c!%8IKS\u0002"+
		"\u0006\u0000\u0000\u0000\u0001\u0000";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}