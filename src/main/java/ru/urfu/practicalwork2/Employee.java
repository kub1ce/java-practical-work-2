package ru.urfu.practicalwork2;

import java.util.Objects;

public class Employee {
    private String fullName;
    private Integer age;
    private String department;
    private Double salary;

    public Employee() {
    }

    public Employee(String fullName, Integer age, String department, Double salary) {
        this.fullName = Objects.requireNonNull(fullName, "фио is null");
        this.age = Objects.requireNonNull(age, "возраст is null");
        this.department = Objects.requireNonNull(department, "отдел is null");
        this.salary = Objects.requireNonNull(salary, "зарплата is null");
        if (age < 0 || salary < 0) {
            throw new IllegalArgumentException("возраст и зарплата > 0");
        }
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = Objects.requireNonNull(fullName);
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = Objects.requireNonNull(age);
        if (age < 0) {
            throw new IllegalArgumentException("age < 0");
        }
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = Objects.requireNonNull(department);
    }

    public Double getSalary() {
        return salary;
    }

    public void setSalary(Double salary) {
        this.salary = Objects.requireNonNull(salary);
        if (salary < 0) {
            throw new IllegalArgumentException("salary < 0");
        }
    }

    @Override
    public String toString() {
        return "Employee{" +
                "fullName='" + fullName + '\'' +
                ", age=" + age +
                ", department='" + department + '\'' +
                ", salary=" + salary +
                '}';
    }
}
