/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.wfs;

import org.opengis.util.CodeList;
import org.opengis.util.SimpleEnumerationType;
import java.util.ArrayList;
import java.util.List;


public class TransactionEventType extends SimpleEnumerationType {
    private static final long serialVersionUID = -4218786755116808448L;
    private static final List VALUES = new ArrayList(5);

    /**
     * Notification of inserted features, after insertion occurred
     * (collection contains newly inserted features)
     */
    public static final TransactionEventType POST_INSERT = new TransactionEventType("PostInsert",
            "Features just inserted");

    /**
     * Notification of updated features, before update occurs
     * (collection contains original values)
     */
    public static final TransactionEventType PRE_UPDATE = new TransactionEventType("PreUpdate",
            "Feature values before update");

    /**
     * Notification of updated features, after update occurs
     * (collection contains updated features)
     */
    public static final TransactionEventType POST_UPDATE = new TransactionEventType("PostUpdate",
            "Feature values after update");

    /**
     * Notification of deleted features, before deletion occurs
     * (collection contains features that will be deleted)
     */
    public static final TransactionEventType PRE_DELETE = new TransactionEventType("PostDelete",
            "Features just deleted");

    protected TransactionEventType(String name, String description) {
        super(VALUES, name, description);
    }

    public CodeList[] family() {
        synchronized (VALUES) {
            return (TransactionEventType[]) VALUES.toArray(new TransactionEventType[VALUES.size()]);
        }
    }
}
