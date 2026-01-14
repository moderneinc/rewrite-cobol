package io.moderne.cobol;

import lombok.RequiredArgsConstructor;
import org.fusesource.jansi.*;
import org.fusesource.jansi.io.AnsiOutputStream;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jspecify.annotations.Nullable;
import org.openrewrite.polyglot.ProgressBar;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.moderne.cobol.internal.DurationUtils.formatDuration;
import static io.moderne.cobol.internal.ModerneColors.Blue;
import static org.fusesource.jansi.Ansi.ansi;
import static org.openrewrite.internal.StringUtils.repeat;

public class DefaultProgressBar implements AutoCloseable, ProgressBar {
    private static final int DEFAULT_TERMINAL_WIDTH = 80;
    private static final ScheduledThreadPoolExecutor EXECUTOR = new ScheduledThreadPoolExecutor(1, runnable -> {
        Thread thread = Executors.defaultThreadFactory().newThread(runnable);
        thread.setName("progress-bar");
        return thread;
    });
    private static Terminal terminal = null;

    private final long start = System.nanoTime();
    private final int maxLength = getTerminalWidth();
    private final PrintWriter out;

    @Nullable
    private final String task;

    private final boolean continuousUpdate;
    private final String finishMessage;
    private final ScheduledFuture<?> scheduledUpdate;

    @Nullable
    private String extraMessage;

    private int max = Integer.MAX_VALUE;

    private int current;
    private volatile int lastRenderLength;
    private volatile boolean first = true;
    private volatile boolean finished;

