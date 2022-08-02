package com.aamrtu.aictestudent;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {

    ImageButton check_for_updates_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        check_for_updates_button = findViewById(R.id.check_for_update_btn);

        check_for_updates_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                check_for_updates_openDialogBox();
            }
        });
    }
    private void check_for_updates_openDialogBox()
    {
        Check_for_updates_dialogbox check_for_updates_dialogbox = new Check_for_updates_dialogbox();
        check_for_updates_dialogbox.show(getSupportFragmentManager(), "check for updates");
    }
}