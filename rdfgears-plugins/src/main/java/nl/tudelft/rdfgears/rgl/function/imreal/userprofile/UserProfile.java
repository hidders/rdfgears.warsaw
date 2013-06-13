package nl.tudelft.rdfgears.rgl.function.imreal.userprofile;

import java.util.List;

public class UserProfile {
	private String uuid;
	private List<Dimension> dimensions;

	public UserProfile(String uuid, List<Dimension> dimensions) {
		super();
		this.uuid = uuid;
		this.dimensions = dimensions;
	}

	public String getUuid() {
		return uuid;
	}

	public List<Dimension> getDimensions() {
		return dimensions;
	}

}
