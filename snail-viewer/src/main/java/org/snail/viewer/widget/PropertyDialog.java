package org.snail.viewer.widget;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import com.ezware.dialog.task.TaskDialogs;
import com.l2fprod.common.swing.JButtonBar;

@SuppressWarnings("serial")
public class PropertyDialog extends StandardDialog {

	public interface AbstractEditorPanel {

		JPanel getPanel();

		String getLabel();

		String getIcon();

		void save() throws Exception;

	}

	private JButtonBar buttonBar = new JButtonBar(JButtonBar.VERTICAL);
	private ButtonGroup menuGroup = new ButtonGroup();
	private JButton acceptButton = new JButton("Accept", new ImageIcon(getClass()
			.getResource("/images/accept-icon.png")));
	private JButton saveButton = new JButton("Save", new ImageIcon(getClass().getResource("/images/ok-icon.png")));
	private JButton closeButton = new JButton("Cancel", new ImageIcon(getClass().getResource("/images/close-icon.png")));

	private JPanel mainPanel = new JPanel();

	public PropertyDialog(final JFrame frame, String title, final AbstractEditorPanel... panels) throws Exception {
		super(frame, title, true);

		buttonBar.setBorder(new EmptyBorder(5, 5, 5, 5));

		setLayout(new GridBagLayout());

		JScrollPane leftBarScrollPane = new JScrollPane(buttonBar);

		leftBarScrollPane.setPreferredSize(new Dimension(100, 300));
		leftBarScrollPane.setMinimumSize(new Dimension(100, 300));

		setLayout(new GridBagLayout());

		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.fill = GridBagConstraints.VERTICAL;
		gridBagConstraints.weighty = 1.0;
		gridBagConstraints.insets = new Insets(5, 5, 0, 0);

		getContentPane().add(leftBarScrollPane, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;

		getContentPane().add(mainPanel, gridBagConstraints);

		for (int iPanel = 0; iPanel < panels.length; iPanel++) {
			JPanel panel = wrapView(panels[iPanel].getPanel());

			if (iPanel == 0) {

				mainPanel.setBorder(new EmptyBorder(5, 5, 0, 5));
				mainPanel.setLayout(new BorderLayout());
				mainPanel.add(panel);

			}

			addAction(panels[iPanel].getLabel(), panels[iPanel].getIcon(), true, mainPanel, panel, buttonBar, menuGroup);

		}

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.fill = GridBagConstraints.NONE;
		gridBagConstraints.anchor = GridBagConstraints.EAST;

		JPanel buttonPanel = new JPanel();

		buttonPanel.setLayout(new GridLayout(1, 3, 5, 0));

		acceptButton.setPreferredSize(new Dimension(80, 30));
		saveButton.setPreferredSize(new Dimension(80, 30));
		closeButton.setPreferredSize(new Dimension(80, 30));

		acceptButton.setMinimumSize(new Dimension(80, 30));
		saveButton.setMinimumSize(new Dimension(80, 30));
		closeButton.setMinimumSize(new Dimension(80, 30));

		buttonPanel.add(acceptButton);
		buttonPanel.add(saveButton);
		buttonPanel.add(closeButton);

		buttonPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5),
				BorderFactory.createEmptyBorder(0, 5, 0, 0)));
		getContentPane().add(buttonPanel, gridBagConstraints);

		pack();

		saveButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {

				try {
					for (AbstractEditorPanel panel : panels) {

						panel.save();

					}

					setVisible(false);

				} catch (Exception e) {
					TaskDialogs.showException(e);

				}

			}

		});

		acceptButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				try {

					for (AbstractEditorPanel panel : panels) {

						panel.save();

					}

				} catch (Exception e) {
					TaskDialogs.showException(e);

				}

			}

		});

		closeButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}

		});

		setLocationRelativeTo(frame);

	}

}
