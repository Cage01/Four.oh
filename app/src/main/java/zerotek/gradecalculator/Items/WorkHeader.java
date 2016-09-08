package zerotek.gradecalculator.Items;

import java.util.ArrayList;

/**
 * Created by Mason on 5/25/2016.
 */
public class WorkHeader extends ArrayList<WorkHeader> {
    private String title;
    private double grade;

    public WorkHeader(String wTitle, double wGrade) {
        title = wTitle;
        grade = wGrade;
    }

    public String getTitle() {
        return title;
    }

    public double getGrade() {
        return grade;
    }
}
