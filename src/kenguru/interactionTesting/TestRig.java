package kenguru.interactionTesting;

import java.io.PrintStream;
import java.util.Scanner;

/**
 * Contains stream wrappers to communicate with other threads.
 * @author kenguru
 */
public record TestRig(PrintStream in, Scanner out) {
}
