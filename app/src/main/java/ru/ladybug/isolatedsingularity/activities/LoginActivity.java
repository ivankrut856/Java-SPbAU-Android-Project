package ru.ladybug.isolatedsingularity.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.IOException;
import java.security.Permission;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.ladybug.isolatedsingularity.R;
import ru.ladybug.isolatedsingularity.net.RetrofitService;
import ru.ladybug.isolatedsingularity.net.retrofitmodels.AuthReportResponse;

public class LoginActivity extends AppCompatActivity {

    private Button loginButton;
    private ProgressBar progressBar;
    private EditText passwordInput;
    private EditText loginInput;

    /** {@inheritDoc} */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginButton = findViewById(R.id.loginButton);
        progressBar = findViewById(R.id.progressBar);
        passwordInput = findViewById(R.id.passwordInput);
        loginInput = findViewById(R.id.loginInput);

        checkDangerousPermissions();

        /*
         * Константы ключей должны быть вынесены в отдельные финальные поля
         * Сообщения, выводимые пользователю, должны быть вынесены в ресурсы
         *   приложения, для удобства использования и локализации
         */

        loginButton.setOnClickListener(view -> {
            Toast.makeText(getApplicationContext(), "Login attempt", Toast.LENGTH_SHORT).show();
            blockUI();
            RetrofitService.getInstance().getServerApi().tryLogin(loginInput.getText().toString(), passwordInput.getText().toString()).enqueue(new Callback<AuthReportResponse>() {
                @Override
                public void onResponse(@NonNull Call<AuthReportResponse> call, @NonNull Response<AuthReportResponse> response) {
                    if (!response.isSuccessful() || response.body() == null) {
                        onFailure(call, new IOException("Bad response"));
                        return;
                    }

                    if (response.body().getResponse().equals("wa")) {
                        Toast.makeText(getApplicationContext(), "Unable to login. Please check login and password", Toast.LENGTH_LONG).show();
                        unblockUI();
                        return;
                    }

                    unblockUI();
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.putExtra("user_id", response.body().getUserId());
                    intent.putExtra("token", response.body().getToken());
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onFailure(@NonNull Call<AuthReportResponse> call, @NonNull Throwable t) {
                    Toast.makeText(getApplicationContext(), "Unable to login. Please check your network connection", Toast.LENGTH_LONG).show();
                    unblockUI();
                    Log.d("DEBUG", "fail", t);
                }
            });
        });
    }

    private void checkDangerousPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION},
                1);
    }

    /** {@inheritDoc} */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length >= 4 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[2] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[3] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    Toast.makeText(this, "Permissions were not granted", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }
    }

    private void blockUI() {
        loginButton.setClickable(false);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void unblockUI() {
        loginButton.setClickable(true);
        progressBar.setVisibility(View.INVISIBLE);
    }

}
