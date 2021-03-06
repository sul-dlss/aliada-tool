// ALIADA - Automatic publication under Linked Data paradigm
//          of library and museum data
//
// Component: aliada-linked-data-server
// Responsible: ALIADA Consortium
package eu.aliada.linkeddataserversetup.impl;

import eu.aliada.shared.log.Log;
import eu.aliada.shared.rdfstore.RDFStoreDAO;
import eu.aliada.linkeddataserversetup.log.MessageCatalog;
import eu.aliada.linkeddataserversetup.model.Job;
import eu.aliada.linkeddataserversetup.model.JobConfiguration;
import eu.aliada.linkeddataserversetup.model.Subset;
import eu.aliada.linkeddataserversetup.rdbms.DBConnectionManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

/**
 * Linked Data Server setup implementation. 
 * It setups the rewrite rules in Virtuoso for de-referencing the dataset URI-s.
 *  
 * @author Idoia Murua
 * @since 1.0
 */
public class LinkedDataServerSetup {
	/** For logging. */
	private static final Log LOGGER = new Log(LinkedDataServerSetup.class);
	/** Format for the ISQL command to execute for the dataset */
	private static final String ISQL_COMMAND_FORMAT_DATASET = "%s %s:%d %s %s %s -u lhost='%s' vhost='%s' uri_id='%s' uri_doc_slash='%s' uri_def='%s' graphs_select_encoded='%s' graphs_encoded='%s' domain_name_encoded='%s' rules_suffix='%s' uri_doc_concept='%s' dataset_page='%s' aliada_ont_encoded='%s' uri_id_encoded='%s' create_virtual_path=%d urrl_list_subset=%d rules_suffix_dataset='%s' uri_doc_concept_parent='%s'";
	/** Format for the ISQL command to execute for the subset */
	private static final String ISQL_COMMAND_FORMAT_SUBSET = "%s %s:%d %s %s %s -u lhost='%s' vhost='%s' uri_id='%s' uri_doc_slash='%s' uri_def='%s' graphs_select_encoded='%s' graphs_encoded='%s' domain_name_encoded='%s' rules_suffix='%s' uri_doc_concept='%s' dataset_page='%s' aliada_ont_encoded='%s' uri_id_encoded='%s' create_virtual_path=%d urrl_list_subset=%d rules_suffix_dataset='%s' uri_doc_concept_parent='%s'";
	/** Dataset Index HTML Page*/
	private static final String DATASET_INDEX_PAGE = "dataset.html"; 
	/** Dataset CSS file*/
	private static final String DATASET_CSS_FILE = "aliada_dataset.css"; 
	/* Input parameters for URL rewrite rules */
	/** Suffix to be added to URL rewrite rules names. */
	private String rulesNamesSuffix;
	/** The URI Document section. */
	private String uriDocPart;
	/** The URI Identifier section. */
	private String uriIdPart;
	/** The URI Ontology section. */
	private String uriDefPart;
	/** The dataset graphs encoded with the FROM clause, URL encoded. */
	private String graphsSelectEncoded;
	/** The dataset graphs encoded with the & clause, URL encoded. */
	private String graphsEncoded;
	/** The dataset Domain name, URL encoded. */
	private String domainNameEncoded;
	/** The URI Identifier section, URL encoded. */
	private String uriIdPartEncoded;
	/** Aliada Ontology URI, URL encoded. */
	private String ontologyEncoded;
	/** The URI Document section + Dataset Concept section. */
	private String uriDocConcept;

	/**
	 * Remove leading and trailing slashes of a given path.
	 *
	 * @param path	path to remove leading and trailing slashes.
	 * @return the path without leading and trailing slashes.
	 * @since 2.0
	 */
	protected String removeLeadingTralingSlashes(final String path){
		String cleanPath = "";
		//Remove leading and trailing spaces
		cleanPath = path.trim();
		//Remove leading slashes
		cleanPath = cleanPath.replaceFirst("^/+", "");
		//Remove trailing slashes
		cleanPath = cleanPath.replaceFirst("/+$", "");
		return cleanPath;
	}

