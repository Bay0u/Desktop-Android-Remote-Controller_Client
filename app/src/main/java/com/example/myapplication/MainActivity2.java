package com.example.myapplication;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil;

import java.util.Arrays;

public class MainActivity2 extends AppCompatActivity {
    static Socket s;
    private Button button;
    private EditText mEdit;
    static InputStream in;
    static OutputStream out;
    static DataOutputStream dos;
    public String mouse ="0@0";
    public String input ="keyboard%";
    static Bundle savedInstanceState;
    static ImageView imageV;
    static SendMessage sendMessageTask;
    static byte[] totalImage =new byte[0];
    static byte[] oldImage = new byte[0];
    static boolean flag=false;
    static boolean flag2=false;
    static boolean isItFull = false;
    static boolean isDown=false;
    String oldCord="0@0";

    String url="https://hatrabbits.com/wp-content/uploads/2017/01/random.jpg";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        imageV = (ImageView) findViewById(R.id.imageView);
        button = (Button) findViewById(R.id.button3); // reference to the keybaord button
        mEdit   = (EditText)findViewById(R.id.textView3);
        if(savedInstanceState!=null){
            totalImage = savedInstanceState.getByteArray("OURSAVEDIMAGE");
        }
        sendMessageTask = new SendMessage();
        sendMessageTask.execute();

    }

    public void keyboard(View v){
        String input = mEdit.getText().toString();
        if(input.equals("")){
            AlertDialog.Builder builder
                    = new AlertDialog
                    .Builder(MainActivity2.this);
            builder.setMessage("You have to write something to send");
            builder.setTitle("Alert !");
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }else{
            if(s.isConnected()) {
                input = "keyboard%" + input;
                flag2=true;
            }
        }

    }

    public boolean onTouchEvent(MotionEvent event) {
        if(s.isConnected()){
        switch (event.getAction()& MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mouse= getXandY(event) + "%ACTION_DOWN";
                flag=true;
                isDown=true;
                oldCord =getXandY(event);
                break;

            case MotionEvent.ACTION_UP:
                mouse= getXandY(event) + "%ACTION_UP";
                isDown=false;
                flag=true;
                break;

            case MotionEvent.ACTION_MOVE:
                if(isDown){
                mouse= getXandY(event) + "%ACTION_MOVE";
                flag=true;
                }
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                if(isDown && !oldCord.equals("")) {
                    mouse= oldCord+"%ACTION_POINTER_DOWN";
                    oldCord="";
                    flag=true;
                }
                break;

//            case MotionEvent.ACTION_CANCEL:
//
//            case MotionEvent.ACTION_POINTER_DOWN:
        }
        }
        return false;
    }

    private String getXandY(MotionEvent event) {
        int x = (int)event.getX();
        int y = (int)event.getY();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        double height = displayMetrics.heightPixels;
        double width = displayMetrics.widthPixels;

        double touchX = (double)x/width;
        double touchY = (double)y/height;
        return touchX+"@"+touchY;
    }

    private void viewImage(byte[] imagebytes) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int height = displayMetrics.heightPixels;
                int width = displayMetrics.widthPixels;
                Bitmap bmp = BitmapFactory.decodeByteArray(imagebytes, 0, imagebytes.length);
                imageV.setImageBitmap(Bitmap.createScaledBitmap(bmp, width, height, false));
            }
        });

    }


    private class SendMessage extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            try {
                s = new Socket("My_IP_Address",4444); // ip address is of localhost because server is running on the same mschine
                if (s.isConnected()) {
                    System.out.println("Connected");
                }
                // sends output to the socket
                out = new DataOutputStream(s.getOutputStream());
                //takes input from socket
                in = new DataInputStream(s.getInputStream());
                dos = new DataOutputStream(out);

            } catch (UnknownHostException u) {
                System.out.println(u);
            } catch (IOException i) {
                System.out.println(i);
            }


            try {
                // Receiving reply from server
                while(s.isConnected()) {
//                    if(in.readUTF().equals("exit")){
//                        System.out.println("Closing connection");
//                        // close connection
//                        s.close();
//                        in.close();
//                    }
                    if(flag){
                        sendMouse(mouse);
                        mouse="";
                        flag = false;
                    }if(flag2){
                        sendKeyboard(input);
                        System.out.println("Sent to server : " + input);
                        input="";
                        flag2 = false;
                    }
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte buffer[] = new byte[100000];
                    baos.write(buffer, 0, in.read(buffer));
                    byte result[] = baos.toByteArray();
                    if(result.length>10){
                    isItFull = check(result);
                    }
                    if(isItFull){
                        result = subArray(result, 0, result.length-6);
                        totalImage=concat(totalImage,result);
                        int number = totalImage.length;
                        //System.out.println("Recieved from server : " + number);
                        //show Screen Shot
                        viewImage(totalImage);
                        oldImage = totalImage;
                        totalImage = new byte[0];
                    }else{

                        totalImage=concat(totalImage,result);
                    }

                }
            } catch (IOException i) {
                System.out.println(i);
            }

            return null;
        }

        private void sendMouse(String mouse) {
            try {
                dos.writeUTF(mouse);
                dos.flush(); // send the message
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        private void sendKeyboard(String key) {
            try {
                dos.writeUTF(key);
                dos.flush(); // send the message
                mEdit.setText("");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        private boolean check(byte[] result) {
            byte[] key = "Bayou".getBytes();//66 97 121 111 117
            byte[] keyTest =new byte[key.length];

            for(int i = 1 ; i<= key.length ; i++){
                keyTest[keyTest.length-i] = result[result.length-i];
            }
            if(Arrays.equals(keyTest, key)){
                return true;
            }

            return false;
        }
        public <T> byte[] subArray(byte[] array, int beg, int end) {
            return Arrays.copyOfRange(array, beg, end + 1);
        }

        public byte[] concat(byte[] array1 , byte[] array2){
            int numberOfImageBytes = array1.length;
            int numberOfKeyBytes = array2.length;
            byte[] result = new byte[numberOfImageBytes + numberOfKeyBytes];
            System.arraycopy(array1, 0, result, 0, numberOfImageBytes);
            System.arraycopy(array2, 0, result, numberOfImageBytes, numberOfKeyBytes);
            return result;
        }


    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putByteArray("OURSAVEDIMAGE",oldImage);
        System.out.println("d5lt outstateee : " + outState.getByteArray("OURSAVEDIMAGE").length );

    }
}
