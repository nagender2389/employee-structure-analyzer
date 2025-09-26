package com.employee.org;

import java.nio.file.Path;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Usage: java -jar employee-structure-analyzer.jar <sample-employees.csv>");
            System.exit(1);
        }
        var employees = CsvParser.parse(Path.of(args[0]));
        var ceo = CsvParser.linkHierarchy(employees);

        System.out.println("== Pay Issues ==");
        List<OrgAnalyzer.PayIssue> payIssues = OrgAnalyzer.findPayIssues(ceo);
        if (payIssues.isEmpty()) System.out.println("No manager pay issues found.");
        else payIssues.forEach(i -> System.out.println(" - " + i));

        System.out.println("\n== Reporting Chain Issues ==");
        List<OrgAnalyzer.ChainIssue> chainIssues = OrgAnalyzer.findChainIssues(ceo);
        if (chainIssues.isEmpty()) System.out.println("No reporting chain issues found.");
        else chainIssues.forEach(i -> System.out.println(" - " + i));
    }
}

