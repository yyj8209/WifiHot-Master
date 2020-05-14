package com.example.myserver;

import android.graphics.Color;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;

public class myLineChart {
    private LineChart lineChart;

    myLineChart(LineChart lineChart){
        this.lineChart = lineChart;

    }

    ArrayList<Entry> values, values1, values2, values3;
    LineDataSet set1,set2,set3;
    private int nTotalNum;
    private static final float A = 5000.0f/32768;
    private static final int MAX_XRANGE = 3000;
    private static final int[] LINE_COLORS = {
            Color.rgb(140, 210, 118),
            Color.rgb(159, 143, 186),
            Color.rgb(233, 197, 23)
    };
    private byte [] buf;
    private int len;

    // 初始化封装为此函数。
    public void initChartSyn(){
        initData();
        initChart();
        initChartData();
        lineChart.invalidate();
    }

    public void initData()
    {
        values = new ArrayList<Entry>();
        values.add(new Entry(1,0));
        values.add(new Entry(2,0));
        values.add(new Entry(3,0));
        values1 = new ArrayList<Entry>();
        values2 = new ArrayList<Entry>();
        values3 = new ArrayList<Entry>();
        nTotalNum = 0;
    }

    public void initChart(){
        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false);//设置图表右边的y轴禁用
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition( XAxis.XAxisPosition.BOTTOM );

        lineChart.setDrawGridBackground(true);  //背景绘制
//        lineChart.setBackgroundColor( BACKGROUND_COLOR);
        lineChart.setGridBackgroundColor( Color.BLACK );
        lineChart.setBorderColor( Color.YELLOW );
        lineChart.setExtraOffsets(0, 10, 0, 10);

        lineChart.getDescription().setEnabled(false);//设置描述文本

        lineChart.setTouchEnabled(true);//设置支持触控手势
        lineChart.setDragEnabled(true);//设置缩放
        lineChart.setScaleEnabled(true);//设置推动
        lineChart.setPinchZoom(true);//如果禁用,扩展可以在x轴和y轴分别完成
        Legend legend = lineChart.getLegend();
        legend.setForm(Legend.LegendForm.LINE);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setDirection(Legend.LegendDirection.LEFT_TO_RIGHT);
        legend.setTextSize(12f);

        lineChart.animateX(10);//默认动画
    }

    public void initChartData(){
        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        if (lineChart.getData() != null && lineChart.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet) lineChart.getData().getDataSetByIndex(0);
            set1.setValues(values);
            set1.setLabel( "初始数据" );
            //添加数据集
            dataSets.add(set1);
            setChartLineStyle(set1,LINE_COLORS[0]);
            lineChart.getData().notifyDataChanged();
            lineChart.notifyDataSetChanged();
        } else {
            // 创建一个数据集,并给它一个类型
            set1 = new LineDataSet(values, "测试数据");
            //添加数据集
            dataSets.add(set1);
            // 在这里设置线
            setChartLineStyle(set1,LINE_COLORS[0]);
        }
        //创建一个数据集的数据对象
        LineData data = new LineData(dataSets);
        //谁知数据
        lineChart.setData(data);
    }

    public void setChartData(){
        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        if (lineChart.getData() != null && lineChart.getData().getDataSetCount() > 0) {

            set1 = new LineDataSet(values1, "通道 1 数据");
            set2 = new LineDataSet(values2, "通道 2 数据");
            set3 = new LineDataSet(values3, "通道 3 数据");
            //添加数据集
            dataSets.add(set1);
            dataSets.add(set2);
            dataSets.add(set3);
            setChartLineStyle(set1,LINE_COLORS[0]);
            setChartLineStyle(set2,LINE_COLORS[1]);
            setChartLineStyle(set3,LINE_COLORS[2]);
            lineChart.getData().notifyDataChanged();
            lineChart.notifyDataSetChanged();
        } else {
            // 创建一个数据集,并给它一个类型
            set1 = new LineDataSet(values, "测试数据");
            //添加数据集
            dataSets.add(set1);
            // 在这里设置线
            setChartLineStyle(set1,LINE_COLORS[0]);
        }
        //创建一个数据集的数据对象
        LineData data = new LineData(dataSets);
        //谁知数据
        lineChart.setData(data);
    }

    public void setData(ArrayList<Entry> values,LineDataSet set, int n){
        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        if (lineChart.getData() != null && lineChart.getData().getDataSetCount() > 0) {
            set = (LineDataSet) lineChart.getData().getDataSetByIndex(n);
            set.setValues(values);
            //添加数据集
            dataSets.add(set);
            setChartLineStyle(set,LINE_COLORS[n]);
            lineChart.getData().notifyDataChanged();
            lineChart.notifyDataSetChanged();
        } else {
            // 创建一个数据集,并给它一个类型
            set = new LineDataSet(values, "测试数据");
            //添加数据集
            dataSets.add(set);
            // 在这里设置线
            setChartLineStyle(set,LINE_COLORS[n]);
        }
        //创建一个数据集的数据对象
        LineData data = new LineData(dataSets);
        //谁知数据
        lineChart.setData(data);
    }

    public void setChartLineStyle(LineDataSet set, int color){
        set.disableDashedLine();
        set.setColor(color);  // Color.BLACK);
        set.setLineWidth(1.5f);
        set.setDrawCircles( false );
        set.setCircleColor(color);
//        set.setCircleRadius(1f);
//        set.setDrawCircleHole(false);
        set.setValueTextSize(0f);
        set.setDrawFilled(false);
        set.setFormSize(15.f);
    }

    public void refreshLineChart(byte[] readBuf, int datLen){   // datLen = msg.arg1.
//        byte[] readBuf = (byte[]) msg.obj;
//        int len = msg.arg1/BYTES_PER_ROW;    // 直采的数据，每组32个字节；保存的dat文件，每组24字节。
        final int BYTES_PER_ROW = 32;
        float [][]CHData = Data_syn.bytesToFloat(readBuf, datLen, BYTES_PER_ROW);

        for (int i = 0; i < len; i++) {
            values1.add(new Entry(nTotalNum + i, A*(float) CHData[0][i]));
            values2.add(new Entry(nTotalNum + i, A*(float) CHData[1][i]));
            values3.add(new Entry(nTotalNum + i, A*(float) CHData[2][i]));
        }

        if (values1.size() - MAX_XRANGE > 0){
            for (int j = 0; j < values1.size()-MAX_XRANGE; j++){
                values1.remove( 0 );
                values2.remove( 0 );
                values3.remove( 0 );
            }
        }

        nTotalNum = nTotalNum + len;
//					Log.e(TAG,"values1长度"+Integer.toString( values1.size() )+
//							"|nTotalNum值 "+Integer.toString( nTotalNum ));
        setChartData();
        lineChart.invalidate();

    }

}
