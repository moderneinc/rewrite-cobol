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
package org.openrewrite.listload.trait;

import lombok.Value;
import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.linkedit.LinkEditParser;
import org.openrewrite.linkedit.trait.LinkEditDeck;
import org.openrewrite.linkedit.tree.LinkEdit;
import org.openrewrite.listload.ListLoadLineReader;
import org.openrewrite.listload.tree.ListLoad;
import org.openrewrite.trait.SimpleTraitMatcher;
import org.openrewrite.trait.Trait;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * What AMBLIST or the binder reported about a load library, read for the modules it names rather than
 * for how the report is laid out.
 * <p>
 * A listing is the only place a module's <em>observed</em> composition is written down. A link-edit
 * deck says what a module was meant to hold; the listing says what it holds — the CSECTs the autocall
 * pulled in as well as the ones the deck asked for, where each one starts and how long it is, and
 * which compiler made it. Reconciling the two is how a shop finds a module built from something other
 * than the source it keeps.
 * <p>
 * Offsets and lengths are given as hexadecimal with no leading zeros, whichever report they came from:
 * AMBLIST writes eight digits and the binder writes as few as one, and an entry point's offset is
 * relative to the module in both, though the binder map prints it relative to its section.
 */
@Value
public class ModuleListing implements Trait<ListLoad.CompilationUnit> {

    /**
     * VS COBOL II, COBOL for MVS & VM, COBOL for OS/390 & VM, and Enterprise COBOL 3, 4 and 5.
     */
    private static final List<String> COBOL_PRODUCTS = Arrays.asList(
            "5668958", "5688197", "5648A25", "5655G53", "5655S71", "5655EC6");

    /**
     * High Level Assembler, which is what made every stub and every byte of the runtime.
     */
    private static final String ASSEMBLER_PRODUCT = "5696234";

    /**
     * PL/I for MVS & VM, and Enterprise PL/I.
     */
    private static final List<String> PLI_PRODUCTS = Arrays.asList("5688235", "5655H31");

    Cursor cursor;

    /**
     * The modules the listing reports, in the order it reports them — every member of a library for an
     * AMBLIST run, the one module that was linked for a binder {@code SYSPRINT}.
     */
    public List<Module> getModules() {
        List<Module> modules = readSummaries(readTranslators());
        modules.addAll(readBinderListing());
        return modules;
    }

    /**
     * What the report was asked for, from the {@code LISTLOAD}/{@code LISTIDR} cards a request deck
     * holds and an AMBLIST report echoes. This is what says which library a report covers.
     */
    public List<Request> getRequests() {
        List<Request> requests = new ArrayList<>();
        List<ListLoad.Line> lines = getTree().getLines();
        for (int i = 0; i < lines.size(); i++) {
            String text = lines.get(i).getText();
            if (!ListLoadLineReader.isRequestCard(text)) {
                continue;
            }
            String card = text.trim();
            String function = card.split("[\\s,=]+", 2)[0].toUpperCase(Locale.ROOT);
            Map<String, String> operands = new HashMap<>();
            for (String operand : card.substring(function.length()).trim().split(",")) {
                String[] keyValue = operand.split("=", 2);
                if (keyValue.length == 2) {
                    operands.put(keyValue[0].trim().toUpperCase(Locale.ROOT), keyValue[1].trim());
                }
            }
            String ddName = operands.containsKey("DDN") ? operands.get("DDN") : operands.get("DDNAME");
            requests.add(new Request(function, ddName, operands.get("OUTPUT"), i + 1));
        }
        return requests;
    }

