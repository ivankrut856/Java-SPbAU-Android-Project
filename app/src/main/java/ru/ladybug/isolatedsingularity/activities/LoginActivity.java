package ru.ladybug.isolatedsingularity.activities;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginButton = findViewById(R.id.loginButton);
        progressBar = findViewById(R.id.progressBar);
        passwordInput = findViewById(R.id.passwordInput);
        loginInput = findViewById(R.id.loginInput);

        checkDangerousPermissions();

        loginButton.setOnClickListener(view -> {
            Toast.makeText(getApplicationContext(), "Login attempt", Toast.LENGTH_SHORT).show();
            blockUI();
            RetrofitService.getInstance().getServerApi().tryLogin(loginInput.getText().toString(), passwordInput.getText().toString()).enqueue(new Callback<AuthReportResponse>() {
                @Override
                public void onResponse(Call<AuthReportResponse> call, Response<AuthReportResponse> response) {
                    if (response.body().getResponse().equals("wa")) {
                        Toast.makeText(getApplicationContext(), "Unable to login. Please check login and password", Toast.LENGTH_LONG).show();
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
                public void onFailure(Call<AuthReportResponse> call, Throwable t) {
                    Toast.makeText(getApplicationContext(), "Unable to login. Please check your network connection", Toast.LENGTH_LONG).show();
                    unblockUI();
                }
            });
        });
    }

    private void checkDangerousPermissions() {
        try {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
        }
        catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Permissions were not granted", Toast.LENGTH_LONG).show();;
            finish();
        }
    }

    public void blockUI() {
        loginButton.setClickable(false);
        progressBar.setVisibility(View.VISIBLE);
    }

    public void unblockUI() {
        loginButton.setClickable(true);
        progressBar.setVisibility(View.INVISIBLE);
    }

}
