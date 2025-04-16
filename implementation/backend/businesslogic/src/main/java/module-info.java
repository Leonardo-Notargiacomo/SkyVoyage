/**
 * Module businesslogic_module.
 */
module businesslogic_module {   
    requires datarecords_module;
    requires persistence_api_module;
    requires businesslogic_api_module;
    requires java.logging;
    
    // The correct module name for Jackson Databind
    requires com.fasterxml.jackson.databind;
    // These additional modules might be required for some Jackson functionality
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;
    requires jbcrypt;

    exports io.github.fontysvenlo.ais.businesslogic;
    exports io.github.fontysvenlo.ais.businesslogic.resources;
}