    /**
     * The module summaries and control section summaries of an AMBLIST {@code LISTLOAD} report.
     * <p>
     * The summary is read as {@code KEY: value} pairs rather than by column, since the columns are the
     * part of the report that moves between releases; a line with no colon carries on the key above
     * it, which is how a module with several aliases writes them.
     */
    private List<Module> readSummaries(Map<String, Map<String, Translator>> translators) {
        List<Module> modules = new ArrayList<>();
        List<ListLoad.Line> lines = getTree().getLines();
        Building building = null;
        boolean sections = false;
        String key = "";

        for (int i = 0; i < lines.size(); i++) {
            String text = lines.get(i).getText();
            String squeezed = ListLoadLineReader.squeeze(text);
            if (squeezed.startsWith("AMBLIST")) {
                // The page heading falls in the middle of whatever it interrupts.
                continue;
            }
            if (squeezed.contains("MODULESUMMARY")) {
                add(modules, building, translators);
                building = new Building(i + 1);
                sections = false;
                key = "";
                continue;
            }
            if (squeezed.contains("CONTROLSECTIONSUMMARY")) {
                sections = true;
                continue;
            }
            if (squeezed.contains("IDENTIFICATIONRECORDDATA")) {
                add(modules, building, translators);
                building = null;
                continue;
            }
            if (building == null) {
                continue;
            }
            if (sections) {
                readControlSection(building, text, i + 1);
            } else {
                int colon = text.indexOf(':');
                if (colon >= 0) {
                    key = ListLoadLineReader.squeeze(text.substring(0, colon));
                    readSummaryLine(building, key, text.substring(colon + 1).trim(), i + 1);
                } else if (!text.trim().isEmpty()) {
                    readSummaryLine(building, key, text.trim(), i + 1);
                }
            }
        }
        add(modules, building, translators);
        return modules;
    }

    private static void readSummaryLine(Building building, String key, String value, int line) {
        switch (key) {
            case "MEMBERNAME":
                building.name = firstWord(value);
                break;
            case "MAINENTRYPOINT":
                building.entryOffset = normalizeHex(firstWord(value));
                break;
            case "LIBRARY":
                for (String operand : value.split("\\s+")) {
                    String[] keyValue = operand.split("=", 2);
                    if (keyValue.length == 2 && "DDNAME".equalsIgnoreCase(keyValue[0])) {
                        building.library = keyValue[1];
                    } else if (keyValue.length == 2 && "DSNAME".equalsIgnoreCase(keyValue[0])) {
                        building.dataSetName = keyValue[1];
                    }
                }
                break;
            case "ALIAS(ES)":
                String alias = firstWord(value);
                if (!alias.isEmpty() && !"NONE".equalsIgnoreCase(alias)) {
                    building.aliases.add(new Name(alias, line));
                }
                break;
            case "MODULESIZE(HEX)":
                building.size = normalizeHex(firstWord(value));
                break;
            default:
                break;
        }
    }

    /**
     * One row of a control section summary: a CSECT with its origin and length, or an alternate entry
     * point into the CSECT above it. A row is recognised by its two hexadecimal columns rather than by
     * where they fall, so the heading and the blank lines fall out on their own.
     */
    private static void readControlSection(Building building, String text, int line) {
        String[] words = text.trim().split("\\s+");
        if (words.length >= 3 && isHex(words[1]) && isHex(words[2])) {
            building.csects.add(new Csect(words[0], normalizeHex(words[1]), normalizeHex(words[2]),
                    words.length > 3 ? words[3] : null, null, null, new ArrayList<>(), null, line));
            // The entry name and its location are the last two columns, where there are any: a name
            // is not hexadecimal and the AMODE before it is, which is what tells them apart.
            if (words.length >= 7 && isHex(words[words.length - 1]) && !isHex(words[words.length - 2])) {
                lastCsect(building).getEntries().add(
                        new Entry(words[words.length - 2], normalizeHex(words[words.length - 1]), line));
            }
        } else if (words.length == 2 && isHex(words[1]) && !building.csects.isEmpty()) {
            lastCsect(building).getEntries().add(new Entry(words[0], normalizeHex(words[1]), line));
        }
    }

