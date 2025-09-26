package com.employee.org;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class CsvParser {

    /** CSV rows: id,name,managerId,salary (no header). CEO row has empty managerId. */
    public static List<Employee> parse(Path csvPath) throws IOException {
        try (BufferedReader br = Files.newBufferedReader(csvPath)) {
            final int[] row = {0};
            return br.lines()
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .peek(s -> row[0]++)
                    .map(line -> {
                        String[] p = line.split(",", -1);
                        if (p.length != 4) {
                            throw new IllegalArgumentException("Invalid CSV at row " + row[0] + ": " + line);
                        }
                        return new Employee(
                                p[0].trim(),
                                p[1].trim(),
                                p[2].trim(),
                                new BigDecimal(p[3].trim())
                        );
                    })
                    .collect(Collectors.toList());
        }
    }

    /** Link hierarchy (set manager/directReports). Returns CEO. */
    public static Employee linkHierarchy(List<Employee> all) {
        Map<String, Employee> byId = all.stream().collect(Collectors.toMap(e -> e.id, e -> e));
        List<Employee> ceos = all.stream().filter(e -> e.managerId == null).collect(Collectors.toList());
        if (ceos.size() != 1) throw new IllegalStateException("Expected exactly one CEO, found: " + ceos.size());
        Employee ceo = ceos.get(0);

        all.stream()
                .filter(e -> e.managerId != null)
                .forEach(e -> {
                    Employee m = byId.get(e.managerId);
                    if (m == null) throw new IllegalStateException("Manager id not found: " + e.managerId + " for " + e);
                    e.manager = m;
                    m.directReports.add(e);
                });

        return ceo;
    }
}

