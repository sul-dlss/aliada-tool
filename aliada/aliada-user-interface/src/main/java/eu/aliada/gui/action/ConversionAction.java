// ALIADA - Automatic publication under Linked Data paradigm
//          of library and museum data
//
// Component: aliada-user-interface
// Responsible: ALIADA Consortium

package eu.aliada.gui.action;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.struts2.ServletActionContext;

import com.opensymphony.xwork2.ActionSupport;

import eu.aliada.gui.log.MessageCatalog;
import eu.aliada.gui.model.FileWork;
import eu.aliada.gui.rdbms.DBConnectionManager;
import eu.aliada.shared.log.Log;
import eu.aliada.shared.rdfstore.RDFStoreDAO;

/**
 * This class help in the process of the xml files importation.
 * @author iosa
 * @version $Revision: 1.1 $
 * @since 1.0
 */
public class ConversionAction extends ActionSupport {
    private static final int NOTEMPLATESELECTED = -1;
    private FileWork importedFile;
    private HashMap<Integer, String> templates;
    private HashMap<Integer, String> graphs;
    private HashMap<String, Boolean> tags;
    private List selectedTags = new ArrayList();
    private String selectedTemplate;
    private String templateName;
    private String templateDescription;
    private boolean showAddTemplateForm;
    private boolean showEditTemplateForm;
    private int showCheckButton;
    private int showRdfizerButton;
    private boolean areTemplates;
    private String selectedGraph;
    private String graphToClean;
    
    private final Log logger = new Log(ConversionAction.class);
    
