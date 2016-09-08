package zerotek.gradecalculator.Adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.daimajia.swipe.SimpleSwipeListener;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.BaseSwipeAdapter;

import java.util.ArrayList;

import zerotek.gradecalculator.Database.Database;
import zerotek.gradecalculator.EditClass;
import zerotek.gradecalculator.GradeActivity;
import zerotek.gradecalculator.R;


public class ClassViewAdapter extends BaseSwipeAdapter {
    ArrayList<String> classes;
    boolean doubleCheck;
    int semesterID;
    Cursor res;


    private Context mContext;

    public ClassViewAdapter(Context mContext, ArrayList<String> theClasses, ArrayList<String> thePoints, ArrayList<String> theWeights, int sSemesterID) {
        this.mContext = mContext;
        this.classes = theClasses;
        this.semesterID = sSemesterID;
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipe;
    }

    @Override
    public View generateView(final int position, ViewGroup parent) {
        final Database database = new Database(mContext);
        final View v = LayoutInflater.from(mContext).inflate(R.layout.swipe_layout, null);
        final SwipeLayout swipeLayout = (SwipeLayout) v.findViewById(getSwipeLayoutResourceId(position));
        swipeLayout.addDrag(SwipeLayout.DragEdge.Left, R.id.bottom_wrapper);
        swipeLayout.addDrag(SwipeLayout.DragEdge.Right, R.id.bottom_wrapper2);

        try {
            swipeLayout.addSwipeListener(new SimpleSwipeListener() {
                @Override
                public void onOpen(SwipeLayout layout) {

                }

                @Override
                public void onClose(SwipeLayout layout) {
                    doubleCheck = false;
                    TextView textView = (TextView) v.findViewById(R.id.trash);
                    textView.setText("Delete");

                }
            });


            swipeLayout.setOnDoubleClickListener(new SwipeLayout.DoubleClickListener() {
                @Override
                public void onDoubleClick(SwipeLayout layout, boolean surface) {
                }
            });
            v.findViewById(R.id.trash).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Cursor res = database.getClasses();
                    res.moveToPosition(position);

                    TextView textView = (TextView) v.findViewById(R.id.trash);

                    if (!doubleCheck) {
                        textView.setText("Are you sure?");
                        doubleCheck = true;
                        return;
                    }
                    if (doubleCheck) {
                        database.removeClass(database.getClassID(classes.get(position), semesterID));
                        Intent intent = new Intent(mContext, GradeActivity.class);
                        intent.putExtra("semesterID", semesterID);
                        mContext.startActivity(intent);
                        //notifyDataSetInvalidated();
                        return;
                    }
                }
            });
            v.findViewById(R.id.edit).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    res = database.getClass(database.getClassID(classes.get(position), semesterID));
                    res.moveToFirst();
                    Intent intent = new Intent(mContext, EditClass.class);
                    intent.putExtra("className", classes.get(position));
                    intent.putExtra("classID", res.getInt(0));//sends the ID of the class
                    intent.putExtra("semesterID", res.getInt(3));//sends the ID of the semester the class is associated to
                    mContext.startActivity(intent);


                }
            });


        } catch (NullPointerException e) {
        }
        return v;
    }

    @Override
    public void fillValues(int position, View convertView) {
        TextView t = (TextView) convertView.findViewById(R.id.position);
        t.setText(classes.get(position));
    }

    @Override
    public int getCount() {
        return classes.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}