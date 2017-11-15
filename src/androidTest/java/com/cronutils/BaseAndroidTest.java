package com.cronutils;

import android.app.Application;
import android.support.test.InstrumentationRegistry;
import org.junit.Before;

import com.jakewharton.threetenabp.AndroidThreeTen;

public class BaseAndroidTest {
    protected final Application application =
            (Application) InstrumentationRegistry.getTargetContext().getApplicationContext();

    @Before
    public void setUp() throws Exception {
        AndroidThreeTen.init(application);
    }
}
