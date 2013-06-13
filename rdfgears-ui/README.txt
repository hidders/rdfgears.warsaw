Development Environment
--------------------------
  - Maven 3.0.3
  - Eclipse 3.7 with Google Plugin and maven plugin installed (GWT 2.4)
    https://developers.google.com/eclipse/docs/install-eclipse-3.7
   
Running and debugging
-------------------------

Running/debugging on development mode (Hosted mode) using maven
command:
  cd rdfgears-ui
  mvn gwt:run

This command will run the application with embedded web server and compile the code in realtime
(refresh the browser to update the changes)

reference: http://mojo.codehaus.org/gwt-maven-plugin/user-guide/hosted.html

Compiling and deploying
-----------------------
command:
  cd rdfgears-ui
  mvn clean package

This command will build RDFGears UI and the result will be:
  rdfgears-ui/target/rdfgears-ui-1.0-SNAPSHOT.war
  
The war file can be deployed on a web server.

Configuration file (rdf-gears-ui-config.xml)
--------------------------------------------
In order to run/debug the application, the configuration parameter have to be configured correctly.
the configuration file can be found in
- rdfgears-ui\src\main\webapp\WEB-INF (source folder)
or
- WEB-INF (compiled project)

configurations,
- BasePath
  The full path to a folder where the workflow files and node's file definition stored
  The folder must have a directory "data" with the following structure
  $BasePath\data\workflows (to store the workflow files)
  $basePath\data\operators (to store the operator definition files)
  $basePath\data\functions (to store the user function definition files)

- RDFGearsRestUrl
  URL to RDFGears-rest application to run the workflow file
  rdfgears-rest must be configured to have the same workflow path with rdfgears-ui ($BasePath\data\workflows)
  reference to configure rdfgears-rest : http://code.google.com/p/rdfgears/wiki/Install_RESTful_Interface
