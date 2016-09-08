package zerotek.gradecalculator.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import zerotek.gradecalculator.Calculations.GradeCalculations;
import zerotek.gradecalculator.Items.Work;
import zerotek.gradecalculator.R;

/**
 * Created by Mason on 3/22/2016.
 */
public class WorkAdapter extends BaseAdapter {

    public ArrayList<Work> work;
    private LayoutInflater workInf;

    public WorkAdapter(Context c, ArrayList<Work> theWork) {
        work = theWork;
        workInf = LayoutInflater.from(c);
    }

    @Override
    public int getCount() {
        return work.size();
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
        GradeCalculations calculations = new GradeCalculations();

        RelativeLayout workLay = (RelativeLayout) workInf.inflate(R.layout.work_text, viewGroup, false);
        TextView titleView = (TextView) workLay.findViewById(R.id.work_title);
        TextView percentView = (TextView) workLay.findViewById(R.id.work_points);

        Work currWork = work.get(position);
        double grade = currWork.getPoints() / currWork.getMaxPoints() * 100;

        String format = String.format("%.0f", grade) + "%" + " - " + String.format("%.0f", currWork.getPoints()) + "/" + String.format("%.0f", currWork.getMaxPoints());


        titleView.setText(currWork.getTitle());
        percentView.setText(format);

        workLay.setTag(position);
        return workLay;
    }


}
