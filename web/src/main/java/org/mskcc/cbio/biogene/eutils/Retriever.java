// $Id: Retriever.java,v 1.3 2012/02/24 20:12:27 grossb Exp $
//------------------------------------------------------------------------------
/** Copyright (c) 2009 Memorial Sloan-Kettering Cancer Center.
 **
 ** Code written by: Ethan Cerami, Benjamin Gross
 ** Authors: Ethan Cerami, Gary Bader, Benjamin Gross, Chris Sander
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
package org.mskcc.cbio.biogene.eutils;

// imports
import org.apache.log4j.Logger;

import java.net.URL;
import java.net.URLConnection;

import java.io.InputStream;
import java.io.ByteArrayOutputStream;

public class Retriever {
	
    private static Logger log = Logger.getLogger(Retriever.class);

    /**
     * Connects to eftech service.
     *
	 * @param url String
     * @return String (xml document)
     * @throws Exception
     */
    public static String connect(String urlStr) throws Exception {

		URL url = new URL(urlStr);
		URLConnection conn = url.openConnection();
		
		InputStream in = conn.getInputStream();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int len;
		byte[] buffer = new byte[4096];
		while ((len = in.read(buffer)) > 0) {
			out.write(buffer, 0, len);
		}
		in.close();

		if (log.isDebugEnabled()) {
			log.debug("Retriever.connect(), received " + out.size() + " bytes.");
		}

		// outta here
		return (out.size() > 0) ? new String(out.toByteArray()) : null;
	}
}