package com.dnd.moneyroutine;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.dnd.moneyroutine.custom.Constants;
import com.dnd.moneyroutine.custom.PreferenceManager;
import com.dnd.moneyroutine.custom.SoftKeyboardDetector;
import com.dnd.moneyroutine.dto.UserForm;
import com.dnd.moneyroutine.service.RequestService;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupStep2Activity extends AppCompatActivity {

    private final static String TAG = "SignupStep2";

    private ImageButton ibBack;
    private EditText etPassword;
    private EditText etPasswordRepeat;
    private TextView tvPasswordConfirm;
    private Button btnNext;
    private ImageButton ibPwRemove;
    private ImageButton ibPwRepeatRemove;

    private ConstraintLayout.LayoutParams contentLayoutParams;
    private InputMethodManager inputManager;
    private SoftKeyboardDetector softKeyboardDetector;

    private UserForm userForm;
    private float scale;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);

        Configuration configuration = new Configuration(newBase.getResources().getConfiguration());
        configuration.fontScale = 1;
        applyOverrideConfiguration(configuration);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_step2);

        initView();
        initField();
        setListener();
    }

    private void initView() {
        ibBack = findViewById(R.id.ib_sign2_back);
        etPassword = findViewById(R.id.et_signup2_password);
        etPasswordRepeat = findViewById(R.id.et_signup2_password_repeat);
        tvPasswordConfirm = findViewById(R.id.tv_password_confirm);
        btnNext = findViewById(R.id.btn_signup2_next);
        ibPwRemove = findViewById(R.id.ib_sign2_password_remove);
        ibPwRepeatRemove = findViewById(R.id.ib_sign2_password_repeat_remove);
    }

    private void initField() {
        contentLayoutParams = (ConstraintLayout.LayoutParams) btnNext.getLayoutParams();

        inputManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);

        softKeyboardDetector = new SoftKeyboardDetector(this);
        addContentView(softKeyboardDetector, new FrameLayout.LayoutParams(-1, -1));

        scale = getResources().getDisplayMetrics().density;
        userForm = (UserForm) getIntent().getSerializableExtra("userForm");
    }

    private void setListener() {
        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ????????? ?????????
                if (etPassword.isFocused() || etPasswordRepeat.isFocused()) {
                    inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }

                joinToServer();
            }
        });

        ibPwRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etPassword.setText("");
            }
        });

        ibPwRepeatRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etPasswordRepeat.setText("");
            }
        });

        // ???????????? ????????? ??? ??????
        softKeyboardDetector.setOnHiddenKeyboard(new SoftKeyboardDetector.OnHiddenKeyboardListener() {
            @Override
            public void onHiddenSoftKeyboard() {
                getCurrentFocus().clearFocus();

                if (btnNext.isEnabled()) {
                    btnNext.setBackgroundResource(R.drawable.button_enabled_true);
                } else {
                    btnNext.setBackgroundResource(R.drawable.button_enabled_false);
                }

                contentLayoutParams.setMarginStart((int) (16 * scale + 0.2f));
                contentLayoutParams.setMarginEnd((int) (16 * scale + 0.2f));
                contentLayoutParams.bottomMargin = (int) (56 * scale + 0.2f);
                btnNext.setLayoutParams(contentLayoutParams);
            }
        });

        // ???????????? ????????? ??? ??????
        softKeyboardDetector.setOnShownKeyboard(new SoftKeyboardDetector.OnShownKeyboardListener() {
            @Override
            public void onShowSoftKeyboard() {
                if (btnNext.isEnabled()) {
                    btnNext.setBackgroundResource(R.drawable.button_enabled_true_keyboard_up);
                } else {
                    btnNext.setBackgroundResource(R.drawable.button_enabled_false_keyboard_up);
                }

                contentLayoutParams.setMarginStart(0);
                contentLayoutParams.setMarginEnd(0);
                contentLayoutParams.bottomMargin = 0;
                btnNext.setLayoutParams(contentLayoutParams);
            }
        });

        //  ??????????????? ?????? ?????? ????????? ????????? ?????????
        TextView.OnEditorActionListener onEditorActionListener = new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                switch (actionId) {
                    case EditorInfo.IME_ACTION_DONE:
                        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                        if (etPassword.isFocused()) {
                            etPassword.clearFocus();
                        }

                        if (etPasswordRepeat.isFocused()) {
                            etPasswordRepeat.clearFocus();
                        }
                        break;
                    default:
                        // ?????? ????????? ??????
                        return false;
                }

                return true;
            }
        };

        etPassword.setOnEditorActionListener(onEditorActionListener);
        etPasswordRepeat.setOnEditorActionListener(onEditorActionListener);

        // focus ??????????????? ??????
        View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focus) {
                switch (view.getId()) {
                    case R.id.et_signup2_password:
                        if (focus) {
                            view.setBackgroundResource(R.drawable.textbox_typing);

                            if (etPassword.length() != 0) {
                                ibPwRemove.setVisibility(View.VISIBLE);
                            }
                        } else {
                            view.setBackgroundResource(R.drawable.textbox_default);
                            ibPwRemove.setVisibility(View.INVISIBLE);
                        }
                        break;
                    case R.id.et_signup2_password_repeat:
                        if (focus) {
                            view.setBackgroundResource(R.drawable.textbox_typing);

                            if (etPasswordRepeat.length() != 0) {
                                ibPwRepeatRemove.setVisibility(View.VISIBLE);

                                if (!etPasswordRepeat.getText().toString().equals(etPassword.getText().toString())) {
                                    etPasswordRepeat.setBackgroundResource(R.drawable.textbox_error);
                                }
                            }
                        } else {
                            view.setBackgroundResource(R.drawable.textbox_default);
                            ibPwRepeatRemove.setVisibility(View.INVISIBLE);
                        }
                        break;
                }
            }
        };

        etPassword.setOnFocusChangeListener(onFocusChangeListener);
        etPasswordRepeat.setOnFocusChangeListener(onFocusChangeListener);

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // ????????????, ???????????? ?????? ?????? ?????? ??????, ?????? ?????? ?????????/????????????
                if (etPasswordRepeat.length() == 0) {
                    tvPasswordConfirm.setVisibility(View.INVISIBLE);

                    if (etPasswordRepeat.isFocused()) {
                        etPasswordRepeat.setBackgroundResource(R.drawable.textbox_typing);
                    } else {
                        etPasswordRepeat.setBackgroundResource(R.drawable.textbox_default);
                    }

                    // ???????????? ????????? ?????? ?????? ???????????? ????????? ??????
                    if (inputManager.isAcceptingText()) {
                        btnNext.setBackgroundColor(Color.parseColor("#ADB5BD"));
                    } else {
                        btnNext.setBackgroundResource(R.drawable.button_enabled_false);
                    }

                    btnNext.setEnabled(false);
                } else {
                    if (etPasswordRepeat.getText().toString().equals(etPassword.getText().toString())) {
                        tvPasswordConfirm.setVisibility(View.INVISIBLE);

                        if (etPasswordRepeat.isFocused()) {
                            etPasswordRepeat.setBackgroundResource(R.drawable.textbox_typing);
                        } else {
                            etPasswordRepeat.setBackgroundResource(R.drawable.textbox_default);
                        }

                        if (inputManager.isAcceptingText()) {
                            btnNext.setBackgroundColor(Color.parseColor("#343a40"));
                        } else {
                            btnNext.setBackgroundResource(R.drawable.button_enabled_true);
                        }

                        btnNext.setEnabled(true);
                    } else {
                        tvPasswordConfirm.setVisibility(View.VISIBLE);
                        etPasswordRepeat.setBackgroundResource(R.drawable.textbox_error);

                        if (inputManager.isAcceptingText()) {
                            btnNext.setBackgroundColor(Color.parseColor("#ADB5BD"));
                        } else {
                            btnNext.setBackgroundResource(R.drawable.button_enabled_false);
                        }

                        btnNext.setEnabled(false);
                    }
                }

                if (etPassword.length() != 0 && etPassword.isFocused()) {
                    ibPwRemove.setVisibility(View.VISIBLE);
                } else {
                    ibPwRemove.setVisibility(View.INVISIBLE);
                }

                if (etPasswordRepeat.length() != 0 && etPasswordRepeat.isFocused()) {
                    ibPwRepeatRemove.setVisibility(View.VISIBLE);
                } else {
                    ibPwRepeatRemove.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };

        etPassword.addTextChangedListener(textWatcher);
        etPasswordRepeat.addTextChangedListener(textWatcher);
    }

    // ?????? ?????? ????????? ??????, ???????????? ?????? ?????? ???????????? ?????? ??? SharedPreferences??? ??????
    private void joinToServer() {
        userForm.setPassword(etPassword.getText().toString());

        Call<JsonObject> call = RequestService.getInstance().join(userForm);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    JsonObject responseJson = response.body();

                    Log.d(TAG, responseJson.toString());

                    if (responseJson.get("statusCode").getAsInt() == 200) {
                        JsonObject data = responseJson.get("data").getAsJsonObject();

                        String token = data.get("accessToken").getAsString();
                        String refreshToken = data.get("refreshToken").getAsString();

                        saveTokenAndMoveActivity(token, refreshToken);
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d(TAG, t.getMessage());
                Toast.makeText(SignupStep2Activity.this, "??????????????? ???????????? ????????????.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ???????????? ????????? ?????? ??????, ?????? ??????
    public void saveTokenAndMoveActivity(String jwtToken, String refreshToken) {
        PreferenceManager.setString(this, Constants.tokenKey, jwtToken);
        PreferenceManager.setString(this, Constants.REFRESH_TOKEN_KEY, refreshToken);

        Intent intent = new Intent(this, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // ?????? ?????? ?????? clear
        intent.putExtra("join", true);
        startActivity(intent);
    }
}