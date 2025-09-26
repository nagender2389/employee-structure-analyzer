package com.employee.org;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Stream;

import static com.employee.org.Money.round2;

public class OrgAnalyzer {

    public static class PayIssue {
        public final Employee manager;
        public final BigDecimal avgDirects;
        public final BigDecimal requiredMin;   // 1.20 * avg
        public final BigDecimal allowedMax;    // 1.50 * avg
        public final BigDecimal delta;         // negative -> underpaid; positive -> overpaid
        public final String type;              // "UNDERPAID" or "OVERPAID"

        public PayIssue(Employee m, BigDecimal avg, BigDecimal min, BigDecimal max, BigDecimal delta, String type) {
            this.manager = m;
            this.avgDirects = round2(avg);
            this.requiredMin = round2(min);
            this.allowedMax = round2(max);
            this.delta = round2(delta);
            this.type = type;
        }

        @Override public String toString() {
            return "UNDERPAID".equals(type)
                    ? String.format("%s earns %s, needs at least %s (avg=%s). Short by %s.",
                    manager, manager.salary, requiredMin, avgDirects, requiredMin.subtract(manager.salary).abs())
                    : String.format("%s earns %s, should be at most %s (avg=%s). Over by %s.",
                    manager, manager.salary, allowedMax, avgDirects, manager.salary.subtract(allowedMax).abs());
        }
    }

    public static class ChainIssue {
        public final Employee employee;
        public final int managersBetween; // > 4
        public final int overBy;          // managersBetween - 4
        public ChainIssue(Employee e, int managersBetween, int overBy) {
            this.employee = e; this.managersBetween = managersBetween; this.overBy = overBy;
        }
        @Override public String toString() {
            return String.format("%s has %d managers between them and the CEO (over by %d).",
                    employee, managersBetween, overBy);
        }
    }

    /** Recursive stream of all employees in the tree (root first). */
    private static Stream<Employee> allEmployees(Employee e) {
        return Stream.concat(Stream.of(e), e.directReports.stream().flatMap(OrgAnalyzer::allEmployees));
    }

    /** Managers outside [1.20 * avgDirects, 1.50 * avgDirects]. Direct reports only. */
    public static List<PayIssue> findPayIssues(Employee ceo) {
        return allEmployees(ceo)
                .filter(Employee::isManager)
                .map(mgr -> mgr.directReports.stream()
                        .mapToDouble(e -> e.salary.doubleValue())
                        .average()
                        .stream() // 0 or 1 double
                        .mapToObj(avgD -> {
                            BigDecimal avg = round2(BigDecimal.valueOf(avgD));
                            BigDecimal min = avg.multiply(BigDecimal.valueOf(1.20));
                            BigDecimal max = avg.multiply(BigDecimal.valueOf(1.50));
                            if (mgr.salary.compareTo(min) < 0) {
                                return new PayIssue(mgr, avg, min, max, mgr.salary.subtract(min), "UNDERPAID");
                            } else if (mgr.salary.compareTo(max) > 0) {
                                return new PayIssue(mgr, avg, min, max, mgr.salary.subtract(max), "OVERPAID");
                            }
                            return null; // in range
                        })
                )
                .flatMap(s -> s)            // flatten OptionalDouble stream → Stream<PayIssue?>
                .filter(Objects::nonNull)
                .toList();
    }

    /** Employees with managersBetweenCeo() > 4. */
    public static List<ChainIssue> findChainIssues(Employee ceo) {
        return allEmployees(ceo)
                .map(e -> {
                    int between = e.managersBetweenCeo();
                    return (between > 4) ? new ChainIssue(e, between, between - 4) : null;
                })
                .filter(Objects::nonNull)
                .toList();
    }
}
