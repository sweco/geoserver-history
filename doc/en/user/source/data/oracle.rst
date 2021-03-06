.. _data_oracle:

Oracle
======

.. note:: GeoServer does not come built-in with support for Oracle; it must be installed through an extension.  Proceed to :ref:`oracle_install` for installation details.

`Oracle Spatial and Locator <http://www.oracle.com/technology/products/spatial/index.html>`_ are the spatial extensions of Oracle.

.. _oracle_install:

Installing the Oracle extension
-------------------------------

#. Download the Oracle extension from the `GeoServer download page <http://geoserver.org/display/GEOS/Download>`_.

   .. warning:: Make sure to match the version of the extension to the version of the GeoServer instance!

#. Extract the contents of the archive into the ``WEB-INF/lib`` directory of he GeoServer installation.

Adding an Oracle datastore
--------------------------

Once the extension is properly installed :guilabel:`Oracle` will be an option in the :guilabel:`Vector Data Sources` list when creating a new data store.

.. figure:: images/oraclecreate.png
   :align: center

   *Oracle in the list of data sources*

Configuring an Oracle datastore
-------------------------------

.. figure:: images/oracleconfigure.png
   :align: center

   *Configuring an Oracle datastore*

.. list-table::
   :widths: 20 80

   * - **Option**
     - **Description**
   * - ``host``
     - The oracle server host name or IP address.
   * - ``port``
     - The port on which the Oracle server is accepting connections - often this is port 1521.
   * - ``database``
     - The name of the database to connect to.
   * - ``schema``
     - The database schema to access tables from. Setting this value greatly increases the speed at which the data store displays its publishable tables and views, so it is advisable to set this.
   * - ``user``
     - The name of the user to use when connecting to the oracle database.
   * - ``password``
     - The password to use when connecting to the database.  Leave blank for no password.
   * - ``max connections``
       ``min connections``
       ``fetch size``
       ``connection timeout``
       ``validate connections``
     - Connection pool configuration parameters. See the :ref:`connection_pooling` section for details.
   * - ``Loose bbox``
     - 	Controls how bounding box comparisons are made against geometries in the database. See the :ref:`oracle_loose_bbox` section below.

.. _oracle_loose_bbox:

Using loose bounding box
````````````````````````

When the ``loose bbox`` option is set, only the bounding box of a geometry is used.  This results in a significant performance gain. The downside is that some geometries may be considered inside of a bounding box when they are technically not.

If the primary use of the database is through :ref:`WMS` this flag can be set safely since a loss of some accuracy is usually acceptable. However if using :ref:`WFS` and making use of BBOX filtering capabilities, this flag should not be set.

Configuring an Oracle database with JNDI
----------------------------------------

See :ref:`tomcat_jndi` for a step by step guide on setting up an Oracle JDNI connection.