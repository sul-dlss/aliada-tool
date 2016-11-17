GRANT ALL PRIVILEGES ON * . * TO 'aliada'@'%' IDENTIFIED BY 'aliada' WITH GRANT OPTION MAX_QUERIES_PER_HOUR 0 MAX_CONNECTIONS_PER_HOUR 0 MAX_UPDATES_PER_HOUR 0 MAX_USER_CONNECTIONS 0 ;

use `aliada`;

INSERT INTO `organisation` (`organisationId`,`org_name`,`org_path`,`org_catalog_url`, `org_description`,`org_home_page`, `aliada_ontology`,`tmp_dir`,`linking_client_app_bin_dir`,
`linking_client_app_user`,`store_ip`,`store_sql_port`,`sql_login`,`sql_password`,`isql_command_path`,`isql_commands_file_dataset_default`,  `isql_commands_file_subset_default`,
`isql_commands_file_graph_dump`, `virtuoso_http_server_root`, `ckan_api_url`,`ckan_api_key`,`dataset_author`,`isql_commands_file_dataset_creation`) VALUES
(2,'Stanford','/opt/app/aliada/aliada-tool/upload','http://library.stanford.edu', 'Stanford University Libraries', 'http://www.stanford.edu/', 'http://aliada-project.eu/2014/aliada-ontology#', '/home/aliada/tmp', '/home/aliada/links-discovery/bin/','linking','localhost',1111,'dba','dba','/home/virtuoso/bin/isql','/home/aliada/linked-data-server/config/isql_rewrite_rules_dataset_default.sql', '/home/aliada/linked-data-server/config/isql_rewrite_rules_subset_default.sql', '/home/aliada/ckan-datahub-page-creation/config/dump_one_graph_nt.sql', '/home/virtuoso/var/lib/virtuoso/vsp', 'https://datahub.io/api/action', '****','Aliada Consortium','/home/aliada/bin/aliada_new_dataset.sql');
