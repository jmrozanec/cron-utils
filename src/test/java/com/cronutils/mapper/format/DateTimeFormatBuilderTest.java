package com.cronutils.mapper.format;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.junit.Test;

import static org.junit.Assert.*;

public class DateTimeFormatBuilderTest {

    @Test
    public void testCreatePatternFor() throws Exception {
        assertForPattern("MMMM dd, YYYY", "June 9, 2011");
        assertForPattern("MMM dd, YYYY", "Jun 9, 2011");
        assertForPattern("MMM dd", "Jun 09");
        assertForPattern("EEEE, MMMM dd, YYYY", "Thursday, June 9, 2011");
        assertForPattern("EEE MMM dd", "Thu Jun 9");
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