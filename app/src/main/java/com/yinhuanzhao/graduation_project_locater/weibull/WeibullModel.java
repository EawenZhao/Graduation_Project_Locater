package com.yinhuanzhao.graduation_project_locater.weibull;

/**
 * Weibull 模型工具类，提供 Weibull 累积分布函数 (CDF) 及计算动态 bin 内概率的方法。
 */
public class WeibullModel {

    /**
     * 计算 Weibull 累积分布函数 F(x)。
     * 当 x 小于位移参数 theta 时，返回 0。
     *
     * @param x      自变量（例如 RSSI 值）
     * @param lambda 尺度参数
     * @param k      形状参数
     * @param theta  位移参数
     * @return Weibull CDF 值
     */
    public static double weibullCDF(double x, double lambda, double k, double theta) {
        if (x < theta) {
            return 0.0;
        }
        double exponent = Math.pow((x - theta) / lambda, k);
        return 1 - Math.exp(-exponent);
    }

    /**
     * 计算动态 bin 内的概率值，动态 bin 定义为 [x - B, x + B]。
     *
     * @param x      实际测量的 RSSI 值
     * @param B      动态 bin 半宽（dBm）
     * @param lambda Weibull 尺度参数
     * @param k      Weibull 形状参数
     * @param theta  Weibull 位移参数
     * @return 区间内的概率值：F(x+B) - F(x-B)
     */
    public static double computeProbability(double x, double B, double lambda, double k, double theta) {
        double cdfUpper = weibullCDF(x + B, lambda, k, theta);
        double cdfLower = weibullCDF(x - B, lambda, k, theta);
        return cdfUpper - cdfLower;
    }
}
