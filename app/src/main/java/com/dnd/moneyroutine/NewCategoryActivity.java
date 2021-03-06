package com.dnd.moneyroutine;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dnd.moneyroutine.custom.Constants;
import com.dnd.moneyroutine.custom.PreferenceManager;
import com.dnd.moneyroutine.custom.SoftKeyboardDetector;
import com.dnd.moneyroutine.dto.CategoryCompact;
import com.dnd.moneyroutine.dto.CustomCategoryCreateDto;
import com.dnd.moneyroutine.service.HeaderRetrofit;
import com.dnd.moneyroutine.service.RetrofitService;
import com.google.gson.JsonObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class NewCategoryActivity extends AppCompatActivity {

    private LinearLayout linearNewCategory;
    private LinearLayout linearNewEx;
    private LinearLayout linearNewIcon;
    private ConstraintLayout bgBlack;
    private TextView tvNewEmoji;
    private EditText etNewCategory;
    private EditText etNewEx;
    private ImageView ivEraseName;
    private ImageView ivEraseEx;
    private ImageView ivBack;

    private Button btnConfirm;

    private String token;

    private SoftKeyboardDetector softKeyboardDetector;
    private InputMethodManager inputManager;
    private ConstraintLayout.LayoutParams contentLayoutParams;
    private float scale;

    boolean flag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_category);

        initView();
        enterNewCategory(); //???????????? ?????? ??????
        addEmoji(); //????????? ??????
        setBtnSize();

        //?????? ??????
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //??????
        btnConfirm.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String name = etNewCategory.getText().toString();
                String ex = etNewEx.getText().toString();
                String icon = tvNewEmoji.getText().toString();

                String iconToByte = "";
                try {
                    iconToByte = URLEncoder.encode(icon, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                customCategoryServer(iconToByte, name, ex);
            }
        });
    }

    private void initView() {
        token = PreferenceManager.getToken(NewCategoryActivity.this, Constants.tokenKey);

        linearNewCategory = findViewById(R.id.linear_new_category);
        linearNewEx = findViewById(R.id.linear_new_category_ex);
        etNewCategory = findViewById(R.id.et_new_category);
        etNewEx = findViewById(R.id.et_new_category_ex);
        ivEraseName = findViewById(R.id.iv_erase_new_category);
        ivEraseEx = findViewById(R.id.iv_erase_new_category_ex);
        btnConfirm = findViewById(R.id.btn_next_new_category);
        ivBack = findViewById(R.id.iv_back_new_category);
        linearNewIcon = findViewById(R.id.linear_emoji);
        tvNewEmoji = findViewById(R.id.tv_emoji);
        bgBlack = findViewById(R.id.bg_black);
        inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
    }

    private void enterNewCategory() {
        //?????? edittext ????????? background ?????????
        etNewCategory.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean isFocus) {
                if (isFocus) {
                    linearNewCategory.setBackgroundResource(R.drawable.textbox_typing);
                    ivEraseName.setVisibility(View.VISIBLE);

                    //x ????????? ????????? edittext ??????
                    ivEraseName.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            etNewCategory.setText("");
                        }
                    });
                } else {
                    linearNewCategory.setBackgroundResource(R.drawable.textbox_default);
                    ivEraseName.setVisibility(View.INVISIBLE);
                }
            }
        });

        //?????? edittext ????????? ????????? ??????
        etNewEx.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean isFocus) {
                if (isFocus) {
                    linearNewEx.setBackgroundResource(R.drawable.textbox_typing);
                    ivEraseEx.setVisibility(View.VISIBLE);

                    ivEraseEx.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            etNewEx.setText("");
                        }
                    });
                } else {
                    linearNewEx.setBackgroundResource(R.drawable.textbox_default);
                    ivEraseEx.setVisibility(View.INVISIBLE);
                }
            }
        });

    }

    private void setBtnSize() {
        softKeyboardDetector = new SoftKeyboardDetector(this);
        addContentView(softKeyboardDetector, new FrameLayout.LayoutParams(-1, -1));

        contentLayoutParams = (ConstraintLayout.LayoutParams) btnConfirm.getLayoutParams();
        scale = getResources().getDisplayMetrics().density;

        bgBlack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bgBlack.setVisibility(View.GONE);
                inputManager.hideSoftInputFromWindow(NewCategoryActivity.this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });


        //????????? ???????????? ???
        softKeyboardDetector.setOnHiddenKeyboard(new SoftKeyboardDetector.OnHiddenKeyboardListener() {
            @Override
            public void onHiddenSoftKeyboard() {

                //????????? ???????????? ????????? ????????? ??????????????? ?????? ?????? ????????????
                bgBlack.setVisibility(View.GONE);

                if (etNewCategory.getText().toString().length() > 0) {
                    btnConfirm.setBackgroundResource(R.drawable.button_enabled_true);
                } else {
                    btnConfirm.setBackgroundResource(R.drawable.button_enabled_false);
                }

                contentLayoutParams.setMarginStart((int) (16 * scale + 0.2f));
                contentLayoutParams.setMarginEnd((int) (16 * scale + 0.2f));
                contentLayoutParams.bottomMargin = (int) (56 * scale + 0.2f);
                btnConfirm.setLayoutParams(contentLayoutParams);
            }
        });

        // ???????????? ???????????? ???
        softKeyboardDetector.setOnShownKeyboard(new SoftKeyboardDetector.OnShownKeyboardListener() {
            @Override
            public void onShowSoftKeyboard() {

                if (etNewCategory.getText().toString().length() > 0) {
                    btnConfirm.setBackgroundResource(R.drawable.button_enabled_true_keyboard_up);
                } else {
                    btnConfirm.setBackgroundResource(R.drawable.button_enabled_false_keyboard_up);
                }

                contentLayoutParams.setMarginStart(0);
                contentLayoutParams.setMarginEnd(0);
                contentLayoutParams.bottomMargin = 0;
                btnConfirm.setLayoutParams(contentLayoutParams);
            }
        });

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //?????? ????????? ?????????????????? ?????? enable
                if (etNewCategory.length() > 0 && etNewEx.length() > 0) {
                    btnConfirm.setEnabled(true);

                    if (inputManager.isAcceptingText()) {
                        btnConfirm.setBackgroundResource(R.drawable.button_enabled_true_keyboard_up);
                    } else {
                        btnConfirm.setBackgroundResource(R.drawable.button_enabled_true);
                    }
                } else {
                    btnConfirm.setEnabled(false);

                    if (inputManager.isAcceptingText()) {
                        btnConfirm.setBackgroundResource(R.drawable.button_enabled_false_keyboard_up);
                    } else {
                        btnConfirm.setBackgroundResource(R.drawable.button_enabled_false);
                    }
                }


            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };

        etNewCategory.addTextChangedListener(textWatcher);
        etNewEx.addTextChangedListener(textWatcher);
    }


    //????????? ??????
    private void addEmoji() {
        EditText et_emoji = findViewById(R.id.et_emoji);
        ArrayList<String> newEmoji = new ArrayList<>();
        newEmoji.add(tvNewEmoji.getText().toString());


        linearNewIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //????????? ?????? ????????? ????????? ????????? ?????? ?????????
                et_emoji.requestFocus();
                et_emoji.setInputType(1);
                inputManager.showSoftInput(et_emoji, 0);

                et_emoji.requestFocus();
                et_emoji.setCursorVisible(true);
                bgBlack.setVisibility(View.VISIBLE);
            }
        });

        et_emoji.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (et_emoji.length() > 0 ) {
                    flag = true;
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

                //????????? ?????? ??????????????? ????????? ?????? textview??? ????????? ??? ??????
                if (flag) {
                    flag = false;

                    if (isEmoji(et_emoji.getText().toString())) {
                        if (tvNewEmoji.getText() != et_emoji.getText()) {
                            tvNewEmoji.setText(et_emoji.getText());
                        }

                        inputManager.hideSoftInputFromWindow(NewCategoryActivity.this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                        bgBlack.setVisibility(View.GONE);
                    } else {
                        Toast.makeText(NewCategoryActivity.this, "???????????? ????????? ?????????", Toast.LENGTH_SHORT).show();
                    }

                    et_emoji.setText("");
                }
            }
        });
    }


    private static boolean isEmoji(String message) {
        Pattern rex = Pattern.compile("[\\x{10000}-\\x{10ffff}\ud800-\udfff]");
//        Pattern rex = Pattern.compile("[^\uAC00-\uD7A3xfe0-9a-zA-Z???-???\\s]");
        Matcher matcher = rex.matcher(message);

        return matcher.matches();
    }

