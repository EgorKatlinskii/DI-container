package com.testTask.interfaces;

public interface Injector {

    /**
     *
     * getting an instance of a class with all injections by the interface class
     *
     * @param type - class object reference
     * @return - class instance
     */
    <T> Provider<T> getProvider(Class<T> type);


    /**
     *
     * registration of binding by interface class and its implementation
     *
     * @param intf - class object reference
     * @param impl - class object reference
     * @return - class instance
     */
    <T> void bind(Class<T> intf, Class<? extends T> impl);


    /**
     *
     * singleton class registration
     *
     * @param intf - class object reference
     * @param impl - class object reference
     * @return - class instance
     */
    <T> void bindSingleton(Class<T> intf, Class<? extends T> impl);

}
