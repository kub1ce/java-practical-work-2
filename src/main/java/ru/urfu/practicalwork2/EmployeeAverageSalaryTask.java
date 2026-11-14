package ru.urfu.practicalwork2;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.Scanner;
import java.util.stream.Collectors;

public final class EmployeeAverageSalaryTask {
    private EmployeeAverageSalaryTask() {
    }

    public static OptionalDouble calculateAverageSalaryByDepartment(
            List<Employee> employees, String department) {
        if (employees == null || department == null) {
            throw new IllegalArgumentException("список сотрудников и отдел is null");
        }

        return employees.stream()
                .filter(employee -> employee != null
                        && employee.getDepartment() != null
                        && employee.getDepartment().equalsIgnoreCase(department.trim()))
                .mapToDouble(employee -> employee.getSalary())
                .average();
    }

    public static ArrayList<Employee> createSampleEmployees() {
        ArrayList<Employee> employees = new ArrayList<>(6);
        employees.add(new Employee("Иванов Андрей Петрович", 28, "Разработка", 125_000.0));
        employees.add(new Employee("Петрова Мария Сергеевна", 34, "Разработка", 145_000.0));
        employees.add(new Employee("Сидоров Максим Игоревич", 25, "Тестирование", 90_000.0));
        employees.add(new Employee("Смирнова Анна Олеговна", 31, "Аналитика", 118_000.0));
        employees.add(new Employee("Кузнецов Дмитрий Павлович", 29, "Разработка", 135_000.0));
        employees.add(new Employee("Волкова Елена Викторовна", 41, "HR", 85_000.0));
        return employees;
    }

    public static void run(Scanner scanner) {
        List<Employee> employees = createSampleEmployees();
        System.out.println("cписок сотрудников:");
        employees.forEach(System.out::println);

        String departments = employees.stream()
                .map(Employee::getDepartment)
                .distinct()
                .sorted()
                .collect(Collectors.joining(", "));
        System.out.println("доступные отделы: " + departments);
        System.out.print("введите название отдела: ");
        String department = scanner.nextLine().trim();

        OptionalDouble average = calculateAverageSalaryByDepartment(employees, department);
        if (average.isPresent()) {
            System.out.printf("средняя зарплата отдела «%s»: %.2f руб.%n",
                    department, average.getAsDouble());
        } else {
            System.out.println("сотрудники указанного отдела не найдены.");
        }
    }

    public static void main(String[] args) {
        run(new Scanner(System.in));
    }
}
