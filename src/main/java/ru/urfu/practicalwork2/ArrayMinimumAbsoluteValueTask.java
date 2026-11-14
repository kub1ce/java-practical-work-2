package ru.urfu.practicalwork2;

import java.util.Arrays;
import java.util.OptionalInt;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

/** Задание № 1, вариант 2: поиск элемента с минимальным модулем. */
public final class ArrayMinimumAbsoluteValueTask {
    private ArrayMinimumAbsoluteValueTask() {
    }

    /**
     * Возвращает исходный элемент массива с наименьшим абсолютным значением.
     * При равных модулях сохраняется первый встретившийся элемент.
     */
    public static OptionalInt findMinimumByAbsoluteValue(int[] values) {
        if (values == null) {
            throw new IllegalArgumentException("Массив не должен быть null");
        }
        if (values.length == 0) {
            return OptionalInt.empty();
        }

        int minimum = values[0];
        long minimumAbsoluteValue = Math.abs((long) minimum);

        for (int i = 1; i < values.length; i++) {
            long absoluteValue = Math.abs((long) values[i]);
            if (absoluteValue < minimumAbsoluteValue) {
                minimum = values[i];
                minimumAbsoluteValue = absoluteValue;
            }
        }

        return OptionalInt.of(minimum);
    }

    public static int[] generateRandomArray(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("Размер массива не может быть отрицательным");
        }

        int[] values = new int[size];
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < values.length; i++) {
            values[i] = random.nextInt(-1_000, 1_001);
        }
        return values;
    }

    public static void run(Scanner scanner) {
        int size = ConsoleInput.readNonNegativeInt(scanner, "Введите количество элементов массива: ");
        int[] values = generateRandomArray(size);

        System.out.println("Сформированный массив: " + Arrays.toString(values));
        OptionalInt result = findMinimumByAbsoluteValue(values);
        if (result.isPresent()) {
            int value = result.getAsInt();
            System.out.printf("Элемент с минимальным модулем: %d (модуль: %d)%n",
                    value, Math.abs((long) value));
        } else {
            System.out.println("Массив пуст. Минимальное значение не определено.");
        }
    }

    public static void main(String[] args) {
        run(new Scanner(System.in));
    }
}