	/**
	 * Encodes the parameters to pass to the ISQL commands.
	 * URLEncode and replace % by %%, for Virtuoso Rewrite Rules.
	 *
	 * @param jobConf	the {@link eu.aliada.linkeddataserversetup.model.JobConfiguration}
	 *               	that contains the ISQL commands parameters.  
	 * @return true if the encoding has been carried out correctly.
	 * @since 1.0
	 */
	public boolean encodeParams(final JobConfiguration jobConf){
		boolean encoded = false;
		try{
			//Remove leading/trailing slashes of URI Document section
			if(jobConf.getUriDocPart() != null) {
				uriDocPart = removeLeadingTralingSlashes(jobConf.getUriDocPart());
			} else {
				uriDocPart = "";
			}
			//Remove leading/trailing slashes of URI identifier section
			if(jobConf.getUriIdPart() != null) {
				uriIdPart = removeLeadingTralingSlashes(jobConf.getUriIdPart());
			} 
			//Remove leading/trailing slashes of URI Ontology section
			if(jobConf.getUriDefPart() != null) {
				uriDefPart = removeLeadingTralingSlashes(jobConf.getUriDefPart());
			} 
			//Encode dataset graphs
			graphsSelectEncoded = "";
			graphsEncoded = "";
			int subsetIndex = 0;
			for (final Iterator<Subset> iterSubsets = jobConf.getSubsets().iterator(); iterSubsets.hasNext();  ) {
				final Subset subset = iterSubsets.next();
				final String graphSelectEncoded = URLEncoder.encode(" FROM <" + subset.getGraph() + ">", "UTF-8");
				String linksGraphSelectEncoded = URLEncoder.encode(" FROM <" + subset.getLinksGraph() + ">", "UTF-8");
				graphsSelectEncoded = graphsSelectEncoded + graphSelectEncoded + linksGraphSelectEncoded;
				String graphEncoded = "";
				if(subsetIndex == 0){
					graphEncoded = "&graph" + URLEncoder.encode("=" + subset.getGraph(), "UTF-8");
				} else {
					graphEncoded = URLEncoder.encode("&graph=" + subset.getGraph(), "UTF-8");
				}
				final String linksGraphEncoded = URLEncoder.encode("&graph=" + subset.getLinksGraph(), "UTF-8");
				graphsEncoded = graphsEncoded + graphEncoded + linksGraphEncoded;
				subsetIndex ++;
			}
			graphsSelectEncoded = graphsSelectEncoded.replace("%", "%%");
			graphsEncoded = graphsEncoded.replace("%", "%%");
			//Encode domain name
			domainNameEncoded = URLEncoder.encode(jobConf.getDomainName(), "UTF-8");
			domainNameEncoded = domainNameEncoded.replace("%", "%%");
			//Encode URI Identifier part
			uriIdPartEncoded = URLEncoder.encode(uriIdPart, "UTF-8");
			uriIdPartEncoded = uriIdPartEncoded.replace("%", "%%");
			//Encode Ontology URI 
			ontologyEncoded = URLEncoder.encode(jobConf.getOntologyUri(), "UTF-8");
			ontologyEncoded = ontologyEncoded.replace("%", "%%");
			//Compose URI document part + URI Concept part
			if(jobConf.getUriDocPart() != null) {
				uriDocConcept = removeLeadingTralingSlashes(jobConf.getUriDocPart());
			} 
			String datasetConceptPart = "";
			if(jobConf.getUriConceptPart() != null) {
				datasetConceptPart = removeLeadingTralingSlashes(jobConf.getUriConceptPart());
				if(datasetConceptPart.length() > 0){
					if(uriDocConcept.length() > 0){
						uriDocConcept = uriDocConcept + "/" + datasetConceptPart;
					} else {
						uriDocConcept = datasetConceptPart;
					}
				}
			}
			//Compose rules name suffix
			rulesNamesSuffix = jobConf.getDomainName().replace("http", ""); 
			rulesNamesSuffix = rulesNamesSuffix.replace(":", "");
			rulesNamesSuffix = rulesNamesSuffix.replace("/", "");
			rulesNamesSuffix = rulesNamesSuffix.replace(".", "");
					
			//Check that we have the parameter values
			if((uriIdPart != null) && (uriDefPart!= null) && (graphsEncoded.length() > 0) && (domainNameEncoded != null) &&
					(ontologyEncoded != null) && (uriDocConcept != null) && (rulesNamesSuffix.length() > 0)) {
				if((uriIdPart.length() > 0) && (uriDefPart.length() > 0) && (ontologyEncoded.length() > 0) && (uriDocConcept.length() > 0) && (domainNameEncoded.length() > 0)) {
					//Check that Identifier, Ontology and Document parts do not contain "/"
					if(!(uriIdPart.contains("/")) && !(uriDefPart.contains("/")) && !(uriDocPart.contains("/"))) {
						encoded = true;
					}
				}
			}
		} catch (UnsupportedEncodingException exception){
			LOGGER.error(MessageCatalog._00038_ENCODING_ERROR, exception);
		}
		return encoded;
	}

