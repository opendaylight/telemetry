.. _telemetry-user-guide:

Telemetry User Guide
====================

Overview
--------

Traditional way to get running information from network device, such as snmp or netflow,
is becoming insufficient in sdn network. Because sdn application needs better performance
and more flexible data format. It‘s necessary to implement a telemetry channel to support
new app in sdn( (e.g. traffic optimization).


Telemetry User-Facing Features
------------------------------
-  **odl-telemetry-all**

   -  This feature contains all other features/bundles of Telemetry project. If you
      install it, it provides all functions that the Telemetry project can support.

-  **odl-telemetry-collect**

   -  This feature provides a function which implements a gRPC server to receive the
      measurement data of device and show them to user.

-  **odl-telemetry-configurator**

   -  This feature mainly provides function about telemetry model data configuration.


How To Start
-------------

Preparing for Installation
~~~~~~~~~~~~~~~~~~~~~~~~~~

1. Forwarding devices must support NETCONF, so that OpenDaylight can connect to them
   and config resoures via NETCONF.

2. Forwarding devices must support gRpc, so that OpenDaylight can complete packet
   in/out procedure, etc.



Installation Feature
~~~~~~~~~~~~~~~~~~~~

Run OpenDaylight and install Telemetry Service *odl-telemetry-all* as shown below:

   feature:install odl-telemetry-all

For a more detailed overview of the Telemetry, see the :ref:`telemetry-dev-guide`.
