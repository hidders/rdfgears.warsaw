package nl.tudelft.rdfgears.engine;

import uk.co.flamingpenguin.jewel.cli.Option;


public interface CmdOptions {
	
	  @Option(shortName="w", longName="workflow", description="Path to the workflow to be executed")
	  String getWorkflowName();
	  boolean isWorkflowName();

	  @Option(shortName="t", longName="typecheck-only", description="Do not execute, only typecheck")
	  boolean getTypecheckOnly();


	  @Option(helpRequest = true, description="show this help message")
	  boolean getHelp();
	  
	  @Option(longName="disable-optimizer", description="Disable the workflow optimizer")
	  boolean getDisableOptimizer();
	  
	  @Option(longName="workflow-path", description="List of ':' separated paths where the \n\t\t(nested) workflows can be found. Default: "+Config.DEFAULT_WORKFLOW_PATHLIST)
	  String getWorkflowPathList();
	  boolean isWorkflowPathList();

	  @Option(longName="outputformat", description="The format for the RGL output data [xml|informal|none]. Default: "+Config.DEFAULT_RGL_SERIALIZATION_FORMAT)
	  String getOutputFormat();
	  boolean isOutputFormat();
	  
	  @Option(shortName="d", longName="debug-level", description="The log4j debug level \n\t\t(DEBUG/INFO/WARN/ERROR/OFF etc)")
  	  String getDebugLevel();
	  boolean isDebugLevel();

	  @Option(longName="server", description="Start RDF Gears as a RESTful HTTP Webservice")
  	  boolean getServer();
	  
	  @Option(longName="port", description="Port for the server")
	  boolean isPort();
	  int getPort();

	  @Option(longName="diskBased", description="Use disk based backend. Only recomended for really large data.")
	  boolean isDiskBased();
}