    protected DefaultProgressBar(PrintWriter out, @Nullable String task,
                                 Duration updateInterval, boolean continuousUpdate, String finishMessage) {
        this.out = out;
        this.task = task;
        this.continuousUpdate = continuousUpdate;
        this.finishMessage = finishMessage;

        // FIXME updateInterval should be LONGER when in CI so that \r normalization to
        // newline in e.g. Jenkins doesn't cause too much output.
        this.scheduledUpdate = EXECUTOR.scheduleAtFixedRate(() -> {
            if (!finished) {
                refresh();
            }
        }, 0, updateInterval.toMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public void intermediateResult(@Nullable String message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        eraseLine();
        out.println(message + repeat(" ", Math.max(0, lastRenderLength - message.length())));
        out.print(render());
        out.flush();
    }

    /**
     * Replaces the progress bar line with a result.
     *
     * @param message A full sentence message used to describe to a developer what has happened
     *                as a result of the task this progress bar was tracking.
     */
    @Override
    public void finish(String message) {
        if (!finished) {
            finished = true;
            eraseLine();
            if (!message.isEmpty()) {
                Matcher matcher = Pattern.compile("(.*)(\\n+)$").matcher(message);
                String progressBarLine = (matcher.matches() ? matcher.group(1) : message) + " " + String.format(ELAPSED, formatDuration(totalElapsed()));
                out.print(progressBarLine + repeat(" ", Math.max(0, lastRenderLength - progressBarLine.length())));
                out.println(matcher.matches() ? matcher.group(2) : "");
            }
        }
    }

    @Override
    public void close() {
        finish(finishMessage);
        scheduledUpdate.cancel(true);
    }

    private void eraseLine() {
        out.print('\r');
    }

    @Override
    public void step() {
        current++;
        if (continuousUpdate) {
            refresh();
        }
    }

    @Override
    @SuppressWarnings("UnusedReturnValue")
    public ProgressBar setExtraMessage(String extraMessage) {
        this.extraMessage = extraMessage;
        if (continuousUpdate) {
            refresh();
        }
        return this;
    }

    @Override
    public DefaultProgressBar setMax(int max) {
        this.current = 0;
        this.max = max;
        if (continuousUpdate) {
            refresh();
        }
        return this;
    }

    private static final int BAR_LENGTH = 13;
    private static final Ansi LEFT_BRACKET = ansi().fgRgb(Blue.rgb()).a("▕");
    private static final Ansi RIGHT_BRACKET = ansi().fgRgb(Blue.rgb()).a("▏").reset();
    private static final Ansi START_BLOCK = ansi().fgRgb(Blue.rgb());
    private static final String BLOCK = "=";
    private static final String SPACE = "-";
    private static final char HEAD = '>';
    private static final Ansi TITLE_FORMAT = ansi().fgRgb(Blue.rgb());
    private static final String ELAPSED = "(%s)";
    private static final Ansi EXTRA_FORMAT = ansi().fgRgb(Blue.rgb());

    private String render() {
        Duration totalElapsed = totalElapsed();
        StringBuilder sb = new StringBuilder(maxLength);

        if (max == Integer.MAX_VALUE) {
            sb.append(LEFT_BRACKET);
            int pos = (int) (totalElapsed.getSeconds() * 10 + totalElapsed.getNano() / 100000000) % ((BAR_LENGTH) * 2);
            if (pos > BAR_LENGTH) {
                pos = (BAR_LENGTH) * 2 - pos;
            }
            sb.append(START_BLOCK);
            sb.append(repeat(SPACE, pos));
            sb.append(BLOCK).append(BLOCK).append(HEAD);
            sb.append(repeat(SPACE, BAR_LENGTH - pos));
        } else {
            int percentage = (int) (normalizedProgress() * 100);
            sb.append(ansi().fgRgb(Blue.rgb()).a(String.format("%3d%%", percentage)));
            sb.append(' ');

            sb.append(LEFT_BRACKET);

            int integral = (int) (normalizedProgress() * BAR_LENGTH);
            sb.append(START_BLOCK);
            sb.append(repeat(BLOCK, integral));
            if (current < max) {
                sb.append(HEAD);
                sb.append(' ');
                sb.append(repeat(SPACE, BAR_LENGTH - integral - 1));
            }
        }
        sb.append(RIGHT_BRACKET);
        sb.append(' ');

        sb.append(TITLE_FORMAT);

        if (task != null) {
            sb.append(task);
            sb.append(' ');
        }

        String elapsed = String.format(ELAPSED, formatDuration(totalElapsed));
        sb.append(elapsed);

        if (extraMessage != null && !extraMessage.isEmpty()) {
            if (task != null) {
                // separation between task and extra message
                sb.append(' ');
            }
            sb.append(EXTRA_FORMAT);
            int remainingLength = maxLength - BAR_LENGTH - (task == null ? 0 : task.length()) - elapsed.length() - 10;
            sb.append(truncateExtraMessage(extraMessage, remainingLength));
        }

        sb.append(ansi().reset());

        // empty out the rest of the line (if the last render was longer than the current one)
        for (int i = 0; i < Math.max(lastRenderLength - strippedLength(sb.toString()), 0); i++) {
            sb.append(' ');
        }

        String s = sb.toString();
        lastRenderLength = strippedLength(s);
        return s;
    }

    private void refresh() {
        try {
            if (!first) {
                out.print('\r');
            }
            out.print(render());
            out.flush();
            first = false;
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static int strippedLength(String ansi) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        AnsiPrintStream ps = new AnsiPrintStream(new AnsiOutputStream(
                bos, new AnsiOutputStream.ZeroWidthSupplier(),
                AnsiMode.Strip, null, AnsiType.Unsupported, AnsiColors.Colors256,
                StandardCharsets.UTF_8, null, null, false), true);
        ps.append(ansi);
        return bos.size();
    }

    private Duration totalElapsed() {
        return Duration.ofNanos(System.nanoTime() - start);
    }

    private static String truncateExtraMessage(String message, int remainingLength) {
        if (remainingLength <= 0) {
            // no room to write the extra message
            return "";
        }
        if (message.length() <= remainingLength) {
            return message;
        }
        return "..." + message.substring(Math.max(message.length() - remainingLength - 3, 0));
    }

    private double normalizedProgress() {
		if (max <= 0) {
			return 0.0;
		}
		if (current > max) {
			return 1.0;
		}
		return ((double) current) / max;
	}

    public static Builder builder(CommandLine.Model.CommandSpec spec) {
        return new Builder(spec);
    }

    @RequiredArgsConstructor
    public static class Builder {
        private final CommandLine.Model.CommandSpec spec;

        @Nullable
        private String task;

        private Duration updateInterval = Duration.ofSeconds(1);
        private boolean continuousUpdate = true;
        private String finishMessage = "Done\n";

        public Builder task(String task) {
            this.task = task;
            return this;
        }

        public Builder updateInterval(Duration updateInterval) {
            this.updateInterval = updateInterval;
            return this;
        }

        public Builder continuousUpdate(boolean continuousUpdate) {
            this.continuousUpdate = continuousUpdate;
            return this;
        }

        public Builder finishMessage(String finishMessage) {
            this.finishMessage = finishMessage;
            return this;
        }

        public DefaultProgressBar build() {
            return new DefaultProgressBar(spec.commandLine().getOut(), task,
                    updateInterval, continuousUpdate, finishMessage);
        }
    }

    public synchronized static int getTerminalWidth() {
        Terminal terminal = getTerminal();
        int width = terminal.getWidth();

        // Workaround for issue #23 under IntelliJ
        return (width >= 10) ? width : DEFAULT_TERMINAL_WIDTH;
    }

    /**
     * <ul>
     *     <li>Creating a terminal is relatively expensive, usually takes between 5-10ms.
     *         <ul>
     *             <li>If updateInterval is set under 10ms creating a new terminal for on every re-render of progress
     *             bar could be a problem.</li>
     *             <li>Especially when multiple progress bars are running in parallel.</li>
     *         </ul>
     *     </li>
     *     <li>Another problem with {@link Terminal} is that once created you can create another instance
     *     (say from different thread), but this instance will be "dumb" until the previously created
     *     terminal is closed.
     *     </li>
     * </ul>
     */
    private static Terminal getTerminal() {
        if (terminal == null) {
            try {
                // Issue #42
                // Defaulting to a dumb terminal when a supported terminal can not be correctly created
                // see https://github.com/jline/jline3/issues/291
                terminal = TerminalBuilder.builder().dumb(true).build();
            } catch (IOException e) {
                throw new RuntimeException("This should never happen! Dumb terminal should have been created.");
            }
        }
        return terminal;
    }
}
