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

            if ("+---------------------------------- BROWSE -----------------------------------+".equals(line.trim())) {
                section = Section.DEFINITION;
                p.append("|DEFINITION_START|");
            } else if ("| =========================================================================== |".equals(line.trim())) {
                if (section == Section.DEFINITION) {
                    p.append("|DEFINITION_END|");
                    p.append("|SCHEDULE_START|");
                    section = Section.SCHEDULE;
                } else if (section == Section.SCHEDULE) {
                    p.append("|SCHEDULE_END|");
                    p.append("|INPUT_START|");
                    section = Section.INPUT;
                } else if (section == Section.INPUT) {
                    p.append("|INPUT_END|");
                    p.append("|OUTPUT_START|");
                    section = Section.OUTPUT;
                } else if (section == Section.OUTPUT) {
                    p.append("|OUTPUT_END|");
                    p.append("|APP_FORM_START|");
                    section = Section.APP_FORM;
                }
            }
            p.append(line);

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

    private enum Section {
        DEFINITION,
        SCHEDULE,
        INPUT,
        OUTPUT,
        APP_FORM
    }
}
