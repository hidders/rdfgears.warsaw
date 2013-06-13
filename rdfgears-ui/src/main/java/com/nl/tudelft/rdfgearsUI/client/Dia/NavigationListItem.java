package com.nl.tudelft.rdfgearsUI.client.Dia;

import com.google.gwt.dom.client.Element;

public abstract class NavigationListItem {
	abstract String getName();
	abstract String getId();
	abstract String getDesc();
	abstract Element getElement();
	abstract void enableEventHandler();
	abstract void setVisible(boolean v);
}
