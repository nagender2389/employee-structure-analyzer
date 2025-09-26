package com.employee.org;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Employee {
    public final String id;
    public final String name;
    public final String managerId; // null for CEO
    public final BigDecimal salary;

    public final List<Employee> directReports = new ArrayList<>();
    public Employee manager;

    public Employee(String id, String name, String managerId, BigDecimal salary) {
        this.id = id;
        this.name = name;
        this.managerId = (managerId == null || managerId.isBlank()) ? null : managerId;
        this.salary = Money.round2(salary);
    }

    public boolean isManager() { return !directReports.isEmpty(); }

    /** managers between this employee and CEO (excludes CEO and this). */
    public int managersBetweenCeo() {
        int count = 0;
        Employee cur = this.manager;
        while (cur != null && cur.manager != null) {
            count++;
            cur = cur.manager;
        }
        return count;
    }

    @Override public String toString() { return name + " [" + id + "]"; }
}
