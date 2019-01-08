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
import org.apache.karaf.shell.console.AbstractAction;
import org.opendaylight.telemetry.console.api.TelemetryCommands;

/**
 * @author: li.jiansong
 **/
@Command(scope = "telemetry", name = "test", description = "set data simulator switch.")

public class TelemetrySimulatorCommand extends AbstractAction {
    protected final TelemetryCommands service;

    public TelemetrySimulatorCommand(TelemetryCommands service) {
        this.service = service;
    }

    @Option(name = "-t",
            aliases = {"--test"},
            description = "set data simulator switch.",
            required = true,
            multiValued = false)
    private boolean test = false;
    @Option(name = "-h",
            aliases = {"--help"},
            description = "help.",
            required = false,
            multiValued = false)
    private String help;
    @Override
    protected Object doExecute() throws Exception {
        if (help != null && !help.isEmpty()) {
            System.out.println("set data simulator switch..please use telemetry:test -t.");
            return  null;
        }
        System.out.println("execute simulator test.");
        return service.switchSender(test);
    }
}
