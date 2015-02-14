# cas35-addon-rest
Modular SpringMVC @Controller based implementation of CAS REST endpoints for CAS 3.5.x series

> This project was developed as part of Unicon's [Open Source Support program](https://unicon.net/opensource).
Professional Support / Integration Assistance for this module is available. For more information [visit](https://unicon.net/opensource/cas).

## Motivation

Current Restlet-based REST module for CAS 3.5.x series has completely broken some overlay builds with runtime dependencies conflicts hell in Restlet and other Spring modules like `cglib`, and `asm`. Rather then "fight" these to squeeze this heavyweight Restlet library into existing overlays, there is a better, lighter-weight implementetion based on SpringMVC `@Controller` programming model, which supports a semi-auto configuration (no need to define any Spring beans, just include the jar on the apps's classpath).

This is the implementation borrow from the REST module in CAS which will be available in CAS `4.1`. By having a seprate micro addon with the same code, CAS `3.5` based deploymnent could enjoy the same enhaced REST implementation that CAS `4.1` builds would enjoy.

### Current version
`1.0.0-RC1`

### Supported CAS version
The minimum supported CAS server version is `3.5.1`

## Usage

TODO...

