package eu.aliada.rdfizer.pipeline.format.marc.frbr.cluster;

/**
 * A cluster entry.
 * 
 * @author Andrea Gazzarini
 * @since 1.0
 */
public final class ClusterEntry {
	public final String heading;
	public final boolean preferred;
	public final String id;
	public final String viafId;
	
	/**
	 * Builds a new cluster entry.
	 * 
	 * @param heading the heading text.
	 * @param preferred indicate that is a preferred form.
	 * @param id the heading identifier.
	 * @param viafId (optional) the VIAF identifier.
	 */
	public ClusterEntry(final String heading, boolean preferred, String id, String viafId) {
		this.heading = heading;
		this.preferred = preferred;
		this.id = id;
		this.viafId = viafId;
	}

	public String getHeading() {
		return heading;
	}

	public boolean isPreferred() {
		return preferred;
	}

	public String getId() {
		return id;
	}

	public String getViafId() {
		return viafId;
	}
}
