package nl.tudelft.rdfgears.rgl.function.imreal.userprofile;

import java.util.List;

public class Dimension {
	private String name;

	private List<DimensionEntry> dimensionEntries;

	public Dimension(String name, List<DimensionEntry> dimensionEntries) {
		super();
		this.name = name;
		this.dimensionEntries = dimensionEntries;
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		for(DimensionEntry de : dimensionEntries)
		{
			if(sb.length()>0)
				sb.append("\n");
			sb.append("ENTRY (");
			sb.append(de.topic);
			sb.append(", ");
			sb.append(de.value);
			sb.append(")");
		}
		return sb.toString();
	}

	public String getName() {
		return name;
	}

	public List<DimensionEntry> getDimensionEntries() {
		return dimensionEntries;
	}

	public static class DimensionEntry {
		private String topic;
		private String value;

		public DimensionEntry(String topic, String value) {
			this.topic = topic;
			this.value = value;
		}

		public String getTopic() {
			return topic;
		}

		public String getValue() {
			return value;
		}

	}
}