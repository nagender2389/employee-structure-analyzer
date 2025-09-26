package com.employee.org;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class OrgAnalyzerTest {

    @Test
    void testPayBandDetection() {
        // CEO -> Manager M1 -> two reports R1(100), R2(100); M1 earns 110 (UNDERPAID)
        var ceo = new Employee("C","CEO", null, new BigDecimal("300"));
        var m1  = new Employee("M1","Mgr", "C", new BigDecimal("110"));
        var r1  = new Employee("R1","R1", "M1", new BigDecimal("100"));
        var r2  = new Employee("R2","R2", "M1", new BigDecimal("100"));

        ceo.directReports.add(m1); m1.manager = ceo;
        m1.directReports.addAll(List.of(r1, r2)); r1.manager = m1; r2.manager = m1;

        var issues = OrgAnalyzer.findPayIssues(ceo);
        assertEquals(1, issues.size());
        var issue = issues.get(0);
        assertEquals("UNDERPAID", issue.type);
        // avg = 100 -> min = 120, max = 150
        assertEquals(new BigDecimal("120.00"), issue.requiredMin);
    }

    @Test
    void testChainTooLong() {
        var c = new Employee("C","CEO", null, new BigDecimal("1"));
        var a = new Employee("A","A", "C", new BigDecimal("1"));
        var b = new Employee("B","B", "A", new BigDecimal("1"));
        var d = new Employee("D","D", "B", new BigDecimal("1"));
        var e = new Employee("E","E", "D", new BigDecimal("1"));
        var f = new Employee("F","F", "E", new BigDecimal("1"));
        // chain C->A->B->D->E->F ; managers between F and CEO = 4 (A,B,D,E) => not reported
        c.directReports.add(a); a.manager = c;
        a.directReports.add(b); b.manager = a;
        b.directReports.add(d); d.manager = b;
        d.directReports.add(e); e.manager = d;
        e.directReports.add(f); f.manager = e;

        var none = OrgAnalyzer.findChainIssues(c);
        assertTrue(none.isEmpty());

        // Add one more level -> now over by 1
        var g = new Employee("G","G", "F", new BigDecimal("1"));
        f.directReports.add(g); g.manager = f;

        var issues = OrgAnalyzer.findChainIssues(c);
        assertEquals(1, issues.size());
        assertEquals(5, issues.get(0).managersBetween); // A,B,D,E,F
        assertEquals(1, issues.get(0).overBy);
    }
}
