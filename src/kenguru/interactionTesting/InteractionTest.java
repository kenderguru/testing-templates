package kenguru.interactionTesting;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Scanner;

/**
 * Artemis-style tests that test and interact with programs.
 * @author kenguru
 */
public final class InteractionTest {
    private final Runnable runnable;
    private final Class<? extends Throwable> expectedThrow;
    private final TestElement[] elements;
    private Throwable throwOnThread = null;
    private boolean hasStarted = false;
    /**
     * Create a test that interacts with the program tested through {@code System.in} and {@code System.out}. Expects no throws.
     * @param runnable the program tested
     * @param interaction the lines of interaction. Input has to be prefixed with '<', expected output with '>' and each line is automatically suffixed with '\n'
     * @throws IllegalArgumentException when a line misses a prefix
     */
    public InteractionTest(Runnable runnable, String[] interaction) {
        this(runnable, null, interaction);
    }
    /**
     * Create a test that interacts with the program tested through {@code System.in} and {@code System.out}.
     * @param runnable the program tested
     * @param expectedThrow the type of exception expected to be thrown
     * @param interaction the lines of interaction. Input has to be prefixed with '<', expected output with '>' and each line is automatically suffixed with '\n'
     * @throws IllegalArgumentException when a line misses a prefix
     */
    public InteractionTest(Runnable runnable, Class<? extends Throwable> expectedThrow, String[] interaction) {
        TestElement[] elements = new TestElement[interaction.length];
        for (int i = 0; i < elements.length; i++) {
            switch (interaction[i].charAt(0)) {
                case '<':
                    elements[i] = new TestElement(interaction[i].substring(1), true);
                    break;
                case '>':
                    elements[i] = new TestElement(interaction[i].substring(1), false);
                    break;
                default:
                    throw new IllegalArgumentException("All interaction lines have to start with '<' or '>'!");
            }
        }
        this.elements = elements;
        this.runnable = runnable;
        this.expectedThrow = expectedThrow;
    }
    /**
     * Runs the interaction. Each interaction can only be run once.
     * @param millis The maximum time the program may run
     * @throws IllegalStateException when {@code run()} was already called on this object
     */
    public void run(long millis) {
        long endTime = System.currentTimeMillis() + millis;
        if (hasStarted) {
            throw new IllegalStateException("Interactions can only be run once!");
        }
        TestRig streams = testSetup();

        Thread t = new Thread(runnable);
        t.setDaemon(true);
        t.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
            public void uncaughtException(Thread thread, Throwable throwable){
                throwOnThread = throwable;
            }
        });
        t.start();

        for (TestElement te : elements) {
            if (te.isInput()) {
                streams.in().println(te.line());
            } else {
                while (!streams.out().hasNextLine()) {
                    if (System.currentTimeMillis() > endTime) {
                        assertFalse("Program does not terminate!",t.isAlive());
                        assertTrue("Program terminates early!",t.isAlive());
                        assertEquals("Program threw early!", null, throwOnThread);
                    }
                }
                assertEquals(streams.out().nextLine(),te.line());
            }
        }

        while (System.currentTimeMillis() <= endTime && throwOnThread == null && t.isAlive()) {}

        if (throwOnThread == null) {
            assertFalse("Program does not terminate!",t.isAlive());
        }
        if (expectedThrow != null) {
            assertThrows(expectedThrow, () -> {
                if (throwOnThread != null)
                    throw throwOnThread;
            });
        }
    }
    private static TestRig testSetup(){
        PipedInputStream testIn = new PipedInputStream();
        PipedOutputStream progOut = new PipedOutputStream();
        PrintStream progPrint = new PrintStream(progOut);

        PipedInputStream progIn = new PipedInputStream();
        PipedOutputStream testOut = new PipedOutputStream();
        PrintStream testPrint = new PrintStream(testOut);
        try {
            progIn.connect(testOut);
            progOut.connect(testIn);
        } catch (IOException e) {
            System.err.println(":(");
        }
        System.setOut(progPrint);
        System.setIn(progIn);

        return new TestRig(testPrint, new Scanner(testIn));
    }
}
