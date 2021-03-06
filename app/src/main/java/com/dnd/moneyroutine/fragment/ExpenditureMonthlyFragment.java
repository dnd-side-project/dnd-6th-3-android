package com.dnd.moneyroutine.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dnd.moneyroutine.R;
import com.dnd.moneyroutine.adapter.ExpenditureCategoryAdapter;
import com.dnd.moneyroutine.custom.Constants;
import com.dnd.moneyroutine.custom.PreferenceManager;
import com.dnd.moneyroutine.custom.YearMonthPickerDialog;
import com.dnd.moneyroutine.dto.CategoryType;
import com.dnd.moneyroutine.dto.ExpenditureDetail;
import com.dnd.moneyroutine.dto.GoalCategoryInfo;
import com.dnd.moneyroutine.dto.MonthlyTrend;
import com.dnd.moneyroutine.dto.ExpenditureStatistics;
import com.dnd.moneyroutine.dto.WeeklyTrend;
import com.dnd.moneyroutine.service.HeaderRetrofit;
import com.dnd.moneyroutine.service.LocalDateSerializer;
import com.dnd.moneyroutine.service.RetrofitService;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;


public class ExpenditureMonthlyFragment extends Fragment {

    private static final String TAG = "ExpenditureMonthly";

    private TextView tvEmpty;
    private LinearLayout llExpenditure;

    private LinearLayout llSelectMonth;
    private TextView tvMonth;
    private TextView tvDate;

    private PieChart pieChart;

    private TextView tvTopCategory;
    private TextView tvTopRatio;
    private TextView tvTotalExpenditure;

    private RecyclerView rvCategory;

    private TextView tvNotice; //????????? ????????? ????????????
    private TextView tvDifference; //?????? ?????? ?????????
    private TextView tvMonthBudgetBarChart; //?????? ?????? ??????
    private TextView tvPossibleText; // ????????? or ??? ??? ?????? ???
    private TextView tvPossibleAmount; //????????? ??? ??? ?????? ??? ??????
    private TextView tvRemainText; // ?????? ??? or ????????? ???
    private TextView tvRemainAmount;//?????? ??? ????????? ??? ??????

    private View viewBarChart1;
    private View viewBarChart2;
    private View viewBarChart3;
    private View viewBarChart4;
    private View viewBarChart5;

    private TextView tvBarChartMonth1;
    private TextView tvBarChartMonth2;
    private TextView tvBarChartMonth3;
    private TextView tvBarChartMonth4;
    private TextView tvBarChartMonth5;

    private String token;

    private ExpenditureStatistics responseMonthStatistics;
    private ArrayList<GoalCategoryInfo> goalCategoryInfoList;
    private DecimalFormat decimalFormat;

    private ExpenditureDetail expenditureDetailDto;

    private List<MonthlyTrend> monthlyTrend;

    private LocalDate nowDate;
    private LocalDate startDate;
    private LocalDate endDate;

    public ExpenditureMonthlyFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_expenditure_monthly, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView(view);
        initField();

        setMonthDate();
        setListener();

