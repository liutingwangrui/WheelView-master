package com.wangpm.wheelviewdemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bigkoo.pickerview.TimePickerView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import wheelview.Common;
import wheelview.WheelView;

public class MainActivity extends Activity implements View.OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private BottomDialog bottomDialog;
    private Button main_show_dialog_btn;
    private Button main_show_bottom_dialog_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        main_show_dialog_btn = (Button) findViewById(R.id.main_show_dialog_btn);
        main_show_bottom_dialog_btn = (Button) findViewById(R.id.main_show_bottom_dialog_btn);

        main_show_dialog_btn .setOnClickListener(this);
        main_show_bottom_dialog_btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.main_show_dialog_btn:
                Calendar dayc1 = new GregorianCalendar();
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                Date daystart = null;
                try {
                    daystart = df.parse("2500-12-31");
                    dayc1.setTime(daystart);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                TimePickerView pvTime = new TimePickerView.Builder(MainActivity.this, new TimePickerView.OnTimeSelectListener() {
                    @Override
                    public void onTimeSelect(Date date2, View v) {//选中事件回调
                        String time = getTime(date2);
                        main_show_dialog_btn.setText(time);
                    }
                })
                        .setType(TimePickerView.Type.YEAR_MONTH_DAY)//默认全部显示
                        .setCancelText("取消")//取消按钮文字
                        .setSubmitText("确定")//确认按钮文字
                        .setContentSize(20)//滚轮文字大小
                        .setTitleSize(20)//标题文字大小
//                        .setTitleText("请选择时间")//标题文字
                        .setOutSideCancelable(true)//点击屏幕，点在控件外部范围时，是否取消显示
                        .isCyclic(false)//是否循环滚动
                        .setTextColorCenter(Color.BLACK)//设置选中项的颜色
                        .setTitleColor(Color.BLACK)//标题文字颜色
                        .setSubmitColor(Color.BLUE)//确定按钮文字颜色
                        .setCancelColor(Color.BLUE)//取消按钮文字颜色
//                        .setTitleBgColor(0xFF666666)//标题背景颜色 Night mode
//                        .setBgColor(0xFF333333)//滚轮背景颜色 Night mode
//                        .setRange(calendar.get(Calendar.YEAR), calendar.get(Calendar.YEAR) +50)//默认是1900-2100年
//                        .setDate(Calendar.getInstance())// 如果不设置的话，默认是系统时间
                        .setRangDate(Calendar.getInstance(),dayc1)//起始终止年月日设定
//                        .setLabel("年","月","日","时","分","秒")
                        .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
//                        .isDialog(true)//是否显示为对话框样式
                        .build();
                pvTime.setDate(Calendar.getInstance());//注：根据需求来决定是否使用该方法（一般是精确到秒的情况），此项可以在弹出选择器的时候重新设置当前时间，避免在初始化之后由于时间已经设定，导致选中时间与当前时间不匹配的问题。
                pvTime.show();

                break;
            case R.id.main_show_bottom_dialog_btn:
                View outerView1 = LayoutInflater.from(this).inflate(R.layout.dialog_select_date_time, null);
                //日期滚轮
                final WheelView wv1 = (WheelView) outerView1.findViewById(R.id.wv1);
                //小时滚轮
                final WheelView wv2 = (WheelView) outerView1.findViewById(R.id.wv2);
                //分钟滚轮
                final WheelView wv3 = (WheelView) outerView1.findViewById(R.id.wv3);

                final TimeRange timeRange = getTimeRange();
                wv1.setItems(Common.buildDays(timeRange),0);
                wv2.setItems(Common.buildHourListStart(timeRange),0);
                wv3.setItems(Common.buildMinuteListStart(timeRange),0);
                //联动逻辑效果
                wv1.setOnItemSelectedListener(new WheelView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(int index,String item) {
                        List hourStrList = Common.buildHoursByDay(wv1, timeRange);
                        int newIndexHour = hourStrList.indexOf(wv2.getSelectedItem());
                        wv2.setItems(hourStrList,newIndexHour);
                        List minStrList = Common.buildMinutesByDayHour(wv1, wv2, timeRange);
                        int newIndexMin = minStrList.indexOf(wv3.getSelectedItem());
                        wv3.setItems(minStrList,newIndexMin);
                    }
                });
                wv2.setOnItemSelectedListener(new WheelView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(int index,String item) {
                        List minStrList = Common.buildMinutesByDayHour(wv1, wv2, timeRange);
                        int newIndexMin = minStrList.indexOf(wv3.getSelectedItem());
                        wv3.setItems(minStrList,newIndexMin);
                    }
                });

                TextView tv_ok = (TextView) outerView1.findViewById(R.id.tv_ok);
                TextView tv_cancel = (TextView) outerView1.findViewById(R.id.tv_cancel);
                //点击确定
                tv_ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        bottomDialog.dismiss();
                        Calendar calendarStart = Calendar.getInstance();
                        int currentYear = calendarStart.get(Calendar.YEAR);
                        String selectDateTimeStrToShow;
                        String mSelectDate = wv1.getSelectedItem();
                        String substring = mSelectDate.substring(0, 2);
                        String substring1 = mSelectDate.substring(3, 5);
                        mSelectDate=currentYear+"."+substring+"."+substring1;
                        String mSelectHour = wv2.getSelectedItem();
                        String mSelectMin = wv3.getSelectedItem();
                        String time = mSelectHour + mSelectMin;
                        time = Common.timeToStr(Common.dateTimeFromCustomStr( mSelectDate, time));
                        selectDateTimeStrToShow = mSelectDate + "  " + time;
                        main_show_bottom_dialog_btn.setText(selectDateTimeStrToShow);

                    }
                });
                //点击取消
                tv_cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        bottomDialog.dismiss();
                    }
                });
                //防止弹出两个窗口
                if (bottomDialog !=null && bottomDialog.isShowing()) {
                    return;
                }

                bottomDialog = new BottomDialog(this, R.style.ActionSheetDialogStyle);
                //将布局设置给Dialog
                bottomDialog.setContentView(outerView1);
                bottomDialog.show();//显示对话框
                break;
        }
    }

    //取一个30天内的时间范围进行显示
    private TimeRange getTimeRange() {
        Calendar calendarStart = Calendar.getInstance();
        int currentMonth = calendarStart.get(Calendar.MONTH)+1;
        int currentYear = calendarStart.get(Calendar.YEAR);
        int urrentDay = calendarStart.get(Calendar.DAY_OF_MONTH);
        int number = getCurrentMonthLastDay()-urrentDay;
        for (int i = currentMonth+1; i < 13; i++) {
            number+=getMonthLastDay(currentYear,i);
        }
        Calendar calendarEnd = Calendar.getInstance();
        calendarEnd.add(Calendar.DAY_OF_YEAR,number);
        TimeRange timeRange = new TimeRange();
        timeRange.setStart_time(calendarStart.getTime());
        timeRange.setEnd_time(calendarEnd.getTime());
        return timeRange;
    }


    /**
     * 取得当月天数
     * */
    public static int getCurrentMonthLastDay()
    {
        Calendar a = Calendar.getInstance();
        a.set(Calendar.DATE, 1);//把日期设置为当月第一天
        a.roll(Calendar.DATE, -1);//日期回滚一天，也就是最后一天
        int maxDate = a.get(Calendar.DATE);
        return maxDate;
    }

    /**
     * 得到指定月的天数
     * */
    public static int getMonthLastDay(int year, int month)
    {
        Calendar a = Calendar.getInstance();
        a.set(Calendar.YEAR, year);
        a.set(Calendar.MONTH, month - 1);
        a.set(Calendar.DATE, 1);//把日期设置为当月第一天
        a.roll(Calendar.DATE, -1);//日期回滚一天，也就是最后一天
        int maxDate = a.get(Calendar.DATE);
        return maxDate;
    }
    public String getTime(Date date) {//可根据需要自行截取数据显示
        SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd");
        String format1 = format.format(date);
        return format1;
    }

}
