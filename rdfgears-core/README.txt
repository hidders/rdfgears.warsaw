RDF Gears is a data integration framework for the Semantic Web. 

* DOWNLOAD
To download a tarball, go to https://bitbucket.org/feliksik/rdfgears/downloads
Mercurial users can do
	hg clone https://bitbucket.org/feliksik/rdfgears

* HACKING
The checkout/download is an Eclipse project that you can import.

* UNITTESTS
RDFGears includes a JUnit Testsuite. All relevant unittests are referenced by 
the class rdgears.tests.Test /test/ directory. 
You can run the unittests from Eclipse, or with the command
	ant Test

* RUNNING
First run 
	$ 'ant build'. 
Then you can execute a workflow with the command (currently *NIX only): 
	$ ./rdfgears -w path/to/workflow 

especially check out the ./rdfgears --help option.


* CHANGELOG
20th July 2011
- API: easy value creation now that ValueFactory provides static methods
- API: easy creation of modifyable records (support of put() method) with
    the method ValueFactory.createModifiableRecordValue(fiMapWithGroupField);
- API: easy creation of Double-literals with ValueFactory.createLiteralDouble(d)
- API: easy creation of a fieldMap with the various methods
    FieldIndexMapFactory.create(...)
- Engine: different output formats (currently 'xml' and 'informal') can be picked 
    with the --outputformat option
- Implementation: many performance optimization, amongst which are faster typed 
     literals (double)
- New functions: implementation of GroupBy function to allow nesting of sparql 
     results. 



Eric Feliksik
feliksik@gmail.com
