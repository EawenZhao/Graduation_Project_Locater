package com.yinhuanzhao.graduation_project_locater;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.yinhuanzhao.graduation_project_locater.fingerprint.FingerprintManager;
import com.yinhuanzhao.graduation_project_locater.weibull.BayesianWeibullAlgorithm;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private WifiManager wifiManager;
    private BroadcastReceiver wifiScanReceiver;
    private TextView txtPosition;
    private FingerprintManager fingerprintManager;
    private BayesianWeibullAlgorithm bayesianWeibullAlgorithm;
    private final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // UI 绑定
        txtPosition = findViewById(R.id.txtPosition);
        Button btnScan = findViewById(R.id.btnScan);

        // 初始化 Wi-Fi 服务
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null) {
            Toast.makeText(this, "Wi-Fi 服务不可用", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(this, "Wi-Fi 未开启，正在开启...", Toast.LENGTH_SHORT).show();
            wifiManager.setWifiEnabled(true);
        }

        // 检查定位权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }

        // 初始化指纹库管理器和定位算法
        fingerprintManager = new FingerprintManager(this);
        bayesianWeibullAlgorithm = new BayesianWeibullAlgorithm();

        // 创建广播接收器，接收扫描结果后调用定位算法
        wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
                if (success) {
                    // 再次检查定位权限
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    List<ScanResult> results = wifiManager.getScanResults();
                    // 使用 BayesianWeibullAlgorithm 估计位置（参考点）
                    int estimatedRefPoint = bayesianWeibullAlgorithm.estimatePosition(
                            results, fingerprintManager.getFingerprintLibrary());
                    txtPosition.setText("估计位置（参考点）: " + estimatedRefPoint);
                } else {
                    Toast.makeText(MainActivity.this, "Wi-Fi 扫描失败", Toast.LENGTH_SHORT).show();
                }
            }
        };

        // 注册广播接收器
        IntentFilter filter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(wifiScanReceiver, filter);

        // 按钮点击事件：启动 Wi-Fi 扫描
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean scanStarted = wifiManager.startScan();
                if (!scanStarted) {
                    Toast.makeText(MainActivity.this, "发起扫描失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(wifiScanReceiver);
    }
}