    public String execute() {
        boolean rdfizerFinished = false;
        HttpSession session = ServletActionContext.getRequest().getSession();
        setImportedFile((FileWork) session.getAttribute("importedFile")); 
        if(session.getAttribute("rdfizerFinished") != null){
            rdfizerFinished = (boolean) session.getAttribute("rdfizerFinished");
        }
        if (rdfizerFinished) {
            setShowCheckButton(0);
            setShowRdfizerButton(1);
        }else if (session.getAttribute("rdfizerJobId") == null) {
            setShowCheckButton(0);
            setShowRdfizerButton(1);
        } else {
            setShowCheckButton(1);
            setShowRdfizerButton(0); 
        }
        getGraphsDb();
        return getTemplatesDb();
    }
    /**
     * Calls to the rdfizer process
     * @return
     * @see
     * @since 1.0
     */
    public String rdfize() {
        HttpSession session = ServletActionContext.getRequest().getSession();
        setImportedFile((FileWork) session.getAttribute("importedFile"));
        importedFile.setTemplate(getSelectedTemplate());
        importedFile.setGraph(getGraphUri(getSelectedGraph()));
        String format = null;
        try {
            format = getFormat(importedFile.getProfile());
            Connection connection = null;
            connection = new DBConnectionManager().getConnection();
            Statement statement = connection.createStatement();
            ResultSet rs = statement
                    .executeQuery("select aliada_ontology,sparql_endpoint_uri,sparql_endpoint_login,sparql_endpoint_password,graph_uri,dataset_base from organisation o INNER JOIN graph g ON o.organisationId=g.organisationId WHERE g.graph_uri='"+importedFile.getGraph()+"'");
            if (rs.next()) {
                PreparedStatement preparedStatement = connection
                        .prepareStatement(
                                "INSERT INTO aliada.rdfizer_job_instances (datafile,format,namespace,graph_name,aliada_ontology,sparql_endpoint_uri,sparql_endpoint_login,sparql_endpoint_password) VALUES(?,?,?,?,?,?,?,?)",
                                PreparedStatement.RETURN_GENERATED_KEYS);
                preparedStatement.setString(1, importedFile.getFile().getAbsolutePath());
                preparedStatement.setString(2, format);
                preparedStatement.setString(3, rs.getString("dataset_base"));
                preparedStatement.setString(4, rs.getString("graph_uri"));
                preparedStatement.setString(5, rs.getString("aliada_ontology"));
                preparedStatement.setString(6, rs.getString("sparql_endpoint_uri"));
                preparedStatement.setString(7, rs.getString("sparql_endpoint_login"));
                preparedStatement.setString(8, rs.getString("sparql_endpoint_password"));
                preparedStatement.executeUpdate();
                ResultSet rs2 = preparedStatement.getGeneratedKeys();
                int addedId = 0;
                if (rs2.next()) {
                    addedId = (int) rs2.getInt(1);
                }
                try {
                    enableRdfizer();
                    createJob(addedId);
                    session.setAttribute("rdfizerFinished", false);
                    session.setAttribute("importedFile", importedFile);
                } catch (IOException e) {
                    logger.error(MessageCatalog._00012_IO_EXCEPTION,e);
                    getTemplatesDb();
                    getGraphsDb();
                    rs2.close();
                    preparedStatement.close();
                    connection.close();
                    return ERROR;
                }
                rs2.close();
                preparedStatement.close();
                rs.close();
                statement.close();
                connection.close();
                session.setAttribute("rdfizerJobId", addedId);
                setShowCheckButton(1);
                getGraphsDb();
                return getTemplatesDb();                
            }
        } catch (SQLException e) {
            logger.error(MessageCatalog._00011_SQL_EXCEPTION,e);
            getGraphsDb();
            getTemplatesDb();
            return ERROR;
        }
        getGraphsDb();
        return getTemplatesDb(); 
    }
    /**
     * Clear the graph
     * @return
     * @see
     * @since 1.0
     */
    public String cleanGraph(){
        Connection connection = null;
        connection = new DBConnectionManager().getConnection();
        Statement statement;
        try {
            statement = connection.createStatement();
            ResultSet rs = statement
                    .executeQuery("select sparql_endpoint_uri,sparql_endpoint_login,sparql_endpoint_password,graph_uri from organisation o INNER JOIN graph g ON o.organisationId=g.organisationId WHERE g.graphId='"+graphToClean+"'");
            if (rs.next()) {
                RDFStoreDAO store = new RDFStoreDAO();
                if(!store.clearGraphBySparql(rs.getString("sparql_endpoint_uri"), rs.getString("sparql_endpoint_login"), rs.getString("sparql_endpoint_password"), rs.getString("graph_uri"))){
                    logger.error(MessageCatalog._00032_CONVERSION_ERROR_CLEARING_GRAPH);
                    rs.close();
                    statement.close();
                    connection.close();
                    getGraphsDb();
                    getTemplatesDb();
                    return ERROR;
                }
                logger.error(MessageCatalog._00034_CONVERSION_GRAPH_CLEANED);
                addActionMessage(getText("conversion.graphCleaned")+rs.getString("graph_uri"));
            }
            rs.close();
            statement.close();
            connection.close();
            return execute(); 
        } catch (SQLException e) {
            logger.error(MessageCatalog._00011_SQL_EXCEPTION,e);
            getGraphsDb();
            getTemplatesDb();
            return ERROR;
        }
        
    }
    /**
     * Gets the format of the file
     * @return
     * @throws SQLException
     * @see
     * @since 1.0
     */
    private String getFormat(String profile) throws SQLException {
        Connection connection = null;
        String format = null;
        connection = new DBConnectionManager().getConnection();
        Statement statement = connection.createStatement();
        ResultSet rs = statement
                .executeQuery("select metadata_name from t_metadata_scheme JOIN profile ON t_metadata_scheme.metadata_code=profile.metadata_scheme_code WHERE profile.profile_name= '"
                        + profile+"'");
        if (rs.next()) {
            format = rs.getString(1);
        }
        rs.close();
        statement.close();
        connection.close();
        return format;
    }

    /**
     * Enables the RDFizer
     * @throws IOException
     * @see
     * @since 1.0
     */
    private void enableRdfizer() throws IOException {
        URL url = new URL("http://aliada:8080/aliada-rdfizer-1.0/enable");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("PUT");
        if (conn.getResponseCode() != HttpURLConnection.HTTP_NO_CONTENT) {
            throw new RuntimeException("Failed : HTTP error code : "
                    + conn.getResponseCode());
        }
        logger.debug(MessageCatalog._00030_CONVERSION_RDFIZE_ENABLE);
        conn.disconnect();
    }

