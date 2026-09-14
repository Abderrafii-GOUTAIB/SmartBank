package org.example.service;

import java.util.Scanner;

public class InputParser {

    // ================================================
    // SAFE PARSING — using wrapper classes
    // ================================================

    // Try to parse an integer from user input
    // Returns an Integer (wrapper) — can be null if parsing fails!
    // A primitive int can NEVER be null, but Integer CAN
    // This is a key reason wrappers exist
    public static Integer tryParseInt(String input) {
        try {
            return Integer.parseInt(input.trim()); // AUTOBOXING: int → Integer
        } catch (NumberFormatException e) {
            return null; // only possible because return type is Integer, not int
        }
    }

    // Try to parse a double from user input
    public static Double tryParseDouble(String input) {
        try {
            return Double.parseDouble(input.trim()); // AUTOBOXING: double → Double
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // Read an int from scanner with validation and retry
    public static int readInt(Scanner scanner, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine();

            Integer parsed = tryParseInt(input); // returns Integer (nullable)

            if (parsed == null) {
                System.out.println("❌ Please enter a valid number!");
                continue; // skip to next iteration — ask again
            }

            // UNBOXING happens here: Integer → int (automatic)
            int value = parsed; // UNBOXING: Integer automatically unwrapped to int

            if (value < min || value > max) {
                System.out.println("❌ Please enter a number between " + min + " and " + max);
                continue;
            }

            return value; // returns int (primitive)
        }
    }

    // Read a positive double from scanner
    public static double readPositiveDouble(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine();

            Double parsed = tryParseDouble(input);

            if (parsed == null) {
                System.out.println("❌ Please enter a valid number!");
                continue;
            }

            // UNBOXING: Double → double
            double value = parsed;

            if (value <= 0) {
                System.out.println("❌ Amount must be positive!");
                continue;
            }

            return value;
        }
    }

    // ================================================
    // WRAPPER COMPARISON TRAP — important lesson
    // ================================================

    public static void demonstrateWrapperTrap() {
        System.out.println("--- Wrapper Comparison Trap ---");

        // Java caches Integer values from -128 to 127
        Integer a = 127;  // autoboxing
        Integer b = 127;  // autoboxing — reuses cached object
        System.out.println("127 == 127:  " + (a == b));       // true (cached!)
        System.out.println("127 equals:  " + a.equals(b));    // true

        Integer c = 128;  // autoboxing
        Integer d = 128;  // autoboxing — new object (outside cache!)
        System.out.println("128 == 128:  " + (c == d));       // FALSE! different objects
        System.out.println("128 equals:  " + c.equals(d));    // true ✅

        System.out.println();
        System.out.println("⚠️  RULE: ALWAYS use .equals() for wrapper comparison, never ==");
        System.out.println("    (Same rule as String!)");
    }
}