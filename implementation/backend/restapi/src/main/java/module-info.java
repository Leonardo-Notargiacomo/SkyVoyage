/**
 * Module restapi_module.
 */
module restapi_module {

    requires businesslogic_api_module;
    requires datarecords_module;

    requires io.javalin;
    requires com.fasterxml.jackson.databind;
    requires annotations;
    requires spring.web;
    requires spring.webmvc;
    requires spring.context;

    exports io.github.fontysvenlo.ais.restapi;
}
