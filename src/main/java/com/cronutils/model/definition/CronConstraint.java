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

package com.cronutils.model.definition;

import java.io.Serializable;

import com.cronutils.model.Cron;

public abstract class CronConstraint implements Serializable {

    private static final long serialVersionUID = 6866660085991775528L;
    private final String description;

    public CronConstraint(final String description) {
        this.description = description;
    }

    public abstract boolean validate(Cron cron);

    public String getDescription() {
        return description;
    }
}
