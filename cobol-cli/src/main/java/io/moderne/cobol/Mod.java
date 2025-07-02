package io.moderne.cobol;

import io.moderne.cobol.internal.DurationUtils;
import io.moderne.cobol.internal.FatalException;
import io.moderne.cobol.internal.ModerneColors;
import org.openrewrite.internal.StringUtils;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;

import java.io.PrintWriter;
import java.time.Duration;

import static io.moderne.cobol.internal.ExceptionPrinter.getSanitizedStackTrace;
import static org.fusesource.jansi.Ansi.ansi;

@Command(name = "mod-cobol",
        description = "Automated code remediation.",
        subcommands = {
                HelpCommand.class,
                Build.class,
                Publish.class,
        })
public class Mod {
    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    private int executionStrategy(CommandLine.ParseResult parseResult) {
        long start = System.nanoTime();
        int result;
        try {
            result = new CommandLine.RunLast().execute(parseResult);
            spec.commandLine().getOut().println(ansi().bold().fgRgb(ModerneColors.Green.rgb())
                    .a("\nMOD-COBOL SUCCEEDED").reset()
                    .a(" in (" + DurationUtils.formatDuration(Duration.ofNanos(System.nanoTime() - start)) + ")"));
        } catch (Throwable e) {
            handleError(start, e);
            result = -1;
        }
        return result;
    }

    private void handleError(long start, Throwable t) {
        if (t instanceof CommandLine.ExecutionException) {
            t = t.getCause();
        }

        PrintWriter out = spec.commandLine().getOut();
        out.println(ansi().fgRgb(ModerneColors.Red.rgb())
                .a("\nFAILURE: mod failed with an exception.\n").reset());

        out.println("* Where:");
        out.println(getSanitizedStackTrace(t));

        out.println("\n* What went wrong:");
        if (t.getMessage() == null) {
            out.println("An unexpected exception was thrown that didn't have a message on it. See the stack trace above.");
        } else {
            out.println(t.getMessage());
        }

        out.println("\n* Try:");
        out.println(ansi().fgRgb(ModerneColors.Yellow.rgb()).a(">").reset().a(" Report to ")
                .bold().a("support@moderne.io").boldOff());
        if (t instanceof FatalException) {
            for (String fixSuggestion : ((FatalException) t).getFixSuggestions()) {
                out.println(ansi().fgRgb(ModerneColors.Yellow.rgb()).a(">").reset() + fixSuggestion);
            }
        }

        out.println(ansi().bold().fgRgb(ModerneColors.Red.rgb())
                .a("\nMOD-COBOL FAILED").reset()
                .a(" in (" + DurationUtils.formatDuration(Duration.ofNanos(System.nanoTime() - start)) + ")"));
    }

    public static void main(String... args) {
        Mod mod = new Mod();
        System.setProperty("picocli.disable.closures", "true");

        if (args.length == 0 || !"detect".equals(args[0])) {
            drawBanner();
        }

        System.exit(new CommandLine(mod)
                .setExecutionStrategy(mod::executionStrategy)
                // @458 Help is normally called by default with PicoCLI, but that fails in GraalVM Native Image
                .execute(args.length == 0 ? new String[]{"help"} : args));
    }

    private static void drawBanner() {
        String versionText = "Moderne CLI for COBOL";

        String margin = StringUtils.repeat(" ", (versionText.length() - 16) / 2);
        System.out.println(
                margin + "@@@@          @@\n" +
                margin + "@    @@@   @@  @\n" +
                margin + "@      @  @@   @\n" +
                margin + "@@@@@@@@@@@@@@@@\n" +
                margin + "@     @ @      @\n" +
                margin + "@   @@  @@@@@@@@\n" +
                margin + "@ @@    @      @\n" +
                margin + "@@@@@@@@@@@@@@@@"
        );
        System.out.println(versionText + "\n");
    }
}
