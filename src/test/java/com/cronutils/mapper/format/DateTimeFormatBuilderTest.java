package com.cronutils.mapper.format;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.junit.Test;

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
public class DateTimeFormatBuilderTest {

    @Test
    public void testCreatePatternFor() throws Exception {
        assertForPattern("MMMM d, YYYY", "June 9, 2011");
        assertForPattern("MMM d, YYYY", "Jun 9, 2011");
        assertForPattern("MMM dd", "Jun 09");
        assertForPattern("EEEE, MMMM d, YYYY", "Thursday, June 9, 2011");
        assertForPattern("EEE MMM d", "Thu Jun 9");
        assertForPattern("MM/dd/YY", "06/09/11");
        assertForPattern("hh:mm:ss a", "01:00:00 AM");
        assertForPattern("HH:mm", "20:52");
        assertForPattern("HH:mm:ss", "01:00:00");
        assertForPattern("HH:mm Z", "01:00 America/Los_Angeles");
    }

    private void assertForPattern(String dateTimePattern, String readablePattern){
        DateTime now = DateTime.now();
        assertEquals(
                DateTimeFormat.forPattern(dateTimePattern).print(now),
                new DateTimeFormatBuilder().createPatternFor(readablePattern).print(DateTime.now())
        );
    }
}