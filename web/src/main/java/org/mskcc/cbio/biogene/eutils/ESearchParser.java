// $Id: ESearchParser.java,v 1.6 2012/02/24 20:12:27 grossb Exp $
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

import org.jdom2.Element;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.StringReader;

import java.util.List;
import java.util.ArrayList;

import org.mskcc.cbio.biogene.schema.*;

public class ESearchParser {

    private static Logger log = Logger.getLogger(ESearchParser.class);
	private static String SEARCH_RESULT_ELEMENT = "eSearchResult";
	private static String COUNT_ELEMENT = "Count";
	private static String ID_LIST_ELEMENT = "IdList";
	private static String ID_VALUE_ATTRIBUTE = "Id";
    private static Integer ID_COUNT;

    /**
     * Parses XML returned from eutils
     *
     * @param content String (xml document)
	 * @param totalCount Integer[]
	 * @return ArrayList<String> (or null if parse error)
     */
    public static ArrayList<String> parse(String content, Integer[] totalCount) {

		try {
			StringReader reader = new StringReader(content);
			SAXBuilder builder = new SAXBuilder();
			Document document = builder.build(reader);
			Element eSearchResult = document.getRootElement();
			if (eSearchResult == null) {
				if (log.isDebugEnabled()) {
					log.debug("ESearchParser.parse(), eSearchResult element not found.");
				}
				return null;
			}

			//  we have something to parse
			if (log.isDebugEnabled()) {
				log.debug("ESearchParser.parse(), eSearchResult element found.");
			}
			return ESearchParser.parseESearchResult(eSearchResult, totalCount);
		}
		catch (Exception e) {
			if (log.isDebugEnabled()) {
				log.debug("ESearchParser.parseESearchResults(), parse error.");
			}
			return null;
		}
	}

	/**
	 * Parses entrez gene element
	 *
	 * @param eSearchResult Element
	 * @param totalCount Integer[]
	 * @return ArrayList<String> or null if parse error
	 */
	private static ArrayList<String> parseESearchResult(Element eSearchResult, Integer[] totalCount) throws Exception {

		// what we return
		ArrayList<String> toReturn = new ArrayList<String>();

		// get count
		Element idCount = eSearchResult.getChild(COUNT_ELEMENT);
		ID_COUNT = (idCount != null) ? new Integer(idCount.getTextNormalize()) : null;
		if (ID_COUNT != null && totalCount != null && totalCount.length > 0) {
			if (log.isDebugEnabled()) {
				log.debug("ESearchParser.parseESearchResults(), setting totalCount parameter (pass by ref) to ID_COUNT: " + ID_COUNT);
			}
			totalCount[0] = ID_COUNT;
		}
		if (ID_COUNT == null) {
			if (log.isDebugEnabled()) {
				log.debug("ESearchParser.parseESearchResults(), cannot parse count.");
			}
			return null;
		}
		else if (ID_COUNT == 0) {
			if (log.isDebugEnabled()) {
				log.debug("ESearchParser.parseESearchResults(), count = 0.");
			}
			return toReturn;
		}
		if (log.isDebugEnabled()) {
			log.debug("ESearchParser.parseESearchResults(), we have a total of " + ID_COUNT + " id(s) to process.");
		}

		// get ids
		Element idList = eSearchResult.getChild(ID_LIST_ELEMENT);
		if (idList != null) {
			List<Element> IDs = idList.getChildren();
			for (Element ID : IDs) {
				if (ID != null) {
					String idStr = ID.getTextNormalize();
					if (log.isDebugEnabled()) {
						log.debug("ESearchParser.parseESearchResults(), we have an id to process: " + idStr);
					}
					if (idStr != null && idStr.length() > 0) {
						toReturn.add(idStr);
					}
				}
			}
		}

		// outta here
		return toReturn;
	}
}