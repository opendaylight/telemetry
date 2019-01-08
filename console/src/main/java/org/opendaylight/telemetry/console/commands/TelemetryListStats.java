/*
 * Copyright © 2017 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.telemetry.console.commands;

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.console.AbstractAction;
import org.opendaylight.telemetry.console.api.TelemetryCommands;


@Command(name = "list", scope = "telemetry",description = "List telemetry stats.")
public class TelemetryListStats extends AbstractAction {
    protected final TelemetryCommands service;
    public TelemetryListStats(final TelemetryCommands service) {
        this.service = service;
    }

    @Option(name = "-e",
            aliases = {"--endpoint"},
            description = "client/server",
            required = true,
            multiValued = false)
    private String endpoint;
    @Option(name = "-h",
            aliases = {"--help"},
            description = "help.",
            required = false,
            multiValued = false)
    private String help;
    @Override
    protected Object doExecute() throws Exception {
        if (help != null && !help.isEmpty()) {
            System.out.println("List telemetry stats.please use list:telemetry -e.");
            return  null;
        }
        String result = service.listStats(endpoint);
        return result;
    }
}
