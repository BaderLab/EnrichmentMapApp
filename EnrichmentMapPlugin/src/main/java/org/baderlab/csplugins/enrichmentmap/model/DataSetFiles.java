package org.baderlab.csplugins.enrichmentmap.model;

public class DataSetFiles {

	//Input File names for any DataSet
	//GMT - gene set definition file
	private String GMTFileName = null;
	//Expression files
	private String expressionFileName = null;

	//Enrichment results files - data set 1
	private String enrichmentFileName1 = null;
	private String enrichmentFileName2 = null;

	//Dataset Rank files
	private String RankedFile = null;

	//Class files - can only be specified when loading in GSEA results with an rpt
	//class files are a specific type of file used in an GSEA analysis indicating which class
	//each column of the expression file belongs to.  It is used in the application to
	//colour the heading on the columns accroding to class or phenotype they belong to.
	private String classFile = null;

	//phenotypes associated with this set of files
	public static final String default_pheno1 = "UP";
	public static final String default_pheno2 = "DOWN";

	private String phenotype1 = default_pheno1;
	private String phenotype2 = default_pheno2;

	private String gseaHtmlReportFile = null;

	public String getGMTFileName() {
		return GMTFileName;
	}

	public void setGMTFileName(String gMTFileName) {
		GMTFileName = gMTFileName;
	}

	public String getExpressionFileName() {
		return expressionFileName;
	}

	public void setExpressionFileName(String expressionFileName) {
		this.expressionFileName = expressionFileName;
	}

	public String getEnrichmentFileName1() {
		return enrichmentFileName1;
	}

	public void setEnrichmentFileName1(String enrichmentFileName1) {
		this.enrichmentFileName1 = enrichmentFileName1;
	}

	public String getEnrichmentFileName2() {
		return enrichmentFileName2;
	}

	public void setEnrichmentFileName2(String enrichmentFileName2) {
		this.enrichmentFileName2 = enrichmentFileName2;
	}

	public String getRankedFile() {
		return RankedFile;
	}

	public void setRankedFile(String rankedFile) {
		RankedFile = rankedFile;
	}

	public String getClassFile() {
		return classFile;
	}

	public void setClassFile(String classFile) {
		this.classFile = classFile;
	}

	public String getGseaHtmlReportFile() {
		return gseaHtmlReportFile;
	}

	public void setGseaHtmlReportFile(String gseaHtmlReportFile) {
		this.gseaHtmlReportFile = gseaHtmlReportFile;
	}

	public String getPhenotype1() {
		return phenotype1;
	}

	public void setPhenotype1(String phenotype1) {
		this.phenotype1 = phenotype1;
	}

	public String getPhenotype2() {
		return phenotype2;
	}

	public void setPhenotype2(String phenotype2) {
		this.phenotype2 = phenotype2;
	}

	public void copy(DataSetFiles copy) {
		this.GMTFileName = copy.getGMTFileName();
		this.expressionFileName = copy.getExpressionFileName();
		this.enrichmentFileName1 = copy.getEnrichmentFileName1();
		this.enrichmentFileName2 = copy.getEnrichmentFileName2();
		this.classFile = copy.getClassFile();
		this.gseaHtmlReportFile = copy.getGseaHtmlReportFile();
		this.RankedFile = copy.getRankedFile();
		this.phenotype1 = copy.getPhenotype1();
		this.phenotype2 = copy.getPhenotype2();
	}


	public boolean isEmpty() {
		if(this.GMTFileName != null && !this.GMTFileName.equalsIgnoreCase(""))
			return false;
		if(this.expressionFileName != null && !this.expressionFileName.equalsIgnoreCase(""))
			return false;
		if(this.enrichmentFileName1 != null && !this.enrichmentFileName1.equalsIgnoreCase(""))
			return false;
		if(this.enrichmentFileName2 != null && !this.enrichmentFileName2.equalsIgnoreCase(""))
			return false;
		return true;
	}
}