    /**
     * Creates a job for the RDFizer
     * @param addedId
     * @throws IOException
     * @see
     * @since 1.0
     */
    private void createJob(int addedId) throws IOException {
        URL url = new URL("http://aliada:8080/aliada-rdfizer-1.0/jobs/" + addedId);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("PUT");
        if (conn.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
            throw new RuntimeException("Failed : HTTP error code : "
                    + conn.getResponseCode());
        }
        logger.debug(MessageCatalog._00031_CONVERSION_RDFIZE_JOB);
        setShowRdfizerButton(0);
        conn.disconnect();
    }
    
    /**
     * Gets the available templates from the DB
     * @return
     * @see
     * @since 1.0
     */
    public String getTemplatesDb() {
        Connection connection = null;
        try {
            connection = new DBConnectionManager().getConnection();
            Statement statement = connection.createStatement();
            ResultSet rs = statement
                    .executeQuery("select * from aliada.template");
            templates = new HashMap<Integer, String>();
            while (rs.next()) {
                templates.put(rs.getInt("template_id"),
                        rs.getString("template_name"));
            }
            rs.close();
            statement.close();
            connection.close();
            if(templates.isEmpty()){
                setAreTemplates(false);
            }
            else{
                setAreTemplates(true);
            }
        } catch (SQLException e) {
            logger.error(MessageCatalog._00011_SQL_EXCEPTION,e);
            return ERROR;
        }
        return SUCCESS;
    }
    /**
     * Gets the available graphs from the DB
     * @return
     * @see
     * @since 1.0
     */
    public String getGraphsDb() {
        String username = (String) ServletActionContext.getRequest().getSession().getAttribute("logedUser");
        Connection connection = null;
        try {
            connection = new DBConnectionManager().getConnection();
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT graphId, graph_uri FROM graph g INNER JOIN user u ON g.organisationId = u.organisationId WHERE u.user_name='"+username+"';");
            graphs = new HashMap<Integer, String>();
            while (rs.next()) {
                graphs.put(rs.getInt("graphId"),
                        rs.getString("graph_uri"));
            }
            rs.close();
            statement.close();
            connection.close();
        } catch (SQLException e) {
            logger.error(MessageCatalog._00011_SQL_EXCEPTION,e);
            return ERROR;
        }
        return SUCCESS;
    }
    
    /**
     * Gets the graph uri from a graph code
     * @return
     * @see
     * @since 1.0
     */
    public String getGraphUri(String graphCode) {
        Connection connection = null;
        String graphUri = "";
        try {
            connection = new DBConnectionManager().getConnection();
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT graph_uri FROM graph WHERE graphId='"+graphCode+"';");
            if (rs.next()) {
                graphUri = rs.getString("graph_uri");
            }
            rs.close();
            statement.close();
            connection.close();
        } catch (SQLException e) {
            logger.error(MessageCatalog._00011_SQL_EXCEPTION,e);
            return "";
        }
        return graphUri;
    }

    /**
     * Displays the template adding form
     * @return
     * @see
     * @since 1.0
     */
    public String showAddTemplate() {
        getTemplatesDb();
        getTagsDb(NOTEMPLATESELECTED);
        setShowAddTemplateForm(true);
        return SUCCESS;
    }

