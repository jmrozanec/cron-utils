package com.cronutils.utils;

import java.util.function.Predicate;

public class Predicates {

    public static <T> Predicate<T> not(Predicate<T> t) {
        return t.negate();
    }
}
