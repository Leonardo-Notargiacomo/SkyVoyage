package io.github.fontysvenlo.ais.restapi;

import io.github.fontysvenlo.ais.businesslogic.api.EmployeeManager;
import io.github.fontysvenlo.ais.datarecords.EmployeeData;
import io.javalin.apibuilder.CrudHandler;
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;

public class EmployeeResource implements CrudHandler {
    private final EmployeeManager employeeManager;
    EmployeeResource(EmployeeManager employeeManager) {
        this.employeeManager = employeeManager;
    }

    @Override
    public void create(@NotNull Context context) {
        EmployeeData employeeData = context.bodyAsClass(EmployeeData.class);
        if (employeeData == null) {
            context.status(400);
            return;
        }
    }

    @Override
    public void getAll(@NotNull Context context) {
        context.status(200);
        context.json(employeeManager.getAll());
    }

    @Override
    public void getOne(@NotNull Context context, @NotNull String s) {
        String username = context.queryParam("username");
        String password = context.queryParam("password");

        if (username == null || password == null) {
            context.status(400);
            return;
        }
        context.json(employeeManager.login(username, password));
        context.status(200);
    }

    @Override
    public void update(@NotNull Context context, @NotNull String s) {
        throw new UnsupportedOperationException("Not implemented yet");

    }

    @Override
    public void delete(@NotNull Context context, @NotNull String s) {
        throw new UnsupportedOperationException("Not implemented yet");

    }
}
