
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;
import java.util.Scanner;

public class ListQuickSort {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Введите количество элементов: ");

        if (!scanner.hasNextInt()) {
            System.out.println("! необходимо ввести целое число");
            return;
        }

        int n = scanner.nextInt();

        if (n < 0) {
            System.out.println("! количество элементов не может быть отрицательным");
            return;
        }

        // список случайных чисел
        ArrayList<Double> numbers = new ArrayList<>(n);
        Random random = new Random();
        for (int i = 0; i < n; i++) {
            numbers.add(random.nextDouble() * 100);
        }

        // черновой вариант
        printList("Исходный список", numbers);

        quickSort(numbers, 0, numbers.size() - 1);

        // итог
        printList("Отсортированный список", numbers);
    }

    // Quick Sort - быстрая сортировка
    private static void quickSort(ArrayList<Double> list, int low, int high) {
        if (low >= high) return;

        int i = low;
        int j = high;

        double pivot = list.get(low + (high - low) / 2);

        while (i <= j) {
            while (i <= high && Double.compare(list.get(i), pivot) < 0) {
                i++;
            }

            while (j >= low && Double.compare(list.get(j), pivot) > 0) {
                j--;
            }

            if (i <= j) {
                double temp = list.get(i);
                list.set(i, list.get(j));
                list.set(j, temp);
                i++;
                j--;
            }
        }

        if (low < j) {
            quickSort(list, low, j);
        }
        if (i < high) {
            quickSort(list, i, high);
        }
    }

    // принт списка в консоль
    private static void printList(String title, ArrayList<Double> list) {
        System.out.print(title + ": [");

        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                System.out.print(", ");
            }
            System.out.printf(Locale.US, "%.2f", list.get(i));
        }

        System.out.println("]");
    }
}