    /**
     * The translator that made each CSECT, from a {@code LISTIDR} report: a {@code CSECT:} line names
     * one and the line under it holds the product number, its version and modification level, and the
     * date it compiled. Keyed by member and then by CSECT, which is how the report itself is keyed.
     */
    private Map<String, Map<String, Translator>> readTranslators() {
        Map<String, Map<String, Translator>> translators = new HashMap<>();
        String member = null;
        String csect = null;
        boolean reading = false;

        for (ListLoad.Line line : getTree().getLines()) {
            String text = line.getText();
            String squeezed = ListLoadLineReader.squeeze(text);
            if (squeezed.contains("IDENTIFICATIONRECORDDATA")) {
                member = wordAfter(text, "MEMBER");
                reading = false;
                csect = null;
                continue;
            }
            if (squeezed.contains("TRANSLATORIDENTIFICATIONDATA")) {
                reading = true;
                csect = null;
                continue;
            }
            if (squeezed.contains("IDENTIFICATIONDATA") || squeezed.contains("USERDATA")) {
                reading = false;
                continue;
            }
            if (!reading || member == null) {
                continue;
            }
            String trimmed = text.trim();
            if (trimmed.toUpperCase(Locale.ROOT).startsWith("CSECT:")) {
                csect = firstWord(trimmed.substring("CSECT:".length()).trim());
                continue;
            }
            String[] words = trimmed.split("\\s+");
            if (csect != null && words.length >= 4) {
                translators.computeIfAbsent(member, k -> new HashMap<>()).put(csect,
                        new Translator(words[0], languageOf(words[0]), words[1] + '.' + words[2], words[3]));
                csect = null;
            }
        }
        return translators;
    }

    /**
     * The module a binder {@code SYSPRINT} reports, from the deck it echoes and the map it printed.
     * <p>
     * The echoed cards are the deck the binder was given, so they are read by the link-edit deck
     * reader rather than a second time here. A card the deck continued past column 72 cannot be
     * recovered — the echo does not show that column — so a continued statement is read as two.
     */
    private List<Module> readBinderListing() {
        List<ListLoad.Line> lines = getTree().getLines();
        List<String> cards = new ArrayList<>();
        List<Integer> cardLines = new ArrayList<>();
        List<Csect> csects = new ArrayList<>();
        boolean map = false;

        for (int i = 0; i < lines.size(); i++) {
            String text = lines.get(i).getText();
            String card = echoedCard(text);
            if (card != null) {
                cards.add(card);
                cardLines.add(i + 1);
                continue;
            }
            String squeezed = ListLoadLineReader.squeeze(text);
            if (squeezed.contains("ENDOFMODULEMAP")) {
                map = false;
            } else if (squeezed.contains("MODULEMAP")) {
                map = true;
            } else if (map) {
                readMapRow(csects, text, i + 1);
            }
        }
        if (cards.isEmpty()) {
            return new ArrayList<>();
        }

        LinkEdit.CompilationUnit deck = LinkEditParser.parse(getTree().getSourcePath(), String.join("\n", cards));
        LinkEditDeck linkEdit = new LinkEditDeck.Matcher().require(deck, null);
        LinkEditDeck.Name module = linkEdit.getModule();
        if (module == null) {
            return new ArrayList<>();
        }

        List<Name> aliases = new ArrayList<>();
        for (LinkEditDeck.Name alias : linkEdit.getAliases()) {
            aliases.add(new Name(alias.getText(), cardLine(cardLines, alias.getLine())));
        }
        LinkEditDeck.Name entry = linkEdit.getEntry();
        List<Module> modules = new ArrayList<>();
        modules.add(new Module(module.getText(), null, null,
                entry == null ? null : new Entry(entry.getText(), offsetOf(csects, entry.getText()),
                        cardLine(cardLines, entry.getLine())),
                aliases, null, csects, cardLine(cardLines, module.getLine())));
        return modules;
    }

    /**
     * One row of a binder module map: a CSECT with the DD and member it was read from, or a label
     * within the CSECT above it. The label's offset is printed relative to its section, so it is added
     * to the section's own origin to say where in the module it lands, which is what AMBLIST prints.
     */
    private static void readMapRow(List<Csect> csects, String text, int line) {
        String[] words = text.trim().split("\\s+");
        if (words.length < 3 || !isHex(words[0])) {
            return;
        }
        if ("CSECT".equalsIgnoreCase(words[2])) {
            String member = words.length > 6 ? words[6] : null;
            csects.add(new Csect(words[1], normalizeHex(words[0]),
                    words.length > 3 ? normalizeHex(words[3]) : "0", "SD",
                    words.length > 4 ? words[4] : null,
                    member == null || member.startsWith("**") ? null : member,
                    new ArrayList<>(), null, line));
        } else if ("LABEL".equalsIgnoreCase(words[2]) && !csects.isEmpty()) {
            Csect csect = csects.get(csects.size() - 1);
            csect.getEntries().add(new Entry(words[1], hex(parseHex(csect.getOffset()) + parseHex(words[0])), line));
        }
    }

