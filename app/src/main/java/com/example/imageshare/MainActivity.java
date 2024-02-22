package com.example.imageshare;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private final int RequestCode = 1000;
    public Switch bluetoothSwitch;
    public Button selectBtn;
    public Button shareBtn;
    public BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    public ImageView imageView;
    public BitmapDrawable drawable;
    public Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //assign variables to identifiers
        bluetoothSwitch = findViewById(R.id.bletoothSwitch);
        selectBtn = findViewById(R.id.button);
        imageView = findViewById(R.id.imageView);
        shareBtn = findViewById(R.id.button2);

        //again open task
        if (mBluetoothAdapter.isEnabled()) {
            bluetoothSwitch.setText("On");
            bluetoothSwitch.setChecked(true);
        } else {
            bluetoothSwitch.setText("off");
            bluetoothSwitch.setChecked(false);
        }

        //switch task
        bluetoothSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mBluetoothAdapter.isEnabled()) {
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(getApplicationContext(), "Bluetooth Permission not granted", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    mBluetoothAdapter.disable();
                    Toast.makeText(getApplicationContext(), "Bluetooth Off", Toast.LENGTH_SHORT).show();
                    bluetoothSwitch.setText("Off");
                } else {
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(getApplicationContext(), "Bluetooth Permission not granted", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 1);
                    Toast.makeText(getApplicationContext(), "Bluetooth On", Toast.LENGTH_SHORT).show();
                    bluetoothSwitch.setText("On");
                }
            }
        });

        //image select button
        selectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, RequestCode);
            }
        });

        //image share button
        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareImage();
            }
        });

    }

    //method of image set to imageview
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode==RESULT_OK)
        {
            if(requestCode==RequestCode)
            {
                imageView.setImageURI(data.getData());
            }
        }
    }

    //image share handle method
    public void shareImage(){
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        drawable = (BitmapDrawable) imageView.getDrawable();
        bitmap = drawable.getBitmap();
        File file = new File(getExternalCacheDir()+"/"+"Select Image");
        Intent intent;
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,outputStream);
            outputStream.flush();
            outputStream.close();
            intent = new Intent(Intent.ACTION_SEND);
            intent.setType("image/jpeg");
            Uri contentUri = FileProvider.getUriForFile(this, "com.example.imageshare.fileprovider", file);
            intent.putExtra(Intent.EXTRA_STREAM, contentUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Share image via:"));
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(this, "Error sharing image", Toast.LENGTH_SHORT).show();
        }
    }
}