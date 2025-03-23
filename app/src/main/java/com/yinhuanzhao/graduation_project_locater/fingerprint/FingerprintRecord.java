package com.yinhuanzhao.graduation_project_locater.fingerprint;

import com.yinhuanzhao.graduation_project_locater.weibull.WeibullParameters;

import java.util.Map;

public class FingerprintRecord {
    public int ref_point;
    public Map<String, WeibullParameters> fingerprint;

    @Override
    public String toString() {
        return "FingerprintRecord{" +
                "ref_point=" + ref_point +
                ", fingerprint=" + fingerprint +
                '}';
    }
}