package com.example.nasa.klog;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity
{
    final Context context = this;
    EditText et;
    Button sn;
    Boolean bug = true;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sn=(Button)findViewById(R.id.scan);
        RelativeLayout relativeLayout=(RelativeLayout)findViewById(R.id.relative);
        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String filepath = Environment.getExternalStorageDirectory().toString() + File.separator + "test.txt";
                FileOutputStream fos = null;
                try
                {
                        fos = new FileOutputStream(filepath);
                        byte[] buffer = "This will be writtent in test.txt".getBytes();
                        fos.write(buffer, 0, buffer.length);
                        fos.close();
                }
                catch (FileNotFoundException e)
                {
                    e.printStackTrace();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    if (fos != null)
                        try
                        {
                            fos.close();
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                }
            }
        });
        Intent intent = new Intent(MainActivity.this , MyService.class);
        startService(intent);
        sn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(et,InputMethodManager.SHOW_FORCED);
                System.out.println("EditTextKeyboard " + imm.getCurrentInputMethodSubtype());
                InputMethodSubtype subtype = imm.getCurrentInputMethodSubtype();
                List<InputMethodInfo> inputMethodInfos = imm.getEnabledInputMethodList();

                for (InputMethodInfo ininfo:inputMethodInfos)
                {
                    Log.d("KP: ","Package :"+ ininfo.getPackageName());
                }
                if (subtype==null)
                {
                    try
                    {
                        ContentResolver resolver=getContentResolver();
                        String oldDefaultKeyboard = Settings.Secure.getString(resolver, Settings.Secure.DEFAULT_INPUT_METHOD);
                        String[] selectedkeyboard = oldDefaultKeyboard.split("/");
                        Log.d("Btype","Enabled: "+selectedkeyboard[0]);

                        for (InputMethodInfo inputinfo : inputMethodInfos)
                        {
                            Log.i("info","Installed Package:" + inputinfo.getPackageName());
                            System.out.println("P"+ getPackageManager());
                            PackageInfo info = getPackageManager().getPackageInfo(inputinfo.getPackageName(), PackageManager.GET_PERMISSIONS);
                            String iv=info.packageName;

//                        String appName = info.applicationInfo.name;
//                        System.out.println("appName: "+appName);

                            final PackageManager pm = getApplicationContext().getPackageManager();
                            ApplicationInfo ai;
                            try
                            {
                                ai = pm.getApplicationInfo( iv, 0);
                            }
                            catch (final PackageManager.NameNotFoundException e)
                            {
                                ai = null;
                            }
                            final String appName = (String) (ai != null ? pm.getApplicationLabel(ai) : "(unknown)");

                            System.out.println("iv "+iv);
                            if(selectedkeyboard[0].equals(iv))
                            {
                                ArrayList<String> permission = new ArrayList<String>();
                                for (String p : info.requestedPermissions)
                                {
                                    Log.d(inputinfo.getPackageName(), "Permission : " + p);
                                    permission.add(p);
                                }
//                        for(String p:permission)
//                        {
//                            System.out.println("xyz :"+p);
                                    if(permission.contains("android.permission.WRITE_EXTERNAL_STORAGE")&&(!permission.contains("android.permission.READ_EXTERNAL_STORAGE")))
                                {
                                    bug=false;
                                    ActivityManager am = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
                                    List<ActivityManager.RunningAppProcessInfo> pids = am.getRunningAppProcesses();
                                    Toast.makeText(getApplicationContext(), "Please Change Input Method Type", Toast.LENGTH_SHORT).show();

                                    AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                                    alert.setMessage("Choose Native KeyBoard or GBoard");
                                    alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            imm.showInputMethodPicker();
                                        }
                                    });
                                    alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
//                                    Toast.makeText(MainActivity.this, "Secure Data @ your own Risk", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    AlertDialog alertDialog = alert.create();
                                    alertDialog.show();
                                }

                                if(permission.contains("android.permission.READ_EXTERNAL_STORAGE")&&permission.contains("android.permission.WRITE_EXTERNAL_STORAGE")&&permission.contains("android.permission.ACCESS_NETWORK_STATE"))
                                {
                                    bug = false;
                                    Toast.makeText(getApplicationContext(), "DATA THEFT OCCURS", Toast.LENGTH_SHORT).show();
                                    String path = Environment.getExternalStorageDirectory().getPath()+"/";

                                    RecursiveFileObserver observer = new RecursiveFileObserver(path, MainActivity.this);
                                    observer.startWatching();
//                                    AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
//                                    alert.setMessage("PERMISSIONS OF "+appName.toUpperCase());
//                                    final ListView listView = new ListView(MainActivity.this);
//                                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
//                                            LinearLayout.LayoutParams.MATCH_PARENT,
//                                            LinearLayout.LayoutParams.MATCH_PARENT);
//                                    listView.setLayoutParams(layoutParams);
//                                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, permission );
//                                    alert.setPositiveButton("Done", new DialogInterface.OnClickListener()
//                                    {
//                                        public void onClick(DialogInterface dialogInterface, int i)
//                                        {
////                                            finishAffinity();
////                                            //imm.showInputMethodPicker();
//                                        }
//                                    });
//                                    listView.setAdapter(arrayAdapter);
//                                    alert.setView(listView);
//                                    AlertDialog alertDialog = alert.create();
//                                    alertDialog.show();
                                    final Dialog dialog = new Dialog(context);
                                    dialog.setContentView(R.layout.dialogbox);
                                    dialog.setTitle("Alert");

                                    TextView text = (TextView) dialog.findViewById(R.id.titlebar);
                                    text.setText("Permissions of "+appName);

                                    ListView plist = (ListView)dialog.findViewById(R.id.permissionlist);
                                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, permission );
                                    plist.setAdapter(arrayAdapter);

                                    Button dialogButton = (Button) dialog.findViewById(R.id.done);
                                    dialogButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v)
                                        {
                                            finish();
                                            dialog.dismiss();
                                        }
                                    });

                                    dialog.show();
                                }
                            }
                        }
//                }
                        if(bug==true)
                        {
                            Toast.makeText(getApplicationContext(), "External Keyboard Being Used but has no bugs", Toast.LENGTH_SHORT).show();
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Native Keyboard is being used", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}

