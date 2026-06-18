package hev.sockstun;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.VpnService;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import zhenshu.Root;
import zhenshu.sunny_zs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static zhenshu.zswenjian.APP_filePath;


/* loaded from: classes.dex */
public class MainActivity extends AppCompatActivity {

    private Button zhenshu;
    private Button button_apps;
    private Button button_control;
    private Button button_save;
    private CheckBox checkbox_global;
    private CheckBox xitong;
    private CheckBox checkbox_ipv4;
    private CheckBox checkbox_ipv6;
    private CheckBox checkbox_udp_in_tcp;
    private EditText edittext_dns_ipv4;
    private EditText edittext_dns_ipv6;
    private EditText edittext_socks_addr;
    private EditText edittext_socks_pass;
    private EditText edittext_socks_port;
    private EditText edittext_socks_user;
    private Preferences prefs;

    private TextView textView20;
    private TextView textView21;

    private static String[] PERMISSIONS_STORAGE = {"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.MANAGE_EXTERNAL_STORAGE"};
    private static final int REQUEST_CODE = 1;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;

    public static void verifyStoragePermissions(MainActivity activity) {
        try {
            int permission = ActivityCompat.checkSelfPermission(activity, "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != 0) {
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @SuppressLint("WrongConstant")
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    // androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.fragment_dashboard);

        //getSupportActionBar().hide();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        this.textView21 = (TextView) findViewById(R.id.textView21);
        this.textView20 = (TextView) findViewById(R.id.textView2);

        this.prefs = new Preferences(this);
        this.edittext_socks_addr = (EditText) findViewById(R.id.edit_ip);
        this.edittext_socks_port = (EditText) findViewById(R.id.edit_port);
        this.edittext_socks_user = (EditText) findViewById(R.id.edit_user);
        this.edittext_socks_pass = (EditText) findViewById(R.id.edit_pwd);
        this.edittext_dns_ipv4 = (EditText) findViewById(R.id.dns_ipv4);
        this.edittext_dns_ipv6 = (EditText) findViewById(R.id.dns_ipv6);
        this.checkbox_ipv4 = (CheckBox) findViewById(R.id.ipv4);
        this.checkbox_ipv6 = (CheckBox) findViewById(R.id.ipv6);
        this.checkbox_global = (CheckBox) findViewById(R.id.global);
        this.xitong = (CheckBox) findViewById(R.id.xitong);

        this.checkbox_udp_in_tcp = (CheckBox) findViewById(R.id.udp_in_tcp);
        this.button_apps = (Button) findViewById(R.id.apps);
        this.button_save = (Button) findViewById(R.id.save);
        this.button_control = (Button) findViewById(R.id.control);
        this.zhenshu = (Button) findViewById(R.id.zhenshu);
        this.checkbox_udp_in_tcp.setOnClickListener(this::OnClickListener);
        this.checkbox_global.setOnClickListener(this::OnClickListener);
        this.xitong.setOnClickListener(this::OnClickListener);
        this.button_apps.setOnClickListener(this::OnClickListener);
        this.button_save.setOnClickListener(this::OnClickListener);
        this.button_control.setOnClickListener(this::OnClickListener);
        this.zhenshu.setOnClickListener(this::OnClickListener);

        writeFile_sh(this);

        if(this.prefs.getvpn()){
            updateUI();
            Intent prepare = VpnService.prepare(this);
            if (prepare != null) {
                startActivityForResult(prepare, 0);
            } else {
                onActivityResult(0, -1, null);
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            if (resultCode == RESULT_OK ) {
                if(!this.prefs.getvpn()){
                    this.prefs.setvpn(true);
                    OnClickListener(button_control);
                }
            } else {
                this.prefs.setvpn(false);
            }
        }
    }

    public static void showConfirmDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("是否已安装");
        builder.setMessage("magiskv20.4+ 或 kernelsu 或 APatch ?");
        builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 用户点击"是"时的处理逻辑
                String jg=sunny_zs.cacerts(true);
                showAlertDialog2(context, "提示", jg);
            }
        });
        builder.setNegativeButton("否", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 用户点击"否"时的处理逻辑
                String jg=sunny_zs.cacerts(false);
                showAlertDialog2(context, "提示", jg);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public static void showAlertDialog2(Context context, String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title).setMessage(message).setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void OnClickListener(View view) {
        if (view == this.checkbox_global || view == this.xitong) {
            savePrefs();
            updateUI();
        }else if (view == this.zhenshu) {
            boolean isRooted = Root.isAppRooted();
            if (isRooted) {
                showConfirmDialog(this);
               /* String jg=sunny_zs.cacerts();
                showAlertDialog(this, "提示", jg);*/
            } else {
                showAlertDialog(this, "提示", "没有ROOT权限!");
            }
        } else if (view == this.button_apps) {
            startActivity(new Intent(this, AppListActivity.class));
        } else if (view == this.button_save) {
            savePrefs();
            Toast.makeText(this, "保存", Toast.LENGTH_SHORT).show();
        } else if (view == this.button_control) {
            if (!this.prefs.getvpn()) {
                savePrefs();
                updateUI();
                Intent prepare = VpnService.prepare(this);
                if (prepare != null) {
                    startActivityForResult(prepare, 0);
                } else {
                    onActivityResult(0, -1, null);
                }
                return;
            }

            boolean enable = this.prefs.getEnable();
            this.prefs.setEnable(!enable);
            savePrefs();
            updateUI();
            Intent intent = new Intent(this, TProxyService.class);
            if (enable) {
                //停止
                this.startService(intent.setAction(TProxyService.ACTION_DISCONNECT));
            } else {
                //启动
                this.startService(intent.setAction(TProxyService.ACTION_CONNECT));
            }
        }
    }

    public void showAlertDialog(Context context, String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title).setMessage(message).setPositiveButton("确定", new DialogInterface.OnClickListener() { // from class: hev.sockstun.zhenshu.MainActivity_zhenshu.3
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    @SuppressLint("WrongConstant")
    private void updateUI() {

        this.edittext_dns_ipv4.setText("");
        this.edittext_dns_ipv6.setText("");
        this.checkbox_ipv4.setChecked(true);
        this.checkbox_ipv6.setChecked(true);

        this.checkbox_ipv4.setVisibility(8);
        this.checkbox_ipv6.setVisibility(8);
        this.edittext_dns_ipv6.setVisibility(8);
        this.edittext_dns_ipv4.setVisibility(8);
        this.checkbox_udp_in_tcp.setVisibility(8);

        this.edittext_socks_addr.setText(this.prefs.getSocksAddress());
        this.edittext_socks_port.setText(Integer.toString(this.prefs.getSocksPort()));
        this.edittext_socks_user.setText(this.prefs.getSocksUsername());
        this.edittext_socks_pass.setText(this.prefs.getSocksPassword());
        this.checkbox_global.setChecked(this.prefs.getGlobal());
        this.xitong.setChecked(this.prefs.getxitong());

        this.checkbox_udp_in_tcp.setChecked(this.prefs.getUdpInTcp());


        boolean z = true;
        boolean z2 = !this.prefs.getEnable();
        this.edittext_socks_addr.setEnabled(z2);
        this.edittext_socks_port.setEnabled(z2);
        this.edittext_socks_user.setEnabled(z2);
        this.edittext_socks_pass.setEnabled(z2);
        this.edittext_dns_ipv4.setEnabled(z2);
        this.edittext_dns_ipv6.setEnabled(z2);
        this.checkbox_udp_in_tcp.setEnabled(z2);
        this.checkbox_global.setEnabled(z2);
        this.checkbox_ipv4.setEnabled(z2);
        this.checkbox_ipv6.setEnabled(z2);
        Button button = this.button_apps;
        if (!z2 || this.prefs.getGlobal()) {
            z = false;
        }
        button.setEnabled(z);
        this.button_save.setEnabled(z2);
        if (z2) {
            this.button_control.setText(R.string.control_enable);
        } else {
            this.button_control.setText(R.string.control_disable);
        }


    }

    private void savePrefs() {
        this.prefs.setSocksAddress(this.edittext_socks_addr.getText().toString());
        this.prefs.setSocksPort(Integer.parseInt(this.edittext_socks_port.getText().toString()));
        this.prefs.setSocksUsername(this.edittext_socks_user.getText().toString());
        this.prefs.setSocksPassword(this.edittext_socks_pass.getText().toString());
        this.prefs.setDnsIpv4(this.edittext_dns_ipv4.getText().toString());
        this.prefs.setDnsIpv6(this.edittext_dns_ipv6.getText().toString());
        if (!this.checkbox_ipv4.isChecked() && !this.checkbox_ipv4.isChecked()) {
            this.checkbox_ipv4.setChecked(this.prefs.getIpv4());
        }
        this.prefs.setIpv4(this.checkbox_ipv4.isChecked());
        this.prefs.setIpv6(this.checkbox_ipv6.isChecked());
        this.prefs.setGlobal(this.checkbox_global.isChecked());
        this.prefs.setUdpInTcp(this.checkbox_udp_in_tcp.isChecked());
        this.prefs.setxitong(this.xitong.isChecked());

    }


    public void writeFile_sh(Context gcontext) {
        String filepath = "/data/user/0/" + APP_filePath + "/sunny.sh";
        try {
            // 读取资源文件的内容
            InputStream inputStream = gcontext.getResources().openRawResource(R.raw.synny);
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            inputStream.close();

            // 创建文件并写入内容
            File file = new File(filepath);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(buffer);
            fos.close();
            Log.d("星智云转发", "写入本APP路径成功");
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("星智云转发", "写入本APP路径>" + String.valueOf(e));
        }
    }

}
