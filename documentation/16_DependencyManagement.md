# Maven

HttpMaid can provide seriously lightweight HTTP endpoints while at the same
time offering a rich set of features. Since not every user of HttpMaid needs to
use all offered features, we have spread them across multiple maven dependencies in order to
keep the individual dependency footprint small. This document provides guidance to anyone
who needs to integrate HttpMaid dependencies into a new or existing Maven project.

## I just want to try it
All HttpMaid integrations have been bundled into a single maven module so that
initial users don't need to bother choosing the correct dependencies. If you
are new to HttpMaid and just want to experience HttpMaid for the first time,
just include this dependency:
<!---[Dependency](groupId=de.quantummaid.httpmaid.integrations artifactId=httpmaid-all version)-->
```xml
<dependency>
    <groupId>de.quantummaid.httpmaid.integrations</groupId>
    <artifactId>httpmaid-all</artifactId>
    <version>0.9.43</version>
</dependency>
```
This contains anything HttpMaid has to offer, but be aware that it probably adds
way more than you need and should not be used in serious projects.
## Production-quality setup
Any setup beyond a simple 5-minute trial run should follow an approach that only adds
dependencies which are actually used.

### The core module
Every HttpMaid configuration needs to include the core module:
<!---[Dependency](groupId=de.quantummaid.httpmaid artifactId=core version)-->
```xml
<dependency>
    <groupId>de.quantummaid.httpmaid</groupId>
    <artifactId>core</artifactId>
    <version>0.9.43</version>
</dependency>
```
It contains the basic HttpMaid builder and the [PureJavaEndpoint](UserGuide.md#Pure Java).

### Integration modules
Depending on which HttpMaid features you intend to use, you need to load additional
dependencies. All integrations and their respective Maven coordinates can be found
in the `/integrations` project subdirectory.

<!---[Nav]-->
[&larr;](15_Client.md)&nbsp;&nbsp;&nbsp;[Overview](../README.md)&nbsp;&nbsp;&nbsp;[&rarr;](17_Faq.md)