	/**
	 * Get the appropiate ISQL commands file.
	 * The ISQL commands are for creating rewrite rules in Virtuoso for
	 * dereferencing the dataset URI-s. 
	 *
	 * @param isqlCommandsFilename			the specific ISQL commands file name. 
	 * @param isqlCommandsFilenameDefault	the default ISQL commands file name.  
	 * @return the name of the commands file to use.
	 * @since 2.0
	 */
	public String getIsqlCommandsFile(final String isqlCommandsFilename, final String isqlCommandsFilenameDefault){
		String fileNameToUse = null;
		//Check if isqlCommandsFilename exists
		if(isqlCommandsFilename != null){
			final File isqlFile = new File(isqlCommandsFilename);
			if (isqlFile.exists()){
				fileNameToUse = isqlCommandsFilename;
			}
		}
		if (fileNameToUse == null){
			//If there is not a ISQL command file specifically for this subset, 
			//use the default one
			//Check if default isqlCommandsFilename exists
			if(isqlCommandsFilenameDefault != null){
				final File isqlFile = new File(isqlCommandsFilenameDefault);
				if (isqlFile.exists()){
					fileNameToUse = isqlCommandsFilenameDefault;
				}
			}
			if (fileNameToUse == null){
				LOGGER.error(MessageCatalog._00031_FILE_NOT_FOUND, isqlCommandsFilenameDefault);
			}
		}
		return fileNameToUse;
	}

	/**
	 * It executes the dataset ISQL commands that set the rewrite rules in Virtuoso 
	 * for dereferencing the dataset URI-s.
	 *
	 * @param jobConf		the {@link eu.aliada.linkeddataserversetup.model.JobConfiguration}
	 *						that contains information to setup the rewrite rules in Virtuoso  
	 * @return if the ISQL commands have been executed successfully.  					
	 * @since 1.0
	 */
	public boolean executeDatasetIsqlCommands(final JobConfiguration jobConf) {
		boolean success = false;
		//Get dataset ISQL commands file for rewriting rules in Virtuoso
		LOGGER.debug(MessageCatalog._00036_GET_ISQL_COMMANDS_FILE);
		final String isqlCommandsFilename = getIsqlCommandsFile(jobConf.getIsqlCommandsDatasetFilename(), jobConf.getIsqlCommandsDatasetFilenameDefault());
		//Variables for creating rules for Document listing with extension 
		int createVirtualPath = 0;
		final int urrlListSubset = 0; //It is not a subset
		final String rulesNamessuffixDataset = rulesNamesSuffix;
		String uriDocConceptParent = "";
		if(uriDocConcept.contains("/")) {
			//If the Concept part contains an /, a parent virtual path must also be created
			//for the Document listing with extension
			createVirtualPath = 1;
			uriDocConceptParent = uriDocConcept.substring(0, uriDocConcept.lastIndexOf('/'));
		}
		//Add a "/" at the beginning and end of the Document concept part
		String uriDocSlash = "/" + uriDocPart;
		if (!uriDocSlash.endsWith("/")) {
			uriDocSlash = uriDocSlash + "/";
		}
		if(isqlCommandsFilename != null){
			//Compose ISQL command execution statement
			final String isqlCommand = String.format(ISQL_COMMAND_FORMAT_DATASET,
					jobConf.getIsqlCommandPath(), jobConf.getStoreIp(),
					jobConf.getStoreSqlPort(), jobConf.getSqlLogin(),
					jobConf.getSqlPassword(), isqlCommandsFilename,
					jobConf.getListeningHost(), jobConf.getVirtualHost(),
					uriIdPart, uriDocSlash, uriDefPart, graphsSelectEncoded,
					graphsEncoded, domainNameEncoded, rulesNamesSuffix,
					uriDocConcept, DATASET_INDEX_PAGE, ontologyEncoded, 
					uriIdPartEncoded, createVirtualPath, urrlListSubset,
					rulesNamessuffixDataset, uriDocConceptParent);
			LOGGER.debug(isqlCommand);
			//Execute ISQL command
			try {
				LOGGER.debug(MessageCatalog._00040_EXECUTING_ISQL);
				final Process commandProcess = Runtime.getRuntime().exec(isqlCommand);
				final BufferedReader stdInput = new BufferedReader(new InputStreamReader(commandProcess.getInputStream()));
				String comOutput = "";
				while ((comOutput = stdInput.readLine()) != null) {
					LOGGER.debug(comOutput);
				}
				success = true;
			} catch (IOException exception) {
				LOGGER.error(MessageCatalog._00033_EXTERNAL_PROCESS_START_FAILURE, exception, isqlCommand);
			}
		}
		return success;
	}

