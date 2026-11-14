package ru.urfu.practicalwork2;

import java.util.Scanner;

final class ConsoleInput {
    private ConsoleInput() {
    }

    static int readNonNegativeInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(input);
                if (value >= 0) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
            }
            System.out.println("введите целое неотрицательное число:");
        }
    }
}
