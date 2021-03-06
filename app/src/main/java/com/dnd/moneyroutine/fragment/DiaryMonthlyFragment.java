package com.dnd.moneyroutine.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dnd.moneyroutine.R;
import com.dnd.moneyroutine.adapter.ExpenditureCategoryAdapter;
import com.dnd.moneyroutine.custom.Common;
import com.dnd.moneyroutine.custom.Constants;
import com.dnd.moneyroutine.custom.EmotionDialog;
import com.dnd.moneyroutine.custom.PreferenceManager;
import com.dnd.moneyroutine.custom.YearMonthPickerDialog;
import com.dnd.moneyroutine.dto.ExpenditureCompact;
import com.dnd.moneyroutine.dto.MonthlyDiary;
import com.dnd.moneyroutine.enums.EmotionEnum;
import com.dnd.moneyroutine.service.HeaderRetrofit;
import com.dnd.moneyroutine.service.LocalDateSerializer;
import com.dnd.moneyroutine.service.RetrofitService;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class DiaryMonthlyFragment extends Fragment {

    private static final String TAG = "DiaryMonthly";

    private LinearLayout llContent;
    private ConstraintLayout clTopCategory;

    private LinearLayout btnSelectYearMonth;
    private TextView tvSelectYearMonth;

    private TextView tvTopEmotionTitle;

    private HorizontalBarChart hcEmotion;
    private TextView tvGoodCnt;
    private TextView tvSosoCnt;
    private TextView tvBadCnt;

    private LinearLayout btnSelectEmotion;
    private ImageView ivSelectEmotion;
    private TextView tvSelectEmotion;
    private TextView tvSelectEmotionDetail;

    private TextView tvTopEmotionCatTitle;
    private TextView tvTopCat;
    private TextView tvtopCatCnt;

    private PieChart pcCategory;

    private TextView tvSelectEmotionTitle;
    private TextView tvSelectEmotionCnt;

    private RecyclerView rvCategory;

    private String token;
    private LocalDate selectDate;
    private EmotionEnum selectEmotion;
    private HashMap<EmotionEnum, Integer> emotionMap;
    private ArrayList<MonthlyDiary> monthlyList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_diary_monthly, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        initView(view);
        initField();
        setListener();

        getMonthlyEmotionList();
    }

    private void initView(View v) {
        llContent = v.findViewById(R.id.ll_monthly_diary_content);
        clTopCategory = v.findViewById(R.id.cl_top_category);

        btnSelectYearMonth = v.findViewById(R.id.ll_diary_select_month);
        tvSelectYearMonth = v.findViewById(R.id.tv_diary_select_month);

        tvTopEmotionTitle = v.findViewById(R.id.tv_month_top_emotion_title);

        hcEmotion = v.findViewById(R.id.bar_month_emotion);
        tvGoodCnt = v.findViewById(R.id.tv_month_good_cnt);
        tvSosoCnt = v.findViewById(R.id.tv_month_soso_cnt);
        tvBadCnt = v.findViewById(R.id.tv_month_bad_cnt);

        btnSelectEmotion = v.findViewById(R.id.ll_month_select_emotion);
        ivSelectEmotion = v.findViewById(R.id.iv_month_select_emotion);
        tvSelectEmotion = v.findViewById(R.id.tv_month_select_emotion);
        tvSelectEmotionDetail = v.findViewById(R.id.tv_month_select_emotion_detail);

        tvTopEmotionCatTitle = v.findViewById(R.id.tv_month_top_cat_emotion_title);
        tvTopCat = v.findViewById(R.id.tv_month_top_category);
        tvtopCatCnt = v.findViewById(R.id.tv_month_top_cat_cnt);
        pcCategory = v.findViewById(R.id.pie_chart_month);
        tvSelectEmotionTitle = v.findViewById(R.id.tv_month_select_emotion_title);
        tvSelectEmotionCnt = v.findViewById(R.id.tv_month_select_emotion_cnt);

        rvCategory = v.findViewById(R.id.rv_month_category);
    }

    private void initField() {
        token = PreferenceManager.getToken(getContext(), Constants.tokenKey);

        selectDate = LocalDate.now();
        tvSelectYearMonth.setText(Common.getMainLocalDateToString(selectDate) + " ?????? ??????");
    }

    private void setListener() {
        btnSelectYearMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ??????, ??? ?????? ??????????????? ?????????
                YearMonthPickerDialog yearMonthPickerDialog = new YearMonthPickerDialog(selectDate, false);
                yearMonthPickerDialog.show(getActivity().getSupportFragmentManager(), "YearMonthPickerDialog");

                yearMonthPickerDialog.setOnSelectListener(new YearMonthPickerDialog.OnSelectListener() {
                    @Override
                    public void onSelect(LocalDate date) {
                        selectDate = date;
                        tvSelectYearMonth.setText(Common.getMainLocalDateToString(date) + " ?????? ??????");

                        getMonthlyEmotionList();
                    }
                });
            }
        });

        btnSelectEmotion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ?????? ?????? ?????? ??????????????? ?????????
                EmotionDialog emotionDialog = new EmotionDialog(selectEmotion);
                emotionDialog.show(getActivity().getSupportFragmentManager(), "EmotionPickerDialog");

                emotionDialog.setOnSelectListener(new EmotionDialog.OnSelectListener() {
                    @Override
                    public void onSelect(EmotionEnum emotion) {
                        selectEmotion = emotion;

                        setSelectEmotion();
                        getMonthlyDiaryByEmotion();
                    }
                });
            }
        });
    }

    // ?????? ?????? ?????? ????????????
    private void getMonthlyEmotionList() {
        HeaderRetrofit headerRetrofit = new HeaderRetrofit();
        Retrofit retrofit = headerRetrofit.getTokenHeaderInstance(token);
        RetrofitService retroService = retrofit.create(RetrofitService.class);

        Call<JsonObject> call = retroService.getMonthlyBestEmotion(selectDate.getYear(), selectDate.getMonthValue());
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    JsonObject responseJson = response.body();

                    Log.d(TAG, responseJson.toString());

                    if (responseJson.get("statusCode").getAsInt() == 200 && !responseJson.get("data").isJsonNull()) {
                        Gson gson = new Gson();
                        emotionMap = gson.fromJson(responseJson.getAsJsonObject("data"), new TypeToken<HashMap<EmotionEnum, Integer>>() {}.getType());

                        if (emotionMap.size() == 0) {
                            tvTopEmotionTitle.setText("?????? ????????? ?????????");
                            llContent.setVisibility(View.GONE);
                        } else {
                            emotionMap.putIfAbsent(EmotionEnum.GOOD, 0);
                            emotionMap.putIfAbsent(EmotionEnum.SOSO, 0);
                            emotionMap.putIfAbsent(EmotionEnum.BAD, 0);

                            setEmotionInfo();
                            llContent.setVisibility(View.VISIBLE);
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

    private void setEmotionInfo() {
        // ?????? ??? ?????? ????????? ?????? ?????? ??????
        Comparator<Map.Entry<EmotionEnum, Integer>> comparator = new Comparator<Map.Entry<EmotionEnum, Integer>>() {
            @Override
            public int compare(Map.Entry<EmotionEnum, Integer> e1, Map.Entry<EmotionEnum, Integer> e2) {
                return e1.getValue().compareTo(e2.getValue());
            }
        };

        // Max Value??? key, value
        Map.Entry<EmotionEnum, Integer> maxEntry = Collections.max(emotionMap.entrySet(), comparator);
        switch (maxEntry.getKey()) {
            case GOOD:
                tvTopEmotionTitle.setText("'??????'??? ????????? ?????? ?????????!");
                break;
            case SOSO:
                tvTopEmotionTitle.setText("'??????' ????????? ?????? ?????????");
                break;
            case BAD:
                tvTopEmotionTitle.setText("?????? ??? ????????? ?????? ??? ????????????????");
                break;
        }

        tvGoodCnt.setText(emotionMap.get(EmotionEnum.GOOD) + "???");
        tvSosoCnt.setText(emotionMap.get(EmotionEnum.SOSO) + "???");
        tvBadCnt.setText(emotionMap.get(EmotionEnum.BAD) + "???");

        selectEmotion = maxEntry.getKey();

        drawEmotionBarChart(); // ?????? ?????? ????????? ?????????
        setSelectEmotion();
        getMonthlyDiaryByEmotion(); // ?????? ?????? ?????? ???????????? ?????? ?????? ?????? ????????????
    }

    // ?????? ?????? ????????? ????????? : ???????????????
    private void drawEmotionBarChart() {
        hcEmotion.setVisibility(View.VISIBLE);
        hcEmotion.removeAllViews();

        List<Integer> colorArray = getColorArray(true);

        ArrayList<BarEntry> barEntries = new ArrayList<>();
        barEntries.add(new BarEntry(0, new float[] {emotionMap.get(EmotionEnum.GOOD), emotionMap.get(EmotionEnum.SOSO), emotionMap.get(EmotionEnum.BAD)}));

        BarDataSet barDataSet = new BarDataSet(barEntries, "emotionChart");
        barDataSet.setColors(colorArray);

        hcEmotion.setData(new BarData(barDataSet));
        hcEmotion.setTouchEnabled(false); // ????????? ?????? X

        // ????????? x, y??? ?????? X
        hcEmotion.getXAxis().setEnabled(false);
        hcEmotion.getAxisLeft().setEnabled(false);
        hcEmotion.getAxisRight().setEnabled(false);

        hcEmotion.getData().setDrawValues(false); // ????????? ?????? ?????? X
        hcEmotion.getDescription().setEnabled(false); // ????????? ?????? ?????? ?????? X
        hcEmotion.getLegend().setEnabled(false); // ????????? ?????? ?????? ?????? ?????? X
    }

    // ????????? ?????? ?????? ?????? ?????????
    private void setSelectEmotion() {
        tvSelectEmotion.setText(selectEmotion.getDetail());
        tvSelectEmotionTitle.setText(selectEmotion.getDetail() + " ?????? ??????");
        tvSelectEmotionCnt.setText(emotionMap.get(selectEmotion) + "???");

        switch (selectEmotion) {
            case GOOD:
                ivSelectEmotion.setImageResource(R.drawable.icon_good);
                tvSelectEmotionDetail.setText("????????????");
                tvSelectEmotionDetail.setTextColor(Color.parseColor("#107D69"));
                break;
            case SOSO:
                ivSelectEmotion.setImageResource(R.drawable.icon_soso);
                tvSelectEmotionDetail.setText("?????? ?????????");
                tvSelectEmotionDetail.setTextColor(Color.parseColor("#1E5CA4"));
                break;
            case BAD:
                ivSelectEmotion.setImageResource(R.drawable.icon_bad);
                tvSelectEmotionDetail.setText("?????????");
                tvSelectEmotionDetail.setTextColor(Color.parseColor("#D13474"));
                break;
        }
    }

    // ?????? ???????????? ?????? ?????? ????????????
    private void getMonthlyDiaryByEmotion() {
        HeaderRetrofit headerRetrofit = new HeaderRetrofit();
        Retrofit retrofit = headerRetrofit.getTokenHeaderInstance(token);
        RetrofitService retroService = retrofit.create(RetrofitService.class);

        Call<JsonObject> call = retroService.getMonthlyDiaryByEmotion(selectDate.getYear(), selectDate.getMonthValue(), selectEmotion);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    JsonObject responseJson = response.body();

                    Log.d(TAG, responseJson.toString());

                    if (responseJson.get("statusCode").getAsInt() == 200 && !responseJson.get("data").isJsonNull()) {
                        JsonArray jsonArray = responseJson.get("data").getAsJsonArray();

                        Gson gson = new GsonBuilder().registerTypeAdapter(LocalDate.class, new LocalDateSerializer()).create();
                        monthlyList = gson.fromJson(jsonArray, new TypeToken<ArrayList<MonthlyDiary>>() {}.getType());

                        if (monthlyList.size() == 0) {
                            clTopCategory.setVisibility(View.GONE);
                            pcCategory.setVisibility(View.GONE);
                            rvCategory.setVisibility(View.GONE);

                            tvTopEmotionCatTitle.setText("?????? ????????? ?????????");
                        } else {
                            clTopCategory.setVisibility(View.VISIBLE);
                            pcCategory.setVisibility(View.VISIBLE);
                            rvCategory.setVisibility(View.VISIBLE);

                            setEtcList();
                            drawCategoryPieChart();
                            setBestCategoryInfo();
                            setExpenditureInfo();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d(TAG, t.getMessage());
                Toast.makeText(getContext(), "??????????????? ???????????? ????????????.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ?????? 3??? ??????????????? ????????? ???????????? ??????
    private void setEtcList() {
        ArrayList<ExpenditureCompact> etcList = new ArrayList<>();
        int etcCnt = 0;

        for (int i = 3; i < monthlyList.size(); i++) {
            MonthlyDiary monthlyDiary = monthlyList.get(i);

            etcCnt += monthlyDiary.getCount();

            for (ExpenditureCompact expenditure : monthlyDiary.getExpenditureList()) {
                expenditure.setCategoryName(monthlyDiary.getName());
            }

            etcList.addAll(monthlyDiary.getExpenditureList());
        }

        if (etcCnt > 0) {
            MonthlyDiary monthlyDiary = monthlyList.get(3);
            monthlyDiary.setName("?????????");
            monthlyDiary.setCount(etcCnt);
            monthlyDiary.setExpenditureList(etcList);

            for (int i = 4; i < monthlyList.size(); i++) {
                monthlyList.remove(i--);
            }
        }
    }

    // ???????????? ?????? ????????? ????????? : ?????? ??????
    private void drawCategoryPieChart() {
        pcCategory.setVisibility(View.VISIBLE);
        pcCategory.removeAllViews();

//        pcCategory.setRotationAngle(100); // ?????? ?????? ?????? (3???????????? ??????)
        pcCategory.getDescription().setEnabled(false); // ?????? ?????? ??????
        pcCategory.getLegend().setEnabled(false); // ?????? ????????? ?????? ?????? ??????

        pcCategory.setExtraOffsets(0, 0, 0, 0); // ?????? ?????? margin ??????
        pcCategory.setTouchEnabled(false); // ?????? ??????????????? ??????
        pcCategory.setDrawHoleEnabled(true); // ????????? hole
        pcCategory.setHoleRadius(55f); // hole ?????? ??????
        pcCategory.setTransparentCircleRadius(0);

        ArrayList<PieEntry> yValues = new ArrayList<>();

        for (int i = 3; i >= 0; i--) {
            try {
                yValues.add(new PieEntry(monthlyList.get(i).getCount()));
            } catch ( IndexOutOfBoundsException e ) {
                yValues.add(new PieEntry(0));
            }
        }

        PieDataSet dataSet = new PieDataSet(yValues, "Countries");

        // ?????? ?????? ??????
        dataSet.setColors(getColorArray(false));

        pcCategory.setDrawMarkers(false); // ?????? ?????? ?????? ??????
        pcCategory.setDrawEntryLabels(false); // ?????? ??? ?????? ??????

        PieData data = new PieData((dataSet));
        dataSet.setDrawValues(false);

        pcCategory.setData(data);
    }

    // ?????? ????????? ????????? ???????????? ?????? ?????????
    private void setBestCategoryInfo() {
        MonthlyDiary best = monthlyList.get(0); // ??? ????????? ?????? ?????? ?????? ????????? ?????? ????????????

        switch (selectEmotion) {
            case GOOD:
                tvTopEmotionCatTitle.setText(best.getName() + "?????? ?????? ????????? ????????????!");
                break;
            case SOSO:
                tvTopEmotionCatTitle.setText(best.getName() + " ?????? ????????? ?????? ????????????");
                break;
            case BAD:
                tvTopEmotionCatTitle.setText(best.getName() + " ?????? ????????? ???????????????????");
                break;
        }

        tvTopCat.setText(best.getName());
        tvtopCatCnt.setText(best.getCount() + "???");
    }

    // ?????? ?????? ?????? ?????? ?????????
    private void setExpenditureInfo() {
        ExpenditureCategoryAdapter monthlyCategoryAdapter = new ExpenditureCategoryAdapter(monthlyList, true);
        rvCategory.setAdapter(monthlyCategoryAdapter);
        rvCategory.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    // ????????? ?????? ?????? ???????????? ?????????
    private List<Integer> getColorArray(boolean emotion) {
        String[] colorStringArray;

        if (emotion) {
            colorStringArray = getResources().getStringArray(R.array.emotion_color);
        } else {
            colorStringArray = getResources().getStringArray(R.array.category_reverse_color);
        }

        List<Integer> colorIntList = new ArrayList<>();

        for(String color : colorStringArray) {
            colorIntList.add(Color.parseColor(color));
        }

        return colorIntList;
    }
}
