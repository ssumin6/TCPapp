package com.network.user.a20160333_proj5;

import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {

    //request_Code
    final int file_choose = 101;

    String dest_IP, key, fo_name;
    Uri file_path;
    int dest_port=-1;
    boolean crypt = true; // true is encryption, false is decryption
    byte[] buf = new byte[1024000];
    int buf_size = 0;

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
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                startActivityForResult(intent, file_choose);
            }
        });

        //Button related to socket connection and data transmission
        connectButton = (Button)findViewById(R.id.button2);
        connectButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                dest_IP = IP_text.getText().toString();
                if (!PORT_text.getText().toString().equals("")) {
                    dest_port = Integer.parseInt(PORT_text.getText().toString());
                }
                key = key_text.getText().toString();
                fo_name = result.getText().toString();

//                if (!fo_name.equals("")){
//                    String path = "/sdcard/";//이 코드만으로는 dir 안 생김
//                    String text = "apple";
//                    try{
//                        File file = new File(path);
//                        if (!file.exists()){
//                            file.mkdirs();
//                        }
//                        File new_file = new File(path,fo_name);
//                        if (!new_file.exists()){
//                            new_file.createNewFile();
//                        }
//                        Writer writer = new FileWriter(new_file);
//                        BufferedWriter bw = new BufferedWriter(writer);
//                        bw.write(text);
//                        bw.close();
//                    }catch(IOException e){
//                        e.printStackTrace();
//                    }
//                }
                if (dest_IP.equals("") || dest_port==-1||key.equals("")||fo_name.equals("")){
                    Toast.makeText(getApplicationContext(),"Fill in Everything",Toast.LENGTH_SHORT).show();
                }else {
                    SendTask task = new SendTask(getApplicationContext(),dest_IP, dest_port, key, crypt, fo_name, buf, buf_size);
                    task.execute();//execute the AsyncTask in background.
                }
                buf_size = 0;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        switch(requestCode){
            case file_choose:
                if (resultCode == RESULT_OK){
                    String file_path = data.getData().getPath();
                    try {//계속 여기서 filenotfound exception 일어남
                        File open_file = new File(file_path);
                        FileInputStream is = new FileInputStream(open_file);
                        int tmp;
                        while((tmp = is.read(buf))>=0){
                            buf_size += tmp;
                        }
                        is.close();
                    }catch(FileNotFoundException e){
                        e.printStackTrace();
                    }catch(IOException e){
                        e.printStackTrace();
                    }

                    Toast.makeText(getApplicationContext(), "File Selection Succeed\nChoosen File : "+file_path,Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
class SendTask extends AsyncTask<Void, Void, Void>{
    final int HEADER_SIZE = 16;//byte 단위

    Context mContext;
    String host_addr;
    int host_port;
    String key, filename;
    byte[] input_data;
    int input_size;
    boolean mode; //false is decryption, true is encryption.


    long chkvalue;

    SendTask(Context context, String addr, int port, String get_key, boolean crypt_mode, String name, byte[] buf, int size){
        mContext = context;
        host_addr = addr;
        host_port = port;
        key = get_key;//encryption|decryption key.
        mode = crypt_mode;
        filename = name;//.txt file name that contains result
        input_data = buf;
        input_size = size;

        chkvalue = -1;
    }

    public short getChecksum(byte[] buf) {
        int data;
        int sum = 0;

        int i;
        for(i=0; i<buf.length; i+=2){
            data = ((buf[i]&0xff)<<8)|(buf[i+1]&0xff);
            sum += data;
            if ((sum>>16)>0){
                sum = sum&0xffff+ (sum>>16);
            }
        }
        if (buf.length%2==1) {
            data = (buf[i]&0xff)<<8;
            sum += data;
            if ((sum>>16)>0){
                sum = sum&0xffff +(sum >>16);
            }
        }

        sum = sum^0xffff;
        return (short)sum;
    }


    @Override
    protected Void doInBackground(Void... arg0){
        Socket socket = null;
        try{
            //make a connection with host
            socket = new Socket(host_addr, host_port);

            //make a packet
            ByteBuffer headerBuffer = ByteBuffer.allocate(HEADER_SIZE + input_size);
            byte[] buf;
            short op = 0;
            if (!mode) {
                op = 1;//decryption
            }
            op = Short.reverseBytes(op);//same as htons
            headerBuffer.putShort(op);
            short checksum = 0; //2 byte checksum
            headerBuffer.putShort(checksum);
            headerBuffer.put(key.getBytes());
            long length = HEADER_SIZE+input_size;
            Log.d("size","size of header is :"+length);
            length = Long.reverseBytes(length);//htonl length
            headerBuffer.putLong(length);
            headerBuffer.put(input_data,0,input_size);

            buf = headerBuffer.array();
            //checksum calculation
            checksum = getChecksum(buf);
            Log.d("CHKSUM", "calculated value : "+checksum);
            headerBuffer.putShort(2,checksum);//put checksum at 2bytes.

            chkvalue = getChecksum(headerBuffer.array());

            Log.d("CHKSUM",""+chkvalue);

            //start outputStream
            OutputStream outputStream= socket.getOutputStream();
            //write header and file.
            outputStream.write(headerBuffer.array());

            //receive packet
//            InputStream inputStream = socket.getInputStream();
//            //read header first
//            byte[] input_header = new byte[HEADER_SIZE];
//            byte[] data = new byte[1024000];
//            inputStream.read(input_header,0, HEADER_SIZE);
//            inputStream.read(data);
//
//            //make file
//            String path = "/sdcard/Test/";
//            File file = new File(path);
//            if (!file.exists()){
//                file.mkdirs();
//            }
//
//            //add file name to the directory
//            File new_file = new File(path, filename);
//            if (!new_file.exists()){
//                //if file doesn't exist, create a file.
//                new_file.createNewFile();
//                Log.d("BACK","CREATE a new FILE");
//            }
//            MediaScannerConnection.scanFile(mContext, new String[]{Environment.getExternalStorageDirectory().getAbsolutePath()+filename}, null, null);
//            FileOutputStream fos = new FileOutputStream(new_file);
//            fos.write(data);
//            Log.d("DATA",""+data);
//
//            //check header
//            fos.close();

            outputStream.close();
//            inputStream.close();
        }catch(UnknownHostException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }finally {
            if (socket != null){
                try {
                    socket.close();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
    @Override
    protected void onPostExecute(Void result){
        super.onPostExecute(result);
        Toast.makeText(mContext, "Connection OKAY HOST \nChecksum of is :" +chkvalue, Toast.LENGTH_SHORT).show();
    }
        }