/**
 * Module persistence_module.
 */
module persistence_module {
    requires persistence_api_module;
    
    requires java.logging;
    requires java.sql;
    requires org.postgresql.jdbc;
    requires java.naming;
    requires businesslogic_api_module;
    requires datarecords_module;

    exports io.github.fontysvenlo.ais.persistence;
}
