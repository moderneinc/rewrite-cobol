/*
 * For commercial customers of Moderne Inc., this repository is licensed per the terms of our contract.
 * For everyone else, this is licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International.
 * See: https://creativecommons.org/licenses/by-nc-sa/4.0/
 */
package org.openrewrite.controlm;

import java.util.Scanner;

public class ControlMLineReader {

    public static String readLines(String source) {
        StringBuilder p = new StringBuilder();

        int cursor = 0;
        Scanner scanner = new Scanner(source);
        Section section = null;
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String prefix = getPrefix(line);
            p.append(prefix);
            line = line.substring(prefix.length());
            // A space is included after markers since the names may include any characters aside from whitespace.
            if ("+---------------------------------- BROWSE -----------------------------------+".equals(line.trim())) {
                section = Section.DEFINITION;
                p.append("<<DEFINITION_START>> ");
                p.append(line);
            } else if ("| =========================================================================== |".equals(line.trim())) {
                if (section == Section.DEFINITION) {
                    p.append("<<DEFINITION_END>> ");
                    p.append("<<SCHEDULE_START>> ");
                    section = Section.SCHEDULE;
                } else if (section == Section.SCHEDULE) {
                    p.append("<<SCHEDULE_END>> ");
                    p.append("<<INPUT_START>> ");
                    section = Section.INPUT;
                } else if (section == Section.INPUT) {
                    p.append("<<INPUT_END>> ");
                    p.append("<<OUTPUT_START>> ");
                    section = Section.OUTPUT;
                } else if (section == Section.OUTPUT) {
                    p.append("<<OUTPUT_END>> ");
                    p.append("<<APP_FORM_START>> ");
                    section = Section.APP_FORM;
                }
                p.append(line);
            } else {
                if (section == Section.INPUT && line.trim().startsWith("|")) {
                    String firstWord = line.trim().substring(1);
                    if (firstWord.trim().startsWith("IN ")) {
                        p.append("<<INPUT_NAMES_START>> ");
                    } else if (firstWord.trim().startsWith("CONTROL ")) {
                        p.append("<<INPUT_NAMES_END>> ");
                    }
                } else if (section == Section.OUTPUT) {
                    String firstWord = line.trim().substring(1);
                    if (firstWord.trim().startsWith("OUT ")) {
                        p.append("<<OUTPUT_NAMES_START>> ");
                    } else if (firstWord.trim().startsWith("AUTO-ARCHIVE ")) {
                        p.append("<<OUTPUT_NAMES_END>> ");
                    }
                }
                // `|` characters are used as column areas for the Control-M template.
                // LINE_START and LINE_END are used to mark the beginning and end of a line
                // so that the `|` is not converted to a NAME token and simplify parsing whitespace.
                p.append("<<LINE_START>>");
                p.append(line);
                p.append("<<LINE_END>>");
            }

            cursor += prefix.length();
            cursor += line.length();
            String endOfLine = source.substring(cursor);
            if (endOfLine.startsWith("\r\n")) {
                p.append("\r\n");
                cursor += 2;
            } else if (endOfLine.startsWith("\n")) {
                p.append("\n");
                cursor += 1;
            }
        }
        return p.toString();
    }

    private static String getPrefix(String line) {
        int i = 0;
        for (char c : line.toCharArray()) {
            if (!Character.isWhitespace(c)) {
                break;
            }
            i++;
        }
        return line.substring(0, i);
    }

    private enum Section {
        DEFINITION,
        SCHEDULE,
        INPUT,
        OUTPUT,
        APP_FORM
    }
}
