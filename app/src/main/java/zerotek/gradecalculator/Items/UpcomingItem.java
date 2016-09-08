package zerotek.gradecalculator.Items;

/**
 * Created by Mason on 6/5/2016.
 */
public class UpcomingItem {
    private String classTitle;
    private String assignmentInfo;

    public UpcomingItem(String wTitle, String wAssignmentInfo) {
        classTitle = wTitle;
        assignmentInfo = wAssignmentInfo;
    }

    public String getClassTitle() {
        return classTitle;
    }

    public String getAssignmentInfo() {
        return assignmentInfo;
    }
}
