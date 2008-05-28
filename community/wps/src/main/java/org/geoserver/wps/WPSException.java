/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

/**
    @author lreed@refractions.net
*/

package org.geoserver.wps;

import org.geoserver.platform.ServiceException;

public class WPSException extends ServiceException
{
    public WPSException(String message)
    {
        super(message);
    }

    public WPSException(String message, String code)
    {
        super(message, code);
    }

    public WPSException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
