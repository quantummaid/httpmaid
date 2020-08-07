package de.quantummaid.httpmaid.tests.givenwhenthen;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

@Slf4j
public class AutoCloseableResolvedParametersSupportExtension implements InvocationInterceptor {
    @java.lang.Override
    public <T> T interceptTestClassConstructor(Invocation<T> invocation, ReflectiveInvocationContext<java.lang.reflect.Constructor<T>> invocationContext, ExtensionContext extensionContext) throws Throwable {
        log.info(String.format("interceptTestClassConstructor()"));
        return invocation.proceed();
    }

    @java.lang.Override
    public void interceptBeforeAllMethod(Invocation<java.lang.Void> invocation, ReflectiveInvocationContext<java.lang.reflect.Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        log.info(String.format("interceptBeforeAllMethod()"));
        invocation.proceed();
    }

    @java.lang.Override
    public void interceptBeforeEachMethod(Invocation<java.lang.Void> invocation, ReflectiveInvocationContext<java.lang.reflect.Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        log.info(String.format("interceptBeforeEachMethod()"));
        invocation.proceed();
    }

    @java.lang.Override
    public void interceptTestMethod(Invocation<java.lang.Void> invocation, ReflectiveInvocationContext<java.lang.reflect.Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        log.info(String.format("interceptTestMethod()"));
        invocation.proceed();
    }

    @java.lang.Override
    public <T> T interceptTestFactoryMethod(Invocation<T> invocation, ReflectiveInvocationContext<java.lang.reflect.Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        log.info(String.format("interceptTestFactoryMethod()"));
        return invocation.proceed();
    }

    @java.lang.Override
    public void interceptTestTemplateMethod(Invocation<java.lang.Void> invocation, ReflectiveInvocationContext<java.lang.reflect.Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        log.info(String.format("interceptTestTemplateMethod()"));
        try {
            invocation.proceed();
        } finally {
           invocationContext.getArguments().stream()
                   .filter(it -> it instanceof AutoCloseable)
                   .forEach(it -> {
                       safelyClose((AutoCloseable) it);
                   });
        }
    }

    private void safelyClose(final AutoCloseable autoCloseable) {
        final String classSimpleName = autoCloseable.getClass().getSimpleName();
        try {
            log.trace("closing AutoCloseable {}...", classSimpleName);
            autoCloseable.close();
            log.debug("closed AutoCloseable {}", classSimpleName);
        } catch (Exception e) {
            log.warn("unable to safely close: {} (cause: {})", classSimpleName, e.toString());
        }
    }

    @java.lang.Override
    public void interceptDynamicTest(Invocation<java.lang.Void> invocation, ExtensionContext extensionContext) throws Throwable {
        log.info(String.format("interceptDynamicTest()"));
        invocation.proceed();
    }

    @java.lang.Override
    public void interceptAfterEachMethod(Invocation<java.lang.Void> invocation, ReflectiveInvocationContext<java.lang.reflect.Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        log.info(String.format("interceptAfterEachMethod()"));
        invocation.proceed();
    }

    @java.lang.Override
    public void interceptAfterAllMethod(Invocation<java.lang.Void> invocation, ReflectiveInvocationContext<java.lang.reflect.Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        log.info(String.format("interceptAfterAllMethod()"));
        invocation.proceed();
    }
}
