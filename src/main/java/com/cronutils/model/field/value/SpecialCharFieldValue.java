package com.cronutils.model.field.value;

import com.cronutils.utils.Preconditions;

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
public class SpecialCharFieldValue extends FieldValue<SpecialChar> {
    private SpecialChar specialChar = SpecialChar.NONE;

    public SpecialCharFieldValue(SpecialChar specialChar) {
        Preconditions.checkNotNull(specialChar, "special char must not be null");
        this.specialChar = specialChar;
    }

    @Override
    public SpecialChar getValue() {
        return specialChar;
    }
}

