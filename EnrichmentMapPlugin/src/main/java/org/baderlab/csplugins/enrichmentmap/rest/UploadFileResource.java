package org.baderlab.csplugins.enrichmentmap.rest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.FormDataParam;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;


@Api(tags="Apps: EnrichmentMap")
@javax.ws.rs.Path("/enrichmentmap/textfileupload")
public class UploadFileResource {
	
	private Path tempDir = null;
	
	
	@POST
	@javax.ws.rs.Path("/")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value="Upload a file.",
		notes=""
	)
	public Response uploadFile(
		@QueryParam("fileName") String fileName,
		@FormDataParam("file") InputStream in
	) {
		try {
			synchronized(this) {
				if(tempDir == null) {
					tempDir = Files.createTempDirectory("em_fileupload_", new FileAttribute<?>[0]);
				}
			}
			
			File tempFile = Files.createTempFile(tempDir, "em_", "_" + fileName, new FileAttribute<?>[0]).toFile();
			tempFile.deleteOnExit();

			try (
				FileWriter fileWriter = new FileWriter(tempFile);
				BufferedWriter buffWriter = new BufferedWriter(fileWriter);
				InputStreamReader reader = new InputStreamReader(in);
				BufferedReader buffReader = new BufferedReader(reader)
			) {
				String firstLine = buffReader.readLine();
				// Skip 3 lines
				buffReader.readLine();
				buffReader.readLine();
				buffReader.readLine();

				String line = buffReader.readLine();
				if(!line.startsWith(firstLine)) {
					while(line != null) {
						buffWriter.write(line);
	
						line = buffReader.readLine();
						if(line.startsWith(firstLine)) {
							break;
						} else {
							buffWriter.newLine();
						}
					}
				}
			}

			String absPath = tempFile.getAbsolutePath();
			absPath = absPath.replace("\\", "\\\\");
			String response = String.format("{\"path\" : \"%s\"}", absPath);
			return Response.status(200).entity(response).build();
			
		} catch(Exception e) {
			e.printStackTrace();
			return Response.serverError().build();
		} 
	}

}
