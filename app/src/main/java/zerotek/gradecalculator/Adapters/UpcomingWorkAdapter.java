package zerotek.gradecalculator.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import zerotek.gradecalculator.Items.UpcomingItem;
import zerotek.gradecalculator.R;

/**
 * Created by Mason on 3/7/2016.
 */
public class UpcomingWorkAdapter extends BaseAdapter {

    public ArrayList<UpcomingItem> upcomingItems;
    private LayoutInflater listInf;

    public UpcomingWorkAdapter(Context c, ArrayList<UpcomingItem> theItems) {
        upcomingItems = theItems;
        listInf = LayoutInflater.from(c);
    }

    @Override
    public int getCount() {
        return upcomingItems.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {

        LinearLayout weightLay = (LinearLayout) listInf.inflate(R.layout.upcoming_work_text, viewGroup, false);
        TextView titleView = (TextView) weightLay.findViewById(R.id.upcoming_class);
        TextView assignmentInfo = (TextView) weightLay.findViewById(R.id.upcoming_assignment_name);

        UpcomingItem currItem = upcomingItems.get(position);
        titleView.setText(currItem.getClassTitle());
        assignmentInfo.setText(currItem.getAssignmentInfo());

        weightLay.setTag(position);
        return weightLay;
    }
}
