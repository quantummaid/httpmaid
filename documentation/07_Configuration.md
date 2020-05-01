# Configuring HttpMaid

HttpMaid is a flexible web framework. In order to
customize it to your needs, you need a way to configure it.
The HttpMaid builder offers a `.configured()` method for this.
It takes a `Configurator` object as an argument. All HttpMaid
integrations described throughout this guide will offer convenient
static methods that create these configurators for you
(we will call them *configurator methods* in the following chapters).
