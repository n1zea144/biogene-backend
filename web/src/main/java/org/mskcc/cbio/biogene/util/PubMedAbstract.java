// $Id
//------------------------------------------------------------------------------
/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 **
 ** Code written by: Benjamin Gross
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
package org.mskcc.cbio.biogene.util;

// imports
import flexjson.*;
import org.apache.log4j.Logger;

import java.net.URL;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;


/**
 * Fetches PubMed Abstracts from the NCBI.
 *
 * @author Benjamin Gross
 */
public class PubMedAbstract {

	// some statics
    private static Logger log = Logger.getLogger(PubMedAbstract.class);
	//private static String URL = "http://www.ncbi.nlm.nih.gov/m/pubmed/";
	private static String URL = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&retmode=xml&id=";

	/**
	 * Given a PubMed Abstract ID, fetches the abstract, returns XML.
	 *
	 * @param httpServletResponse HttpServletResponse
	 * @param pubMedAbstractID String
	 */
	public static void fetchXML(HttpServletResponse httpServletResponse, String pubMedAbstractID) {

		if (log.isDebugEnabled()) {
			log.debug("PubMedAbstract.fetchXML(), URL: " + URL + pubMedAbstractID);
		}

		PrintWriter writer = null;
		try {
			httpServletResponse.setContentType("text/xml; charset=UTF-8");
			writer = httpServletResponse.getWriter();

			String URLToAbstract = URL + pubMedAbstractID;
			URL pubMedAbstractURL = new URL(URLToAbstract);
			BufferedReader in = new BufferedReader(new InputStreamReader(pubMedAbstractURL.openStream(), "UTF-8"));
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				writer.print(inputLine);
			}
			in.close();
		}
		catch (Exception e) {
			if (writer != null) {
				writer.print("Error: internal error: " + e.getMessage() + "\n");
			}
		}

		if (writer != null) {
			writer.flush();
			writer.close();
		}
	}

	/**
	 * Given a PubMed Abstract ID, fetches the abstract, returns JSON.
	 *
	 * @param httpServletResponse HttpServletResponse
	 * @param pubMedAbstractID String
	 */
	public static void fetchJSON(HttpServletResponse httpServletResponse, String pubMedAbstractID) {

		if (log.isDebugEnabled()) {
			log.debug("PubMedAbstract.fetchJSON(), URL: " + URL + pubMedAbstractID);
		}

		PrintWriter writer = null;
		try {
			httpServletResponse.setContentType("application/json; charset=utf-8");
			writer = httpServletResponse.getWriter();

			String URLToAbstract = URL + pubMedAbstractID;
			URL pubMedAbstractURL = new URL(URLToAbstract);

			// read contents into a string
			String inputLine;
			StringBuilder builder = new StringBuilder();
			BufferedReader in = new BufferedReader(new InputStreamReader(pubMedAbstractURL.openStream(), "UTF-8"));
			while ((inputLine = in.readLine()) != null) {
				builder.append(inputLine);
			}
			in.close();

			// convert and write JSON to writer
			JSONSerializer serializer = new JSONSerializer();
			writer.write(serializer.serialize(builder.toString()));
		}
		catch (Exception e) {
			if (writer != null) {
				writer.print("Error: internal error: " + e.getMessage() + "\n");
			}
		}

		if (writer != null) {
			writer.flush();
			writer.close();
		}
	}
}
