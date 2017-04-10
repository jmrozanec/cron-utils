package com.cronutils.model.field.expression;

import android.support.test.runner.AndroidJUnit4;

import com.cronutils.BaseAndroidTest;
import com.cronutils.model.field.value.IntegerFieldValue;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
/*
 * Copyright 2015 jmrozanec
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@RunWith(AndroidJUnit4.class)
public class EveryTest {
    @Test
    public void testGetTime() throws Exception {
        int every = 5;
        assertEquals(every, (int)new Every(new IntegerFieldValue(every)).getPeriod().getValue());
    }

    @Test
    public void testGetTimeNull() throws Exception {
        assertEquals(1, (int)new Every(null).getPeriod().getValue());
    }
}