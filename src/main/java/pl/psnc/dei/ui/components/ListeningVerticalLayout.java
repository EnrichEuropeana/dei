package pl.psnc.dei.ui.components;

import com.vaadin.flow.component.ClickNotifier;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class ListeningVerticalLayout extends VerticalLayout implements ClickNotifier<ListeningVerticalLayout> {

	public ListeningVerticalLayout() {
	}

	public ListeningVerticalLayout(Component... children) {
		super(children);
	}
}
