package zerotek.gradecalculator.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import zerotek.gradecalculator.Calculations.GradeCalculations;
import zerotek.gradecalculator.Items.Work;
import zerotek.gradecalculator.Items.WorkHeader;
import zerotek.gradecalculator.R;

/**
 * Created by Mason on 3/22/2016.
 */
public class WorkAdapterExpandable extends BaseExpandableListAdapter {

    public ArrayList<Work> work;
    private Context cContext;
    private ArrayList<WorkHeader> workHeader;
    private HashMap<ArrayList<WorkHeader>, ArrayList<Work>> workList;

    public WorkAdapterExpandable(Context context, ArrayList<WorkHeader> thisHeader, HashMap<ArrayList<WorkHeader>, ArrayList<Work>> thisWork, ArrayList<Work> workW) {
        this.cContext = context;
        this.workHeader = thisHeader;
        this.workList = thisWork;
        this.work = workW;
    }


    @Override
    public int getGroupCount() {
        return workHeader.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return work.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return workHeader.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this.workList.get(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.cContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.work_header, null);
        }
        TextView titleView = (TextView) convertView.findViewById(R.id.category_header);
        TextView gradeView = (TextView) convertView.findViewById(R.id.category_grade);

        WorkHeader currCat = workHeader.get(groupPosition);
        GradeCalculations calculations = new GradeCalculations();

        String format;
        if (currCat.getGrade() != -1) {
            int iGrade = (int) Math.round(currCat.getGrade() * 100);

            format = String.format("%.0f", currCat.getGrade() * 100) + "%" + " - " + calculations.letterGrade(iGrade);
        } else {
            format = "No Grade";
        }

        titleView.setText(currCat.getTitle());
        gradeView.setText(format);

        convertView.setTag(groupPosition);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {


        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.cContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.work_text, null);
        }
        TextView titleView = (TextView) convertView.findViewById(R.id.work_title);
        TextView percentView = (TextView) convertView.findViewById(R.id.work_points);


        try {
            Work currWork = work.get(childPosition);

            double grade = currWork.getPoints() / currWork.getMaxPoints() * 100;
            titleView.setText(currWork.getTitle());

            if (currWork.getPoints() != 0.0 && currWork.getMaxPoints() != 0.0) {
                String format = String.format("%.0f", grade) + "%" + " - " + String.format("%.0f", currWork.getPoints()) + "/" + String.format("%.0f", currWork.getMaxPoints());
                percentView.setText(format);
            } else percentView.setText("");

        } catch (IndexOutOfBoundsException e) {
        }
        convertView.setTag(childPosition);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
