/** Copyright (c) 2013 Memorial Sloan-Kettering Cancer Center.
 **
 ** This library is free software; you can redistribute it and/or modify it
 ** under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 2.1 of the License, or
 ** any later version.
 **
 ** This library is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 ** documentation provided hereunder is on an "as is" basis, and
 ** Memorial Sloan-Kettering Cancer Center
 ** has no obligations to provide maintenance, support,
 ** updates, enhancements or modifications.  In no event shall
 ** Memorial Sloan-Kettering Cancer Center
 ** be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if
 ** Memorial Sloan-Kettering Cancer Center
 ** has been advised of the possibility of such damage.  See
 ** the GNU Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this library; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 **/
package org.mskcc.cbio.biogene.importer.config.internal;

// imports
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Constructor;

public class ClassLoader
{
	private static Log LOG = LogFactory.getLog(ClassLoader.class);

	public static Object getInstance(String className, Object[] args) throws Exception
	{
		if (className == null || className.length() == 0) {
			throw new IllegalArgumentException("className must not be null");
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug("getInstance(), className: " + className);
		}

		try {
			Class<?> clazz = Class.forName(className);
			Constructor[] constructors = clazz.getConstructors();
			// our classes only have the one constructor
			return constructors[0].newInstance(args);
		}
		catch (Exception e) {
			LOG.error(("Failed to instantiate " + className), e) ;
			throw e;
		}
	}
}
