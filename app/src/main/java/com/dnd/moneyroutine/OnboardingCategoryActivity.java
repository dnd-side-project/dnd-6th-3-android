package com.dnd.moneyroutine;

import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.dnd.moneyroutine.adapter.CategoryGridViewAdapter;
import com.dnd.moneyroutine.custom.Constants;
import com.dnd.moneyroutine.custom.ExpandableHeightGridView;
import com.dnd.moneyroutine.custom.PreferenceManager;
import com.dnd.moneyroutine.dto.CategoryCompact;
import com.dnd.moneyroutine.dto.GoalCategoryCreateDto;
import com.dnd.moneyroutine.service.HeaderRetrofit;
import com.dnd.moneyroutine.service.RetrofitService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class OnboardingCategoryActivity extends AppCompatActivity {

    private static final String TAG = "OnBoardingCategory";

    private ExpandableHeightGridView gridView;

    private ConstraintLayout background;
    private LinearLayout llAddCategory;
    private Button btnNext;

    private String token;

    private CategoryGridViewAdapter categoryGridViewAdapter;

    private ArrayList<CategoryCompact> categories;
    private ArrayList<CategoryCompact> selectCategories;

    private ArrayList<Integer> id;
    private ArrayList<String> icon;
    private ArrayList<String> name;
    private ArrayList<String> ex;

    private ArrayList<Integer> selectedItem = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding_catecory);

        token = PreferenceManager.getToken(this, Constants.tokenKey);

        initView();
        getAllCategory();

        selectItem();
        setButtonListener();
    }

    private void initView() {
        gridView = (ExpandableHeightGridView) findViewById(R.id.gv_category);
        gridView.setExpanded(true);
        btnNext = (Button) findViewById(R.id.btn_next1);

        llAddCategory = findViewById(R.id.btn_addcategory);
    }

    private void initAdapter() {
        String[] grayIcons = {"@drawable/coffee_gray", "@drawable/food_gray", "@drawable/beer_gray", "@drawable/book_gray", "@drawable/bus_gray", "@drawable/bag_gray", "@drawable/computer_gray","@drawable/tissue_gray", "@drawable/pill_gray"};

        id = new ArrayList<>();
        icon = new ArrayList<>();
        name = new ArrayList<>();
        ex = new ArrayList<>();

        categoryGridViewAdapter = new CategoryGridViewAdapter();

        for (CategoryCompact category : categories) {
            if (category.isCustom()) {
                try {
                    icon.add(URLDecoder.decode(category.getEmoji(), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            } else {
                icon.add(grayIcons[category.getCategoryId() - 1]);
            }

            id.add(category.getCategoryId());
            name.add(category.getName());
            ex.add(category.getDetail());

            categoryGridViewAdapter.addItem(category);
        }

        gridView.setAdapter(categoryGridViewAdapter);
    }

    private void selectItem() {
        //????????? ??????
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ImageView ivIcon = view.findViewById(R.id.iv_category_icon);
                TextView tvDetail = view.findViewById(R.id.tv_category_ex);

                TypedArray colorIconId = getResources().obtainTypedArray(R.array.basicColorCategory);
                TypedArray grayIconId = getResources().obtainTypedArray(R.array.basicGrayCategory);

                background = (ConstraintLayout) view;
                gridView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE);

                if (selectedItem.contains(position)) {
                    //????????????
                    background.setSelected(false);
                    selectedItem.remove(Integer.valueOf(position));
                } else {
                    //??????
                    background.setSelected(true);
                    selectedItem.add(position);
                }

                if (background.isSelected()) {
                    //?????????
                    background.setBackgroundResource(R.drawable.button_category_clicked);
                    if (position < 9){ //?????? ??????????????? ?????? ????????????
                        ivIcon.setImageResource(colorIconId.getResourceId(position, 0));
                    }

                    tvDetail.setTextColor(Color.parseColor("#212529"));
                } else {
                    //???????????????
                    background.setBackgroundResource(R.drawable.button_category_unclicked);
                    if (position < 9){ //?????? ??????????????? ?????? ????????????
                        ivIcon.setImageResource(grayIconId.getResourceId(position, 0));
                    }

                    tvDetail.setTextColor(Color.parseColor("#868E96"));
                }

                //???????????? ???????????? ?????? ?????? ?????????
                btnNext.setEnabled(selectedItem.size() > 0);
            }
        });
    }

    private void setButtonListener() {
        //???????????? ?????? ??????
        llAddCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), NewCategoryActivity.class);
                startActivityResult.launch(intent);
            }
        });

        // ?????? ????????????
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectCategories = new ArrayList<>();
                Collections.sort(selectedItem);

                ArrayList<GoalCategoryCreateDto> goalCategoryCreateDtoList = new ArrayList<>();

                //????????? ?????????
                for (int i = 0; i < selectedItem.size(); i++) {
                    int index = selectedItem.get(i);
                    GoalCategoryCreateDto goalCategoryCreateDto;

                    if (index < 9){
                        selectCategories.add(new CategoryCompact(id.get(index), null, name.get(index), ex.get(index), false));
                        goalCategoryCreateDto = new GoalCategoryCreateDto(0, id.get(index), false);
                    } else{
                        selectCategories.add(new CategoryCompact(id.get(index), icon.get(index), name.get(index), ex.get(index), true));
                        goalCategoryCreateDto = new GoalCategoryCreateDto(0, id.get(index), true);
                    }

                    goalCategoryCreateDtoList.add(i, goalCategoryCreateDto);
                }

                Intent intent = new Intent(getApplicationContext(), OnboardingEntireBudgetActivity.class);
                intent.putExtra("selectCategory", selectCategories);
                intent.putExtra("goalCategoryCreateDtoList",  goalCategoryCreateDtoList);
                startActivity(intent);
            }
        });
    }

    private void getAllCategory() {
        HeaderRetrofit headerRetrofit = new HeaderRetrofit();
        Retrofit retrofit = headerRetrofit.getTokenHeaderInstance(token);
        RetrofitService retroService = retrofit.create(RetrofitService.class);

        Call<JsonObject> call = retroService.getCategoryList();
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    JsonObject responseJson = response.body();

                    Log.d(TAG, responseJson.toString());

                    if (responseJson.get("statusCode").getAsInt() == 200) {
                        if (!responseJson.get("data").isJsonNull()) {
                            JsonArray jsonArray = responseJson.get("data").getAsJsonArray();

                            Gson gson = new Gson();
                            categories = gson.fromJson(jsonArray, new TypeToken<ArrayList<CategoryCompact>>() {}.getType());

                            if (categories != null) {
                                initAdapter();
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(OnboardingCategoryActivity.this, "??????????????? ???????????? ????????????.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ????????? ????????? ?????? ??????????????? ????????? ??? ????????????
    ActivityResultLauncher<Intent> startActivityResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent intent = result.getData();
                        CategoryCompact newCategory = (CategoryCompact) intent.getSerializableExtra("newCategory");

                        int newCategoryId = newCategory.getCategoryId();
                        String newIcon = newCategory.getEmoji();
                        String newName = newCategory.getName();
                        String newEx = newCategory.getDetail();

                        id.add(newCategoryId);
                        icon.add(newIcon);
                        name.add(newName);
                        ex.add(newEx);

                        categoryGridViewAdapter.addItem(new CategoryCompact(newCategoryId, newIcon, newName, newEx, true));

                        gridView.invalidateViews();
                        gridView.setAdapter(categoryGridViewAdapter);

                        // ??? ???????????? ?????? ??? ????????? ???????????? ?????? ????????? ????????? background ??????
                        ViewTreeObserver vto = gridView.getViewTreeObserver();
                        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                            @Override
                            public void onGlobalLayout() {
                                String[] colorIcons = {"@drawable/coffee_color", "@drawable/food_color", "@drawable/beer_color", "@drawable/book_color", "@drawable/bus_color", "@drawable/bag_color", "@drawable/computer_color","@drawable/tissue_color", "@drawable/pill_color"};

                                for (int x = 0; x < selectedItem.size(); x++) {
                                    ConstraintLayout cl = (ConstraintLayout) gridView.getChildAt(selectedItem.get(x));
                                    ImageView iv = (ImageView) cl.findViewById(R.id.iv_category_icon);
                                    cl.setBackgroundResource(R.drawable.button_category_clicked);

                                    // ?????? ??????????????? ??????
                                    if (selectedItem.get(x) < 9){
                                        int resId = getResources().getIdentifier( colorIcons[selectedItem.get(x)], "drawable", getPackageName());
                                        iv.setImageResource(resId); // ?????? ????????????
                                    }
                                }
                            }
                        });

                    }
                }
            });

}
