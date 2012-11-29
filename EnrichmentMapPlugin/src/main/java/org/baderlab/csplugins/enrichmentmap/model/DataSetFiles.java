package org.baderlab.csplugins.enrichmentmap.model;

public class DataSetFiles {

	//Input File names for any DataSet
    //GMT - gene set definition file
    private String GMTFileName;
    //Expression files
    private String expressionFileName;

    //Enrichment results files - data set 1
    private String enrichmentFileName1;
    private String enrichmentFileName2;

    //Dataset Rank files
    private String RankedFile = null;

    //Class files - can only be specified when loading in GSEA results with an rpt
    //class files are a specific type of file used in an GSEA analysis indicating which class
    //each column of the expression file belongs to.  It is used in the application to
    //colour the heading on the columns accroding to class or phenotype they belong to.
    private String classFile;

    //class file designations that were loaded in from a session file.
    //need a temporary place for these class definition as
    private String[] temp_class1 = null;
    
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

	public String[] getTemp_class1() {
		return temp_class1;
	}

	public void setTemp_class1(String[] temp_class1) {
		this.temp_class1 = temp_class1;
	}

	public String getGseaHtmlReportFile() {
		return gseaHtmlReportFile;
	}

	public void setGseaHtmlReportFile(String gseaHtmlReportFile) {
		this.gseaHtmlReportFile = gseaHtmlReportFile;
	}

	public void copy(DataSetFiles copy){
		this.GMTFileName = copy.getGMTFileName();
		this.expressionFileName = copy.getExpressionFileName();
		this.enrichmentFileName1 = copy.getEnrichmentFileName1();
		this.enrichmentFileName2 = copy.getEnrichmentFileName2();
		this.classFile = copy.getClassFile();
		this.temp_class1 = copy.getTemp_class1();
		this.gseaHtmlReportFile = copy.getGseaHtmlReportFile();
		this.RankedFile = copy.getRankedFile();
	}
	
	public String toString(String dataset){
		StringBuffer paramVariables = new StringBuffer();
		//file names
        paramVariables.append(dataset+ "%" + this.getClass().getSimpleName() + "%GMTFileName\t" + GMTFileName + "\n");
        paramVariables.append(dataset+ "%" +this.getClass().getSimpleName() + "%expressionFileName\t" + expressionFileName + "\n");

        //TODO fix typo in field
        paramVariables.append(dataset+ "%" +this.getClass().getSimpleName() + "%enrichmentFileName1\t" + enrichmentFileName1 + "\n");//TODO: fix Typo and take care of legacy issue!
        paramVariables.append(dataset+ "%" +this.getClass().getSimpleName() + "%enrichmentFileName2\t" + enrichmentFileName2 + "\n");

        paramVariables.append(dataset+ "%" +this.getClass().getSimpleName() + "%gseaHtmlReportFileDataset\t" + gseaHtmlReportFile + "\n");        
        paramVariables.append(dataset+ "%" +this.getClass().getSimpleName() + "%classFile\t" + classFile  + "\n");
        paramVariables.append(dataset+ "%" +this.getClass().getSimpleName() + "%RankedFile\t" + RankedFile + "\n");
		return paramVariables.toString();
	}
	
	public boolean isEmpty(){
		if(this.GMTFileName != null && !this.GMTFileName.equalsIgnoreCase(""))
			return false;
		if(this.expressionFileName != null && !this.expressionFileName.equalsIgnoreCase(""))
			return false;
		if(this.enrichmentFileName1!= null && ! this.enrichmentFileName1.equalsIgnoreCase(""))
			return false;
		if(this.enrichmentFileName2!= null && ! this.enrichmentFileName2.equalsIgnoreCase(""))
			return false;
		return true;
	}
}
