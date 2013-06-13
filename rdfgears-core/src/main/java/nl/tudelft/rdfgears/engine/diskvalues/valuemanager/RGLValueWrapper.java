package nl.tudelft.rdfgears.engine.diskvalues.valuemanager;

import java.io.Serializable;

import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;

public class RGLValueWrapper implements Serializable {
	private RGLValue rglValue;
	private boolean everDumped;
	
	public RGLValueWrapper(RGLValue rglValue, boolean everDumped) {
		this.rglValue = rglValue;
		this.everDumped = everDumped;
	}

	public RGLValue getRglValue() {
		return rglValue;
	}

	public void setRglValue(RGLValue rglValue) {
		this.rglValue = rglValue;
	}

	public boolean isEverDumped() {
		return everDumped;
	}

	public void setEverDumped(boolean everDumped) {
		this.everDumped = everDumped;
	}
	
}
