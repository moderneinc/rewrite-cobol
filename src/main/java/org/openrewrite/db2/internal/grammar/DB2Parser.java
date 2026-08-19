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
// Generated from /Users/jon/Projects/github/openrewrite/rewrite-cobol/.claude/worktrees/ims-or-cics/src/main/antlr-db2/DB2Parser.g4 by ANTLR 4.13.2
package org.openrewrite.db2.internal.grammar;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue", "this-escape"})
public class DB2Parser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		WS=1, LINE_COMMENT=2, BLOCK_COMMENT=3, CREATE=4, TABLE=5, INDEX=6, UNIQUE=7, 
		ALTER=8, ADD=9, ON=10, IN=11, PRIMARY=12, FOREIGN=13, KEY=14, REFERENCES=15, 
		CONSTRAINT=16, CHECK=17, NOT=18, NULL=19, WHERE=20, ASC=21, DESC=22, RANDOM=23, 
		LPAREN=24, RPAREN=25, COMMA=26, SEMI=27, DOT=28, STRING=29, DELIMITED_IDENTIFIER=30, 
		PLACEHOLDER=31, NUMBER=32, IDENTIFIER=33, OTHER=34;
	public static final int
		RULE_compilationUnit = 0, RULE_statement = 1, RULE_createTable = 2, RULE_tableElement = 3, 
		RULE_columnDefinition = 4, RULE_dataType = 5, RULE_columnAttribute = 6, 
		RULE_tableConstraint = 7, RULE_constraintBody = 8, RULE_constraintOption = 9, 
		RULE_columnList = 10, RULE_tableOption = 11, RULE_createIndex = 12, RULE_indexModifier = 13, 
		RULE_indexKey = 14, RULE_indexOption = 15, RULE_alterTable = 16, RULE_alterAction = 17, 
		RULE_unknownStatement = 18, RULE_unknownStart = 19, RULE_qualifiedName = 20, 
		RULE_identifier = 21, RULE_water = 22, RULE_elementWater = 23, RULE_parenGroup = 24;
	private static String[] makeRuleNames() {
		return new String[] {
			"compilationUnit", "statement", "createTable", "tableElement", "columnDefinition", 
			"dataType", "columnAttribute", "tableConstraint", "constraintBody", "constraintOption", 
			"columnList", "tableOption", "createIndex", "indexModifier", "indexKey", 
			"indexOption", "alterTable", "alterAction", "unknownStatement", "unknownStart", 
			"qualifiedName", "identifier", "water", "elementWater", "parenGroup"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, null, null, null, "'CREATE'", "'TABLE'", "'INDEX'", "'UNIQUE'", 
			"'ALTER'", "'ADD'", "'ON'", "'IN'", "'PRIMARY'", "'FOREIGN'", "'KEY'", 
			"'REFERENCES'", "'CONSTRAINT'", "'CHECK'", "'NOT'", "'NULL'", "'WHERE'", 
			"'ASC'", "'DESC'", "'RANDOM'", "'('", "')'", "','", "';'", "'.'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "WS", "LINE_COMMENT", "BLOCK_COMMENT", "CREATE", "TABLE", "INDEX", 
			"UNIQUE", "ALTER", "ADD", "ON", "IN", "PRIMARY", "FOREIGN", "KEY", "REFERENCES", 
			"CONSTRAINT", "CHECK", "NOT", "NULL", "WHERE", "ASC", "DESC", "RANDOM", 
			"LPAREN", "RPAREN", "COMMA", "SEMI", "DOT", "STRING", "DELIMITED_IDENTIFIER", 
			"PLACEHOLDER", "NUMBER", "IDENTIFIER", "OTHER"
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

	@Override
	public String getGrammarFileName() { return "DB2Parser.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public DB2Parser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CompilationUnitContext extends ParserRuleContext {
		public TerminalNode EOF() { return getToken(DB2Parser.EOF, 0); }
		public List<StatementContext> statement() {
			return getRuleContexts(StatementContext.class);
		}
		public StatementContext statement(int i) {
			return getRuleContext(StatementContext.class,i);
		}
		public CompilationUnitContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_compilationUnit; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DB2ParserListener ) ((DB2ParserListener)listener).enterCompilationUnit(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DB2ParserListener ) ((DB2ParserListener)listener).exitCompilationUnit(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DB2ParserVisitor ) return ((DB2ParserVisitor<? extends T>)visitor).visitCompilationUnit(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CompilationUnitContext compilationUnit() throws RecognitionException {
		CompilationUnitContext _localctx = new CompilationUnitContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_compilationUnit);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(53);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 34326183934L) != 0)) {
				{
				{
				setState(50);
				statement();
				}
				}
				setState(55);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(56);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class StatementContext extends ParserRuleContext {
		public CreateTableContext createTable() {
			return getRuleContext(CreateTableContext.class,0);
		}
		public CreateIndexContext createIndex() {
			return getRuleContext(CreateIndexContext.class,0);
		}
		public AlterTableContext alterTable() {
			return getRuleContext(AlterTableContext.class,0);
		}
		public UnknownStatementContext unknownStatement() {
			return getRuleContext(UnknownStatementContext.class,0);
		}
		public StatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DB2ParserListener ) ((DB2ParserListener)listener).enterStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DB2ParserListener ) ((DB2ParserListener)listener).exitStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DB2ParserVisitor ) return ((DB2ParserVisitor<? extends T>)visitor).visitStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StatementContext statement() throws RecognitionException {
		StatementContext _localctx = new StatementContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_statement);
		try {
			setState(62);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(58);
				createTable();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(59);
				createIndex();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(60);
				alterTable();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(61);
				unknownStatement();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CreateTableContext extends ParserRuleContext {
		public TerminalNode CREATE() { return getToken(DB2Parser.CREATE, 0); }
		public TerminalNode TABLE() { return getToken(DB2Parser.TABLE, 0); }
		public QualifiedNameContext qualifiedName() {
			return getRuleContext(QualifiedNameContext.class,0);
		}
		public TerminalNode LPAREN() { return getToken(DB2Parser.LPAREN, 0); }
		public List<TableElementContext> tableElement() {
			return getRuleContexts(TableElementContext.class);
		}
		public TableElementContext tableElement(int i) {
			return getRuleContext(TableElementContext.class,i);
		}
		public TerminalNode RPAREN() { return getToken(DB2Parser.RPAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(DB2Parser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(DB2Parser.COMMA, i);
		}
		public List<TableOptionContext> tableOption() {
			return getRuleContexts(TableOptionContext.class);
		}
		public TableOptionContext tableOption(int i) {
			return getRuleContext(TableOptionContext.class,i);
		}
		public TerminalNode SEMI() { return getToken(DB2Parser.SEMI, 0); }
		public CreateTableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_createTable; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DB2ParserListener ) ((DB2ParserListener)listener).enterCreateTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DB2ParserListener ) ((DB2ParserListener)listener).exitCreateTable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DB2ParserVisitor ) return ((DB2ParserVisitor<? extends T>)visitor).visitCreateTable(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CreateTableContext createTable() throws RecognitionException {
		CreateTableContext _localctx = new CreateTableContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_createTable);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(64);
			match(CREATE);
			setState(65);
			match(TABLE);
			setState(66);
			qualifiedName();
			setState(67);
			match(LPAREN);
			setState(68);
			tableElement();
			setState(73);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(69);
				match(COMMA);
				setState(70);
				tableElement();
				}
				}
				setState(75);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(76);
			match(RPAREN);
			setState(80);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,3,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(77);
					tableOption();
					}
					} 
				}
				setState(82);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,3,_ctx);
			}
			setState(84);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
			case 1:
				{
				setState(83);
				match(SEMI);
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TableElementContext extends ParserRuleContext {
		public TableConstraintContext tableConstraint() {
			return getRuleContext(TableConstraintContext.class,0);
		}
		public ColumnDefinitionContext columnDefinition() {
			return getRuleContext(ColumnDefinitionContext.class,0);
		}
		public TableElementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tableElement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DB2ParserListener ) ((DB2ParserListener)listener).enterTableElement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DB2ParserListener ) ((DB2ParserListener)listener).exitTableElement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DB2ParserVisitor ) return ((DB2ParserVisitor<? extends T>)visitor).visitTableElement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TableElementContext tableElement() throws RecognitionException {
		TableElementContext _localctx = new TableElementContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_tableElement);
		try {
			setState(88);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case UNIQUE:
			case PRIMARY:
			case FOREIGN:
			case CONSTRAINT:
			case CHECK:
				enterOuterAlt(_localctx, 1);
				{
				setState(86);
				tableConstraint();
				}
				break;
			case DELIMITED_IDENTIFIER:
			case PLACEHOLDER:
			case IDENTIFIER:
				enterOuterAlt(_localctx, 2);
				{
				setState(87);
				columnDefinition();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ColumnDefinitionContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public DataTypeContext dataType() {
			return getRuleContext(DataTypeContext.class,0);
		}
		public List<ColumnAttributeContext> columnAttribute() {
			return getRuleContexts(ColumnAttributeContext.class);
		}
		public ColumnAttributeContext columnAttribute(int i) {
			return getRuleContext(ColumnAttributeContext.class,i);
		}
		public ColumnDefinitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_columnDefinition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DB2ParserListener ) ((DB2ParserListener)listener).enterColumnDefinition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DB2ParserListener ) ((DB2ParserListener)listener).exitColumnDefinition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DB2ParserVisitor ) return ((DB2ParserVisitor<? extends T>)visitor).visitColumnDefinition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ColumnDefinitionContext columnDefinition() throws RecognitionException {
		ColumnDefinitionContext _localctx = new ColumnDefinitionContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_columnDefinition);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(90);
			identifier();
			setState(91);
			dataType();
			setState(95);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 34124857070L) != 0)) {
				{
				{
				setState(92);
				columnAttribute();
				}
				}
				setState(97);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class DataTypeContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode LPAREN() { return getToken(DB2Parser.LPAREN, 0); }
		public List<TerminalNode> NUMBER() { return getTokens(DB2Parser.NUMBER); }
		public TerminalNode NUMBER(int i) {
			return getToken(DB2Parser.NUMBER, i);
		}
		public TerminalNode RPAREN() { return getToken(DB2Parser.RPAREN, 0); }
		public TerminalNode COMMA() { return getToken(DB2Parser.COMMA, 0); }
		public DataTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dataType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DB2ParserListener ) ((DB2ParserListener)listener).enterDataType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DB2ParserListener ) ((DB2ParserListener)listener).exitDataType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DB2ParserVisitor ) return ((DB2ParserVisitor<? extends T>)visitor).visitDataType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DataTypeContext dataType() throws RecognitionException {
		DataTypeContext _localctx = new DataTypeContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_dataType);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(98);
			identifier();
			setState(106);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,8,_ctx) ) {
			case 1:
				{
				setState(99);
				match(LPAREN);
				setState(100);
				match(NUMBER);
				setState(103);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(101);
					match(COMMA);
					setState(102);
					match(NUMBER);
					}
				}

				setState(105);
				match(RPAREN);
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ColumnAttributeContext extends ParserRuleContext {
		public TerminalNode NOT() { return getToken(DB2Parser.NOT, 0); }
		public TerminalNode NULL() { return getToken(DB2Parser.NULL, 0); }
		public ElementWaterContext elementWater() {
			return getRuleContext(ElementWaterContext.class,0);
		}
		public ColumnAttributeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_columnAttribute; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DB2ParserListener ) ((DB2ParserListener)listener).enterColumnAttribute(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DB2ParserListener ) ((DB2ParserListener)listener).exitColumnAttribute(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DB2ParserVisitor ) return ((DB2ParserVisitor<? extends T>)visitor).visitColumnAttribute(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ColumnAttributeContext columnAttribute() throws RecognitionException {
		ColumnAttributeContext _localctx = new ColumnAttributeContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_columnAttribute);
		try {
			setState(112);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,9,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(108);
				match(NOT);
				setState(109);
				match(NULL);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(110);
				match(NULL);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(111);
				elementWater();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TableConstraintContext extends ParserRuleContext {
		public ConstraintBodyContext constraintBody() {
			return getRuleContext(ConstraintBodyContext.class,0);
		}
		public TerminalNode CONSTRAINT() { return getToken(DB2Parser.CONSTRAINT, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TableConstraintContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tableConstraint; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DB2ParserListener ) ((DB2ParserListener)listener).enterTableConstraint(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DB2ParserListener ) ((DB2ParserListener)listener).exitTableConstraint(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DB2ParserVisitor ) return ((DB2ParserVisitor<? extends T>)visitor).visitTableConstraint(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TableConstraintContext tableConstraint() throws RecognitionException {
		TableConstraintContext _localctx = new TableConstraintContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_tableConstraint);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(116);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==CONSTRAINT) {
				{
				setState(114);
				match(CONSTRAINT);
				setState(115);
				identifier();
				}
			}

			setState(118);
			constraintBody();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ConstraintBodyContext extends ParserRuleContext {
		public TerminalNode PRIMARY() { return getToken(DB2Parser.PRIMARY, 0); }
		public TerminalNode KEY() { return getToken(DB2Parser.KEY, 0); }
		public List<ColumnListContext> columnList() {
			return getRuleContexts(ColumnListContext.class);
		}
		public ColumnListContext columnList(int i) {
			return getRuleContext(ColumnListContext.class,i);
		}
		public List<ConstraintOptionContext> constraintOption() {
			return getRuleContexts(ConstraintOptionContext.class);
		}
		public ConstraintOptionContext constraintOption(int i) {
			return getRuleContext(ConstraintOptionContext.class,i);
		}
		public TerminalNode FOREIGN() { return getToken(DB2Parser.FOREIGN, 0); }
		public TerminalNode REFERENCES() { return getToken(DB2Parser.REFERENCES, 0); }
		public QualifiedNameContext qualifiedName() {
			return getRuleContext(QualifiedNameContext.class,0);
		}
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode UNIQUE() { return getToken(DB2Parser.UNIQUE, 0); }
		public TerminalNode CHECK() { return getToken(DB2Parser.CHECK, 0); }
		public ParenGroupContext parenGroup() {
			return getRuleContext(ParenGroupContext.class,0);
		}
		public ConstraintBodyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_constraintBody; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DB2ParserListener ) ((DB2ParserListener)listener).enterConstraintBody(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DB2ParserListener ) ((DB2ParserListener)listener).exitConstraintBody(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DB2ParserVisitor ) return ((DB2ParserVisitor<? extends T>)visitor).visitConstraintBody(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ConstraintBodyContext constraintBody() throws RecognitionException {
		ConstraintBodyContext _localctx = new ConstraintBodyContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_constraintBody);
		int _la;
		try {
			int _alt;
			setState(162);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case PRIMARY:
				enterOuterAlt(_localctx, 1);
				{
				setState(120);
				match(PRIMARY);
				setState(121);
				match(KEY);
				setState(122);
				columnList();
				setState(126);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,11,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(123);
						constraintOption();
						}
						} 
					}
					setState(128);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,11,_ctx);
				}
				}
				break;
			case FOREIGN:
				enterOuterAlt(_localctx, 2);
				{
				setState(129);
				match(FOREIGN);
				setState(130);
				match(KEY);
				setState(132);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 11811160064L) != 0)) {
					{
					setState(131);
					identifier();
					}
				}

				setState(134);
				columnList();
				setState(135);
				match(REFERENCES);
				setState(136);
				qualifiedName();
				setState(138);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,13,_ctx) ) {
				case 1:
					{
					setState(137);
					columnList();
					}
					break;
				}
				setState(143);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,14,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(140);
						constraintOption();
						}
						} 
					}
					setState(145);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,14,_ctx);
				}
				}
				break;
			case UNIQUE:
				enterOuterAlt(_localctx, 3);
				{
				setState(146);
				match(UNIQUE);
				setState(147);
				columnList();
				setState(151);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,15,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(148);
						constraintOption();
						}
						} 
					}
					setState(153);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,15,_ctx);
				}
				}
				break;
			case CHECK:
				enterOuterAlt(_localctx, 4);
				{
				setState(154);
				match(CHECK);
				setState(155);
				parenGroup();
				setState(159);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,16,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(156);
						constraintOption();
						}
						} 
					}
					setState(161);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,16,_ctx);
				}
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ConstraintOptionContext extends ParserRuleContext {
		public ElementWaterContext elementWater() {
			return getRuleContext(ElementWaterContext.class,0);
		}
		public ConstraintOptionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_constraintOption; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DB2ParserListener ) ((DB2ParserListener)listener).enterConstraintOption(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DB2ParserListener ) ((DB2ParserListener)listener).exitConstraintOption(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DB2ParserVisitor ) return ((DB2ParserVisitor<? extends T>)visitor).visitConstraintOption(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ConstraintOptionContext constraintOption() throws RecognitionException {
		ConstraintOptionContext _localctx = new ConstraintOptionContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_constraintOption);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(164);
			elementWater();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ColumnListContext extends ParserRuleContext {
		public TerminalNode LPAREN() { return getToken(DB2Parser.LPAREN, 0); }
		public List<IdentifierContext> identifier() {
			return getRuleContexts(IdentifierContext.class);
		}
		public IdentifierContext identifier(int i) {
			return getRuleContext(IdentifierContext.class,i);
		}
		public TerminalNode RPAREN() { return getToken(DB2Parser.RPAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(DB2Parser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(DB2Parser.COMMA, i);
		}
		public ColumnListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_columnList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DB2ParserListener ) ((DB2ParserListener)listener).enterColumnList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DB2ParserListener ) ((DB2ParserListener)listener).exitColumnList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DB2ParserVisitor ) return ((DB2ParserVisitor<? extends T>)visitor).visitColumnList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ColumnListContext columnList() throws RecognitionException {
		ColumnListContext _localctx = new ColumnListContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_columnList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(166);
			match(LPAREN);
			setState(167);
			identifier();
			setState(172);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(168);
				match(COMMA);
				setState(169);
				identifier();
				}
				}
				setState(174);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(175);
			match(RPAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TableOptionContext extends ParserRuleContext {
		public TerminalNode IN() { return getToken(DB2Parser.IN, 0); }
		public QualifiedNameContext qualifiedName() {
			return getRuleContext(QualifiedNameContext.class,0);
		}
		public WaterContext water() {
			return getRuleContext(WaterContext.class,0);
		}
		public TableOptionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tableOption; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DB2ParserListener ) ((DB2ParserListener)listener).enterTableOption(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DB2ParserListener ) ((DB2ParserListener)listener).exitTableOption(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DB2ParserVisitor ) return ((DB2ParserVisitor<? extends T>)visitor).visitTableOption(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TableOptionContext tableOption() throws RecognitionException {
		TableOptionContext _localctx = new TableOptionContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_tableOption);
		try {
			setState(180);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,19,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(177);
				match(IN);
				setState(178);
				qualifiedName();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(179);
				water();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CreateIndexContext extends ParserRuleContext {
		public TerminalNode CREATE() { return getToken(DB2Parser.CREATE, 0); }
		public TerminalNode INDEX() { return getToken(DB2Parser.INDEX, 0); }
		public List<QualifiedNameContext> qualifiedName() {
			return getRuleContexts(QualifiedNameContext.class);
		}
		public QualifiedNameContext qualifiedName(int i) {
			return getRuleContext(QualifiedNameContext.class,i);
		}
		public TerminalNode ON() { return getToken(DB2Parser.ON, 0); }
		public TerminalNode LPAREN() { return getToken(DB2Parser.LPAREN, 0); }
		public List<IndexKeyContext> indexKey() {
			return getRuleContexts(IndexKeyContext.class);
		}
		public IndexKeyContext indexKey(int i) {
			return getRuleContext(IndexKeyContext.class,i);
		}
		public TerminalNode RPAREN() { return getToken(DB2Parser.RPAREN, 0); }
		public List<IndexModifierContext> indexModifier() {
			return getRuleContexts(IndexModifierContext.class);
		}
		public IndexModifierContext indexModifier(int i) {
			return getRuleContext(IndexModifierContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(DB2Parser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(DB2Parser.COMMA, i);
		}
		public List<IndexOptionContext> indexOption() {
			return getRuleContexts(IndexOptionContext.class);
		}
		public IndexOptionContext indexOption(int i) {
			return getRuleContext(IndexOptionContext.class,i);
		}
		public TerminalNode SEMI() { return getToken(DB2Parser.SEMI, 0); }
		public CreateIndexContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_createIndex; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DB2ParserListener ) ((DB2ParserListener)listener).enterCreateIndex(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DB2ParserListener ) ((DB2ParserListener)listener).exitCreateIndex(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DB2ParserVisitor ) return ((DB2ParserVisitor<? extends T>)visitor).visitCreateIndex(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CreateIndexContext createIndex() throws RecognitionException {
		CreateIndexContext _localctx = new CreateIndexContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_createIndex);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(182);
			match(CREATE);
			setState(186);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 1835136L) != 0)) {
				{
				{
				setState(183);
				indexModifier();
				}
				}
				setState(188);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(189);
			match(INDEX);
			setState(190);
			qualifiedName();
			setState(191);
			match(ON);
			setState(192);
			qualifiedName();
			setState(193);
			match(LPAREN);
			setState(194);
			indexKey();
			setState(199);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(195);
				match(COMMA);
				setState(196);
				indexKey();
				}
				}
				setState(201);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(202);
			match(RPAREN);
			setState(206);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,22,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(203);
					indexOption();
					}
					} 
				}
				setState(208);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,22,_ctx);
			}
			setState(210);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,23,_ctx) ) {
			case 1:
				{
				setState(209);
				match(SEMI);
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class IndexModifierContext extends ParserRuleContext {
		public TerminalNode UNIQUE() { return getToken(DB2Parser.UNIQUE, 0); }
		public TerminalNode WHERE() { return getToken(DB2Parser.WHERE, 0); }
		public TerminalNode NOT() { return getToken(DB2Parser.NOT, 0); }
		public TerminalNode NULL() { return getToken(DB2Parser.NULL, 0); }
		public IndexModifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_indexModifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DB2ParserListener ) ((DB2ParserListener)listener).enterIndexModifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DB2ParserListener ) ((DB2ParserListener)listener).exitIndexModifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DB2ParserVisitor ) return ((DB2ParserVisitor<? extends T>)visitor).visitIndexModifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IndexModifierContext indexModifier() throws RecognitionException {
		IndexModifierContext _localctx = new IndexModifierContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_indexModifier);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(212);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 1835136L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class IndexKeyContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode ASC() { return getToken(DB2Parser.ASC, 0); }
		public TerminalNode DESC() { return getToken(DB2Parser.DESC, 0); }
		public TerminalNode RANDOM() { return getToken(DB2Parser.RANDOM, 0); }
		public IndexKeyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_indexKey; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DB2ParserListener ) ((DB2ParserListener)listener).enterIndexKey(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DB2ParserListener ) ((DB2ParserListener)listener).exitIndexKey(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DB2ParserVisitor ) return ((DB2ParserVisitor<? extends T>)visitor).visitIndexKey(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IndexKeyContext indexKey() throws RecognitionException {
		IndexKeyContext _localctx = new IndexKeyContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_indexKey);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(214);
			identifier();
			setState(216);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 14680064L) != 0)) {
				{
				setState(215);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 14680064L) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class IndexOptionContext extends ParserRuleContext {
		public WaterContext water() {
			return getRuleContext(WaterContext.class,0);
		}
		public IndexOptionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_indexOption; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DB2ParserListener ) ((DB2ParserListener)listener).enterIndexOption(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DB2ParserListener ) ((DB2ParserListener)listener).exitIndexOption(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DB2ParserVisitor ) return ((DB2ParserVisitor<? extends T>)visitor).visitIndexOption(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IndexOptionContext indexOption() throws RecognitionException {
		IndexOptionContext _localctx = new IndexOptionContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_indexOption);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(218);
			water();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AlterTableContext extends ParserRuleContext {
		public TerminalNode ALTER() { return getToken(DB2Parser.ALTER, 0); }
		public TerminalNode TABLE() { return getToken(DB2Parser.TABLE, 0); }
		public QualifiedNameContext qualifiedName() {
			return getRuleContext(QualifiedNameContext.class,0);
		}
		public List<AlterActionContext> alterAction() {
			return getRuleContexts(AlterActionContext.class);
		}
		public AlterActionContext alterAction(int i) {
			return getRuleContext(AlterActionContext.class,i);
		}
		public TerminalNode SEMI() { return getToken(DB2Parser.SEMI, 0); }
		public AlterTableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_alterTable; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DB2ParserListener ) ((DB2ParserListener)listener).enterAlterTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DB2ParserListener ) ((DB2ParserListener)listener).exitAlterTable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DB2ParserVisitor ) return ((DB2ParserVisitor<? extends T>)visitor).visitAlterTable(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AlterTableContext alterTable() throws RecognitionException {
		AlterTableContext _localctx = new AlterTableContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_alterTable);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(220);
			match(ALTER);
			setState(221);
			match(TABLE);
			setState(222);
			qualifiedName();
			setState(224); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(223);
					alterAction();
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(226); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,25,_ctx);
			} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
			setState(229);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,26,_ctx) ) {
			case 1:
				{
				setState(228);
				match(SEMI);
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AlterActionContext extends ParserRuleContext {
		public TableConstraintContext tableConstraint() {
			return getRuleContext(TableConstraintContext.class,0);
		}
		public TerminalNode ADD() { return getToken(DB2Parser.ADD, 0); }
		public WaterContext water() {
			return getRuleContext(WaterContext.class,0);
		}
		public AlterActionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_alterAction; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DB2ParserListener ) ((DB2ParserListener)listener).enterAlterAction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DB2ParserListener ) ((DB2ParserListener)listener).exitAlterAction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DB2ParserVisitor ) return ((DB2ParserVisitor<? extends T>)visitor).visitAlterAction(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AlterActionContext alterAction() throws RecognitionException {
		AlterActionContext _localctx = new AlterActionContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_alterAction);
		int _la;
		try {
			setState(236);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,28,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(232);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ADD) {
					{
					setState(231);
					match(ADD);
					}
				}

				setState(234);
				tableConstraint();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(235);
				water();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class UnknownStatementContext extends ParserRuleContext {
		public UnknownStartContext unknownStart() {
			return getRuleContext(UnknownStartContext.class,0);
		}
		public List<WaterContext> water() {
			return getRuleContexts(WaterContext.class);
		}
		public WaterContext water(int i) {
			return getRuleContext(WaterContext.class,i);
		}
		public TerminalNode SEMI() { return getToken(DB2Parser.SEMI, 0); }
		public UnknownStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_unknownStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DB2ParserListener ) ((DB2ParserListener)listener).enterUnknownStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DB2ParserListener ) ((DB2ParserListener)listener).exitUnknownStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DB2ParserVisitor ) return ((DB2ParserVisitor<? extends T>)visitor).visitUnknownStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UnknownStatementContext unknownStatement() throws RecognitionException {
		UnknownStatementContext _localctx = new UnknownStatementContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_unknownStatement);
		try {
			int _alt;
			setState(249);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case WS:
			case LINE_COMMENT:
			case BLOCK_COMMENT:
			case CREATE:
			case TABLE:
			case INDEX:
			case UNIQUE:
			case ALTER:
			case ADD:
			case ON:
			case IN:
			case PRIMARY:
			case FOREIGN:
			case KEY:
			case REFERENCES:
			case CONSTRAINT:
			case CHECK:
			case NOT:
			case NULL:
			case WHERE:
			case ASC:
			case DESC:
			case RANDOM:
			case LPAREN:
			case COMMA:
			case DOT:
			case STRING:
			case DELIMITED_IDENTIFIER:
			case PLACEHOLDER:
			case NUMBER:
			case IDENTIFIER:
			case OTHER:
				enterOuterAlt(_localctx, 1);
				{
				setState(238);
				unknownStart();
				setState(242);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,29,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(239);
						water();
						}
						} 
					}
					setState(244);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,29,_ctx);
				}
				setState(246);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,30,_ctx) ) {
				case 1:
					{
					setState(245);
					match(SEMI);
					}
					break;
				}
				}
				break;
			case SEMI:
				enterOuterAlt(_localctx, 2);
				{
				setState(248);
				match(SEMI);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class UnknownStartContext extends ParserRuleContext {
		public TerminalNode CREATE() { return getToken(DB2Parser.CREATE, 0); }
		public TerminalNode TABLE() { return getToken(DB2Parser.TABLE, 0); }
		public TerminalNode INDEX() { return getToken(DB2Parser.INDEX, 0); }
		public TerminalNode UNIQUE() { return getToken(DB2Parser.UNIQUE, 0); }
		public TerminalNode SEMI() { return getToken(DB2Parser.SEMI, 0); }
		public TerminalNode ALTER() { return getToken(DB2Parser.ALTER, 0); }
		public WaterContext water() {
			return getRuleContext(WaterContext.class,0);
		}
		public UnknownStartContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_unknownStart; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DB2ParserListener ) ((DB2ParserListener)listener).enterUnknownStart(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DB2ParserListener ) ((DB2ParserListener)listener).exitUnknownStart(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DB2ParserVisitor ) return ((DB2ParserVisitor<? extends T>)visitor).visitUnknownStart(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UnknownStartContext unknownStart() throws RecognitionException {
		UnknownStartContext _localctx = new UnknownStartContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_unknownStart);
		int _la;
		try {
			setState(256);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case CREATE:
				enterOuterAlt(_localctx, 1);
				{
				setState(251);
				match(CREATE);
				setState(252);
				_la = _input.LA(1);
				if ( _la <= 0 || ((((_la) & ~0x3f) == 0 && ((1L << _la) & 134217952L) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case ALTER:
				enterOuterAlt(_localctx, 2);
				{
				setState(253);
				match(ALTER);
				setState(254);
				_la = _input.LA(1);
				if ( _la <= 0 || (_la==TABLE || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case WS:
			case LINE_COMMENT:
			case BLOCK_COMMENT:
			case TABLE:
			case INDEX:
			case UNIQUE:
			case ADD:
			case ON:
			case IN:
			case PRIMARY:
			case FOREIGN:
			case KEY:
			case REFERENCES:
			case CONSTRAINT:
			case CHECK:
			case NOT:
			case NULL:
			case WHERE:
			case ASC:
			case DESC:
			case RANDOM:
			case LPAREN:
			case COMMA:
			case DOT:
			case STRING:
			case DELIMITED_IDENTIFIER:
			case PLACEHOLDER:
			case NUMBER:
			case IDENTIFIER:
			case OTHER:
				enterOuterAlt(_localctx, 3);
				{
				setState(255);
				water();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class QualifiedNameContext extends ParserRuleContext {
		public List<IdentifierContext> identifier() {
			return getRuleContexts(IdentifierContext.class);
		}
		public IdentifierContext identifier(int i) {
			return getRuleContext(IdentifierContext.class,i);
		}
		public List<TerminalNode> DOT() { return getTokens(DB2Parser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(DB2Parser.DOT, i);
		}
		public QualifiedNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_qualifiedName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DB2ParserListener ) ((DB2ParserListener)listener).enterQualifiedName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DB2ParserListener ) ((DB2ParserListener)listener).exitQualifiedName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DB2ParserVisitor ) return ((DB2ParserVisitor<? extends T>)visitor).visitQualifiedName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final QualifiedNameContext qualifiedName() throws RecognitionException {
		QualifiedNameContext _localctx = new QualifiedNameContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_qualifiedName);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(258);
			identifier();
			setState(263);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,33,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(259);
					match(DOT);
					setState(260);
					identifier();
					}
					} 
				}
				setState(265);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,33,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class IdentifierContext extends ParserRuleContext {
		public TerminalNode IDENTIFIER() { return getToken(DB2Parser.IDENTIFIER, 0); }
		public TerminalNode DELIMITED_IDENTIFIER() { return getToken(DB2Parser.DELIMITED_IDENTIFIER, 0); }
		public TerminalNode PLACEHOLDER() { return getToken(DB2Parser.PLACEHOLDER, 0); }
		public IdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_identifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DB2ParserListener ) ((DB2ParserListener)listener).enterIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DB2ParserListener ) ((DB2ParserListener)listener).exitIdentifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DB2ParserVisitor ) return ((DB2ParserVisitor<? extends T>)visitor).visitIdentifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IdentifierContext identifier() throws RecognitionException {
		IdentifierContext _localctx = new IdentifierContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_identifier);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(266);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 11811160064L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class WaterContext extends ParserRuleContext {
		public TerminalNode SEMI() { return getToken(DB2Parser.SEMI, 0); }
		public TerminalNode LPAREN() { return getToken(DB2Parser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(DB2Parser.RPAREN, 0); }
		public TerminalNode CREATE() { return getToken(DB2Parser.CREATE, 0); }
		public TerminalNode ALTER() { return getToken(DB2Parser.ALTER, 0); }
		public ParenGroupContext parenGroup() {
			return getRuleContext(ParenGroupContext.class,0);
		}
		public WaterContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_water; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DB2ParserListener ) ((DB2ParserListener)listener).enterWater(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DB2ParserListener ) ((DB2ParserListener)listener).exitWater(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DB2ParserVisitor ) return ((DB2ParserVisitor<? extends T>)visitor).visitWater(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WaterContext water() throws RecognitionException {
		WaterContext _localctx = new WaterContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_water);
		int _la;
		try {
			setState(270);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case WS:
			case LINE_COMMENT:
			case BLOCK_COMMENT:
			case TABLE:
			case INDEX:
			case UNIQUE:
			case ADD:
			case ON:
			case IN:
			case PRIMARY:
			case FOREIGN:
			case KEY:
			case REFERENCES:
			case CONSTRAINT:
			case CHECK:
			case NOT:
			case NULL:
			case WHERE:
			case ASC:
			case DESC:
			case RANDOM:
			case COMMA:
			case DOT:
			case STRING:
			case DELIMITED_IDENTIFIER:
			case PLACEHOLDER:
			case NUMBER:
			case IDENTIFIER:
			case OTHER:
				enterOuterAlt(_localctx, 1);
				{
				setState(268);
				_la = _input.LA(1);
				if ( _la <= 0 || ((((_la) & ~0x3f) == 0 && ((1L << _la) & 184549648L) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case LPAREN:
				enterOuterAlt(_localctx, 2);
				{
				setState(269);
				parenGroup();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ElementWaterContext extends ParserRuleContext {
		public TerminalNode COMMA() { return getToken(DB2Parser.COMMA, 0); }
		public TerminalNode SEMI() { return getToken(DB2Parser.SEMI, 0); }
		public TerminalNode LPAREN() { return getToken(DB2Parser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(DB2Parser.RPAREN, 0); }
		public TerminalNode CREATE() { return getToken(DB2Parser.CREATE, 0); }
		public TerminalNode ALTER() { return getToken(DB2Parser.ALTER, 0); }
		public ParenGroupContext parenGroup() {
			return getRuleContext(ParenGroupContext.class,0);
		}
		public ElementWaterContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_elementWater; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DB2ParserListener ) ((DB2ParserListener)listener).enterElementWater(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DB2ParserListener ) ((DB2ParserListener)listener).exitElementWater(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DB2ParserVisitor ) return ((DB2ParserVisitor<? extends T>)visitor).visitElementWater(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ElementWaterContext elementWater() throws RecognitionException {
		ElementWaterContext _localctx = new ElementWaterContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_elementWater);
		int _la;
		try {
			setState(274);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case WS:
			case LINE_COMMENT:
			case BLOCK_COMMENT:
			case TABLE:
			case INDEX:
			case UNIQUE:
			case ADD:
			case ON:
			case IN:
			case PRIMARY:
			case FOREIGN:
			case KEY:
			case REFERENCES:
			case CONSTRAINT:
			case CHECK:
			case NOT:
			case NULL:
			case WHERE:
			case ASC:
			case DESC:
			case RANDOM:
			case DOT:
			case STRING:
			case DELIMITED_IDENTIFIER:
			case PLACEHOLDER:
			case NUMBER:
			case IDENTIFIER:
			case OTHER:
				enterOuterAlt(_localctx, 1);
				{
				setState(272);
				_la = _input.LA(1);
				if ( _la <= 0 || ((((_la) & ~0x3f) == 0 && ((1L << _la) & 251658512L) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case LPAREN:
				enterOuterAlt(_localctx, 2);
				{
				setState(273);
				parenGroup();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ParenGroupContext extends ParserRuleContext {
		public List<TerminalNode> LPAREN() { return getTokens(DB2Parser.LPAREN); }
		public TerminalNode LPAREN(int i) {
			return getToken(DB2Parser.LPAREN, i);
		}
		public List<TerminalNode> RPAREN() { return getTokens(DB2Parser.RPAREN); }
		public TerminalNode RPAREN(int i) {
			return getToken(DB2Parser.RPAREN, i);
		}
		public List<ParenGroupContext> parenGroup() {
			return getRuleContexts(ParenGroupContext.class);
		}
		public ParenGroupContext parenGroup(int i) {
			return getRuleContext(ParenGroupContext.class,i);
		}
		public ParenGroupContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_parenGroup; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DB2ParserListener ) ((DB2ParserListener)listener).enterParenGroup(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DB2ParserListener ) ((DB2ParserListener)listener).exitParenGroup(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DB2ParserVisitor ) return ((DB2ParserVisitor<? extends T>)visitor).visitParenGroup(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ParenGroupContext parenGroup() throws RecognitionException {
		ParenGroupContext _localctx = new ParenGroupContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_parenGroup);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(276);
			match(LPAREN);
			setState(281);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 34326183934L) != 0)) {
				{
				setState(279);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case WS:
				case LINE_COMMENT:
				case BLOCK_COMMENT:
				case CREATE:
				case TABLE:
				case INDEX:
				case UNIQUE:
				case ALTER:
				case ADD:
				case ON:
				case IN:
				case PRIMARY:
				case FOREIGN:
				case KEY:
				case REFERENCES:
				case CONSTRAINT:
				case CHECK:
				case NOT:
				case NULL:
				case WHERE:
				case ASC:
				case DESC:
				case RANDOM:
				case COMMA:
				case SEMI:
				case DOT:
				case STRING:
				case DELIMITED_IDENTIFIER:
				case PLACEHOLDER:
				case NUMBER:
				case IDENTIFIER:
				case OTHER:
					{
					setState(277);
					_la = _input.LA(1);
					if ( _la <= 0 || (_la==LPAREN || _la==RPAREN) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					}
					break;
				case LPAREN:
					{
					setState(278);
					parenGroup();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				setState(283);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(284);
			match(RPAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\u0004\u0001\"\u011f\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002"+
		"\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007\u000f"+
		"\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007\u0012"+
		"\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002\u0015\u0007\u0015"+
		"\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017\u0002\u0018\u0007\u0018"+
		"\u0001\u0000\u0005\u00004\b\u0000\n\u0000\f\u00007\t\u0000\u0001\u0000"+
		"\u0001\u0000\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0003\u0001"+
		"?\b\u0001\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002"+
		"\u0001\u0002\u0001\u0002\u0005\u0002H\b\u0002\n\u0002\f\u0002K\t\u0002"+
		"\u0001\u0002\u0001\u0002\u0005\u0002O\b\u0002\n\u0002\f\u0002R\t\u0002"+
		"\u0001\u0002\u0003\u0002U\b\u0002\u0001\u0003\u0001\u0003\u0003\u0003"+
		"Y\b\u0003\u0001\u0004\u0001\u0004\u0001\u0004\u0005\u0004^\b\u0004\n\u0004"+
		"\f\u0004a\t\u0004\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001"+
		"\u0005\u0003\u0005h\b\u0005\u0001\u0005\u0003\u0005k\b\u0005\u0001\u0006"+
		"\u0001\u0006\u0001\u0006\u0001\u0006\u0003\u0006q\b\u0006\u0001\u0007"+
		"\u0001\u0007\u0003\u0007u\b\u0007\u0001\u0007\u0001\u0007\u0001\b\u0001"+
		"\b\u0001\b\u0001\b\u0005\b}\b\b\n\b\f\b\u0080\t\b\u0001\b\u0001\b\u0001"+
		"\b\u0003\b\u0085\b\b\u0001\b\u0001\b\u0001\b\u0001\b\u0003\b\u008b\b\b"+
		"\u0001\b\u0005\b\u008e\b\b\n\b\f\b\u0091\t\b\u0001\b\u0001\b\u0001\b\u0005"+
		"\b\u0096\b\b\n\b\f\b\u0099\t\b\u0001\b\u0001\b\u0001\b\u0005\b\u009e\b"+
		"\b\n\b\f\b\u00a1\t\b\u0003\b\u00a3\b\b\u0001\t\u0001\t\u0001\n\u0001\n"+
		"\u0001\n\u0001\n\u0005\n\u00ab\b\n\n\n\f\n\u00ae\t\n\u0001\n\u0001\n\u0001"+
		"\u000b\u0001\u000b\u0001\u000b\u0003\u000b\u00b5\b\u000b\u0001\f\u0001"+
		"\f\u0005\f\u00b9\b\f\n\f\f\f\u00bc\t\f\u0001\f\u0001\f\u0001\f\u0001\f"+
		"\u0001\f\u0001\f\u0001\f\u0001\f\u0005\f\u00c6\b\f\n\f\f\f\u00c9\t\f\u0001"+
		"\f\u0001\f\u0005\f\u00cd\b\f\n\f\f\f\u00d0\t\f\u0001\f\u0003\f\u00d3\b"+
		"\f\u0001\r\u0001\r\u0001\u000e\u0001\u000e\u0003\u000e\u00d9\b\u000e\u0001"+
		"\u000f\u0001\u000f\u0001\u0010\u0001\u0010\u0001\u0010\u0001\u0010\u0004"+
		"\u0010\u00e1\b\u0010\u000b\u0010\f\u0010\u00e2\u0001\u0010\u0003\u0010"+
		"\u00e6\b\u0010\u0001\u0011\u0003\u0011\u00e9\b\u0011\u0001\u0011\u0001"+
		"\u0011\u0003\u0011\u00ed\b\u0011\u0001\u0012\u0001\u0012\u0005\u0012\u00f1"+
		"\b\u0012\n\u0012\f\u0012\u00f4\t\u0012\u0001\u0012\u0003\u0012\u00f7\b"+
		"\u0012\u0001\u0012\u0003\u0012\u00fa\b\u0012\u0001\u0013\u0001\u0013\u0001"+
		"\u0013\u0001\u0013\u0001\u0013\u0003\u0013\u0101\b\u0013\u0001\u0014\u0001"+
		"\u0014\u0001\u0014\u0005\u0014\u0106\b\u0014\n\u0014\f\u0014\u0109\t\u0014"+
		"\u0001\u0015\u0001\u0015\u0001\u0016\u0001\u0016\u0003\u0016\u010f\b\u0016"+
		"\u0001\u0017\u0001\u0017\u0003\u0017\u0113\b\u0017\u0001\u0018\u0001\u0018"+
		"\u0001\u0018\u0005\u0018\u0118\b\u0018\n\u0018\f\u0018\u011b\t\u0018\u0001"+
		"\u0018\u0001\u0018\u0001\u0018\u0000\u0000\u0019\u0000\u0002\u0004\u0006"+
		"\b\n\f\u000e\u0010\u0012\u0014\u0016\u0018\u001a\u001c\u001e \"$&(*,."+
		"0\u0000\b\u0002\u0000\u0007\u0007\u0012\u0014\u0001\u0000\u0015\u0017"+
		"\u0002\u0000\u0005\u0007\u001b\u001b\u0002\u0000\u0005\u0005\u001b\u001b"+
		"\u0002\u0000\u001e\u001f!!\u0004\u0000\u0004\u0004\b\b\u0018\u0019\u001b"+
		"\u001b\u0003\u0000\u0004\u0004\b\b\u0018\u001b\u0001\u0000\u0018\u0019"+
		"\u0131\u00005\u0001\u0000\u0000\u0000\u0002>\u0001\u0000\u0000\u0000\u0004"+
		"@\u0001\u0000\u0000\u0000\u0006X\u0001\u0000\u0000\u0000\bZ\u0001\u0000"+
		"\u0000\u0000\nb\u0001\u0000\u0000\u0000\fp\u0001\u0000\u0000\u0000\u000e"+
		"t\u0001\u0000\u0000\u0000\u0010\u00a2\u0001\u0000\u0000\u0000\u0012\u00a4"+
		"\u0001\u0000\u0000\u0000\u0014\u00a6\u0001\u0000\u0000\u0000\u0016\u00b4"+
		"\u0001\u0000\u0000\u0000\u0018\u00b6\u0001\u0000\u0000\u0000\u001a\u00d4"+
		"\u0001\u0000\u0000\u0000\u001c\u00d6\u0001\u0000\u0000\u0000\u001e\u00da"+
		"\u0001\u0000\u0000\u0000 \u00dc\u0001\u0000\u0000\u0000\"\u00ec\u0001"+
		"\u0000\u0000\u0000$\u00f9\u0001\u0000\u0000\u0000&\u0100\u0001\u0000\u0000"+
		"\u0000(\u0102\u0001\u0000\u0000\u0000*\u010a\u0001\u0000\u0000\u0000,"+
		"\u010e\u0001\u0000\u0000\u0000.\u0112\u0001\u0000\u0000\u00000\u0114\u0001"+
		"\u0000\u0000\u000024\u0003\u0002\u0001\u000032\u0001\u0000\u0000\u0000"+
		"47\u0001\u0000\u0000\u000053\u0001\u0000\u0000\u000056\u0001\u0000\u0000"+
		"\u000068\u0001\u0000\u0000\u000075\u0001\u0000\u0000\u000089\u0005\u0000"+
		"\u0000\u00019\u0001\u0001\u0000\u0000\u0000:?\u0003\u0004\u0002\u0000"+
		";?\u0003\u0018\f\u0000<?\u0003 \u0010\u0000=?\u0003$\u0012\u0000>:\u0001"+
		"\u0000\u0000\u0000>;\u0001\u0000\u0000\u0000><\u0001\u0000\u0000\u0000"+
		">=\u0001\u0000\u0000\u0000?\u0003\u0001\u0000\u0000\u0000@A\u0005\u0004"+
		"\u0000\u0000AB\u0005\u0005\u0000\u0000BC\u0003(\u0014\u0000CD\u0005\u0018"+
		"\u0000\u0000DI\u0003\u0006\u0003\u0000EF\u0005\u001a\u0000\u0000FH\u0003"+
		"\u0006\u0003\u0000GE\u0001\u0000\u0000\u0000HK\u0001\u0000\u0000\u0000"+
		"IG\u0001\u0000\u0000\u0000IJ\u0001\u0000\u0000\u0000JL\u0001\u0000\u0000"+
		"\u0000KI\u0001\u0000\u0000\u0000LP\u0005\u0019\u0000\u0000MO\u0003\u0016"+
		"\u000b\u0000NM\u0001\u0000\u0000\u0000OR\u0001\u0000\u0000\u0000PN\u0001"+
		"\u0000\u0000\u0000PQ\u0001\u0000\u0000\u0000QT\u0001\u0000\u0000\u0000"+
		"RP\u0001\u0000\u0000\u0000SU\u0005\u001b\u0000\u0000TS\u0001\u0000\u0000"+
		"\u0000TU\u0001\u0000\u0000\u0000U\u0005\u0001\u0000\u0000\u0000VY\u0003"+
		"\u000e\u0007\u0000WY\u0003\b\u0004\u0000XV\u0001\u0000\u0000\u0000XW\u0001"+
		"\u0000\u0000\u0000Y\u0007\u0001\u0000\u0000\u0000Z[\u0003*\u0015\u0000"+
		"[_\u0003\n\u0005\u0000\\^\u0003\f\u0006\u0000]\\\u0001\u0000\u0000\u0000"+
		"^a\u0001\u0000\u0000\u0000_]\u0001\u0000\u0000\u0000_`\u0001\u0000\u0000"+
		"\u0000`\t\u0001\u0000\u0000\u0000a_\u0001\u0000\u0000\u0000bj\u0003*\u0015"+
		"\u0000cd\u0005\u0018\u0000\u0000dg\u0005 \u0000\u0000ef\u0005\u001a\u0000"+
		"\u0000fh\u0005 \u0000\u0000ge\u0001\u0000\u0000\u0000gh\u0001\u0000\u0000"+
		"\u0000hi\u0001\u0000\u0000\u0000ik\u0005\u0019\u0000\u0000jc\u0001\u0000"+
		"\u0000\u0000jk\u0001\u0000\u0000\u0000k\u000b\u0001\u0000\u0000\u0000"+
		"lm\u0005\u0012\u0000\u0000mq\u0005\u0013\u0000\u0000nq\u0005\u0013\u0000"+
		"\u0000oq\u0003.\u0017\u0000pl\u0001\u0000\u0000\u0000pn\u0001\u0000\u0000"+
		"\u0000po\u0001\u0000\u0000\u0000q\r\u0001\u0000\u0000\u0000rs\u0005\u0010"+
		"\u0000\u0000su\u0003*\u0015\u0000tr\u0001\u0000\u0000\u0000tu\u0001\u0000"+
		"\u0000\u0000uv\u0001\u0000\u0000\u0000vw\u0003\u0010\b\u0000w\u000f\u0001"+
		"\u0000\u0000\u0000xy\u0005\f\u0000\u0000yz\u0005\u000e\u0000\u0000z~\u0003"+
		"\u0014\n\u0000{}\u0003\u0012\t\u0000|{\u0001\u0000\u0000\u0000}\u0080"+
		"\u0001\u0000\u0000\u0000~|\u0001\u0000\u0000\u0000~\u007f\u0001\u0000"+
		"\u0000\u0000\u007f\u00a3\u0001\u0000\u0000\u0000\u0080~\u0001\u0000\u0000"+
		"\u0000\u0081\u0082\u0005\r\u0000\u0000\u0082\u0084\u0005\u000e\u0000\u0000"+
		"\u0083\u0085\u0003*\u0015\u0000\u0084\u0083\u0001\u0000\u0000\u0000\u0084"+
		"\u0085\u0001\u0000\u0000\u0000\u0085\u0086\u0001\u0000\u0000\u0000\u0086"+
		"\u0087\u0003\u0014\n\u0000\u0087\u0088\u0005\u000f\u0000\u0000\u0088\u008a"+
		"\u0003(\u0014\u0000\u0089\u008b\u0003\u0014\n\u0000\u008a\u0089\u0001"+
		"\u0000\u0000\u0000\u008a\u008b\u0001\u0000\u0000\u0000\u008b\u008f\u0001"+
		"\u0000\u0000\u0000\u008c\u008e\u0003\u0012\t\u0000\u008d\u008c\u0001\u0000"+
		"\u0000\u0000\u008e\u0091\u0001\u0000\u0000\u0000\u008f\u008d\u0001\u0000"+
		"\u0000\u0000\u008f\u0090\u0001\u0000\u0000\u0000\u0090\u00a3\u0001\u0000"+
		"\u0000\u0000\u0091\u008f\u0001\u0000\u0000\u0000\u0092\u0093\u0005\u0007"+
		"\u0000\u0000\u0093\u0097\u0003\u0014\n\u0000\u0094\u0096\u0003\u0012\t"+
		"\u0000\u0095\u0094\u0001\u0000\u0000\u0000\u0096\u0099\u0001\u0000\u0000"+
		"\u0000\u0097\u0095\u0001\u0000\u0000\u0000\u0097\u0098\u0001\u0000\u0000"+
		"\u0000\u0098\u00a3\u0001\u0000\u0000\u0000\u0099\u0097\u0001\u0000\u0000"+
		"\u0000\u009a\u009b\u0005\u0011\u0000\u0000\u009b\u009f\u00030\u0018\u0000"+
		"\u009c\u009e\u0003\u0012\t\u0000\u009d\u009c\u0001\u0000\u0000\u0000\u009e"+
		"\u00a1\u0001\u0000\u0000\u0000\u009f\u009d\u0001\u0000\u0000\u0000\u009f"+
		"\u00a0\u0001\u0000\u0000\u0000\u00a0\u00a3\u0001\u0000\u0000\u0000\u00a1"+
		"\u009f\u0001\u0000\u0000\u0000\u00a2x\u0001\u0000\u0000\u0000\u00a2\u0081"+
		"\u0001\u0000\u0000\u0000\u00a2\u0092\u0001\u0000\u0000\u0000\u00a2\u009a"+
		"\u0001\u0000\u0000\u0000\u00a3\u0011\u0001\u0000\u0000\u0000\u00a4\u00a5"+
		"\u0003.\u0017\u0000\u00a5\u0013\u0001\u0000\u0000\u0000\u00a6\u00a7\u0005"+
		"\u0018\u0000\u0000\u00a7\u00ac\u0003*\u0015\u0000\u00a8\u00a9\u0005\u001a"+
		"\u0000\u0000\u00a9\u00ab\u0003*\u0015\u0000\u00aa\u00a8\u0001\u0000\u0000"+
		"\u0000\u00ab\u00ae\u0001\u0000\u0000\u0000\u00ac\u00aa\u0001\u0000\u0000"+
		"\u0000\u00ac\u00ad\u0001\u0000\u0000\u0000\u00ad\u00af\u0001\u0000\u0000"+
		"\u0000\u00ae\u00ac\u0001\u0000\u0000\u0000\u00af\u00b0\u0005\u0019\u0000"+
		"\u0000\u00b0\u0015\u0001\u0000\u0000\u0000\u00b1\u00b2\u0005\u000b\u0000"+
		"\u0000\u00b2\u00b5\u0003(\u0014\u0000\u00b3\u00b5\u0003,\u0016\u0000\u00b4"+
		"\u00b1\u0001\u0000\u0000\u0000\u00b4\u00b3\u0001\u0000\u0000\u0000\u00b5"+
		"\u0017\u0001\u0000\u0000\u0000\u00b6\u00ba\u0005\u0004\u0000\u0000\u00b7"+
		"\u00b9\u0003\u001a\r\u0000\u00b8\u00b7\u0001\u0000\u0000\u0000\u00b9\u00bc"+
		"\u0001\u0000\u0000\u0000\u00ba\u00b8\u0001\u0000\u0000\u0000\u00ba\u00bb"+
		"\u0001\u0000\u0000\u0000\u00bb\u00bd\u0001\u0000\u0000\u0000\u00bc\u00ba"+
		"\u0001\u0000\u0000\u0000\u00bd\u00be\u0005\u0006\u0000\u0000\u00be\u00bf"+
		"\u0003(\u0014\u0000\u00bf\u00c0\u0005\n\u0000\u0000\u00c0\u00c1\u0003"+
		"(\u0014\u0000\u00c1\u00c2\u0005\u0018\u0000\u0000\u00c2\u00c7\u0003\u001c"+
		"\u000e\u0000\u00c3\u00c4\u0005\u001a\u0000\u0000\u00c4\u00c6\u0003\u001c"+
		"\u000e\u0000\u00c5\u00c3\u0001\u0000\u0000\u0000\u00c6\u00c9\u0001\u0000"+
		"\u0000\u0000\u00c7\u00c5\u0001\u0000\u0000\u0000\u00c7\u00c8\u0001\u0000"+
		"\u0000\u0000\u00c8\u00ca\u0001\u0000\u0000\u0000\u00c9\u00c7\u0001\u0000"+
		"\u0000\u0000\u00ca\u00ce\u0005\u0019\u0000\u0000\u00cb\u00cd\u0003\u001e"+
		"\u000f\u0000\u00cc\u00cb\u0001\u0000\u0000\u0000\u00cd\u00d0\u0001\u0000"+
		"\u0000\u0000\u00ce\u00cc\u0001\u0000\u0000\u0000\u00ce\u00cf\u0001\u0000"+
		"\u0000\u0000\u00cf\u00d2\u0001\u0000\u0000\u0000\u00d0\u00ce\u0001\u0000"+
		"\u0000\u0000\u00d1\u00d3\u0005\u001b\u0000\u0000\u00d2\u00d1\u0001\u0000"+
		"\u0000\u0000\u00d2\u00d3\u0001\u0000\u0000\u0000\u00d3\u0019\u0001\u0000"+
		"\u0000\u0000\u00d4\u00d5\u0007\u0000\u0000\u0000\u00d5\u001b\u0001\u0000"+
		"\u0000\u0000\u00d6\u00d8\u0003*\u0015\u0000\u00d7\u00d9\u0007\u0001\u0000"+
		"\u0000\u00d8\u00d7\u0001\u0000\u0000\u0000\u00d8\u00d9\u0001\u0000\u0000"+
		"\u0000\u00d9\u001d\u0001\u0000\u0000\u0000\u00da\u00db\u0003,\u0016\u0000"+
		"\u00db\u001f\u0001\u0000\u0000\u0000\u00dc\u00dd\u0005\b\u0000\u0000\u00dd"+
		"\u00de\u0005\u0005\u0000\u0000\u00de\u00e0\u0003(\u0014\u0000\u00df\u00e1"+
		"\u0003\"\u0011\u0000\u00e0\u00df\u0001\u0000\u0000\u0000\u00e1\u00e2\u0001"+
		"\u0000\u0000\u0000\u00e2\u00e0\u0001\u0000\u0000\u0000\u00e2\u00e3\u0001"+
		"\u0000\u0000\u0000\u00e3\u00e5\u0001\u0000\u0000\u0000\u00e4\u00e6\u0005"+
		"\u001b\u0000\u0000\u00e5\u00e4\u0001\u0000\u0000\u0000\u00e5\u00e6\u0001"+
		"\u0000\u0000\u0000\u00e6!\u0001\u0000\u0000\u0000\u00e7\u00e9\u0005\t"+
		"\u0000\u0000\u00e8\u00e7\u0001\u0000\u0000\u0000\u00e8\u00e9\u0001\u0000"+
		"\u0000\u0000\u00e9\u00ea\u0001\u0000\u0000\u0000\u00ea\u00ed\u0003\u000e"+
		"\u0007\u0000\u00eb\u00ed\u0003,\u0016\u0000\u00ec\u00e8\u0001\u0000\u0000"+
		"\u0000\u00ec\u00eb\u0001\u0000\u0000\u0000\u00ed#\u0001\u0000\u0000\u0000"+
		"\u00ee\u00f2\u0003&\u0013\u0000\u00ef\u00f1\u0003,\u0016\u0000\u00f0\u00ef"+
		"\u0001\u0000\u0000\u0000\u00f1\u00f4\u0001\u0000\u0000\u0000\u00f2\u00f0"+
		"\u0001\u0000\u0000\u0000\u00f2\u00f3\u0001\u0000\u0000\u0000\u00f3\u00f6"+
		"\u0001\u0000\u0000\u0000\u00f4\u00f2\u0001\u0000\u0000\u0000\u00f5\u00f7"+
		"\u0005\u001b\u0000\u0000\u00f6\u00f5\u0001\u0000\u0000\u0000\u00f6\u00f7"+
		"\u0001\u0000\u0000\u0000\u00f7\u00fa\u0001\u0000\u0000\u0000\u00f8\u00fa"+
		"\u0005\u001b\u0000\u0000\u00f9\u00ee\u0001\u0000\u0000\u0000\u00f9\u00f8"+
		"\u0001\u0000\u0000\u0000\u00fa%\u0001\u0000\u0000\u0000\u00fb\u00fc\u0005"+
		"\u0004\u0000\u0000\u00fc\u0101\b\u0002\u0000\u0000\u00fd\u00fe\u0005\b"+
		"\u0000\u0000\u00fe\u0101\b\u0003\u0000\u0000\u00ff\u0101\u0003,\u0016"+
		"\u0000\u0100\u00fb\u0001\u0000\u0000\u0000\u0100\u00fd\u0001\u0000\u0000"+
		"\u0000\u0100\u00ff\u0001\u0000\u0000\u0000\u0101\'\u0001\u0000\u0000\u0000"+
		"\u0102\u0107\u0003*\u0015\u0000\u0103\u0104\u0005\u001c\u0000\u0000\u0104"+
		"\u0106\u0003*\u0015\u0000\u0105\u0103\u0001\u0000\u0000\u0000\u0106\u0109"+
		"\u0001\u0000\u0000\u0000\u0107\u0105\u0001\u0000\u0000\u0000\u0107\u0108"+
		"\u0001\u0000\u0000\u0000\u0108)\u0001\u0000\u0000\u0000\u0109\u0107\u0001"+
		"\u0000\u0000\u0000\u010a\u010b\u0007\u0004\u0000\u0000\u010b+\u0001\u0000"+
		"\u0000\u0000\u010c\u010f\b\u0005\u0000\u0000\u010d\u010f\u00030\u0018"+
		"\u0000\u010e\u010c\u0001\u0000\u0000\u0000\u010e\u010d\u0001\u0000\u0000"+
		"\u0000\u010f-\u0001\u0000\u0000\u0000\u0110\u0113\b\u0006\u0000\u0000"+
		"\u0111\u0113\u00030\u0018\u0000\u0112\u0110\u0001\u0000\u0000\u0000\u0112"+
		"\u0111\u0001\u0000\u0000\u0000\u0113/\u0001\u0000\u0000\u0000\u0114\u0119"+
		"\u0005\u0018\u0000\u0000\u0115\u0118\b\u0007\u0000\u0000\u0116\u0118\u0003"+
		"0\u0018\u0000\u0117\u0115\u0001\u0000\u0000\u0000\u0117\u0116\u0001\u0000"+
		"\u0000\u0000\u0118\u011b\u0001\u0000\u0000\u0000\u0119\u0117\u0001\u0000"+
		"\u0000\u0000\u0119\u011a\u0001\u0000\u0000\u0000\u011a\u011c\u0001\u0000"+
		"\u0000\u0000\u011b\u0119\u0001\u0000\u0000\u0000\u011c\u011d\u0005\u0019"+
		"\u0000\u0000\u011d1\u0001\u0000\u0000\u0000&5>IPTX_gjpt~\u0084\u008a\u008f"+
		"\u0097\u009f\u00a2\u00ac\u00b4\u00ba\u00c7\u00ce\u00d2\u00d8\u00e2\u00e5"+
		"\u00e8\u00ec\u00f2\u00f6\u00f9\u0100\u0107\u010e\u0112\u0117\u0119";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}