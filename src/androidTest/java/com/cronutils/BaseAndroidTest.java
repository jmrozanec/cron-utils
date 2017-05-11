package com.cronutils;


import android.app.Application;
import android.support.test.InstrumentationRegistry;
import com.jakewharton.threetenabp.AndroidThreeTen;
import org.junit.Before;

public class BaseAndroidTest {
    protected final Application application =
            (Application) InstrumentationRegistry.getTargetContext().getApplicationContext();

    @Before
    public void setUp() throws Exception {
        AndroidThreeTen.init(application);
    }
}