	/**
	 * It executes the subset ISQL commands that set the rewrite rules in Virtuoso 
	 * for dereferencing the subset URI-s.
	 *
	 * @param jobConf		the {@link eu.aliada.linkeddataserversetup.model.JobConfiguration}
	 *						that contains information to setup the rewrite rules in Virtuoso  
	 * @param subset		the {@link eu.aliada.linkeddataserversetup.model.Subset}
	 *						that contains information to setup the rewrite rules in Virtuoso  
	 * @return if the ISQL commands have been executed successfully.  					
	 * @since 2.0
	 */
	public boolean executeSubsetIsqlCommands(final JobConfiguration jobConf, final Subset subset) {
		boolean success = false;

		//Encode subset graphs
		String subgraphsSelectEncoded = "";
		String subgraphsEncoded = "";
		try	{
			final String graphSelectEncoded = URLEncoder.encode(" FROM <" + subset.getGraph() + ">", "UTF-8");
			String linksGraphSelectEncoded = URLEncoder.encode(" FROM <" + subset.getLinksGraph() + ">", "UTF-8");
			subgraphsSelectEncoded = graphSelectEncoded + linksGraphSelectEncoded;
			
			String graphEncoded = "";
			graphEncoded = "&graph" + URLEncoder.encode("=" + subset.getGraph(), "UTF-8");
			final String linksGraphEncoded = URLEncoder.encode("&graph=" + subset.getLinksGraph(), "UTF-8");
			subgraphsEncoded = graphEncoded + linksGraphEncoded;
			
			subgraphsSelectEncoded = subgraphsSelectEncoded.replace("%", "%%");
			subgraphsEncoded = subgraphsEncoded.replace("%", "%%");
		} catch (UnsupportedEncodingException exception){
			LOGGER.error(MessageCatalog._00038_ENCODING_ERROR, exception);
		}
		
		//Get global ISQL commands file for rewriting rules in Virtuoso
		LOGGER.debug(MessageCatalog._00036_GET_ISQL_COMMANDS_FILE);
		final String isqlCommandsFilename = getIsqlCommandsFile(subset.getIsqlCommandsSubsetFilename(), jobConf.getIsqlCommandsSubsetFilenameDefault());

		//Compose URI document part + URI Concept part + Subset URI Concept part
		String uriDocConceptSubset = "";
		if(subset.getUriConceptPart() != null) {
			uriDocConceptSubset = removeLeadingTralingSlashes(subset.getUriConceptPart());
			if(uriDocConceptSubset.length() > 0) {
				uriDocConceptSubset = uriDocConcept + "/" + uriDocConceptSubset;
			}
		} 
		
		//Variables for creating rules for Document listing with extension 
		int createVirtualPath = 0;
		final int urrlListSubset = 1; //It is a subset
		final String rulesNamessuffixDataset = rulesNamesSuffix;
		String uriDocConceptParent = "";
		if(subset.getUriConceptPart().contains("/")) {
			//If the Concept part of the subset contains an /, 
			//a parent virtual path must also be created
			//for the Document listing with extension
			createVirtualPath = 1;
			uriDocConceptParent = uriDocConceptSubset.substring(0, uriDocConceptSubset.lastIndexOf('/'));
		}
		//Add a "/" at the beginning and end of the Document concept part
		String uriDocSlash = "/" + uriDocPart;
		if (!uriDocSlash.endsWith("/")) {
			uriDocSlash = uriDocSlash + "/";
		}
		//If the URI Concept part of the subset is empty, its 
		//corresponding URL Rewrite rules will not be created
		if((isqlCommandsFilename != null) && (uriDocConceptSubset.length() > 0)){
			//Compose Rules Names suffix for the subset, 
			//adding the subset concept part of the URI
			String rulesNamesSuffixSubset = rulesNamesSuffix + uriDocConceptSubset;
			rulesNamesSuffixSubset = rulesNamesSuffixSubset.replace("/", "");
			//Compose ISQL command execution statement
			final String isqlCommand = String.format(ISQL_COMMAND_FORMAT_SUBSET,
					jobConf.getIsqlCommandPath(), jobConf.getStoreIp(),
					jobConf.getStoreSqlPort(), jobConf.getSqlLogin(),
					jobConf.getSqlPassword(), isqlCommandsFilename,
					jobConf.getListeningHost(), jobConf.getVirtualHost(),
					uriIdPart,  uriDocSlash, uriDefPart, subgraphsSelectEncoded,
					subgraphsEncoded, domainNameEncoded, rulesNamesSuffixSubset, 
					uriDocConceptSubset, DATASET_INDEX_PAGE, ontologyEncoded, 
					uriIdPartEncoded, createVirtualPath, urrlListSubset,
					rulesNamessuffixDataset, uriDocConceptParent);
			LOGGER.debug(isqlCommand);
			//Execute ISQL command
			try {
				LOGGER.debug(MessageCatalog._00040_EXECUTING_ISQL);
				final Process commandProcess = Runtime.getRuntime().exec(isqlCommand);
				final BufferedReader stdInput = new BufferedReader(new InputStreamReader(commandProcess.getInputStream()));
				String comOutput = "";
				while ((comOutput = stdInput.readLine()) != null) {
					LOGGER.debug(comOutput);
				}
				success = true;
			} catch (IOException exception) {
				LOGGER.error(MessageCatalog._00033_EXTERNAL_PROCESS_START_FAILURE, exception, isqlCommand);
			}
		}
		return success;
	}

