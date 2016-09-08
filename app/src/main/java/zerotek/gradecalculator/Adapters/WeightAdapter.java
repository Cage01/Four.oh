package zerotek.gradecalculator.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import zerotek.gradecalculator.Items.Weight;
import zerotek.gradecalculator.R;

/**
 * Created by Mason on 3/7/2016.
 */
public class WeightAdapter extends BaseAdapter {

    public ArrayList<Weight> weights;
    private LayoutInflater weightInf;

    public WeightAdapter(Context c, ArrayList<Weight> theWeights) {
        weights = theWeights;
        weightInf = LayoutInflater.from(c);
    }

    @Override
    public int getCount() {
        return weights.size();
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

        RelativeLayout weightLay = (RelativeLayout) weightInf.inflate(R.layout.weight_text, viewGroup, false);
        TextView titleView = (TextView) weightLay.findViewById(R.id.weight_name);
        TextView percentView = (TextView) weightLay.findViewById(R.id.weight_percent);

        Weight currWeight = weights.get(position);
        titleView.setText(currWeight.getTitle());
        percentView.setText(currWeight.getPercent());

        weightLay.setTag(position);
        return weightLay;
    }
}
