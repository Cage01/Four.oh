package zerotek.gradecalculator.Items;

/**
 * Created by Mason on 3/22/2016.
 */
public class Work {
    private String title;
    private Double points;
    private Double maxPoints;

    public Work(String wTitle, double wPoints, double wMaxPoints) {
        title = wTitle;
        points = wPoints;
        maxPoints = wMaxPoints;
    }

    public String getTitle() {
        return title;
    }

    public Double getPoints() {
        return points;
    }

    public Double getMaxPoints() {
        return maxPoints;
    }

}
