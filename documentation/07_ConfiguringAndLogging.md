# Configuring HttpMaid and Logging

HttpMaid is a flexible web framework. In order to
customize it to your needs, you need a way to configure it.
The HttpMaid builder offers a `.configured()` method for this.
It takes a `Configurator` object as an argument. All HttpMaid
integrations described throughout this guide will offer convenient
static methods that create these configurators for you
(we will call them *configurator methods* in the following chapters).
As an example, if you want to configure how HttpMaid will log,
you can use the configurator methods provided in the `LoggerConfigurators` class:

- `toLogUsing()` - logs using the passed implementation of the `Logger` interface

- `toLogToStdout()` - logs everything to `STDOUT`

- `toLogToStderr()` - logs everything to `STDERR`

- `toLogToStdoutAndStderr()` - logs messages to `STDOUT` and exceptions to `STDERR`

- `toDropAllLogMessages()` - does not log at all

The default setting is to log to both `STDOUT` and `STDERR` - if you want to
change that to log only to `STDOUT`, the configuration would look like this:
<!---[CodeSnippet] (logging)-->
```java
final HttpMaid httpMaid = anHttpMaid()
        .configured(toLogToStdout())
        .build();
```

<!---[Nav]-->
[&larr;](06_ServingFiles.md)&nbsp;&nbsp;&nbsp;[Overview](../README.md)&nbsp;&nbsp;&nbsp;[&rarr;](08_Exceptions.md)