//    private static boolean isLetter(String message){
//        Pattern rex = Pattern.compile("^[0-9a-zA-Z???-???]*$");
//        Matcher matcher = rex.matcher(message);
//
//        return matcher.find();
//    }

    private void customCategoryServer(String emoji, String name, String detail) {
        CustomCategoryCreateDto customCategory = new CustomCategoryCreateDto(emoji, name, detail);

        HeaderRetrofit headerRetrofit = new HeaderRetrofit();
        Retrofit retrofit = headerRetrofit.getTokenHeaderInstance(token);
        RetrofitService retroService = retrofit.create(RetrofitService.class);

        Call<JsonObject> call = retroService.create(customCategory);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    JsonObject responseJson = response.body();
                    Log.d("custom category", responseJson.toString());

                    if (responseJson.get("statusCode").getAsInt() == 200 && !responseJson.get("data").isJsonNull()) {
                        int newCategoryId = responseJson.get("data").getAsInt();

                        CategoryCompact newItem = new CategoryCompact(newCategoryId, emoji, name, detail, true);

                        Intent intent = new Intent(getApplicationContext(), OnboardingCategoryActivity.class);
                        intent.putExtra("newCategory", newItem);
                        setResult(RESULT_OK, intent);

                        finish();
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("custom category", "fail: " + t.getMessage());
                Toast.makeText(NewCategoryActivity.this, "??????????????? ???????????? ????????????.", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
