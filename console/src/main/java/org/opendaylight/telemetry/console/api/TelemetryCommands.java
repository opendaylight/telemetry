/*
 * Copyright © 2017 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.telemetry.console.api;

import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.datastorage.rev180326.telemetry.data.model.TelemetryData;

import java.util.List;

public interface TelemetryCommands {
    String listStats(String endpoint);

    boolean switchSender(boolean debug);

    List<TelemetryData> getTelemetryData();
}