        getMonthlyStatistics(startDate, endDate);
        getMonthlyTrend(nowDate);
    }

    private void initView(View v) {
        tvEmpty = v.findViewById(R.id.tv_monthly_empty);
        llExpenditure = v.findViewById(R.id.ll_montly_expenditure_content);

        llSelectMonth = v.findViewById(R.id.ll_month_select);
        tvMonth = v.findViewById(R.id.tv_expenditure_month);
        tvDate = v.findViewById(R.id.tv_start_end_month);

        pieChart = v.findViewById(R.id.pie_chart_month);

        tvTopCategory = v.findViewById(R.id.tv_month_top_category);
        tvTopRatio = v.findViewById(R.id.tv_month_top_percent);
        tvTotalExpenditure = v.findViewById(R.id.tv_total_month);

        rvCategory = v.findViewById(R.id.rv_month_category);

        tvNotice = v.findViewById(R.id.tv_monthly_notice);
        tvDifference = v.findViewById(R.id.tv_monthly_difference);
        tvMonthBudgetBarChart = v.findViewById(R.id.tv_month_budget_bar_chart);
        tvPossibleText = v.findViewById(R.id.tv_month_possible_text);
        tvPossibleAmount = v.findViewById(R.id.tv_month_possible_amount);
        tvRemainText = v.findViewById(R.id.tv_remain_text);
        tvRemainAmount = v.findViewById(R.id.tv_remain_amount);

        viewBarChart1 = v.findViewById(R.id.view_monthly_bar_chart_1);
        viewBarChart2 = v.findViewById(R.id.view_monthly_bar_chart_2);
        viewBarChart3 = v.findViewById(R.id.view_monthly_bar_chart_3);
        viewBarChart4 = v.findViewById(R.id.view_monthly_bar_chart_4);
        viewBarChart5 = v.findViewById(R.id.view_monthly_bar_chart_5);

        tvBarChartMonth1 = v.findViewById(R.id.tv_month_bar_chart_text1);
        tvBarChartMonth2 = v.findViewById(R.id.tv_month_bar_chart_text2);
        tvBarChartMonth3 = v.findViewById(R.id.tv_month_bar_chart_text3);
        tvBarChartMonth4 = v.findViewById(R.id.tv_month_bar_chart_text4);
        tvBarChartMonth5 = v.findViewById(R.id.tv_month_bar_chart_text5);
    }

    private void initField() {
        token = PreferenceManager.getToken(getContext(), Constants.tokenKey);
        nowDate = LocalDate.now();
        decimalFormat = new DecimalFormat("#,###");
    }

    private void setMonthDate() {
        startDate = nowDate.withDayOfMonth(1);
        endDate = nowDate.withDayOfMonth(nowDate.lengthOfMonth());

        String start = startDate.format(DateTimeFormatter.ofPattern("M.d"));
        String end = endDate.format(DateTimeFormatter.ofPattern("M.d"));
        tvDate.setText(start + " ~ " + end);

        tvMonth.setText(startDate.format(DateTimeFormatter.ofPattern("y. M???")));

//        startDate = YearMonth.now().atDay(1);
//        endDate = YearMonth.now().atEndOfMonth();
    }

    private void setListener(){
        llSelectMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ??????, ??? ?????? ??????????????? ?????????
                YearMonthPickerDialog yearMonthPickerDialog = new YearMonthPickerDialog(nowDate, false);
                yearMonthPickerDialog.show(getActivity().getSupportFragmentManager(), "YearMonthPickerDialog");

                yearMonthPickerDialog.setOnSelectListener(new YearMonthPickerDialog.OnSelectListener() {
                    @Override
                    public void onSelect(LocalDate date) {
                        nowDate = date;

                        setMonthDate();
                        getMonthlyStatistics(startDate, endDate);
                        getMonthlyTrend(startDate);
                    }
                });
            }
        });
    }

    // ?????? ?????? ?????? ????????????
    private void getMonthlyStatistics(LocalDate startDate, LocalDate endDate) {
        HeaderRetrofit headerRetrofit = new HeaderRetrofit();
        Retrofit retrofit = headerRetrofit.getTokenHeaderInstance(token);
        RetrofitService retroService = retrofit.create(RetrofitService.class);

        Call<JsonObject> call = retroService.getMonthlyStatistics(startDate, endDate);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    JsonObject responseJson = response.body();

                    Log.d(TAG, responseJson.toString());

                    if (responseJson.get("statusCode").getAsInt() == 200) {
                        if (!responseJson.get("data").isJsonNull()) {
                            Gson gson = new GsonBuilder().registerTypeAdapter(LocalDate.class, new LocalDateSerializer()).create();
                            responseMonthStatistics = gson.fromJson(responseJson.getAsJsonObject("data"), new TypeToken<ExpenditureStatistics>() {}.getType());

                            if (responseMonthStatistics.getGoalCategoryInfoList().isEmpty()) {
                                tvEmpty.setVisibility(View.VISIBLE);
                                llExpenditure.setVisibility(View.GONE);
                            } else {
                                tvEmpty.setVisibility(View.GONE);
                                llExpenditure.setVisibility(View.VISIBLE);

                                setEtcList();
                                drawPieChart();
                                setContent();
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(getContext(), "??????????????? ???????????? ????????????.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setEtcList() {
        goalCategoryInfoList = (ArrayList<GoalCategoryInfo>) responseMonthStatistics.getGoalCategoryInfoList();

        ArrayList<CategoryType> etcCategoryTypes = new ArrayList<>();
        ArrayList<String> etcCategoryNames = new ArrayList<>();

        int etcPercent = 0;
        int etcExpense = 0;

        for (int i = 3; i < goalCategoryInfoList.size(); i++) {
            GoalCategoryInfo info = goalCategoryInfoList.get(i);

            etcPercent += info.getPercentage();
            etcExpense += info.getExpense();

            if (!etcCategoryNames.contains(info.getCategoryName())) {
                etcCategoryNames.add(info.getCategoryName());
                etcCategoryTypes.add(info.getCategoryType());
            }
        }

        if (etcExpense > 0) {
            GoalCategoryInfo info = goalCategoryInfoList.get(3);

            info.setCategoryName("?????????");
            info.setPercentage(etcPercent);
            info.setExpense(etcExpense);

            info.setEtcCategoryNames(etcCategoryNames);
            info.setEtcCategoryTypes(etcCategoryTypes);

            for (int i = 4; i < goalCategoryInfoList.size(); i++) {
                goalCategoryInfoList.remove(i--);
            }
        }
    }

    // ???????????? ?????? ?????? ?????????
    private void drawPieChart() {
        pieChart.setVisibility(View.VISIBLE);
        pieChart.removeAllViews();

//        pieChart.setRotationAngle(-100); //?????? ?????? ?????? (3???????????? ??????)
        pieChart.getDescription().setEnabled(false); //?????? ?????? ??????
        pieChart.getLegend().setEnabled(false); //?????? ????????? ?????? ?????? ??????

        pieChart.setExtraOffsets(0, 0, 0, 0); //?????? ?????? margin ??????
        pieChart.setTouchEnabled(false); // ?????? ??????????????? ??????
        pieChart.setDrawHoleEnabled(true); //????????? hole
        pieChart.setHoleRadius(55f); //hole ?????? ??????
        pieChart.setTransparentCircleRadius(0);

        ArrayList<PieEntry> values = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            try {
                values.add(new PieEntry(goalCategoryInfoList.get(i).getPercentage()));
            } catch ( IndexOutOfBoundsException e ) {
                values.add(new PieEntry(0));
            }
        }

        PieDataSet dataSet = new PieDataSet(values, "??? ??????");

        //?????? ?????? ??????
        List<Integer> colors = getColorArray();
        dataSet.setColors(colors);

        pieChart.setDrawMarkers(false); //?????? ?????? ?????? ??????
        pieChart.setDrawEntryLabels(false); //????????? ?????? ??????

        PieData data = new PieData((dataSet));
        dataSet.setDrawValues(false);

        pieChart.setData(data);
    }

    private void setContent() {
        tvTotalExpenditure.setText(decimalFormat.format(responseMonthStatistics.getTotalExpense()) + "???");
        tvTopCategory.setText(goalCategoryInfoList.get(0).getCategoryName());
        tvTopRatio.setText(goalCategoryInfoList.get(0).getPercentage() + "%");

        ExpenditureCategoryAdapter expenditureCategoryAdapter = new ExpenditureCategoryAdapter(goalCategoryInfoList, false, false, startDate, endDate);
        rvCategory.setAdapter(expenditureCategoryAdapter);
        rvCategory.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    // ?????? ?????? ?????? ???????????? : ?????? ???????????? ??????
    private void getMonthlyTrend(LocalDate currentDate) {
        HeaderRetrofit headerRetrofit = new HeaderRetrofit();
        Retrofit retrofit = headerRetrofit.getTokenHeaderInstance(token);
        RetrofitService retroService = retrofit.create(RetrofitService.class);

        Call<JsonObject> call = retroService.getMonthlyTrend(currentDate);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    JsonObject responseJson = response.body();

                    Log.d(TAG, responseJson.toString());

                    Gson gson = new GsonBuilder().registerTypeAdapter(LocalDate.class, new LocalDateSerializer()).create();
                    ArrayList<MonthlyTrend> monthlyTrends = gson.fromJson(responseJson.getAsJsonObject("data").getAsJsonArray("monthExpenseInfoDtoList"),
                            new TypeToken<ArrayList<MonthlyTrend>>() {}.getType());
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(getContext(), "??????????????? ???????????? ????????????.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ????????? ?????? ?????? ???????????? ?????????
    private List<Integer> getColorArray() {
        String[] colorStringArray = getResources().getStringArray(R.array.category_color);;

        List<Integer> colorIntList = new ArrayList<>();

        for(String color : colorStringArray) {
            colorIntList.add(Color.parseColor(color));
        }

        return colorIntList;
    }

//    private void drawBarChart() {
//
//        int overHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 74, getResources().getDisplayMetrics());
//        int lessHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 121, getResources().getDisplayMetrics());
//        int nowHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 169, getResources().getDisplayMetrics());
//
//
//        if (monthlyTrend.get(0) != null) {
//            tvBarChartMonth1.setText(monthlyTrend.get(0).getMonth() + "???");
//            if (monthlyTrend.get(0).getMonthExpense() > monthlyTrend.get(0).getBudget()) {
//                viewBarChart1.setLayoutParams(new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, overHeight));
//
//            } else {
//                viewBarChart1.setLayoutParams(new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, lessHeight));
//            }
//        }
//
//        if (monthlyTrend.get(1) != null) {
//            tvBarChartMonth1.setText(monthlyTrend.get(1).getMonth() + "???");
//            if (monthlyTrend.get(1).getMonthExpense() > monthlyTrend.get(1).getBudget()) {
//                viewBarChart2.setLayoutParams(new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, overHeight));
//
//            } else {
//                viewBarChart2.setLayoutParams(new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, lessHeight));
//            }
//        }
//
//        if (monthlyTrend.get(2) != null) {
//            tvBarChartMonth1.setText(monthlyTrend.get(2).getMonth() + "???");
//            if (monthlyTrend.get(2).getMonthExpense() > monthlyTrend.get(2).getBudget()) {
//                viewBarChart3.setLayoutParams(new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, overHeight));
//
//            } else {
//                viewBarChart3.setLayoutParams(new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, lessHeight));
//            }
//
//        }
//
//        if (monthlyTrend.get(3) != null) {
//            tvBarChartMonth1.setText(monthlyTrend.get(3).getMonth() + "???");
//            if (monthlyTrend.get(3).getMonthExpense() > monthlyTrend.get(3).getBudget()) {
//                viewBarChart4.setLayoutParams(new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, overHeight));
//
//            } else {
//                viewBarChart4.setLayoutParams(new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, lessHeight));
//            }
//        }
//
//        if (monthlyTrend.get(4) != null) {
//            tvBarChartMonth1.setText(monthlyTrend.get(4).getMonth() + "???");
//            viewBarChart5.setLayoutParams(new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, nowHeight));
//            if (monthlyTrend.get(4).getMonthExpense() > monthlyTrend.get(4).getBudget()) {
//                tvBarChartMonth1.setBackgroundResource(R.drawable.bar_chart_now_high);
//                tvNotice.setText("????????? ????????? ????????????");
//
//            } else {
//                tvBarChartMonth1.setBackgroundResource(R.drawable.bar_chart_now_low);
//
//                tvNotice.setText("????????? ????????? ????????????");
//
//
//                tvDifference.setText("??????" + formatter.format(monthlyTrend.get(4).getBudget() - monthlyTrend.get(4).getMonthExpense()) + "??? ??? ??? ??? ?????????");
//                tvDifference.setTextColor(Color.parseColor("#107D69"));
//
//                tvMonthBudgetBarChart.setText(formatter.format(monthlyTrend.get(4).getBudget()) + "???");
//
//                tvPossibleText.setText("??? ??? ?????? ???");
//                tvPossibleAmount.setText(formatter.format(monthlyTrend.get(4).getBudget() - monthlyTrend.get(4).getMonthExpense()) + "???");
//                tvPossibleAmount.setTextColor(Color.parseColor("#107D6"));
//
//                tvRemainText.setVisibility(View.INVISIBLE);
//                tvRemainAmount.setVisibility(View.INVISIBLE);
//
//            }
//        }
//
//    }
//
//    private void setMonthlyTrendText() {
//        monthDifference = now.getMonthValue() - startDate.getMonthValue();
//
//        if (monthDifference == 1) {
//            if (monthlyTrend.get(3).getMonthExpense() > monthlyTrend.get(3).getBudget()) {
//                setTextOver(3);
//                viewBarChart4.setBackgroundResource(R.drawable.bar_chart_now_high);
//            } else {
//                setTextLess(3);
//                viewBarChart4.setBackgroundResource(R.drawable.bar_chart_now_low);
//            }
//        } else if (monthDifference == 2) {
//            if (monthlyTrend.get(2).getMonthExpense() > monthlyTrend.get(3).getBudget()) {
//                setTextOver(2);
//                viewBarChart3.setBackgroundResource(R.drawable.bar_chart_now_high);
//            } else {
//                setTextLess(2);
//                viewBarChart3.setBackgroundResource(R.drawable.bar_chart_now_low);
//            }
//        } else if (monthDifference == 3) {
//            if (monthlyTrend.get(1).getMonthExpense() > monthlyTrend.get(3).getBudget()) {
//                setTextOver(1);
//                viewBarChart2.setBackgroundResource(R.drawable.bar_chart_now_high);
//            } else {
//                setTextLess(1);
//                viewBarChart2.setBackgroundResource(R.drawable.bar_chart_now_low);
//            }
//        } else if (monthDifference == 4) {
//            if (monthlyTrend.get(0).getMonthExpense() > monthlyTrend.get(3).getBudget()) {
//                setTextOver(0);
//                viewBarChart1.setBackgroundResource(R.drawable.bar_chart_now_high);
//            } else {
//                setTextLess(0);
//                viewBarChart1.setBackgroundResource(R.drawable.bar_chart_now_low);
//            }
//        }
//
//    }
//
//    //?????????
//    private void setTextOver(int position) {
//
//        tvNotice.setText(formatter.format(monthlyTrend.get(position).getMonthExpense() - monthlyTrend.get(position).getBudget()) + "??? ???????????????");
//
//        tvDifference.setText("???" + formatter.format(monthlyTrend.get(position).getBudget() - monthlyTrend.get(position).getMonthExpense()) + "??? ????????????");
//        tvDifference.setTextColor(Color.parseColor("#107D69"));
//
//        tvMonthBudgetBarChart.setText(formatter.format(monthlyTrend.get(position).getBudget()) + "???");
//
//        tvPossibleText.setText("??? ??????");
//        tvPossibleAmount.setText(formatter.format(monthlyTrend.get(position).getMonthExpense()) + "???");
//        tvPossibleAmount.setTextColor(Color.parseColor("#212529"));
//
//
//        tvRemainText.setVisibility(View.VISIBLE);
//        tvRemainText.setText("?????? ???");
//        tvRemainAmount.setVisibility(View.VISIBLE);
//        tvRemainAmount.setText(formatter.format(monthlyTrend.get(position).getBudget() - monthlyTrend.get(position).getMonthExpense()) + "???");
//        tvRemainAmount.setTextColor(Color.parseColor("#107D69"));
//
//    }
//
//    //?????? ??????(?????? ???)
//    private void setTextLess(int position) {
//
//
//        if (monthlyTrend.get(position).getMonthExpense() < monthlyTrend.get(position).getBudget()) {
//
//            tvDifference.setText(monthlyTrend.get(position).getMonth() + "??? ?????? ??????");
//            tvDifference.setTextColor(Color.parseColor("#107D69"));
//
//            tvDifference.setText(formatter.format(monthlyTrend.get(position).getMonthExpense() - monthlyTrend.get(position).getBudget()) + "??? ???????????????");
//            tvDifference.setTextColor(Color.parseColor("#FC3781"));
//
//            tvMonthBudgetBarChart.setText(formatter.format(monthlyTrend.get(position).getBudget()) + "???");
//
//            tvPossibleText.setText("??? ??????");
//            tvPossibleAmount.setText(formatter.format(monthlyTrend.get(position).getMonthExpense()) + "???");
//            tvPossibleAmount.setTextColor(Color.parseColor("#212529"));
//
//
//            tvRemainText.setVisibility(View.VISIBLE);
//            tvRemainText.setText("????????? ???");
//            tvRemainAmount.setVisibility(View.VISIBLE);
//            tvRemainAmount.setText(formatter.format(monthlyTrend.get(position).getMonthExpense() - monthlyTrend.get(position).getBudget()) + "???");
//            tvRemainAmount.setTextColor(Color.parseColor("#FC3781"));
//        }
//    }

}

