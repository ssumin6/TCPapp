package com.network.user.a20160333_proj5;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    EditText IP_text, PORT_text, key_text, result;
    Button fileChoose, connectButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        IP_text = (EditText)findViewById(R.id.edit1);
        PORT_text = (EditText)findViewById(R.id.edit2);
        key_text = (EditText)findViewById(R.id.edit3);
        result = (EditText)findViewById(R.id.edit4);

        fileChoose = (Button)findViewById(R.id.button1);
        fileChoose.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){

            }
        });

        connectButton = (Button)findViewById(R.id.button2);
        connectButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
//                Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT).show();
            }
        });

    }
}