    /**
     * The card a {@code IEW2322I} message echoes, with the message's own fields taken off, or null for
     * any other line.
     */
    private static @Nullable String echoedCard(String text) {
        String trimmed = text.trim();
        if (!trimmed.startsWith("IEW2322I")) {
            return null;
        }
        String[] words = trimmed.split("\\s+", 4);
        return words.length < 4 ? "" : words[3];
    }

    private static int cardLine(List<Integer> cardLines, int card) {
        return card >= 1 && card <= cardLines.size() ? cardLines.get(card - 1) : 1;
    }

    /**
     * Where a name the deck named as its entry point lands in the module, which the binder map says
     * only by having placed the CSECT or the label of that name.
     */
    private static @Nullable String offsetOf(List<Csect> csects, String name) {
        for (Csect csect : csects) {
            if (csect.getName().equalsIgnoreCase(name)) {
                return csect.getOffset();
            }
            for (Entry entry : csect.getEntries()) {
                if (entry.getName().equalsIgnoreCase(name)) {
                    return entry.getOffset();
                }
            }
        }
        return null;
    }

    private static void add(List<Module> modules, @Nullable Building building,
                            Map<String, Map<String, Translator>> translators) {
        if (building == null || building.name == null || building.name.isEmpty()) {
            return;
        }
        Map<String, Translator> byCsect = translators.getOrDefault(building.name, new HashMap<>());
        List<Csect> csects = new ArrayList<>(building.csects.size());
        for (Csect csect : building.csects) {
            csects.add(csect.withTranslator(byCsect.get(csect.getName())));
        }
        modules.add(new Module(building.name, building.library, building.dataSetName,
                entryAt(csects, building.entryOffset), building.aliases, building.size, csects,
                building.line));
    }

    /**
     * The name control arrives at, which the module summary gives as an offset. A module entered at a
     * label — every DL/I program is — says so by that offset matching an entry name rather than the
     * start of a CSECT.
     */
    private static @Nullable Entry entryAt(List<Csect> csects, @Nullable String offset) {
        if (offset == null) {
            return null;
        }
        for (Csect csect : csects) {
            for (Entry entry : csect.getEntries()) {
                if (entry.getOffset().equals(offset)) {
                    return entry;
                }
            }
        }
        for (Csect csect : csects) {
            if (csect.getOffset().equals(offset)) {
                return new Entry(csect.getName(), offset, csect.getLine());
            }
        }
        return null;
    }

    private static Csect lastCsect(Building building) {
        return building.csects.get(building.csects.size() - 1);
    }

    private static String firstWord(String value) {
        String[] words = value.trim().split("\\s+", 2);
        return words[0];
    }

    private static @Nullable String wordAfter(String text, String word) {
        String[] words = text.trim().split("\\s+");
        for (int i = 0; i < words.length - 1; i++) {
            if (word.equalsIgnoreCase(words[i])) {
                return words[i + 1];
            }
        }
        return null;
    }

    private static boolean isHex(String word) {
        if (word.isEmpty() || word.length() > 16) {
            return false;
        }
        for (int i = 0; i < word.length(); i++) {
            if (Character.digit(word.charAt(i), 16) < 0) {
                return false;
            }
        }
        return true;
    }

    private static String normalizeHex(String word) {
        return isHex(word) ? hex(parseHex(word)) : word;
    }

    private static String hex(long value) {
        return Long.toHexString(value).toUpperCase(Locale.ROOT);
    }

    private static long parseHex(String word) {
        return isHex(word) ? Long.parseLong(word, 16) : 0;
    }

