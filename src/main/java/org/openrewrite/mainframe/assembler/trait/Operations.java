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
package org.openrewrite.mainframe.assembler.trait;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * What an operation is, which nothing in the source says.
 * <p>
 * An HLASM statement's operation is a machine instruction, an assembler instruction, or the name of a
 * macro — and the three are written identically. Telling them apart is a table, and this is it: what is
 * neither a mnemonic nor a directive is an invocation of a macro, whether the shop wrote the macro or
 * IBM ships it in {@code SYS1.MACLIB}.
 * <p>
 * The mnemonic table is the S/370, ESA/390 and z/Architecture instructions a business application
 * uses. It is not the whole architecture, so a program using an instruction left out of it reports one
 * macro invocation too many. That errs the way a reader should: a name it does not know is reported
 * rather than passed over.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class Operations {

    /**
     * The assembler's own statements: the ones that lay out a program rather than run in it, and the
     * conditional assembly that decides which of them the assembler sees.
     */
    private static final Set<String> DIRECTIVES = set(
            "ACTR", "ADATA", "AEJECT", "AGO", "AIF", "AINSERT", "ALIAS", "AMODE", "ANOP", "AREAD",
            "ASPACE", "CATTR", "CCW", "CCW0", "CCW1", "CEJECT", "CNOP", "COM", "COPY", "CSECT", "CXD",
            "DC", "DROP", "DS", "DSECT", "DXD", "EJECT", "END", "ENTRY", "EQU", "EXITCTL", "EXTRN",
            "GBLA", "GBLB", "GBLC", "ICTL", "ISEQ", "LCLA", "LCLB", "LCLC", "LOCTR", "LTORG", "MACRO",
            "MEND", "MEXIT", "MHELP", "MNOTE", "OPSYN", "ORG", "POP", "PRINT", "PROCESS", "PUNCH",
            "PUSH", "REPRO", "RMODE", "RSECT", "SETA", "SETAF", "SETB", "SETC", "SETCF", "SPACE",
            "START", "TITLE", "USING", "WXTRN", "XATTR");

    /**
     * The machine instructions, by mnemonic. Extended branch mnemonics such as {@code BE} and
     * {@code JNE} are here beside the {@code BC} and {@code BRC} they stand for, because a program
     * writes them and not the mask.
     */
    private static final Set<String> INSTRUCTIONS = set(
            "A", "AD", "ADB", "ADBR", "ADR", "ADTR", "AE", "AEB", "AEBR", "AER", "AFI", "AG", "AGF",
            "AGFI", "AGFR", "AGH", "AGHI", "AGHIK", "AGR", "AGRK", "AGSI", "AH", "AHHHR", "AHHLR",
            "AHI", "AHIK", "AHY", "AL", "ALC", "ALCG", "ALCGR", "ALCR", "ALFI", "ALG", "ALGF", "ALGFI",
            "ALGFR", "ALGHSIK", "ALGR", "ALGRK", "ALGSI", "ALHHHR", "ALHHLR", "ALHSIK", "ALR", "ALRK",
            "AIH", "ALSI", "ALSIH", "ALY", "AP", "AR", "ARK", "ASI", "AXBR", "AXR", "AXTR", "AY",
            "B", "BAKR", "BAL", "BALR", "BAS", "BASR", "BASSM", "BC", "BCR", "BCT", "BCTG", "BCTGR", "BCTR",
            "BE", "BER", "BH", "BHR", "BI", "BL", "BLR", "BM", "BMR", "BNE", "BNER", "BNH", "BNHR",
            "BNL", "BNLR", "BNM", "BNMR", "BNO", "BNOR", "BNP", "BNPR", "BNZ", "BNZR", "BO", "BOR",
            "BP", "BPP", "BPR", "BPRP", "BR", "BRAS", "BRASL", "BRC", "BRCL", "BRCT", "BRCTG", "BRCTH",
            "BRE", "BREL", "BRH", "BRHL", "BRL", "BRM", "BRML", "BRNE", "BRNEL", "BRNH", "BRNHL",
            "BRNL", "BRNLL", "BRNM", "BRNML", "BRNO", "BRNOL", "BRNP", "BRNPL", "BRNZ", "BRNZL", "BRO",
            "BROL", "BRP", "BRPL", "BRU", "BRUL", "BRXH", "BRXHG", "BRXLE", "BRXLG", "BRZ", "BRZL",
            "BSA", "BSG", "BSM", "BXH", "BXHG", "BXLE", "BXLEG", "BZ", "BZR",
            "C", "CD", "CDB", "CDBR", "CDFBR", "CDFR", "CDFTR", "CDGBR", "CDGR", "CDLFTR", "CDLGTR",
            "CDR", "CDS", "CDSG", "CDSTR", "CDSY", "CDTR",
            "CE", "CEB", "CEBR", "CEDTR", "CEFBR", "CEFR", "CEGBR", "CEGR", "CER", "CEXTR", "CFDBR",
            "CFDR", "CFDTR", "CFEBR", "CFER", "CFI", "CFXBR", "CFXR", "CFXTR",
            "CG", "CGDBR", "CGDR", "CGEBR", "CGER", "CGF", "CGFI", "CGFR", "CGFRL", "CGH", "CGHI", "CGHRL",
            "CGHSI", "CGIB", "CGIJ", "CGIJE", "CGIJH", "CGIJL", "CGIJNE", "CGIJNH", "CGIJNL", "CGIT",
            "CGR", "CGRB", "CGRJ", "CGRJE", "CGRJH", "CGRJL", "CGRJNE", "CGRJNH", "CGRJNL", "CGRL",
            "CGRT", "CGXBR", "CGXR", "CGXTR", "CH", "CHF", "CHHR", "CHHSI",
            "CHI", "CHLR", "CHRL", "CHSI", "CHY", "CIB", "CIH", "CIJ", "CIJE", "CIJH", "CIJL", "CIJNE",
            "CIJNH", "CIJNL", "CIT", "CKSM", "CL", "CLC", "CLCL", "CLCLE", "CLCLU", "CLFDTR", "CLFHSI",
            "CLFI", "CLFIT", "CLFXTR", "CLG", "CLGDTR", "CLGF", "CLGFI", "CLGFR", "CLGFRL", "CLGHRL",
            "CLGHSI", "CLGIB", "CLGIJ", "CLGIJE", "CLGIJH", "CLGIJL", "CLGIJNE", "CLGIJNH", "CLGIJNL",
            "CLGIT", "CLGR", "CLGRB", "CLGRJ", "CLGRJE", "CLGRJH", "CLGRJL", "CLGRJNE", "CLGRJNH",
            "CLGRJNL", "CLGRL", "CLGRT", "CLGT", "CLGXTR", "CLHF", "CLHHR", "CLHHSI",
            "CLHLR", "CLHRL", "CLI", "CLIB", "CLIH", "CLIJ", "CLIJE", "CLIJH", "CLIJL", "CLIJNE",
            "CLIJNH", "CLIJNL", "CLIY", "CLM", "CLMH", "CLMY", "CLR",
            "CLRB", "CLRJ", "CLRJE", "CLRJH", "CLRJL", "CLRJNE", "CLRJNH", "CLRJNL", "CLRL", "CLRT",
            "CLST", "CLT", "CLY", "CMPSC", "CP", "CPSDR", "CPYA", "CR",
            "CRB", "CRJ", "CRJE", "CRJH", "CRJL", "CRJNE", "CRJNH", "CRJNL", "CRL", "CRT",
            "CS", "CSCH", "CSDTR", "CSG", "CSP", "CSPG", "CSST", "CSXTR",
            "CSY", "CU12", "CU14", "CU21", "CU24", "CU41", "CU42", "CUDTR", "CUSE", "CUTFU", "CUUTF",
            "CUXTR", "CVB", "CVBG", "CVBY", "CVD", "CVDG", "CVDY", "CXBR", "CXFBR", "CXFR", "CXFTR",
            "CXGBR", "CXGR", "CXGTR", "CXLFTR", "CXLGTR", "CXR", "CXSTR", "CXTR", "CY",
            "D", "DD", "DDB", "DDBR", "DDR", "DDTR", "DE", "DEB", "DEBR", "DER", "DIAG", "DIDBR",
            "DIEBR", "DL", "DLG", "DLGR", "DLR", "DP", "DR", "DSG", "DSGF", "DSGFR", "DSGR", "DXBR",
            "DXR", "DXTR",
            "EAR", "ECAG", "ECTG", "ED", "EDMK", "EEDTR", "EEXTR", "EFPC", "EPAIR", "EPAR", "EPSW",
            "EREG", "EREGG", "ESAIR", "ESAR", "ESDTR", "ESEA", "ESTA", "ESXTR", "ETND", "EX", "EXRL",
            "FIDBR", "FIDTR", "FIEBR", "FIXBR", "FIXTR", "FLOGR",
            "HDR", "HER", "HSCH",
            "IAC", "IC", "ICM", "ICMH", "ICMY", "ICY", "IEDTR", "IEXTR", "IIHF", "IIHH", "IIHL",
            "IILF", "IILH", "IILL", "IPK", "IPM", "IPTE", "ISKE", "IVSK",
            "J", "JAS", "JASL", "JC", "JCT", "JCTG", "JE", "JG", "JH", "JL", "JLC", "JLE", "JLH", "JLL",
            "JLM", "JLNE", "JLNH", "JLNL", "JLNM", "JLNO", "JLNOP", "JLNP", "JLNZ", "JLO", "JLP",
            "JLU", "JLZ", "JM", "JNE", "JNH", "JNL", "JNM", "JNO", "JNOP", "JNP", "JNZ", "JO", "JP",
            "JXH", "JXHG", "JXLE", "JXLEG", "JZ",
            "KDB", "KDBR", "KDTR", "KEB", "KEBR", "KIMD", "KLMD", "KM", "KMAC", "KMC", "KMCTR", "KMF",
            "KMO", "KXBR", "KXTR",
            "L", "LA", "LAA", "LAAG", "LAAL", "LAALG", "LAE", "LAEY", "LAM", "LAMY", "LAN", "LANG",
            "LAO", "LAOG", "LARL", "LASP", "LAT", "LAX", "LAXG", "LAY", "LB", "LBH", "LBR", "LCDBR",
            "LCDFR", "LCEBR", "LCGFR", "LCGR", "LCR", "LCTL", "LCTLG", "LCXBR", "LD", "LDE", "LDEB",
            "LDEBR", "LDER", "LDETR", "LDGR", "LDR", "LDXBR", "LDXR", "LDXTR", "LDY", "LE", "LEDBR",
            "LEDR", "LEDTR", "LER", "LEXBR", "LEXR", "LEY", "LFAS", "LFH", "LFHAT", "LFPC", "LG",
            "LGAT", "LGB", "LGBR", "LGDR", "LGF", "LGFI", "LGFR", "LGFRL", "LGH", "LGHI", "LGHR",
            "LGHRL", "LGR", "LGRL", "LH", "LHH", "LHHR", "LHI", "LHLR", "LHR", "LHRL", "LHY", "LLC",
            "LLCH", "LLCHHR", "LLCHLR", "LLCLHR", "LLCR",
            "LLGC", "LLGCR", "LLGF", "LLGFAT", "LLGFR", "LLGFRL", "LLGH", "LLGHR", "LLGHRL", "LLGT",
            "LLGTAT", "LLGTR", "LLH", "LLHFR", "LLHH", "LLHHHR", "LLHHLR", "LLHLHR", "LLHR", "LLHRL",
            "LLIHF", "LLIHH", "LLIHL", "LLILF",
            "LLILH", "LLILL", "LM", "LMD", "LMG", "LMH", "LMY", "LNDBR", "LNDFR", "LNEBR", "LNGFR",
            "LNGR", "LNR", "LNXBR", "LOC", "LOCG", "LOCGR", "LOCR", "LPD", "LPDBR", "LPDFR", "LPDG",
            "LPEBR", "LPGFR", "LPGR", "LPQ", "LPR", "LPSW", "LPSWE", "LPTEA", "LPXBR", "LR", "LRA",
            "LRAG", "LRAY", "LRDR", "LRER", "LRL", "LRV", "LRVG", "LRVGR", "LRVH", "LRVR", "LT",
            "LTDBR", "LTDR", "LTDTR", "LTEBR", "LTER", "LTG", "LTGF", "LTGFR", "LTGR", "LTR", "LTXBR",
            "LTXR", "LTXTR", "LURA", "LURAG", "LXD", "LXDB", "LXDBR", "LXDR", "LXDTR", "LXE", "LXEB",
            "LXEBR", "LXER", "LXR", "LY", "LZDR", "LZER", "LZXR",
            "M", "MAD", "MADB", "MADBR", "MADR", "MAE", "MAEB", "MAEBR", "MAER", "MC", "MD", "MDB",
            "MDBR", "MDE", "MDEB", "MDEBR", "MDER", "MDR", "MDTR", "ME", "MEE", "MEEB", "MEEBR",
            "MEER", "MFY", "MGHI", "MH", "MHI", "MHY", "ML", "MLG", "MLGR", "MLR", "MP", "MR", "MS",
            "MSCH", "MSD", "MSDB", "MSDBR", "MSDR", "MSE", "MSEB", "MSEBR", "MSER", "MSFI", "MSG",
            "MSGF", "MSGFI", "MSGFR", "MSGR", "MSR", "MSTA", "MSY", "MVC", "MVCDK", "MVCIN", "MVCK",
            "MVCL", "MVCLE", "MVCLU", "MVCOS", "MVCP", "MVCS", "MVCSK", "MVGHI", "MVHHI", "MVHI",
            "MVI", "MVIY", "MVN", "MVO", "MVPG", "MVST", "MVZ", "MXBR", "MXD", "MXDB", "MXDBR",
            "MXDR", "MXR", "MXTR",
            "N", "NC", "NG", "NGR", "NGRK", "NI", "NIAI", "NIHF", "NIHH", "NIHL", "NILF", "NILH",
            "NILL", "NIY", "NOP", "NOPR", "NR", "NRK", "NTSTG", "NY",
            "O", "OC", "OG", "OGR", "OGRK", "OI", "OIHF", "OIHH", "OIHL", "OILF", "OILH", "OILL",
            "OIY", "OR", "ORK", "OY",
            "PACK", "PALB", "PC", "PCC", "PFD", "PFDRL", "PFPO", "PGIN", "PGOUT", "PKA", "PKU", "PLO",
            "POPCNT", "PPA", "PR", "PT", "PTF", "PTFF", "PTI", "PTLB",
            "QADTR", "QAXTR",
            "RCHP", "RISBG", "RISBGN", "RISBGZ", "RISBHG", "RISBHGZ", "RISBLG", "RISBLGZ", "RLL",
            "RLLG", "RNSBG", "ROSBG", "RP",
            "RRBE", "RRBM", "RRDTR", "RRXTR", "RSCH", "RXSBG",
            "S", "SAC", "SACF", "SAL", "SAM24", "SAM31", "SAM64", "SAR", "SCHM", "SCK", "SCKC",
            "SCKPF", "SD", "SDB", "SDBR", "SDR", "SDTR", "SE", "SEB", "SEBR", "SER", "SFASR", "SFPC",
            "SG", "SGF", "SGFR", "SGH", "SGR", "SGRK", "SH", "SHHHR", "SHHLR", "SHY", "SIGP", "SL",
            "SLA", "SLAG", "SLAK", "SLB", "SLBG", "SLBGR", "SLBR", "SLDA", "SLDL", "SLDT", "SLFI",
            "SLG", "SLGF", "SLGFI", "SLGFR", "SLGR", "SLGRK", "SLHHHR", "SLHHLR", "SLL", "SLLG",
            "SLLK", "SLR", "SLRK", "SLXT", "SLY", "SP", "SPKA", "SPM", "SPT", "SPX", "SQD", "SQDB",
            "SQDBR", "SQDR", "SQE", "SQEB", "SQEBR", "SQER", "SQXBR", "SQXR", "SR", "SRA", "SRAG",
            "SRAK", "SRDA", "SRDL", "SRDT", "SRK", "SRL", "SRLG", "SRLK", "SRNM", "SRNMB", "SRNMT",
            "SRP", "SRST", "SRSTU", "SRXT", "SSAIR", "SSAR", "SSCH", "SSKE", "SSM", "ST", "STAM",
            "STAMY", "STAP", "STC", "STCH", "STCK", "STCKC", "STCKE", "STCKF", "STCM", "STCMH",
            "STCMY", "STCPS", "STCRW", "STCTL", "STCTG", "STCY", "STD", "STDY", "STE", "STEY", "STFH",
            "STFL", "STFLE", "STFPC", "STG", "STGRL", "STH", "STHH", "STHRL", "STHY", "STIDP", "STM",
            "STMG", "STMH", "STMY", "STNSM", "STOC", "STOCG", "STOSM", "STPQ", "STPT", "STPX", "STRAG",
            "STRL", "STRV", "STRVG", "STRVH", "STSCH", "STSI", "STURA", "STURG", "STY", "SU", "SUR",
            "SVC", "SW", "SWR", "SXBR", "SXR", "SXTR", "SY",
            "TABORT", "TAM", "TAR", "TB", "TBDR", "TBEDR", "TBEGIN", "TBEGINC", "TCDB", "TCEB",
            "TCXB", "TDCDT", "TDCET", "TDCXT", "TDGDT", "TDGET", "TDGXT", "TEND", "THDER", "THDR",
            "TM", "TMH", "TMHH", "TMHL", "TML", "TMLH", "TMLL", "TMY", "TP", "TPI", "TPROT", "TR",
            "TRACE", "TRACG", "TRAP2", "TRAP4", "TRE", "TROO", "TROT", "TRT", "TRTE", "TRTO", "TRTR",
            "TRTRE", "TRTT", "TS", "TSCH",
            "UNPK", "UNPKA", "UNPKU", "UPT",
            "X", "XC", "XG", "XGR", "XGRK", "XI", "XIHF", "XILF", "XIY", "XR", "XRK", "XSCH", "XY",
            "ZAP");

    static boolean isDirective(String operation) {
        return DIRECTIVES.contains(operation.toUpperCase(Locale.ROOT));
    }

    static boolean isInstruction(String operation) {
        return INSTRUCTIONS.contains(operation.toUpperCase(Locale.ROOT));
    }

    /**
     * Whether the operation is the name of a macro, which is what anything the assembler does not
     * define itself has to be. A variable symbol is not a name at all — the operation is decided when
     * the macro is expanded — so it is left out.
     */
    static boolean isMacro(String operation) {
        return !operation.isEmpty() && operation.charAt(0) != '&' &&
               !isDirective(operation) && !isInstruction(operation);
    }

    private static Set<String> set(String... names) {
        return Collections.unmodifiableSet(new HashSet<>(Arrays.asList(names)));
    }
}
