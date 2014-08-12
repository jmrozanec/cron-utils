package com.cron.utils;
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
public enum CronFieldName {
    SECOND(0), MINUTE(1), HOUR(2), DAY_OF_MONTH(3), MONTH(4), DAY_OF_WEEK(5), YEAR(6);

    private int order;

    private CronFieldName(int order) {
        this.order = order;
    }

    public int getOrder() {
        return order;
    }
}
