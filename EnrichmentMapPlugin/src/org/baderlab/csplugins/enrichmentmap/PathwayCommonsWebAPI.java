/**
 **                       EnrichmentMap Cytoscape Plugin
 **
 ** Copyright (c) 2008-2009 Bader Lab, Donnelly Centre for Cellular and Biomolecular 
 ** Research, University of Toronto
 **
 ** Contact: http://www.baderlab.org
 **
 ** Code written by: Carl Song
 ** 	This class is a simplified implementation of the following packages:
 **		 	org.cytoscape.coreplugin.cpath2.web_service
 **			org.cytoscape.coreplugin.cpath2.http
 **
 ** Authors: Carl Song, Daniele Merico, Ruth Isserlin, Oliver Stueker, Gary D. Bader
 **
 ** This library is free software; you can redistribute it and/or modify it
 ** under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 2.1 of the License, or
 ** (at your option) any later version.
 **
 ** This library is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 ** documentation provided hereunder is on an "as is" basis, and
 ** University of Toronto
 ** has no obligations to provide maintenance, support, updates, 
 ** enhancements or modifications.  In no event shall the
 ** University of Toronto
 ** be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if
 ** University of Toronto
 ** has been advised of the possibility of such damage.  
 ** See the GNU Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this library; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 **
 **/

package org.baderlab.csplugins.enrichmentmap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.util.URIUtil;

/**
 * @author carlsong
 *	adapted from org.cytoscape.coreplugin.cpath2.web_service.CPathWebServiceImpl
 */
public class PathwayCommonsWebAPI {
	HttpClient client = new HttpClient();
	String liveUrl = "http://www.pathwaycommons.org/pc/webservice.do";
	HttpMethodBase method = new GetMethod(liveUrl);
	
	/**
	 * @param queryString
	 * @return HTTPResponse in String format
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public String query(String queryString) throws IOException, InterruptedException {
		method.setQueryString(URIUtil.encodeQuery(queryString));
	    int statusCode = client.executeMethod(method);
	    if (statusCode != 200) {
	    	throw new InterruptedException("HTTP Status Code:  " + statusCode + ":  " + HttpStatus.getStatusText(statusCode) + ".");
	    }
	    //  Read in Content
	    InputStream instream = method.getResponseBodyAsStream();
	    ByteArrayOutputStream outstream = new ByteArrayOutputStream(4096);
	    byte[] buffer = new byte[4096];
	    int len;
	    while ((len = instream.read(buffer)) > 0) {
	    	outstream.write(buffer, 0, len);
	    }
	    instream.close();
	    return new String(outstream.toByteArray());
	}	
}
