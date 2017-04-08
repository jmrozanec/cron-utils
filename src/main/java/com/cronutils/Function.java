package com.cronutils;

/**
 * Created by kiran on 19/3/17.
 */

public interface Function<T, R> {
    R apply(T t);
}