    /**
     * The language a translator's product number says it compiled. A product this does not know is
     * carried by number rather than guessed at: a shop's own preprocessor writes an identification
     * record too, and so does every compiler IBM does not sell.
     */
    private static Language languageOf(String productId) {
        String product = productId.replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
        for (String cobol : COBOL_PRODUCTS) {
            if (product.startsWith(cobol)) {
                return Language.COBOL;
            }
        }
        if (product.startsWith(ASSEMBLER_PRODUCT)) {
            return Language.ASSEMBLER;
        }
        for (String pli : PLI_PRODUCTS) {
            if (product.startsWith(pli)) {
                return Language.PLI;
            }
        }
        return Language.UNKNOWN;
    }

    /**
     * What a report says about one load module.
     */
    @Value
    public static class Module {
        String name;

        /**
         * The DD the library was read through, which only AMBLIST reports.
         */
        @Nullable
        String library;

        @Nullable
        String dataSetName;

        /**
         * Where control arrives, resolved from the offset the report gives to the name that stands
         * there.
         */
        @Nullable
        Entry entry;

        /**
         * The other names the module answers to. Each is a directory entry of its own, so a step or a
         * {@code CALL} naming one finds this module.
         */
        List<Name> aliases;

        @Nullable
        String size;

        List<Csect> csects;

        /**
         * The one-based line the module's summary opens on.
         */
        int line;
    }

    /**
     * A control section the module holds: what the compiler or the assembler produced and the binder
     * placed. A program's own CSECT carries the program's name; the rest are the stubs a language
     * interface supplies and the runtime the autocall pulled in.
     */
    @Value
    public static class Csect {
        String name;

        /**
         * Where the section starts in the module.
         */
        String offset;

        String length;

        /**
         * {@code SD} for a section definition, as both reports write it.
         */
        @Nullable
        String type;

        /**
         * The DD the section was read from, which only a binder map reports.
         */
        @Nullable
        String ddName;

        /**
         * The member the section was read from, or null for the object the step itself compiled.
         */
        @Nullable
        String member;

        /**
         * The alternate names control may arrive at within this section: {@code DFHEI1} in
         * {@code DFHECI}, {@code CBLTDLI} in {@code DFSLI000}, the {@code DLITCBL} a DL/I program
         * declares.
         */
        List<Entry> entries;

        /**
         * What compiled it, which only a {@code LISTIDR} report says.
         */
        @Nullable
        Translator translator;

        int line;

        public Csect withTranslator(@Nullable Translator translator) {
            return translator == this.translator ? this :
                    new Csect(name, offset, length, type, ddName, member, entries, translator, line);
        }
    }

    /**
     * A name control may arrive at, and where in the module it stands.
     */
    @Value
    public static class Entry {
        String name;

        @Nullable
        String offset;

        int line;
    }

    /**
     * A name the report writes down, and the one-based line it was written on.
     */
    @Value
    public static class Name {
        String text;
        int line;
    }

    /**
     * What compiled a CSECT, from its identification record.
     */
    @Value
    public static class Translator {
        /**
         * The product number as the record holds it, such as {@code 5648A25} for COBOL for OS/390.
         */
        String productId;

        Language language;

        /**
         * The version and modification level, as {@code version.modification}.
         */
        String version;

        String date;
    }

    /**
     * A report AMBLIST was asked for.
     */
    @Value
    public static class Request {
        String function;

        @Nullable
        String ddName;

        @Nullable
        String output;

        int line;
    }

    public enum Language {
        COBOL,
        PLI,
        ASSEMBLER,
        UNKNOWN
    }

    public static class Matcher extends SimpleTraitMatcher<ModuleListing> {

        @Override
        protected @Nullable ModuleListing test(Cursor cursor) {
            return cursor.getValue() instanceof ListLoad.CompilationUnit ? new ModuleListing(cursor) : null;
        }
    }

    /**
     * A module being read out of a summary, before the control section summary under it has said what
     * its entry point is called.
     */
    private static class Building {
        final int line;
        final List<Name> aliases = new ArrayList<>();
        final List<Csect> csects = new ArrayList<>();

        @Nullable
        String name;

        @Nullable
        String library;

        @Nullable
        String dataSetName;

        @Nullable
        String entryOffset;

        @Nullable
        String size;

        Building(int line) {
            this.line = line;
        }
    }

    @Override
    public String toString() {
        return "LISTING " + getTree().getSourcePath();
    }
}
