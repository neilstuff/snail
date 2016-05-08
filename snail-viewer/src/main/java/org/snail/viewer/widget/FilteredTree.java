package org.snail.viewer.widget;

import static org.snail.viewer.util.WidgetUtils.createImageIcon;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.util.Enumeration;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

/**
 * Tree widget which allows the tree to be filtered on keystroke time. Only
 * nodes who's toString matches the search field will remain in the tree or its
 * parents.
 * 
 * Copyright (c) Oliver.Watkins
 */

@SuppressWarnings("serial")
public class FilteredTree extends JPanel {

	public interface NodeListener {

		boolean keepNode(TreeNode node);

	}

	private String filteredText = "";
	private DefaultTreeModel originalTreeModel;
	private DefaultMutableTreeNode root;
	private JScrollPane scrollpane;
	private JTree tree = new JTree();
	private NodeListener listener;

	public FilteredTree(DefaultMutableTreeNode root, NodeListener listener) {

		this.root = root;
		this.listener = listener;

		guiLayout();

	}

	private void guiLayout() {

		tree.setCellRenderer(new Renderer());

		final SearchTextField searchField = new SearchTextField(createImageIcon("/images/filter-icon-16.png"), "Filter");

		searchField.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(final DocumentEvent e) {
				filterTree(searchField.getText());
			}

			@Override
			public void insertUpdate(final DocumentEvent e) {
				filterTree(searchField.getText());
			}

			@Override
			public void changedUpdate(final DocumentEvent e) {
				filterTree(searchField.getText());
			}

		});

		originalTreeModel = new DefaultTreeModel(root);

		tree.setModel(originalTreeModel);

		this.setLayout(new BorderLayout(5, 5));

		add(searchField, BorderLayout.NORTH);
		add(scrollpane = new JScrollPane(tree), BorderLayout.CENTER);

		scrollpane.setBorder(BorderFactory.createEtchedBorder());

	}

	/**
	 * 
	 * @param text
	 */

	private void filterTree(String text) {
		filteredText = text;
		// get a copy
		DefaultMutableTreeNode node = copyNode(root);

		if (listener.keepNode(node) && text.trim().toString().equals("")) {

			originalTreeModel.setRoot(root);

			tree.setModel(originalTreeModel);
			tree.updateUI();
			scrollpane.getViewport().setView(tree);

			for (int iRow = 0; iRow < tree.getRowCount(); iRow++) {
				tree.expandRow(iRow);
			}

			return;

		} else {
			TreeNodeBuilder builder = new TreeNodeBuilder(text);
			node = builder.prune(node);

			originalTreeModel.setRoot(node);

			tree.setModel(originalTreeModel);
			tree.updateUI();
			scrollpane.getViewport().setView(tree);

		}

		for (int iRow = 0; iRow < tree.getRowCount(); iRow++) {

			tree.expandRow(iRow);

		}

	}

	/**
	 * Clone/Copy a tree node. TreeNodes in Swing don't support deep cloning.
	 * 
	 * @param originalNode
	 *            to be cloned
	 * @return cloned copy
	 */
	private DefaultMutableTreeNode copyNode(DefaultMutableTreeNode originalNode) {
		DefaultMutableTreeNode newOne = new DefaultMutableTreeNode();

		newOne.setUserObject(originalNode.getUserObject());

		@SuppressWarnings("unchecked")
		Enumeration<DefaultMutableTreeNode> node = originalNode.children();

		while (node.hasMoreElements()) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.nextElement();

			newOne.add(copyNode(child));

		}

		return newOne;

	}

	/**
	 * Renders bold any tree nodes who's toString() value starts with the
	 * filtered text we are filtering on.
	 * 
	 * @author Oliver.Watkins
	 */
	public class Renderer extends DefaultTreeCellRenderer {

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
				boolean leaf, int row, boolean hasfocus) {

			Component component = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row,
					hasfocus);

			if (component instanceof JLabel) {

				if (!filteredText.equals("") && value.toString().startsWith(filteredText)) {
					Font f = component.getFont();
					f = new Font("Dialog", Font.BOLD, f.getSize());
					component.setFont(f);
				} else {
					Font f = component.getFont();
					f = new Font("Dialog", Font.PLAIN, f.getSize());
					component.setFont(f);
				}

			}

			return component;

		}

	}

	public JTree getTree() {
		return tree;
	}

	public void setRoot(DefaultMutableTreeNode root) {

		this.root = root;

		((DefaultTreeModel) getTree().getModel()).setRoot(root);

	}

	/**
	 * Class that prunes off all leaves which do not match the search string.
	 * 
	 * @author Oliver.Watkins
	 */

	public class TreeNodeBuilder {

		private String textToMatch;

		public TreeNodeBuilder(String textToMatch) {

			this.textToMatch = textToMatch;

		}

		public DefaultMutableTreeNode prune(DefaultMutableTreeNode root) {
			boolean badLeaves = true;

			// keep looping through until tree contains only leaves that match
			while (badLeaves) {

				badLeaves = removeBadLeaves(root);

			}

			boolean uncleaned = true;

			loop: while (uncleaned) {

				for (int iChild = 0; iChild < root.getChildCount(); iChild++) {
					if (cleanFolders(root, (DefaultMutableTreeNode) root.getChildAt(iChild))) {

						iChild = 0;

						uncleaned = true;

						continue loop;

					}

				}

				uncleaned = false;

			}

			return root;

		}

		/**
		 * Remove Bad Leaves
		 * 
		 * @param root
		 *            the Root or Parent
		 * 
		 * @return boolean bad leaves were returned
		 */

		private boolean removeBadLeaves(DefaultMutableTreeNode root) {
			boolean badLeaves = false;

			// reference first leaf

			DefaultMutableTreeNode leaf = root.getFirstLeaf();

			// if leaf is root then its the only node

			if (leaf.isRoot()) {

				return false;

			}

			DefaultMutableTreeNode nextLeaf = leaf.getNextLeaf();

			while (nextLeaf != null) {

				// if it does not start with the text then snip it off its
				// parent

				if (listener.keepNode(leaf) && !leaf.toString().contains(textToMatch)) {

					clean(leaf);

					badLeaves = true;

				}

				nextLeaf = leaf.getNextLeaf();
				leaf = nextLeaf;

			}

			return badLeaves;

		}

		boolean cleanFolders(DefaultMutableTreeNode parent, DefaultMutableTreeNode child) {
			boolean unclean = false;

			if (!(listener.keepNode(child)) && child.isLeaf()) {

				parent.remove(child);

				return true;

			}

			loop: for (int iChild = 0; iChild < child.getChildCount(); iChild++) {

				if (cleanFolders(child, (DefaultMutableTreeNode) child.getChildAt(iChild))) {

					iChild = 0;

					unclean = true;

					break loop;

				}

			}

			return unclean;

		}

		void clean(DefaultMutableTreeNode leaf) {
			DefaultMutableTreeNode parent = (DefaultMutableTreeNode) leaf.getParent();
			parent.remove(leaf);

		}

	}

}