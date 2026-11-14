package ru.urfu.practicalwork2;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

public final class QuickSortTask {
    private QuickSortTask() {
    }

    public static void quickSort(List<Double> values) {
        Objects.requireNonNull(values, "Список null");
        for (Double value : values) {
            Objects.requireNonNull(value, "Список содержит null");
        }
        if (values.size() < 2) {
            return;
        }

        Deque<Range> pendingRanges = new ArrayDeque<>();
        pendingRanges.push(new Range(0, values.size() - 1));

        while (!pendingRanges.isEmpty()) {
            Range range = pendingRanges.pop();
            int low = range.low();
            int high = range.high();

            while (low < high) {
                int pivotIndex = ThreadLocalRandom.current().nextInt(low, high + 1);
                double pivot = values.get(pivotIndex);

                int less = low;
                int current = low;
                int greater = high;

                while (current <= greater) {
                    int comparison = Double.compare(values.get(current), pivot);
                    if (comparison < 0) {
                        swap(values, less++, current++);
                    } else if (comparison > 0) {
                        swap(values, current, greater--);
                    } else {
                        current++;
                    }
                }

                int leftSize = less - low;
                int rightSize = high - greater;

                if (leftSize < rightSize) {
                    if (greater + 1 < high) {
                        pendingRanges.push(new Range(greater + 1, high));
                    }
                    high = less - 1;
                } else {
                    if (low < less - 1) {
                        pendingRanges.push(new Range(low, less - 1));
                    }
                    low = greater + 1;
                }
            }
        }
    }

    private static void swap(List<Double> values, int first, int second) {
        if (first == second) {
            return;
        }
        Double temporary = values.get(first);
        values.set(first, values.get(second));
        values.set(second, temporary);
    }

    private record Range(int low, int high) {
    }

    public static ArrayList<Double> generateRandomList(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("Размер списка отрицательный");
        }

        ArrayList<Double> values = new ArrayList<>(size);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < size; i++) {
            values.add(random.nextDouble(-1_000.0, 1_000.0));
        }
        return values;
    }

    public static void printList(String title, List<Double> values) {
        System.out.print(title + ": [");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                System.out.print(", ");
            }
            System.out.printf(Locale.US, "%.2f", values.get(i));
        }
        System.out.println("]");
    }

    public static void run(Scanner scanner) {
        int size = ConsoleInput.readNonNegativeInt(scanner, "Введите количество элементов списка: ");
        ArrayList<Double> values = generateRandomList(size);

        printList("Исходный список", values);
        quickSort(values);
        printList("Отсортированный список", values);
    }

    public static void main(String[] args) {
        run(new Scanner(System.in));
    }
}
