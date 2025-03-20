package com.yinhuanzhao.graduation_project_locater;

import android.net.wifi.ScanResult;

import com.yinhuanzhao.graduation_project_locater.util.DataProcessor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WKNNAlgorithm {
    private int k;

    public WKNNAlgorithm(int k) {
        this.k = k;
    }

    /**
     * 根据当前扫描结果与指纹库（均为归一化后的数据）计算欧式距离，返回距离最小的参考点
     */
    public int estimatePosition(List<ScanResult> currentScan, List<FingerprintRecord> fingerprintLibrary) {
        // 先将当前扫描结果转换成 BSSID -> RSSI 映射（未归一化）
        HashMap<String, Double> currentRssiMap = new HashMap<>();
        for (ScanResult result : currentScan) {
            currentRssiMap.put(result.BSSID, (double) result.level);
        }
        // 使用你提供的归一化方法对当前扫描结果进行归一化
        HashMap<String, Double> normalizedCurrent = DataProcessor.minMaxNormalize(currentRssiMap);

        double minDistance = Double.MAX_VALUE;
        int bestRefPoint = -1;

        // 遍历指纹库中的每个记录，计算归一化后的欧式距离
        for (FingerprintRecord record : fingerprintLibrary) {
            double distance = calculateDistance(normalizedCurrent, record.fingerprint);
            if (distance < minDistance) {
                minDistance = distance;
                bestRefPoint = record.ref_point;
            }
        }
        return bestRefPoint;
    }

    /**
     * 计算当前扫描数据与指纹向量之间的欧式距离
     * 仅考虑两个集合中都存在的 MAC 地址；如果没有共同项，则返回一个很大的数
     */
    private double calculateDistance(HashMap<String, Double> current, Map<String, Double> fingerprint) {
        double sum = 0;
        int count = 0;
        for (Map.Entry<String, Double> entry : fingerprint.entrySet()) {
            String mac = entry.getKey();
            if (current.containsKey(mac)) {
                double diff = current.get(mac) - entry.getValue();
                sum += diff * diff;
                count++;
            }
        }
        if (count == 0) {
            return Double.MAX_VALUE;
        }
        return Math.sqrt(sum);
    }
}
