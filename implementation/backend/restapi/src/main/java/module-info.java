/**
 * Module restapi_module.
 */
module restapi_module {

    requires io.javalin;
    requires com.fasterxml.jackson.databind;
    requires businesslogic_module;
    requires businesslogic_api_module;
    requires datarecords_module;
    requires java.logging;
    requires java.net.http;
    requires spring.web;

    exports io.github.fontysvenlo.ais.restapi;

    opens io.github.fontysvenlo.ais.restapi to org.junit.platform.commons;
}