/*
 * For commercial customers of Moderne Inc., this repository is licensed per the terms of our contract.
 * For everyone else, this is licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International.
 * See: https://creativecommons.org/licenses/by-nc-sa/4.0/
 */
// Generated from java-escape by ANTLR 4.11.1
package org.openrewrite.controlm.internal.grammar;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
public class ControlMLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.11.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		UTF_8_BOM=1, WS=2, EOL=3, CM=4, CA_START=5, STRINGLITERAL=6, TEXT=7, CM_STRINGLITERAL=8,
		CM_TEXT=9;
	public static final int
		INSIDE_CM=1;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE", "INSIDE_CM"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"UTF_8_BOM", "WS", "EOL", "CM", "CA_START", "STRINGLITERAL", "TEXT",
			"LF", "CR", "FORM_FEED", "CM_WS", "CM_EOL", "CM_CA_START", "CM_STRINGLITERAL",
			"CM_TEXT"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'\\uFEFF'", null, null, "'^^CM^^'", "'^^CA_START^^'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "UTF_8_BOM", "WS", "EOL", "CM", "CA_START", "STRINGLITERAL", "TEXT",
			"CM_STRINGLITERAL", "CM_TEXT"
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


	public ControlMLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "ControlMLexer.g4"; }

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
		"\u0004\u0000\t\u0084\u0006\uffff\uffff\u0006\uffff\uffff\u0002\u0000\u0007"+
		"\u0000\u0002\u0001\u0007\u0001\u0002\u0002\u0007\u0002\u0002\u0003\u0007"+
		"\u0003\u0002\u0004\u0007\u0004\u0002\u0005\u0007\u0005\u0002\u0006\u0007"+
		"\u0006\u0002\u0007\u0007\u0007\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n"+
		"\u0007\n\u0002\u000b\u0007\u000b\u0002\f\u0007\f\u0002\r\u0007\r\u0002"+
		"\u000e\u0007\u000e\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001"+
		"\u0001\u0004\u0001&\b\u0001\u000b\u0001\f\u0001\'\u0001\u0001\u0001\u0001"+
		"\u0001\u0002\u0003\u0002-\b\u0002\u0001\u0002\u0001\u0002\u0003\u0002"+
		"1\b\u0002\u0001\u0002\u0001\u0002\u0001\u0003\u0001\u0003\u0001\u0003"+
		"\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003"+
		"\u0001\u0003\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004"+
		"\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004"+
		"\u0001\u0004\u0001\u0004\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005"+
		"\u0001\u0005\u0005\u0005Q\b\u0005\n\u0005\f\u0005T\t\u0005\u0001\u0005"+
		"\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0005\u0005"+
		"\\\b\u0005\n\u0005\f\u0005_\t\u0005\u0001\u0005\u0003\u0005b\b\u0005\u0001"+
		"\u0006\u0004\u0006e\b\u0006\u000b\u0006\f\u0006f\u0001\u0006\u0003\u0006"+
		"j\b\u0006\u0001\u0007\u0001\u0007\u0001\b\u0001\b\u0001\t\u0001\t\u0001"+
		"\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\u000b\u0001\u000b\u0001\u000b"+
		"\u0001\u000b\u0001\u000b\u0001\u000b\u0001\f\u0001\f\u0001\f\u0001\f\u0001"+
		"\r\u0001\r\u0001\u000e\u0001\u000e\u0000\u0000\u000f\u0002\u0001\u0004"+
		"\u0002\u0006\u0003\b\u0004\n\u0005\f\u0006\u000e\u0007\u0010\u0000\u0012"+
		"\u0000\u0014\u0000\u0016\u0000\u0018\u0000\u001a\u0000\u001c\b\u001e\t"+
		"\u0002\u0000\u0001\u0005\u0004\u0000\t\t\f\f  ;;\u0003\u0000\n\n\r\r\""+
		"\"\u0003\u0000\n\n\r\r\'\'\u0006\u0000\n\n\r\r  \"\"\'\'^^\u0003\u0000"+
		"\"\"\'\'^^\u008b\u0000\u0002\u0001\u0000\u0000\u0000\u0000\u0004\u0001"+
		"\u0000\u0000\u0000\u0000\u0006\u0001\u0000\u0000\u0000\u0000\b\u0001\u0000"+
		"\u0000\u0000\u0000\n\u0001\u0000\u0000\u0000\u0000\f\u0001\u0000\u0000"+
		"\u0000\u0000\u000e\u0001\u0000\u0000\u0000\u0001\u0016\u0001\u0000\u0000"+
		"\u0000\u0001\u0018\u0001\u0000\u0000\u0000\u0001\u001a\u0001\u0000\u0000"+
		"\u0000\u0001\u001c\u0001\u0000\u0000\u0000\u0001\u001e\u0001\u0000\u0000"+
		"\u0000\u0002 \u0001\u0000\u0000\u0000\u0004%\u0001\u0000\u0000\u0000\u0006"+
		"0\u0001\u0000\u0000\u0000\b4\u0001\u0000\u0000\u0000\n>\u0001\u0000\u0000"+
		"\u0000\fa\u0001\u0000\u0000\u0000\u000ei\u0001\u0000\u0000\u0000\u0010"+
		"k\u0001\u0000\u0000\u0000\u0012m\u0001\u0000\u0000\u0000\u0014o\u0001"+
		"\u0000\u0000\u0000\u0016q\u0001\u0000\u0000\u0000\u0018v\u0001\u0000\u0000"+
		"\u0000\u001a|\u0001\u0000\u0000\u0000\u001c\u0080\u0001\u0000\u0000\u0000"+
		"\u001e\u0082\u0001\u0000\u0000\u0000 !\u0005\u8000\ufeff\u0000\u0000!"+
		"\"\u0001\u0000\u0000\u0000\"#\u0006\u0000\u0000\u0000#\u0003\u0001\u0000"+
		"\u0000\u0000$&\u0007\u0000\u0000\u0000%$\u0001\u0000\u0000\u0000&\'\u0001"+
		"\u0000\u0000\u0000\'%\u0001\u0000\u0000\u0000\'(\u0001\u0000\u0000\u0000"+
		"()\u0001\u0000\u0000\u0000)*\u0006\u0001\u0001\u0000*\u0005\u0001\u0000"+
		"\u0000\u0000+-\u0003\u0012\b\u0000,+\u0001\u0000\u0000\u0000,-\u0001\u0000"+
		"\u0000\u0000-.\u0001\u0000\u0000\u0000.1\u0003\u0010\u0007\u0000/1\u0003"+
		"\u0014\t\u00000,\u0001\u0000\u0000\u00000/\u0001\u0000\u0000\u000012\u0001"+
		"\u0000\u0000\u000023\u0006\u0002\u0001\u00003\u0007\u0001\u0000\u0000"+
		"\u000045\u0005^\u0000\u000056\u0005^\u0000\u000067\u0005C\u0000\u0000"+
		"78\u0005M\u0000\u000089\u0005^\u0000\u00009:\u0005^\u0000\u0000:;\u0001"+
		"\u0000\u0000\u0000;<\u0006\u0003\u0000\u0000<=\u0006\u0003\u0002\u0000"+
		"=\t\u0001\u0000\u0000\u0000>?\u0005^\u0000\u0000?@\u0005^\u0000\u0000"+
		"@A\u0005C\u0000\u0000AB\u0005A\u0000\u0000BC\u0005_\u0000\u0000CD\u0005"+
		"S\u0000\u0000DE\u0005T\u0000\u0000EF\u0005A\u0000\u0000FG\u0005R\u0000"+
		"\u0000GH\u0005T\u0000\u0000HI\u0005^\u0000\u0000IJ\u0005^\u0000\u0000"+
		"J\u000b\u0001\u0000\u0000\u0000KR\u0005\"\u0000\u0000LQ\b\u0001\u0000"+
		"\u0000MN\u0005\"\u0000\u0000NQ\u0005\"\u0000\u0000OQ\u0005\'\u0000\u0000"+
		"PL\u0001\u0000\u0000\u0000PM\u0001\u0000\u0000\u0000PO\u0001\u0000\u0000"+
		"\u0000QT\u0001\u0000\u0000\u0000RP\u0001\u0000\u0000\u0000RS\u0001\u0000"+
		"\u0000\u0000SU\u0001\u0000\u0000\u0000TR\u0001\u0000\u0000\u0000Ub\u0005"+
		"\"\u0000\u0000V]\u0005\'\u0000\u0000W\\\b\u0002\u0000\u0000XY\u0005\'"+
		"\u0000\u0000Y\\\u0005\'\u0000\u0000Z\\\u0005\"\u0000\u0000[W\u0001\u0000"+
		"\u0000\u0000[X\u0001\u0000\u0000\u0000[Z\u0001\u0000\u0000\u0000\\_\u0001"+
		"\u0000\u0000\u0000][\u0001\u0000\u0000\u0000]^\u0001\u0000\u0000\u0000"+
		"^`\u0001\u0000\u0000\u0000_]\u0001\u0000\u0000\u0000`b\u0005\'\u0000\u0000"+
		"aK\u0001\u0000\u0000\u0000aV\u0001\u0000\u0000\u0000b\r\u0001\u0000\u0000"+
		"\u0000ce\b\u0003\u0000\u0000dc\u0001\u0000\u0000\u0000ef\u0001\u0000\u0000"+
		"\u0000fd\u0001\u0000\u0000\u0000fg\u0001\u0000\u0000\u0000gj\u0001\u0000"+
		"\u0000\u0000hj\u0007\u0004\u0000\u0000id\u0001\u0000\u0000\u0000ih\u0001"+
		"\u0000\u0000\u0000j\u000f\u0001\u0000\u0000\u0000kl\u0005\n\u0000\u0000"+
		"l\u0011\u0001\u0000\u0000\u0000mn\u0005\r\u0000\u0000n\u0013\u0001\u0000"+
		"\u0000\u0000op\u0005\f\u0000\u0000p\u0015\u0001\u0000\u0000\u0000qr\u0003"+
		"\u0004\u0001\u0000rs\u0001\u0000\u0000\u0000st\u0006\n\u0003\u0000tu\u0006"+
		"\n\u0001\u0000u\u0017\u0001\u0000\u0000\u0000vw\u0003\u0006\u0002\u0000"+
		"wx\u0001\u0000\u0000\u0000xy\u0006\u000b\u0004\u0000yz\u0006\u000b\u0001"+
		"\u0000z{\u0006\u000b\u0005\u0000{\u0019\u0001\u0000\u0000\u0000|}\u0003"+
		"\n\u0004\u0000}~\u0001\u0000\u0000\u0000~\u007f\u0006\f\u0006\u0000\u007f"+
		"\u001b\u0001\u0000\u0000\u0000\u0080\u0081\u0003\f\u0005\u0000\u0081\u001d"+
		"\u0001\u0000\u0000\u0000\u0082\u0083\u0003\u000e\u0006\u0000\u0083\u001f"+
		"\u0001\u0000\u0000\u0000\f\u0000\u0001\',0PR[]afi\u0007\u0006\u0000\u0000"+
		"\u0000\u0001\u0000\u0005\u0001\u0000\u0007\u0002\u0000\u0007\u0003\u0000"+
		"\u0004\u0000\u0000\u0007\u0005\u0000";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}