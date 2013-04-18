// $Id: Writer.java,v 1.4 2013/02/04 19:42:45 grossb Exp $
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

import flexjson.*;

import javax.servlet.http.HttpServletResponse;

import javax.xml.namespace.QName;
import javax.xml.bind.Marshaller;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import java.io.StringWriter;
import java.io.IOException;
import java.io.ByteArrayOutputStream;

import org.mskcc.cbio.biogene.schema.*;

public class Writer {

	public static final String XML_FORMAT = "xml";
	public static final String JSON_FORMAT = "json";
	
    private static Logger log = Logger.getLogger(Writer.class);

	/**
	 * Writes an error.
	 *
	 * @param response HTTPServletResponse
	 * @param result String
	 * @param outputType String
	 */
	public static void writeError(HttpServletResponse response, EUtils.RETURN_CODES result, String outputType) {
		GeneInfoList toMarshall = new GeneInfoList();
		toMarshall.setReturnCode(result.toString());
		toMarshall.setCount(0);
		Writer.write(response, toMarshall, outputType);
	}

    /**
     * Writes response.
     *
	 * @param response HttpServletResponse
	 * @param geneInfoList GeneInfoList
	 * @param outputType String
     */
    public static void write(HttpServletResponse response, GeneInfoList geneInfoList, String outputType) {

		// create marshaller
		try {
			if (outputType.equals(XML_FORMAT)) {
				JAXBContext jaxbContext = JAXBContext.newInstance("org.mskcc.cbio.biogene.schema");
				Marshaller marshaller = jaxbContext.createMarshaller();
				marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
				marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

				// marshall data
				StringWriter writer = new StringWriter();
				QName qName = new QName("", "biogene_info_list");
				marshaller.marshal(new JAXBElement(qName, GeneInfoList.class, geneInfoList), writer);

				// write out xml		
				returnContent(response, writer.toString(), "text/xml");
			}
			else {
				JSONSerializer serializer = new JSONSerializer();
				/*
				returnContent(response, serializer.exclude("*.class")
							  .include("geneInfo", "geneInfo.geneRif")
							  .serialize(geneInfoList), "text/json");
				*/
				returnContent(response, serializer.exclude("*.class").deepSerialize(geneInfoList), "text/json");
			}
		}
		catch(JAXBException e) {
			log.error("JAXB Exception", e);
		}
	}

	/**
	 * Return output to Client
	 *
     * @param response  Servlet Response.
     * @param outputString XML String
	 * @param contentType String
	 */
    private static void returnContent(HttpServletResponse response, String outputString, String contentType) {
	
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			out.write(outputString.getBytes());
			response.setContentType(contentType);
			if (log.isDebugEnabled()) {
				log.debug("EFetch.returnXml(), Content Length: " + out.size());
			}
			response.setContentLength(out.size());
			out.writeTo(response.getOutputStream());
			out.flush();
			out.close();
		}
		catch (IOException e) {
			log.error("IO Error", e);
		}
	}
}