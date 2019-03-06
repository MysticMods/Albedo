package com.hrznstudio.albedo.util;

@FunctionalInterface
public interface TriConsumer<A,B,C> {
    void apply(A a, B b, C c);
}