    /**
     * Adds a new template to the DB
     * @return
     * @see
     * @since 1.0
     */
    public String addTemplate() {
        Connection connection = null;
        int id;
        try {
            connection = new DBConnectionManager().getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "INSERT INTO template VALUES (default,'" + this.templateName
                            + "', '" + this.templateDescription + "')",
                    PreparedStatement.RETURN_GENERATED_KEYS);
            preparedStatement.executeUpdate();
            ResultSet rs = preparedStatement.getGeneratedKeys();
            int idTemplate = 0;
            if (rs.next()) {
                idTemplate = (int) rs.getInt(1);
            }
            preparedStatement.close();
            Statement statement = connection.createStatement();
            Iterator iterator = selectedTags.iterator();
            while (iterator.hasNext()) {
                statement = connection.createStatement();
                statement
                        .executeUpdate("INSERT INTO template_xml_tag VALUES ('"
                                + idTemplate + "', '" + iterator.next() + "')");
                statement.close();
            }
            connection.close();
            setShowAddTemplateForm(false);
            addActionMessage(getText("template.save.ok"));
            logger.debug(MessageCatalog._00060_CONVERSION_TEMPLATE_ADDED);
            getTemplatesDb();
            return SUCCESS;
        } catch (SQLException e) {
            logger.error(MessageCatalog._00011_SQL_EXCEPTION,e);
            getTemplatesDb();
            return ERROR;
        }
    }

    /**
     * Deletes a template from the DB
     * @return
     * @see
     * @since 1.0
     */
    public String deleteTemplate() {
        Connection connection = null;
        try {
            connection = new DBConnectionManager().getConnection();
            Statement statement = connection.createStatement();
            statement
                    .executeUpdate("DELETE tags.* FROM template_xml_tag tags INNER JOIN template temp ON tags.template_id=temp.template_id  WHERE temp.template_name='"
                            + getSelectedTemplate() + "'");
            statement.close();
            statement = connection.createStatement();
            int correct = statement
                    .executeUpdate("DELETE FROM aliada.template WHERE template_name='"
                            + getSelectedTemplate() + "'");
            statement.close();
            connection.close();
            if(correct==0){
                addActionError(getText("template.not.selected"));                
            }
            else{
                addActionMessage(getText("template.delete.ok"));
            }
        } catch (SQLException e) {
            getTemplatesDb();
            logger.error(MessageCatalog._00011_SQL_EXCEPTION,e);
            return ERROR;
        }
        getTemplatesDb();
        return SUCCESS;
    }

    /**
     * Displays the form to edit the template
     * @return
     * @see
     * @since 1.0
     */
    public String showEditTemplate() {
        Connection connection = null;
        try {
            connection = new DBConnectionManager().getConnection();
            Statement statement = connection.createStatement();
            ResultSet rs = statement
                    .executeQuery("select * from aliada.template where template_name='"
                            + this.selectedTemplate + "'");
            if (rs.next()) {
                int idTemplate = rs.getInt("template_id");
                this.templateName = rs.getString("template_name");
                this.templateDescription = rs.getString("template_description");
                statement.close();
                rs.close();
                connection.close();
                getTemplatesDb();
                getTagsDb(idTemplate);
                setShowEditTemplateForm(true);
                ServletActionContext.getRequest().getSession()
                        .setAttribute("selectedTemplateId", idTemplate);
                return SUCCESS;
            } else {
                addActionError(getText("template.not.selected"));
                statement.close();
                rs.close();
                connection.close();
                getTemplatesDb();
                getTagsDb(NOTEMPLATESELECTED);
                return ERROR;
            }
        } catch (SQLException e) {
            logger.error(MessageCatalog._00011_SQL_EXCEPTION,e);
            getTemplatesDb();
            getTagsDb(NOTEMPLATESELECTED);
            return ERROR;
        }
    }

    /**
     * Updates a existing template
     * @return
     * @see
     * @since 1.0
     */
    public String editTemplate() {
        Connection connection = null;
        int idTemplate = (int) ServletActionContext.getRequest().getSession()
                .getAttribute("selectedTemplateId");
        try {
            connection = new DBConnectionManager().getConnection();
            Statement statement = connection.createStatement();
            statement
                    .executeUpdate("UPDATE template set template_description='"
                            + this.templateDescription + "' where template_id='"
                            + idTemplate + "'");
            statement.close();
            statement = connection.createStatement();
            statement
                    .executeUpdate("DELETE FROM aliada.template_xml_tag WHERE template_id="
                            + idTemplate);
            statement.close();
            Iterator iterator = selectedTags.iterator();
            while (iterator.hasNext()) {
                statement = connection.createStatement();
                statement
                        .executeUpdate("INSERT IGNORE INTO template_xml_tag VALUES ('"
                                + idTemplate + "', '" + iterator.next() + "')");
                statement.close();
            }
            ServletActionContext.getRequest().getSession()
                    .removeAttribute("selectedTemplateId");
            connection.close();
            addActionMessage(getText("template.save.ok"));
            getTemplatesDb();
        } catch (SQLException e) {
            logger.error(MessageCatalog._00011_SQL_EXCEPTION,e);
            setShowEditTemplateForm(false);
            return ERROR;
        }
        setShowEditTemplateForm(false);
        return SUCCESS;
    }

    /**
     * Gets teh available tags from the DB
     * @param templateId
     * @see
     * @since 1.0
     */
    private void getTagsDb(int templateId) {
        Connection connection = null;
        try {
            connection = new DBConnectionManager().getConnection();
            Statement statement = connection.createStatement();
            ResultSet rs = statement
                    .executeQuery("select * from aliada.xml_tag");
            List tagNames = new ArrayList<String>();
            while (rs.next()) {
                tagNames.add(rs.getString("xml_tag_id"));
            }
            rs.close();
            statement.close();
            tags = new HashMap<String, Boolean>();
            Iterator iterator = tagNames.iterator();
            if (templateId != NOTEMPLATESELECTED) {
                statement = connection.createStatement();
                rs = statement
                        .executeQuery("select xml_tag_id from aliada.template_xml_tag WHERE template_id="
                                + templateId);
                List tagsChecked = new ArrayList<String>();
                while (rs.next()) {
                    tagsChecked.add(rs.getString(1));
                }
                while (iterator.hasNext()) {
                    String listTagName = (String) iterator.next();
                    if (tagsChecked.contains(listTagName)) {
                        tags.put(listTagName, true);
                    } else {
                        tags.put(listTagName, false);
                    }
                }
            } else {
                while (iterator.hasNext()) {
                    tags.put((String) iterator.next(), false);
                }
            }
            rs.close();
            statement.close();
            connection.close();
        } catch (SQLException e) {
            logger.error(MessageCatalog._00011_SQL_EXCEPTION,e);
        }
    }

    /**
     * @return Returns the templates.
     * @exception
     * @since 1.0
     */
    public HashMap<Integer, String> getTemplates() {
        return templates;
    }

    /**
     * @param templates
     *            The templates to set.
     * @exception
     * @since 1.0
     */
    public void setTemplates(HashMap<Integer, String> templates) {
        this.templates = templates;
    }

    /**
     * @return Returns the importedFile.
     * @exception
     * @since 1.0
     */
    public FileWork getImportedFile() {
        return importedFile;
    }

    /**
     * @param importedFile
     *            The importedFile to set.
     * @exception
     * @since 1.0
     */
    public void setImportedFile(FileWork importedFile) {
        this.importedFile = importedFile;
    }

    /**
     * @return Returns the selectedTemplate.
     * @exception
     * @since 1.0
     */
    public String getSelectedTemplate() {
        return selectedTemplate;
    }

    /**
     * @param selectedTemplate
     *            The selectedTemplate to set.
     * @exception
     * @since 1.0
     */
    public void setSelectedTemplate(String selectedTemplate) {
        this.selectedTemplate = selectedTemplate;
    }

    /**
     * @return Returns the templateName.
     * @exception
     * @since 1.0
     */
    public String getTemplateName() {
        return templateName;
    }

    /**
     * @param templateName
     *            The templateName to set.
     * @exception
     * @since 1.0
     */
    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    /**
     * @return Returns the templateDescription.
     * @exception
     * @since 1.0
     */
    public String getTemplateDescription() {
        return templateDescription;
    }

    /**
     * @param templateDescription
     *            The templateDescription to set.
     * @exception
     * @since 1.0
     */
    public void setTemplateDescription(String templateDescription) {
        this.templateDescription = templateDescription;
    }

    /**
     * @return Returns the showAddTemplateForm.
     * @exception
     * @since 1.0
     */
    public boolean isShowAddTemplateForm() {
        return showAddTemplateForm;
    }

    /**
     * @param showAddTemplateForm
     *            The showAddTemplateForm to set.
     * @exception
     * @since 1.0
     */
    public void setShowAddTemplateForm(boolean showAddTemplateForm) {
        this.showAddTemplateForm = showAddTemplateForm;
    }

    /**
     * @return Returns the showEditTemplateForm.
     * @exception
     * @since 1.0
     */
    public boolean isShowEditTemplateForm() {
        return showEditTemplateForm;
    }

    /**
     * @param showEditTemplateForm
     *            The showEditTemplateForm to set.
     * @exception
     * @since 1.0
     */
    public void setShowEditTemplateForm(boolean showEditTemplateForm) {
        this.showEditTemplateForm = showEditTemplateForm;
    }

    /**
     * @return Returns the tags.
     * @exception
     * @since 1.0
     */
    public HashMap<String, Boolean> getTags() {
        return tags;
    }

    /**
     * @param tags
     *            The tags to set.
     * @exception
     * @since 1.0
     */
    public void setTags(HashMap<String, Boolean> tags) {
        this.tags = tags;
    }

    /**
     * @return Returns the showCheckButton.
     * @exception
     * @since 1.0
     */
    public int getShowCheckButton() {
        return showCheckButton;
    }

    /**
     * @param showCheckButton
     *            The showCheckButton to set.
     * @exception
     * @since 1.0
     */
    public void setShowCheckButton(int showCheckButton) {
        this.showCheckButton = showCheckButton;
    }

    /**
     * @return Returns the selectedTags.
     * @exception
     * @since 1.0
     */
    public List getSelectedTags() {
        return selectedTags;
    }

    /**
     * @param selectedTags
     *            The selectedTags to set.
     * @exception
     * @since 1.0
     */
    public void setSelectedTags(List selectedTags) {
        this.selectedTags = selectedTags;
    }

    /**
     * @return Returns the showRdfizerButton.
     * @exception
     * @since 1.0
     */
    public int getShowRdfizerButton() {
        return showRdfizerButton;
    }

    /**
     * @param showRdfizerButton The showRdfizerButton to set.
     * @exception
     * @since 1.0
     */
    public void setShowRdfizerButton(int showRdfizerButton) {
        this.showRdfizerButton = showRdfizerButton;
    }

    /**
     * @return Returns the areTemplates.
     * @exception
     * @since 1.0
     */
    public boolean isAreTemplates() {
        return areTemplates;
    }

    /**
     * @param areTemplates The areTemplates to set.
     * @exception
     * @since 1.0
     */
    public void setAreTemplates(boolean areTemplates) {
        this.areTemplates = areTemplates;
    }
    /**
     * @return Returns the graphs.
     * @exception
     * @since 1.0
     */
    public HashMap<Integer, String> getGraphs() {
        return graphs;
    }
    /**
     * @param graphs The graphs to set.
     * @exception
     * @since 1.0
     */
    public void setGraphs(HashMap<Integer, String> graphs) {
        this.graphs = graphs;
    }
    /**
     * @return Returns the selectedGraph.
     * @exception
     * @since 1.0
     */
    public String getSelectedGraph() {
        return selectedGraph;
    }
    /**
     * @param selectedGraph The selectedGraph to set.
     * @exception
     * @since 1.0
     */
    public void setSelectedGraph(String selectedGraph) {
        this.selectedGraph = selectedGraph;
    }
    /**
     * @return Returns the graphToClean.
     * @exception
     * @since 1.0
     */
    public String getGraphToClean() {
        return graphToClean;
    }
    /**
     * @param graphToClean The graphToClean to set.
     * @exception
     * @since 1.0
     */
    public void setGraphToClean(String graphToClean) {
        this.graphToClean = graphToClean;
    }

}
