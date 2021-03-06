// ALIADA - Automatic publication under Linked Data paradigm
//          of library and museum data
//
// Component: aliada-ckan-datahub-page-creation
// Responsible: ALIADA Consortium
package eu.aliada.ckancreation.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Dataset information.
 * 
 * @author Idoia Murua
 * @since 2.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Dataset {

	/** The dataset name. */
	private String name;
	/** The dataset id in CKAN. */
	private String id;
	/** The dataset author. */
	private String author;
	/** The dataset author. */
	@JsonProperty("author_email") 
	private String authorEmail;
	/** The dataset description. */
	private String notes;
	/** The dataset source URL. */
	@JsonProperty("url")
	private String sourceUrl;
	/** The organization owner of the dataset. */
	@JsonProperty("owner_org")
	private String ownerOrg;
	/** The dataset license Id. */
	@JsonProperty("license_id")
	private String licenseCKANId;
	/** The dataset state. */
	private String state;
	/** The number of triples of the dataset. */
	private int triples;
	/** The dataset version. */
	private String version;
	/** The tags associated to the dataset. */
	private ArrayList<Map<String, String>> tags = new ArrayList<Map<String, String>>();
	/** The extra attributes for the dataset. */
	private ArrayList<Map<String, String>> extras = new ArrayList<Map<String, String>>();

	/**
	 * Constructor.
	 * 
	 * @since 2.0
	 */
	public Dataset() {
	}

	/**
	 * Constructor.
	 * 
	 * @param name			the dataset name.
	 * @param id			the dataset id in CKAN.
	 * @param author		the dataset author.
	 * @param notes			the dataset description.
	 * @param sourceUrl		the dataset source URL.
	 * @param ownerOrg		the organization owner of the dataset.
	 * @param licenseCKANId	the dataset license id.
	 * @param state			the dataset state.
	 * @param triples		the number of triples in the dataset.
	 * @since 2.0
	 */
	public Dataset(final String name, final String id, final String author, final String notes, 
			final String sourceUrl, final String ownerOrg, final String licenseCKANId, 
			final String state, final int triples, final String authorEmail)
	{
		this.name = name.toLowerCase();
		this.id = id;
		this.author = author;
		this.authorEmail = authorEmail;
		this.notes = notes;
		this.sourceUrl = sourceUrl;
		this.ownerOrg = ownerOrg;
		this.licenseCKANId = licenseCKANId;
		this.state = state;
		this.triples = triples;
		final java.util.Date today = new java.util.Date();
		this.version = today.toString();
		//Set the tags associated to the dataset
		setTag("lod");
		setTag("publications");
		setTag("published-by-producer");
//		setTag("deref-vocab");
		setTag("no-proprietary-vocab");
		setTag("no-vocab-mappings");
		setTag("format-owl");
		setTag("format-frbroo");
		setTag("format-foaf");
		setTag("format-skos");
		setTag("format-wgs84");
		setTag("format-owltime");
		setTag("provenance-metadata");
		setTag("license-metadata");
	}
	
	/**
	 * Returns the dataset name.
	 * 
	 * @return The dataset name.
	 * @since 2.0
	 */
	public String getName() {
		return this.name;
	}
	/**
	 * Sets the dataset name.
	 * 
	 * @param name The dataset name.
	 * @since 2.0
	 */
	public void setName(final String name) {
		this.name = name.toLowerCase();
	}

	/**
	 * Returns the dataset id in CKAN.
	 * 
	 * @return The dataset id in CKAN.
	 * @since 2.0
	 */
	public String getId() {
		return this.id;
	}
	/**
	 * Sets the dataset id in CKAN.
	 * 
	 * @param id The dataset id in CKAN.
	 * @since 2.0
	 */
	public void setId(final String id) {
		this.id = id;
	}

	/**
	 * Returns the dataset author.
	 * 
	 * @return The dataset author.
	 * @since 2.0
	 */
	public String getAuthor() {
		return this.author;
	}
	/**
	 * Sets the dataset author.
	 * 
	 * @param author The dataset author.
	 * @since 2.0
	 */
	public void setAuthor(final String author) {
		this.author = author;
	}
	
	/**
	 * Returns the dataset author e-mail.
	 * 
	 * @return The dataset author e-mail.
	 * @since 2.0
	 */
	public String getAuthorEmail() {
		return this.authorEmail;
	}
	/**
	 * Sets the dataset author e-mail.
	 * 
	 * @param authorEmail The dataset author e-mail.
	 * @since 2.0
	 */
	public void setAuthorEmail(final String authorEmail) {
		this.authorEmail = authorEmail;
	}
	
	/**
	 * Returns the dataset description.
	 * 
	 * @return The dataset description.
	 * @since 2.0
	 */
	public String getNotes() {
		return this.notes;
	}
	/**
	 * Sets the dataset description.
	 * 
	 * @param description The dataset description.
	 * @since 2.0
	 */
	public void setNotes(final String notes) {
		this.notes = notes;
	}

	/**
	 * Returns the dataset source URL.
	 * 
	 * @return The dataset source URL.
	 * @since 2.0
	 */
	public String getSourceUrl() {
		return this.sourceUrl;
	}
	/**
	 * Sets the dataset source URL.
	 * 
	 * @param sourceUrl The dataset source URL.
	 * @since 2.0
	 */
	public void setSourceUrl(final String sourceUrl) {
		this.sourceUrl = sourceUrl;
	}

	/**
	 * Returns the organization owner of the dataset.
	 * 
	 * @return The organization owner of the dataset.
	 * @since 2.0
	 */
	public String getOwnerOrg() {
		return this.ownerOrg;
	}
	/**
	 * Sets the organization owner of the dataset.
	 * 
	 * @param ownerOrg The organization owner of the dataset.
	 * @since 2.0
	 */
	public void setOwnerOrg(final String ownerOrg) {
		this.ownerOrg = ownerOrg;
	}
	
	/**
	 * Returns the dataset license Id.
	 * 
	 * @return The dataset license Id.
	 * @since 2.0
	 */
	public String getLicenseCKANId() {
		return this.licenseCKANId;
	}
	/**
	 * Sets the dataset license Id.
	 * 
	 * @param licenseCKANId The dataset license Id.
	 * @since 2.0
	 */
	public void setLicenseCKANId(final String licenseCKANId) {
		this.licenseCKANId = licenseCKANId;
	}

	/**
	 * Returns the dataset state.
	 * 
	 * @return The dataset state.
	 * @since 2.0
	 */
	public String getState() {
		return this.state;
	}
	/**
	 * Sets the dataset state.
	 * 
	 * @param state The dataset state.
	 * @since 2.0
	 */
	public void setState(final String state) {
		this.state = state;
	}

	/**
	 * Returns the dataset version.
	 * 
	 * @return The dataset version.
	 * @since 2.0
	 */
	public String getVersion() {
		return this.version;
	}
	/**
	 * Sets the dataset version.
	 * 
	 * @param state The dataset version.
	 * @since 2.0
	 */
	public void setVersion(final String version) {
		this.version = version;
	}

	/**
	 * Returns the number of triples of the dataset.
	 * 
	 * @return The number of triples of the dataset.
	 * @since 2.0
	 */
	public int getTriples() {
		return this.triples;
	}
	/**
	 * Sets the number of triples of the dataset.
	 * 
	 * @param triples The number of triples of the dataset.
	 * @since 2.0
	 */
	public void setTriples(final int triples) {
		this.triples = triples;
	}

	/**
	 * Returns the tags associated to the dataset.
	 * 
	 * @return The tags associated to the dataset.
	 * @since 2.0
	 */
	public ArrayList<Map<String, String>> getTags() {
		return this.tags;
	}
	/**
	 * Set the tags associated to the dataset.
	 * 
	 * @param tags The tags associated to the dataset.
	 * @since 2.0
	 */
	public void setTags(final ArrayList<Map<String, String>> tags) {
		this.tags = tags;
	}
	/**
	 * Adds a tag associated to the dataset.
	 * 
	 * @param user The tag to add.
	 * @since 2.0
	 */
	public void addTag(final Map<String, String> tag) {
		tags.add(tag);
	}

	////////////////////////////////////////////
	/**
	 * Adds a tag associated to the dataset.
	 * 
	 * @param tagName The name of the tag.
	 * @since 2.0
	 */
	public void setTag(final String tagName) {
		final Map<String, String> tag = new HashMap<String, String>();
		tag.put("name", tagName);		
		this.tags.add(tag);
	}
	////////////////////////////////////////////

	/**
	 * Returns the extra attributes for the dataset.
	 * 
	 * @return The extra attributes for the dataset.
	 * @since 2.0
	 */
	public ArrayList<Map<String, String>> getExtras() {
		return this.extras;
	}
	/**
	 * Set the extra attributes for the dataset.
	 * 
	 * @param extras The extra attributes to set.
	 * @since 2.0
	 */
	public void setExtras(final ArrayList<Map<String, String>> extras) {
		this.extras = extras;
	}
	/**
	 * Adds a extra attribute for the dataset.
	 * 
	 * @param extra The extra attribute to add.
	 * @since 2.0
	 */
	public void addExtra(final Map<String, String> extra) {
		extras.add(extra);
	}
	////////////////////////////////////////////
	/**
	 * Adds a extra to the dataset.
	 * 
	 * @param extraKey		Key name.
	 * @param extraValue	Key value.
	 * @since 2.0
	 */
	public void setExtra(final String extraKey, final String extraValue) {
		final Map<String, String> extra = new HashMap<String, String>();
		extra.put("key", extraKey);		
		extra.put("value", extraValue);		
		this.extras.add(extra);
	}
	////////////////////////////////////////////
}
