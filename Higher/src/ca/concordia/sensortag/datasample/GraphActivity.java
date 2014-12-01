package ca.concordia.sensortag.datasample;

import java.util.List;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.widget.LinearLayout;

public class GraphActivity extends Activity {
	
	private Context mContext;
	private List<DBContainers.StepInfo> liStepInfo;
	private int mSessionIndex;
	/* Service */
	private RecordService.Binder mRecSvc = null;
	BluetoothDevice mBtDevice = null;
	
	public GraphActivity (int index) {
		mSessionIndex = index;
		setUpGUI();
	}
	public void setUpGUI(){
		mContext = this.getApplicationContext();
		liStepInfo = mRecSvc.getAllStepInfo(mSessionIndex);
		
		//Add steps: altitude & timestamp as series data
		GraphViewData[] stepData = new GraphViewData[]{   //X, Y
										new GraphViewData(1.00, 2.00),
										new GraphViewData(2.00, 3.00)};
		for (int i = 0; i<liStepInfo.size() - 1; i++){
			double time = liStepInfo.get(i).getTime_stamp()*1000;
			stepData[i] = new GraphViewData(time, liStepInfo.get(i).getAltitude());
		}
		
		GraphViewSeries stepSeries = new GraphViewSeries( stepData );
		GraphView posGraph = new LineGraphView(mContext, "Position");
		posGraph.addSeries( stepSeries );
		
//		LinearLayout layout = (LinearLayout) findViewById(R.id.layout);
//		layout.addView(posGraph);
	}
}