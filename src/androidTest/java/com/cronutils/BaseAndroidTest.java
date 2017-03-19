package com.cronutils;


import android.app.Application;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.jakewharton.threetenabp.AndroidThreeTen;

import org.junit.Before;
import org.junit.runner.RunWith;

public class BaseAndroidTest {
    protected final Application application =
            (Application) InstrumentationRegistry.getTargetContext().getApplicationContext();

    @Before
    public void setUp() throws Exception {
        AndroidThreeTen.init(application);
    }
}
