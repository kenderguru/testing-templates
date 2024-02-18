package kenguru.interactionTesting;

import java.util.Scanner;

import org.junit.Test;

/**
 * Example code that shows InteractionTest usage.
 * @author kenguru
 */
public class Testing {
    /**
     * Example code; tests a simple interaction.
     */
    @Test
    public void interaction() {
        new InteractionTest(() -> Testing.main(null), new String[]{
            "<3",
            "<10",
            ">0,30"
        }).run(10000);
    }
    /**
     * Example code; tests a simple interaction.
     */
    @Test
    public void testingTestClass() {
        new InteractionTest(() -> {
            Scanner s = new Scanner(System.in);
            System.out.println(s.nextLine());
            s.close();
        }, new String[]{
            "<3",
            ">3"
        }).run(1000);
    }
    /**
     * Example code; tests a function for an exception throw.
     */
    @Test
    public void interactionWithException() {
        new InteractionTest(() -> Testing.main(null), IllegalArgumentException.class, new String[]{
            "<3",
            "<0"
        }).run(100);
    }
    /**
     * Example code; tested function
     * @param args command line arguments
     */
    public static void main(String[] args) {
        Scanner s = new Scanner(System.in);
        int a = s.nextInt();
        int b = s.nextInt();
        s.close();
        if (b == 0) {
            throw new IllegalArgumentException("Cannot divide by zero!");
        }
        System.out.println(String.format("%.2f", a/(double)b));
    }
}
