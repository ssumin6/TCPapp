package com.network.user.a20160333_proj5;

import android.content.Context;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity {

    String dest_IP, key;
    int dest_port;
    boolean crypt = true; // true is encryption, false is decryption
    EditText IP_text, PORT_text, key_text, result;
    Button fileChoose, connectButton;
    RadioGroup crypto;
    RadioButton encry_button, decry_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        IP_text = (EditText)findViewById(R.id.edit1);
        PORT_text = (EditText)findViewById(R.id.edit2);
        key_text = (EditText)findViewById(R.id.edit3);
        result = (EditText)findViewById(R.id.edit4);

        //RadioGroup to choose cryptography mode whether to encrypt or decrypt.
        crypto = (RadioGroup)findViewById(R.id.radio_group);
        encry_button = (RadioButton)findViewById(R.id.encrypt);
        decry_button = (RadioButton)findViewById(R.id.decrypt);
        encry_button.setChecked(true);//initially set to encrypt mode.

        crypto.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId){
                //change boolean variable when the group selection is changed.
                switch(checkedId){
                    case R.id.encrypt:
                        crypt = true;
                        break;
                    case R.id.decrypt:
                        crypt = false;
                        break;
                }
            }
        });

        //Button to choose file
        fileChoose = (Button)findViewById(R.id.button1);
        fileChoose.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){

            }
        });

        //Button related to socket connection and data transmission
        connectButton = (Button)findViewById(R.id.button2);
        connectButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                dest_IP = IP_text.getText().toString();
                dest_port = Integer.getInteger(PORT_text.getText().toString());
                key = key_text.getText().toString();

                SendTask task = new SendTask(getApplicationContext(),dest_IP, dest_port, key, crypt);
                task.execute();//execute the AsyncTask in background.
            }
        });

    }
}
class SendTask extends AsyncTask<Void, Void, Void>{

    Context mContext;
    String host_addr;
    int host_port;
    String key;
    boolean mode; //false is decryption, true is encryption.

    SendTask(Context context, String addr, int port, String get_key, boolean crypt_mode){
        mContext = context;
        host_addr = addr;
        host_port = port;
        key = get_key;
        mode = crypt_mode;
    }

    @Override
    protected Void doInBackground(Void... arg0){
        Socket socket = null;
        try{
            //make a connection with host
            socket = new Socket(host_addr, host_port);

            //send packet


            //receive packet

        }catch(UnknownHostException e){
            e.printStackTrace();
            Toast.makeText(mContext, "UnknownHostException", Toast.LENGTH_SHORT).show();
        }catch(IOException e){
            e.printStackTrace();
            Toast.makeText(mContext, "IOException", Toast.LENGTH_SHORT).show();
        }finally {
            if (socket != null){
                try {
                    socket.close();
                }catch(IOException e){
                    e.printStackTrace();
                    Toast.makeText(mContext, "IOException", Toast.LENGTH_SHORT).show();
                }
            }
        }
        return null;
    }
    @Override
    protected void onPostExecute(Void result){
        super.onPostExecute(result);
        Toast.makeText(mContext, "Received Text from HOST", Toast.LENGTH_SHORT).show();
    }
        }