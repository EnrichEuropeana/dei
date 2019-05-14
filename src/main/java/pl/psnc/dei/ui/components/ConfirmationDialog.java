package pl.psnc.dei.ui.components;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * Simple confirmation dialog.
 */
public class ConfirmationDialog extends Dialog {

	private Label title;
	private VerticalLayout content;
	private Button confirm;

	/**
	 * Creates new empty confirmation dialog.
	 */
	public ConfirmationDialog() {
		setMaxWidth("300px");
		createHeader();
		createContent();
		createFooter();
	}

	/**
	 * Creates new confirmation dialog and sets basic content.
	 *
	 * @param title dialog title
	 * @param contentText main text of the dialog
	 * @param listener action for 'Confirm' button
	 */
	public ConfirmationDialog(String title, String contentText, ComponentEventListener listener) {
		this();
		setTitle(title);
		addContent(contentText);
		addConfirmationListener(listener);
	}

	/**
	 * Sets title of the dialog
	 *
	 * @param title title text
	 */
	public void setTitle(String title) {
		this.title.setText(title);
	}

	/**
	 * Adds new text component to dialog
	 *
	 * @param contentText content text
	 */
	public void addContent(String contentText) {
		Label text = new Label(contentText);
		text.setMaxWidth("500px");
		content.add(text);
	}

	/**
	 * Sets 'Confirm' button action
	 *
	 * @param listener action
	 */
	public void addConfirmationListener(ComponentEventListener listener) {
		confirm.addClickListener(listener);
	}

	private void createHeader() {
		this.title = new Label();
		this.title.setClassName("confirmation-dialog-title");
		Button close = new Button();
		close.setIcon(VaadinIcon.CLOSE.create());
		close.addClickListener(buttonClickEvent -> close());

		HorizontalLayout header = new HorizontalLayout();
		header.add(this.title, close);
		header.setFlexGrow(1, this.title);
		header.setAlignItems(FlexComponent.Alignment.CENTER);
		add(header);
	}

	private void createContent() {
		content = new VerticalLayout();
		content.setPadding(false);
		add(content);
	}

	private void createFooter() {
		Button abort = new Button("Cancel");
		abort.addClickListener(buttonClickEvent -> close());
		confirm = new Button("Confirm");
		confirm.addClickListener(buttonClickEvent -> close());

		HorizontalLayout footer = new HorizontalLayout();
		footer.add(abort, confirm);
		footer.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
		add(footer);
	}
}
