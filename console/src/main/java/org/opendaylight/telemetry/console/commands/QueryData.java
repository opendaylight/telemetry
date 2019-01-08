/*
 * Copyright © 2017 CTCC.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.telemetry.console.commands;

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.opendaylight.telemetry.console.api.TelemetryCommands;

import org.apache.karaf.shell.console.AbstractAction;

/**
 * @author: li.jiansong
 **/
@Command(name = "telemetry", scope = "query", description = "query telemetry data.")
public class QueryData extends AbstractAction {
    protected final TelemetryCommands service;

    public QueryData(TelemetryCommands service) {
        this.service = service;
    }
    @Option(name = "-all",
            aliases = {"--all"},
            description = "query all data of telemetry collector.",
            required = true,
            multiValued = false)
    private String all;
    @Option(name = "-h",
            aliases = {"--help"},
            description = "help.",
            required = false,
            multiValued = false)
    private String help;
    @Override
    protected Object doExecute() throws Exception {
        if (help != null && !help.isEmpty()) {
            System.out.println("query telemetry data.please use telemetry:query -all.");
            return  null;
        }
        System.out.println("execute action of querying data.");
        service.getTelemetryData();
        return null;
    }
}
