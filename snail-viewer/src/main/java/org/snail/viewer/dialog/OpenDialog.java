package org.snail.viewer.dialog;

/**
 * Connection Dialog - obtains the connection 'uri' which identifies the repository
 * 
 * @author Neil Brittliff
 * 
 *         (c) 2013 - Neil Brittliff
 * 
 */
import static org.snail.viewer.util.WidgetUtils.createImageIcon;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.JXErrorPane;
import org.jdesktop.swingx.error.ErrorInfo;
import org.snail.viewer.widget.StandardDialog;

@SuppressWarnings("serial")
public class OpenDialog extends StandardDialog implements Closeable {

	public interface OpenAction {

		boolean open(Closeable closeable, String uri) throws Exception;

	}

	final JComboBox<String> uriFileName;

	public String getUriFileName() {

		return uriFileName.getEditor().getItem().toString();

	}

	public OpenDialog(final Frame frame, String title, final OpenAction openAction) {
		super(frame, title, false);

		status = STATUS.CANCEL;

		JPanel contentPanel = new JPanel(new GridBagLayout());

		contentPanel.setPreferredSize(new Dimension(350, 90));
		contentPanel.setBackground(Color.white);

		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

		JLabel inputLable = new JLabel("Enter RDF File:");

		inputLable.setForeground(Color.BLUE.darker().darker());

		JLabel uriNameLabel = new JLabel("File:");

		Preferences preferences = Preferences.userRoot();

		String uris = preferences.get("snail-uris", "");

		uriFileName = new JComboBox<String>(StringUtils.split(uris, ","));

		uriFileName.setEditable(true);

		JButton directoryNameSelect = new JButton(createImageIcon("/images/folder-icon-16.png"));

		directoryNameSelect.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();
				
				if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					
					uriFileName.getEditor().setItem(file.getAbsolutePath());
					
				}				
			
			}

		});

		GridBagConstraints constraints = new GridBagConstraints();

		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridwidth = 6;
		constraints.insets = new Insets(5, 5, 5, 10);

		contentPanel.add(inputLable, constraints);

		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.gridwidth = 1;
		constraints.insets = new Insets(5, 5, 5, 5);

		contentPanel.add(uriNameLabel, constraints);

		constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridwidth = 3;
		constraints.gridx = 1;
		constraints.gridy = 1;
		constraints.weightx = 1.0;
		constraints.insets = new Insets(5, 5, 5, 5);

		contentPanel.add(uriFileName, constraints);

		constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridwidth = 1;
		constraints.gridx = 5;
		constraints.gridy = 1;
		constraints.insets = new Insets(5, 5, 5, 5);

		contentPanel.add(directoryNameSelect, constraints);

		getContentPane().add(BorderLayout.NORTH, contentPanel);

		JPanel buttonPanel = new JPanel(new GridBagLayout());

		JButton loginButton = new JButton("Load", createImageIcon("/images/ok-icon.png"));

		constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridwidth = 1;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.insets = new Insets(5, 5, 5, 5);

		buttonPanel.add(loginButton, constraints);

		JButton cancelButton = new JButton("Cancel", createImageIcon("/images/close-icon.png"));

		loginButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {

				try {
					if (openAction.open(OpenDialog.this, getUriFileName())) {

						status = STATUS.OK;
						
						setVisible(false);

					}

				} catch (Exception e) {
					ErrorInfo info = new ErrorInfo("Load Error", "Unable to Load", e.toString(), "category",
							new Exception(), Level.ALL, null);

					JXErrorPane.showDialog(frame, info);

				}

			}

		});

		cancelButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				dispose();

			}

		});

		constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridwidth = 1;
		constraints.gridx = 1;
		constraints.gridy = 0;
		constraints.insets = new Insets(5, 5, 5, 5);

		buttonPanel.add(cancelButton, constraints);

		loginButton.setPreferredSize(new Dimension(90, 30));
		cancelButton.setPreferredSize(new Dimension(90, 30));

		getContentPane().add(BorderLayout.SOUTH, buttonPanel);

		pack();

		setSize(350, 160);

		setLocationRelativeTo(frame);

	}

	@Override
	public void close() throws IOException {

		dispose();

	}

}
