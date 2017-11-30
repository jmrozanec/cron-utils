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

package com.cronutils.model.field.expression;

/**
 * Represents a question mark (?) value on cron expression field.
 */
public final class QuestionMark extends FieldExpression {

    private static final long serialVersionUID = -3043597498019873616L;
    static final QuestionMark INSTANCE = new QuestionMark();

    /**
     * Should be package private and not be instantiated elsewhere. Class should become package private too.
     */
    private QuestionMark() {
    }

    @Override
    public String asString() {
        return "?";
    }

    @Override
    public String toString() {
        return "QuestionMark{}";
    }
}
