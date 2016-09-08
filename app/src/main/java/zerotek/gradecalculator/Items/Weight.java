package zerotek.gradecalculator.Items;

/**
 * Created by Mason on 3/7/2016.
 */
public class Weight {
    private String title;
    private String percent;

    public Weight(String wTitle, String wPercent) {
        title = wTitle;
        percent = wPercent;
    }

    public String getTitle() {
        return title;
    }

    public String getPercent() {
        return percent;
    }
}
