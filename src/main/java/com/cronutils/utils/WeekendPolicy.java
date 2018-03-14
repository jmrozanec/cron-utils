/*
 * Copyright 2014 jmrozanec
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
package com.cronutils.utils;

import java.time.ZonedDateTime;

public enum WeekendPolicy {
    THURSDAY_FRIDAY, FRIDAY_SATURDAY, SATURDAY_SUNDAY;

    public static int daysToWeekend(WeekendPolicy policy, ZonedDateTime date) {
        int dow = date.getDayOfWeek().getValue();
        switch (policy) {
            case THURSDAY_FRIDAY:
                if(dow < 4) {
                    return 4 - dow;
                }
                if(dow > 5) {
                    return 7 - dow + 4;
                }
                break;
            case FRIDAY_SATURDAY:
                if(dow < 5) {
                    return 6 - dow;
                }
                if(dow > 6) {
                    return 7 - dow + 5;
                }
                break;
            case SATURDAY_SUNDAY:
                if(dow < 6) {
                    return Math.max(0, 6 - dow);
                }
                break;
        }
        return 0;
    }

    public static int daysFromWeekend(WeekendPolicy policy, ZonedDateTime date) {
        int dow = date.getDayOfWeek().getValue();
        switch (policy) {
            case THURSDAY_FRIDAY:
                if(dow<4) {
                    return dow;
                }
                if(dow>5) {
                    return dow - 5;
                }
                break;
            case FRIDAY_SATURDAY:
                if(dow<5) {
                    return dow;
                }
                if(dow>6) {
                    return dow - 6;
                }
                break;
            case SATURDAY_SUNDAY:
                if(dow<6) {
                    return dow;
                }
                break;
        }
        return 0;
    }
}