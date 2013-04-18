// $Id
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
package org.mskcc.cbio.biogene.servlet;

// imports
import org.mskcc.cbio.biogene.eutils.EUtils;
import org.mskcc.cbio.biogene.eutils.Writer;
import org.mskcc.cbio.biogene.util.cache.EhCache;
import org.mskcc.cbio.biogene.util.PubMedAbstract;
import org.mskcc.cbio.biogene.tool.Console;
import org.mskcc.cbio.biogene.task.PopulateCacheTask;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.PrintWriter;
import java.io.IOException;

public class WebService extends HttpServlet {

    /**
     * Shutdown the Servlet.
     */
    public void destroy() {
        super.destroy();
        System.err.println("Shutting Down the BioGENE Server...");
		EhCache.shutDownCache();
    }

    /**
     * Initializes Servlet with parameters in web.xml file.
     *
     * @throws javax.servlet.ServletException Servlet Initialization Error.
     */
    public void init() throws ServletException {
        super.init();
        System.out.println("Starting up the BioGENE Server...");
		try {
            System.out.println("Initializing Cache...");
            EhCache.resetAllCaches();
		}
		catch (Exception e) {
			throw new ServletException(e);
		}
    }

    protected void doGet(HttpServletRequest httpServletRequest,
						 HttpServletResponse httpServletResponse) throws ServletException, IOException {
		try {
			processClient(httpServletRequest, httpServletResponse);
		}
		catch (Exception e) {
			throw new ServletException(e);
		}
    }

    protected void doPost(HttpServletRequest httpServletRequest,
						  HttpServletResponse httpServletResponse) throws ServletException, IOException {
		try {
			processClient(httpServletRequest, httpServletResponse);
		}
		catch (Exception e) {
			throw new ServletException(e);
		}
    }

    private void processClient(HttpServletRequest httpServletRequest,
							   HttpServletResponse httpServletResponse) throws Exception {

		if (httpServletRequest.getParameter("cmd") != null) {
			String command = httpServletRequest.getParameter("cmd");
			if (command.equals("getPubMedAbstract")) {
				getPubMedAbstract(httpServletRequest, httpServletResponse);
			}
			processCommand(httpServletRequest, httpServletResponse);
		}
		else {
			processQuery(httpServletRequest, httpServletResponse);
		}
    }

	private void getPubMedAbstract(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {

		String pubMedAbstractID = httpServletRequest.getParameter("id");
		if (pubMedAbstractID == null) {
			PrintWriter writer = httpServletResponse.getWriter();
			writer.print("Error: you must specify a id parameter.\n");
		}
		else {
			PubMedAbstract.fetchJSON(httpServletResponse, pubMedAbstractID);
		}
	}

	private void processCommand(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {

		ServletOutputStream responseOutputStream = httpServletResponse.getOutputStream();
		String command = httpServletRequest.getParameter("cmd");

		if (command.equals("p")) {
			String organism = httpServletRequest.getParameter("org");
			Integer retStart = null;
			Integer numberToFetchPerRequest = null;
			Integer totalNumberToFetch = null;
			Integer eUtilDelay = null;
			try {
				retStart = new Integer(httpServletRequest.getParameter("retstart"));
				numberToFetchPerRequest = new Integer(httpServletRequest.getParameter("fetch"));
				totalNumberToFetch = new Integer(httpServletRequest.getParameter("total"));
				eUtilDelay = new Integer(httpServletRequest.getParameter("delay"));
			}
			catch (NumberFormatException e) {
				retStart = PopulateCacheTask.DEFAULT_RETSTART;
				numberToFetchPerRequest = PopulateCacheTask.DEFAULT_NUMBER_TO_FETCH_PER_REQUEST;
				totalNumberToFetch = PopulateCacheTask.DEFAULT_TOTAL_NUMBER_TO_FETCH;
				eUtilDelay = PopulateCacheTask.DEFAULT_EUTIL_DELAY;
			}
			Console console = new Console(organism,
										  retStart,
										  numberToFetchPerRequest,
										  totalNumberToFetch,
										  eUtilDelay);
			console.populateCache();
		}
		else if (command.equals("i")) {
			Console.initializeCache();
		}
		responseOutputStream.print("Processing command:  " + command + "\n");
		responseOutputStream.flush();
		responseOutputStream.close();
	}

	private void processQuery(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {

        ServletOutputStream responseOutputStream = httpServletResponse.getOutputStream();
		String query = httpServletRequest.getParameter("query");
		String organism = httpServletRequest.getParameter("org");
		String outputType = httpServletRequest.getParameter("format");
		Integer retMax = null;
		Integer retStart = null;

		try {
			retMax = new Integer(httpServletRequest.getParameter("retmax"));
			retStart = new Integer(httpServletRequest.getParameter("retstart"));
			// sanity checks
			if (retStart < 0) retStart = 0;
			if (retMax < 0) retMax = 5;
		}
		catch (NumberFormatException e) {
			retMax = 5;
			retStart = 0;
		}

		outputType = (outputType == null) ? Writer.XML_FORMAT : outputType;
		if (!outputType.equals(Writer.JSON_FORMAT) && !outputType.equals(Writer.XML_FORMAT)) outputType = null;

		if (query != null && organism != null && outputType != null) {
			try {
				EUtils.processRequest(query, organism, retStart, retMax, outputType, httpServletResponse);
			} catch (Exception e) {
				outputError(responseOutputStream, "internal error:  " + e.getMessage());
			} finally {
				responseOutputStream.flush();
				responseOutputStream.close();
			}
		}
		else {
			if (query == null) {
				outputMissingParameterError(responseOutputStream, "query");
			}
			if (organism == null) {
				outputMissingParameterError(responseOutputStream, "org");
			}
			if (outputType == null) {
				outputError(responseOutputStream, "Unrecognized output type");
			}
		}
	}

    private void outputError(ServletOutputStream responseOutputStream, String msg) throws IOException {
        responseOutputStream.print("Error:  " + msg + "\n");
    }

    private void outputMissingParameterError (ServletOutputStream responseOutputStream, String missingParameter) throws IOException {
        outputError (responseOutputStream, "you must specify a " + missingParameter + " parameter.");
    }
}
