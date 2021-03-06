package com.dnd.moneyroutine.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.dnd.moneyroutine.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

// 지출 날짜 캘린더 gridview에 사용되는 adapter
public class CalendarAdapter extends ArrayAdapter<Date> {

    private LayoutInflater inflater;

    private int currentYear; // 현재 년도
    private int currentMonth; // 현재 월
    private Calendar selectDate; // 선택된 날짜

    public CalendarAdapter(@NonNull Context context, ArrayList<Date> days, int year, int month, Calendar selectDate) {
        super(context, R.layout.item_day, days);
        inflater = LayoutInflater.from(context);

        this.currentYear = year;
        this.currentMonth = month;
        this.selectDate = selectDate;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        Date date = getItem(position);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        int day = calendar.get(Calendar.DATE);
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);

        // inflate item if it does not exist yet
        if (view == null) {
            view = inflater.inflate(R.layout.item_day, parent, false);
        }

        ConstraintLayout clDay = view.findViewById(R.id.con_day);
        TextView tvDay = view.findViewById(R.id.item_day);

        // clear styling
        tvDay.setTextColor(Color.parseColor("#212529"));

        if (month != currentMonth || year != currentYear) {
            clDay.setVisibility(View.INVISIBLE);
        } else if (month == (selectDate.get(Calendar.MONTH) + 1) && year == selectDate.get(Calendar.YEAR) && day == selectDate.get(Calendar.DATE)) {
            // 오늘일 경우 view를 다르게 표시
            clDay.setBackgroundResource(R.drawable.circle_343a40);
            tvDay.setTextColor(Color.WHITE);
        }

        tvDay.setText(String.valueOf(calendar.get(Calendar.DATE)));

        return view;
    }
}
