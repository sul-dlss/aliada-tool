--------------------------------
  --Install Faceted Browser VAD Package
  --------------------------------
  vad_install('$ARGV[6]/fct_dav.vad', 0);
  --------------------------------
  --Upload Aliada ontology and MARC Codes List
  --------------------------------
  delete from db.dba.load_list;
  SPARQL CLEAR GRAPH  <http://aliada-project.eu/2014/aliada-ontology#>; 
  ld_dir ('$ARGV[6]', 'aliada-ontology.owl', 'http://aliada-project.eu/2014/aliada-ontology#');
  ld_dir ('$ARGV[6]', 'languages.rdf', 'http://id.loc.gov/vocabulary/languages');
  ld_dir ('$ARGV[6]', 'aliada_languages_spanish.nt', 'http://id.loc.gov/vocabulary/languages');
  ld_dir ('$ARGV[6]', 'countries.rdf', 'http://id.loc.gov/vocabulary/countries');
  ld_dir ('$ARGV[6]', 'gacs.rdf', 'http://id.loc.gov/vocabulary/geographicAreas');
  rdf_loader_run ();
  DB.DBA.RDF_LOAD_RDFXML (http_get ('http://www.w3.org/2002/07/owl.rdf'),'no', 'http://aliada-project.eu/2014/aliada-ontology#');
  DB.DBA.RDF_LOAD_RDFXML (http_get ('http://erlangen-crm.org/current/'),'no', 'http://aliada-project.eu/2014/aliada-ontology#');
  DB.DBA.RDF_LOAD_RDFXML (http_get ('http://erlangen-crm.org/efrbroo/'),'no', 'http://aliada-project.eu/2014/aliada-ontology#');
  --------------------------------
  --Create user aliada_dev for SPARQL_UPDATE point
  --------------------------------
  DB.DBA.USER_CREATE ("aliada_dev", "aliada", vector('SQL_ENABLE', 1, 'DAV_ENABLE', 1));
  GRANT SPARQL_UPDATE TO "aliada_dev";
  --------------------------------
  --Add resource Type (mime type) for downloading correctly the generated triples from CKAN Datahub
  --------------------------------
  INSERT INTO WS.WS.SYS_DAV_RES_TYPES (T_EXT, T_TYPE) VALUES ('gz', 'application/x-ntriples');
