package com.testTask.service;

import com.testTask.exception.BindingNotFoundException;
import com.testTask.exception.ConstructorNotFoundException;
import com.testTask.exception.TooManyConstructorsException;
import com.testTask.interfaces.Injector;
import com.testTask.interfaces.Provider;
import com.testTask.interfaces.annotation.Inject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class InjectorImpl implements Injector {
    private final Map<Class<?>, Class<?>> bindingMap = new HashMap<>();
    private final Map<Class<?>, Object> singletonInstances = new HashMap<>();

    @Override
    public <T> Provider<T> getProvider(Class<T> type) {
        synchronized (this) {
            Object classInstance = null;
            try {
                Constructor<?> constructor;
                Class<?> impl = bindingMap.get(type);
                if (impl == null) {
                    return null;
                }

                int countInjectedConstructors = getCountInjectedConstructors(impl);

                if (countInjectedConstructors == 0) {
                    constructor = impl.getConstructor();
                    classInstance = constructor.newInstance();
                } else if (countInjectedConstructors == 1) {
                    constructor = Stream.of(impl.getConstructors())
                            .filter((construct) -> construct.isAnnotationPresent(Inject.class))
                            .findFirst()
                            .get();
                    Class<?>[] parametersTypes = constructor.getParameterTypes();
                    List<Object> listOfInjecting = new LinkedList<>();
                    for (Class<?> classType : parametersTypes) {
                        if (!bindingMap.containsKey(classType)) {
                            throw new BindingNotFoundException("Binding not found");
                        }
                        listOfInjecting.add(
                                singletonInstances.containsKey(classType) ? singletonInstances.get(classType)
                                        : this.getProvider(classType).getInstance()
                        );
                    }
                    classInstance = constructor.newInstance(listOfInjecting.toArray());
                } else {
                    throw new TooManyConstructorsException("There are many constructors with @Inject annotation");
                }

            } catch (NoSuchMethodException | IllegalAccessException |
                    InstantiationException | InvocationTargetException |
                    BindingNotFoundException | TooManyConstructorsException e) {
                System.out.println(e.getMessage());
            }
            return (Provider<T>) classInstance;
        }

    }

    private int getCountInjectedConstructors(Class<?> impl) {
        return (int) Stream.of(impl.getConstructors())
                .filter((construct) -> construct.isAnnotationPresent(Inject.class))
                .count();
    }


    @Override
    public <T> void bind(Class<T> intf, Class<? extends T> impl) {
        try {
            int countInjectedConstructors = getCountInjectedConstructors(impl);
            if (countInjectedConstructors == 0) {
                boolean defaultConstructor = Stream.of(impl.getConstructors())
                        .anyMatch((construct) -> construct.getParameterCount() == 0);
                if (!defaultConstructor) {
                    throw new ConstructorNotFoundException("no default constructor");
                }
            } else if (countInjectedConstructors > 1) {
                throw new TooManyConstructorsException("There are many constructors with @Inject annotation");
            }
            bindingMap.put(intf, impl);
        } catch (TooManyConstructorsException | ConstructorNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }


    @Override
    public <T> void bindSingleton(Class<T> intf, Class<? extends T> impl) {
        bind(intf, impl);
        Object singletonInstance = getProvider(intf).getInstance();
        singletonInstances.put(intf, singletonInstance);
    }
}