	/**
	 * Create the default HTML page for the dataset and for the domain.
	 *
	 * @param jobConf		the {@link eu.aliada.linkeddataserversetup.model.JobConfiguration}
	 *						that contains information to create the dataset HTML page.  
	 * @param db			The DDBB connection. 
	 * @return 
	 * @since 2.0
	 */
	public void createDatasetDefaultPage(final JobConfiguration jobConf, final DBConnectionManager dbConn){
		//Create the folder where the page resides, if it does not exist
		final String pageFolder = jobConf.getVirtHttpServRoot() + File.separator + rulesNamesSuffix;
		final String pageURL = "http://" + jobConf.getDomainName();
		final File fFolder = new File(pageFolder);
		if (!fFolder.exists()) {
			fFolder.mkdir();
		}
		//Update the dataset web page root in the DB
		dbConn.updateDatasetWebPageRoot(jobConf.getDatasetId(), pageFolder);
		
		final String pagePath = pageFolder + File.separator + DATASET_INDEX_PAGE;
		//Remove the page if it already exists
		final File fPage = new File(pagePath);
		if (fPage.exists()) {
			fPage.delete();
		}
		
		//Copy image and CSS files to web server folder
		copyFilesToWebServerPath(jobConf, pageFolder, pageURL);
		final String orgLogoPath = jobConf.getOrgImageURL();
		final String styleSheetPath = jobConf.getCssFileURL();
		//Get the number of triples of the dataset
		final int numTriples = calculateDatasetNumTriples(jobConf.getSparqlEndpointUri(), jobConf.getSparqlLogin(), jobConf.getSparqlPassword(), jobConf.getSubsets());
		//Now, create a new one
		try {
			final FileWriter fstream = new FileWriter(pagePath);
			final BufferedWriter out = new BufferedWriter(fstream);
			String line = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">";
        	out.write(line);
        	out.newLine();
        	line = "<html>";
        	out.write(line);
        	out.newLine();
        	line = "<head>";
        	out.write(line);
        	out.newLine();
        	line = "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\">";
        	out.write(line);
        	out.newLine();
        	line = "<title>" + jobConf.getDatasetDesc().toUpperCase() + "</title>";
        	out.write(line);
        	out.newLine();
        	line = "<link rel=\"stylesheet\" href=\"" + styleSheetPath + "\" type=\"text/css\">";
        	out.write(line);
        	out.newLine();
        	line = "</head>";
        	out.write(line);
        	out.newLine();
        	line = "<body>";
        	out.write(line);
        	out.newLine();
        	line = "<img src=\"" + orgLogoPath+ "\">";
        	out.write(line);
        	out.newLine();
        	line = "<h1>" + jobConf.getDatasetDesc() + "</h1>";
        	out.write(line);
        	out.newLine();
        	line ="<table><colgroup><col width=\"25%\"><col width=\"75%\"></colgroup>";
        	out.write(line);
        	out.newLine();
        	//Description
        	line =String.format("<tr><td class=\"label\">%s</td><td class=\"input\">%s</td></tr>", "description", jobConf.getDatasetLongDesc());
        	out.write(line);
        	out.newLine();
        	//Publisher
        	line =String.format("<tr><td class=\"label\">%s</td><td class=\"input\">%s</td></tr>", "publisher", jobConf.getOrgName().toUpperCase());
        	out.write(line);
        	out.newLine();
        	//Source URL
        	line =String.format("<tr><td class=\"label\">%s</td><td class=\"input\"><a href=\"%s\" target=\"_blank\">%s</a></td></tr>", "source", jobConf.getDatasetSourceURL(), jobConf.getDatasetSourceURL());
        	out.write(line);
        	out.newLine();
        	//Created
        	line =String.format("<tr><td class=\"label\">%s</td><td class=\"input\">%s</td></tr>", "created", getStringNow());
        	out.write(line);
        	out.newLine();
        	//Contributor
        	line =String.format("<tr><td class=\"label\">%s</td><td class=\"input\">%s</td></tr>", "contributor", jobConf.getDatasetAuthor());
        	out.write(line);
        	out.newLine();
        	//License URL
        	line =String.format("<tr><td class=\"label\">%s</td><td class=\"input\"><a href=\"%s\" target=\"_blank\">%s</a></td></tr>", "license", jobConf.getLicenseURL(), jobConf.getLicenseURL());
        	out.write(line);
        	out.newLine();
        	//SPARQL endpoint URL
        	line =String.format("<tr><td class=\"label\">%s</td><td class=\"input\"><a href=\"%s\" target=\"_blank\">%s</a></td></tr>", "SPARQL endpoint", jobConf.getPublicSparqlEndpointUri(), jobConf.getPublicSparqlEndpointUri());
        	out.write(line);
        	out.newLine();
        	//Vocabulary URL
        	line =String.format("<tr><td class=\"label\">%s</td><td class=\"input\"><a href=\"%s\" target=\"_blank\">%s</a></td></tr>", "vocabulary", jobConf.getOntologyUri(), jobConf.getOntologyUri());
        	out.write(line);
        	out.newLine();
        	//Number of triples
        	line =String.format("<tr><td class=\"label\">%s</td><td class=\"input\">%s</td></tr>", "number of triples", numTriples);
        	out.write(line);
        	out.newLine();
        	//List resources of dataset
        	final String datasetUri = "http://" + jobConf.getDomainName() + "/" + uriDocConcept;  
        	line =String.format("<tr><td class=\"label\">%s</td><td class=\"input\"><a href=\"%s\" target=\"_blank\">%s</a></td></tr>", "list of resources", datasetUri, datasetUri);
        	out.write(line);
        	out.newLine();
        	//List subsets
        	line =String.format("<tr><td class=\"label\">%s</td><td class=\"input\"><ul>", "subsets");
        	out.write(line);
        	out.newLine();
			for (final Iterator<Subset> iterSubsets = jobConf.getSubsets().iterator(); iterSubsets.hasNext();  ) {
				final Subset subset = iterSubsets.next();
				String uriDocConceptSubset = "";
				if(subset.getUriConceptPart() != null) {
					uriDocConceptSubset = removeLeadingTralingSlashes(subset.getUriConceptPart());
				} 				
				if(uriDocConceptSubset.length() > 0) {
		        	//List resources of subset
					final String subsetUri = datasetUri + "/" + uriDocConceptSubset;
		        	line =String.format("<li>%s: <a href=\"%s\" target=\"_blank\">%s</a></li>", subset.getDescription(), subsetUri, subsetUri);
		        	out.write(line);
		        	out.newLine();
				}
			}
			line = "</ul></td></tr>";
        	out.write(line);
        	out.newLine();
			line = "</table>";
        	out.write(line);
        	out.newLine();
        	
        	line = "</body>";
        	out.write(line);
        	out.newLine();
        	line = "</html>";
        	out.write(line);
        	out.newLine();
	    	out.close();
		} catch (IOException exception) {
			LOGGER.error(MessageCatalog._00034_FILE_CREATION_FAILURE, exception, pagePath);
		}
		
	}

