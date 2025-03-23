package com.yinhuanzhao.graduation_project_locater.fingerprint;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class FingerprintManager {
    private Context context;
    private List<FingerprintRecord> fingerprintLibrary;

    public FingerprintManager(Context context) {
        this.context = context;
        loadFingerprintLibrary();
    }

    private void loadFingerprintLibrary() {
        try {
            InputStream is = context.getAssets().open("fingerprint_library.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);

            Gson gson = new Gson();
            Type listType = new TypeToken<List<FingerprintRecord>>() {}.getType();
            fingerprintLibrary = gson.fromJson(json, listType);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<FingerprintRecord> getFingerprintLibrary() {
        return fingerprintLibrary;
    }
}
