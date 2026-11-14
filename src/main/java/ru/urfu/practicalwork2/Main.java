package ru.urfu.practicalwork2;

import java.util.Scanner;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("1 - минимум по омдулю в массиве");
            System.out.println("2 - quicksort");
            System.out.println("3 - Stream API");
            System.out.println("4 - извлечение user-agent из JSON");
            System.out.println("5 - Скачивание файла");
            System.out.println("0 - exit");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> ArrayMinimumAbsoluteValueTask.run(scanner);
                case "2" -> QuickSortTask.run(scanner);
                case "3" -> EmployeeAverageSalaryTask.run(scanner);
                case "4" -> HttpUserAgentTask.run();
                case "5" -> FileDownloadTask.run(scanner);
                case "0" -> {
                    return;
                }
                default -> System.out.println("-");
            }
            System.out.println();
        }
    }
}
