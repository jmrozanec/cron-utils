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

package com.cronutils.model.field;

/**
 * Enumerates cron field names.
 */
public enum CronFieldName {
    SECOND(0), MINUTE(1), HOUR(2), DAY_OF_MONTH(3), MONTH(4), DAY_OF_WEEK(5), YEAR(6), DAY_OF_YEAR(7);
    /**
     * DAY_OF_YEAR is similar to DAY_OF_MONTH whose main purpose is the definition of
     * effective bi-, tri- or quad-weekly schedules via proprietary cron expressions.
     */

    private int order;

    /**
     * Constructor.
     *
     * @param order - specified order between cron fields.
     *              Used to be able to compare fields and sort them
     */
    CronFieldName(final int order) {
        this.order = order;
    }

    /**
     * Returns the order number that corresponds to the field.
     *
     * @return order number - int
     */
    public int getOrder() {
        return order;
    }
}