	/**
	 * Returns the current date.
	 * 
	 * @return The current date.
	 * @since 2.0
	 */
	protected String getStringNow(){
		final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); 
		final Date actualDate = new Date(); 
		return dateFormat.format(actualDate); 
	}
   
	/**
	 * It copies some files (the organisation image file and CSS file) 
	 * to the dataset web root.
	 *
	 * @param jobConf		the {@link eu.aliada.linkeddataserversetup.model.JobConfiguration}
	 *						that contains information to setup the rewrite rules in Virtuoso.  
	 * @param pageFolder	the dataset web page folder.  
	 * @param pageURL		the dataset web page URL.  
	 * @return true if the files has been copied correctly. False otherwise.  					
	 * @since 2.0
	 */
	public boolean copyFilesToWebServerPath(final JobConfiguration jobConf, final String pageFolder, final String pageURL) {
		boolean success = false;
		try {
			//Copy the CSS file to the definitive folder
			final InputStream cssInpuStream= getClass().getResourceAsStream("/"+ DATASET_CSS_FILE);
			final String definitiveCssFileName = pageFolder + File.separator + "aliada_dataset.css";
			final File definitiveCssFile = new File (definitiveCssFileName);
			final FileOutputStream cssOutputStream =  new FileOutputStream(definitiveCssFile);			
			int read = 0;
			final byte[] bytes = new byte[1024]; 
			while ((read = cssInpuStream.read(bytes)) != -1) {
				cssOutputStream.write(bytes, 0, read);
			}
			cssOutputStream.close();
			jobConf.setCssFilePath(definitiveCssFileName);
			final String cssFileURL = pageURL + "/" + DATASET_CSS_FILE;
			jobConf.setCssFileURL(cssFileURL);

			//Move the organization image file from TMP folder to the definitive folder
			final File orgImageInitFile= new File(jobConf.getOrgImagePath());
			final String definitiveImgFileName = pageFolder + File.separator + "orgLogo.jpeg";
			final File definitiveImgFile = new File (definitiveImgFileName);
			Files.move(orgImageInitFile.toPath(), definitiveImgFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
			jobConf.setOrgImagePath(definitiveImgFileName);
			final String orgImageURL = pageURL + "/" + definitiveImgFile.getName();
			jobConf.setOrgImageURL(orgImageURL);

			success = true;
		} catch (Exception exception) {
			LOGGER.error(MessageCatalog._00035_FILE_ACCESS_FAILURE, exception);
		}
		return success;
	}
	
	/**
	 * It calculates the number of triples contained in the subsets of a dataset.
	 *
	 * @param sparqlEndpoint	the SPARQL endpoint of the dataset. 
	 * @param user				the user name for the SPARQl endpoint.
	 * @param password			the password for the SPARQl endpoint.
	 * @param subsetsList		the {@link eu.aliada.ckancreation.model.Subset}
	 *							that contains information of the graphs of the subset.  
	 * @return the number of triples contained in the subsets of the dataset.  					
	 * @since 2.0
	 */
	public int calculateDatasetNumTriples(final String sparqlEndpoint, final String user, final String password, final ArrayList<Subset> subsetsList) {
		int numTriples = 0;
		//Get subset graphs and get number of triples
		for (final Iterator<Subset> iterSubsets = subsetsList.iterator(); iterSubsets.hasNext();  ) {
			final Subset subset = iterSubsets.next();
			//Get number of triples of each subgraph
			final RDFStoreDAO rdfstoreDAO = new RDFStoreDAO();
			LOGGER.debug(MessageCatalog._00045_GETTING_NUM_TRIPLES, sparqlEndpoint, subset.getGraph(), user, password);
			subset.setGraphNumTriples(rdfstoreDAO.getNumTriples(sparqlEndpoint, subset.getGraph(), user, password));
			LOGGER.debug(MessageCatalog._00045_GETTING_NUM_TRIPLES, sparqlEndpoint, subset.getLinksGraph(), user, password);
			subset.setLinksGraphNumTriples(rdfstoreDAO.getNumTriples(sparqlEndpoint, subset.getLinksGraph(), user, password));
			numTriples = numTriples+ subset.getGraphNumTriples() + subset.getLinksGraphNumTriples();
		}
		return numTriples;
	}
	

	/**
	 * It setups the rewrite rules in Virtuoso for dereferencing the dataset URI-s.
	 *
	 * @param jobConf		the {@link eu.aliada.linkeddataserversetup.model.JobConfiguration}
	 *						that contains information to setup the rewrite rules in Virtuoso  
	 * @param db			The DDBB connection. 
	 * @return the {@link eu.aliada.linkedDataServerSetup.model.job} created.  					
	 * @since 1.0
	 */
	public Job setup(final JobConfiguration jobConf, final DBConnectionManager dbConn) {
		LOGGER.debug(MessageCatalog._00030_STARTING);
		//Update job start-date in DDBB
		dbConn.updateJobStartDate(jobConf.getId());
		//URLEncode and prepare some command parameters for Virtuoso Rewrite Rules
		LOGGER.debug(MessageCatalog._00037_ENCODE_PARAMS);
		final boolean encoded = encodeParams(jobConf);
		if(encoded) {
			//Execute global ISQL commands file for rewriting rules in Virtuoso
			final boolean success = executeDatasetIsqlCommands(jobConf);
			if (success) {
				//Get subset ISQL commands file for rewriting rules in Virtuoso and execute them
				for (final Iterator<Subset> iterSubsets = jobConf.getSubsets().iterator(); iterSubsets.hasNext();  ) {
					final Subset subset = iterSubsets.next();
					executeSubsetIsqlCommands(jobConf, subset);
				}
			}
			//Create default HTML page for the dataset and the domain
			createDatasetDefaultPage(jobConf, dbConn);
		} else {
			LOGGER.error(MessageCatalog._00039_INPUT_PARAMS_ERROR, jobConf.getId());
		}
		

		//Update job end_date of DDBB
		LOGGER.debug(MessageCatalog._00057_UPDATING_JOB_DDBB, jobConf.getId());
		dbConn.updateJobEndDate(jobConf.getId());
		final Job job = dbConn.getJob(jobConf.getId());
		LOGGER.debug(MessageCatalog._00041_STOPPED);
		return job;
	}

}
