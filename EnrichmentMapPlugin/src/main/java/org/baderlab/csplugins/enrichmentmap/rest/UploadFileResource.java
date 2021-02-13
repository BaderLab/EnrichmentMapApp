package org.baderlab.csplugins.enrichmentmap.rest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.FormDataParam;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;


@Api(tags="Apps: EnrichmentMap")
@javax.ws.rs.Path("/enrichmentmap/fileupload")
public class UploadFileResource {
	
	private Path tempDir = null;
	
	
	@POST
	@javax.ws.rs.Path("/")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value="Upload a file.",
		notes=""
	)
	public Response uploadFile(@FormDataParam("file") InputStream in) {
		try {
			synchronized(this) {
				if(tempDir == null) {
					tempDir = Files.createTempDirectory("em_fileupload_", new FileAttribute<?>[0]);
				}
			}
			
			File tempFile = Files.createTempFile(tempDir, "em_", "", new FileAttribute<?>[0]).toFile();
			tempFile.deleteOnExit();
			
			try(FileOutputStream out = new FileOutputStream(tempFile)) {
				int read = 0;
				byte[] bytes = new byte[1024];
				while((read = in.read(bytes)) != -1) {
					out.write(bytes, 0, read);
				}
				out.flush();
			}
			
			String absPath = tempFile.getAbsolutePath();
			String response = String.format("{\"path\" : \"%s\"}", absPath);
			return Response.status(200).entity(response).build();
			
		} catch(Exception e) {
			e.printStackTrace();
			return Response.serverError().build();
		} 
	}

}
