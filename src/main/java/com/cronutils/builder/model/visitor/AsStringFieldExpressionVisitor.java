package com.cronutils.builder.model.visitor;


import com.cronutils.builder.model.*;
import com.cronutils.model.field.constraint.FieldConstraints;
import com.cronutils.model.field.value.FieldValue;
import com.cronutils.model.field.value.IntegerFieldValue;
import com.google.common.base.Function;

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
/**
 * Represents FieldExpression values as string.
 * Returns same FieldExpression instance, since no change is performed on it.
 */
public class AsStringFieldExpressionVisitor //implements FieldExpressionVisitor
{
    //TODO make return type generic, to adapt to FieldExpression or string or what required.
}