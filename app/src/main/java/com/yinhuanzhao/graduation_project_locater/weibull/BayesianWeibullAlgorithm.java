package com.yinhuanzhao.graduation_project_locater.weibull;

import android.net.wifi.ScanResult;

import com.yinhuanzhao.graduation_project_locater.fingerprint.FingerprintRecord;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 基于 Weibull-Bayesian 密度模型的定位算法实现。
 *
 * 主要流程：
 * 1. 从实时扫描数据中构建 BSSID -> RSSI 的映射（同一 AP 选取信号较强的 RSSI）。
 * 2. 对于指纹库中每个 FingerprintRecord（包含各 AP 的 WeibullParameters），
 *    遍历所有共同 AP，利用 WeibullModel.computeProbability() 计算动态 bin（±5 dBm）内的概率，
 *    累加对数概率得到该指纹点的总体似然值。
 * 3. 返回似然值最大的参考点 ID，若没有共同 AP，则返回 -1。
 */
public class BayesianWeibullAlgorithm {
    // 动态 bin 半宽，单位 dBm（论文建议为 5 dBm）
    private static final double BIN_WIDTH = 5.0;
    // 防止概率为零时取对数，加一个极小值
    private static final double EPSILON = 1e-6;

    /**
     * 根据当前扫描结果与指纹库计算定位参考点。
     * @param currentScan         实时 Wi-Fi 扫描结果列表
     * @param fingerprintLibrary  指纹库，每个记录中包含各 AP 的 WeibullParameters
     * @return 最佳匹配的参考点 ID，若无共同 AP 返回 -1
     */
    public int estimatePosition(List<ScanResult> currentScan, List<FingerprintRecord> fingerprintLibrary) {
        if (currentScan == null || currentScan.isEmpty() ||
                fingerprintLibrary == null || fingerprintLibrary.isEmpty()) {
            return -1;
        }

        // 构造当前扫描数据映射：BSSID -> RSSI（选取信号较强的）
        Map<String, Double> currentRssiMap = new HashMap<>();
        for (ScanResult result : currentScan) {
            if (currentRssiMap.containsKey(result.BSSID)) {
                double existing = currentRssiMap.get(result.BSSID);
                if (result.level > existing) {
                    currentRssiMap.put(result.BSSID, (double) result.level);
                }
            } else {
                currentRssiMap.put(result.BSSID, (double) result.level);
            }
        }

        double bestLogLikelihood = Double.NEGATIVE_INFINITY;
        int bestRefPoint = -1;
        boolean anyCommonAP = false;

        // 遍历指纹库中的每个记录
        for (FingerprintRecord record : fingerprintLibrary) {
            double logLikelihood = 0.0;
            boolean recordHasCommon = false;

            for (Map.Entry<String, WeibullParameters> entry : record.fingerprint.entrySet()) {
                String bssid = entry.getKey();
                if (currentRssiMap.containsKey(bssid)) {
                    recordHasCommon = true;
                    anyCommonAP = true;
                    double measuredRssi = currentRssiMap.get(bssid);
                    WeibullParameters params = entry.getValue();
                    // 计算动态 bin 内的概率值
                    double probability = WeibullModel.computeProbability(
                            measuredRssi, BIN_WIDTH,
                            params.getLambda(), params.getK(), params.getTheta());
                    // 累加对数概率（避免直接乘积下溢）
                    logLikelihood += Math.log(probability + EPSILON);
                }
            }
            if (recordHasCommon && logLikelihood > bestLogLikelihood) {
                bestLogLikelihood = logLikelihood;
                bestRefPoint = record.ref_point;
            }
        }

        if (!anyCommonAP) {
            return -1;
        }

        return bestRefPoint;
    }
}
