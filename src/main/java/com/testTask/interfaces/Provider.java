package com.testTask.interfaces;

public interface Provider<T>{

    /**
     *
     * registration of binding by interface class and its implementation
     *
     * @return - get instance
     */
    T getInstance();
}
