package org.snail.viewer.controller;

import static org.snail.viewer.util.WidgetUtils.addAction;
import static org.snail.viewer.util.WidgetUtils.createImageIcon;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.Point2D;
import java.io.Closeable;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.FileManager;
import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXMultiSplitPane;
import org.jdesktop.swingx.MultiSplitLayout;
import org.snail.viewer.dialog.OpenDialog;
import org.snail.viewer.jung.graph.DirectedModelGraph;
import org.snail.viewer.jung.layout.LatticeLayout;
import org.snail.viewer.panel.DistancePanel;
import org.snail.viewer.util.WidgetUtils.ButtonManager;
import org.snail.viewer.widget.AboutDialog;
import org.snail.viewer.widget.DetailDialog;
import org.snail.viewer.widget.FilteredTree;
import org.snail.viewer.widget.FilteredTree.NodeListener;
import org.snail.viewer.widget.GlassPane;

import com.ezware.dialog.task.TaskDialogs;
import com.jgoodies.forms.builder.ListViewBuilder;
import com.jgoodies.looks.windows.WindowsLookAndFeel;

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.LayoutDecorator;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.control.SatelliteVisualizationViewer;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.picking.PickedState;

/**
 * Snail Navigator Controller
 *
 * This is a Swing Application to manage a RDF View
 *
 * @author Neil Brittliff
 *
 */
@SuppressWarnings("serial")
public class Controller extends JPanel {

	class ExpandableLayoutDecorator<V, E> extends LayoutDecorator<V, E> {
		Dimension dimension = null;

		public ExpandableLayoutDecorator(Layout<V, E> delegate) {
			super(delegate);

		}

		@Override
		public Dimension getSize() {

			if (dimension == null) {

				dimension = delegate.getSize();

			}

			return dimension;

		}

		@Override
		public void setSize(Dimension dimension) {

			if (delegate.getSize() == null) {

				delegate.setSize(dimension);

			}

			this.dimension = dimension;

		}

	}

	enum GRAPH {

		SPIDER, CENTRAL

	}

	final JFrame frame;
	private FilteredTree graphTree;

	private JXMultiSplitPane mainSplitPane;
	private JPanel rowPanel;
	private JEditorPane cellPane;

	JPanel satellitePane;

	private JXLabel statusBar;

	final GlassPane glassPane;

	JPanel graphContainer;

	private final ButtonManager graphButtons;

	final static public ResourceBundle TEXTS = PropertyResourceBundle.getBundle("locale.Texts");

	Graph<RDFNode, Statement> graph;
	VisualizationViewer<RDFNode, Statement> viewer;
	Model model;

	final MutableInt pickedVertexCounter;
	final MutableInt pickedEdgeCounter;

	JXButton zoomInButton;
	JXButton zoomOutButton;
	
	/**
	 * Controller
	 *
	 * @param frame
	 *            the main frame
	 *
	 * @throws Exception
	 *             thrown if the frame could not be created
	 *
	 */
	public Controller(final JFrame frame) throws Exception {
		this.frame = frame;
		List<Image> images = new LinkedList<Image>();

		pickedVertexCounter = new MutableInt(0);
		pickedEdgeCounter = new MutableInt(0);

		statusBar = new JXLabel("Ready");
		statusBar.setBorder(new EmptyBorder(0, 5, 10, 5));

		UIManager.put("Tree.collapsedIcon", createImageIcon("/images/collapsed-icon.png"));
		UIManager.put("Tree.expandedIcon", createImageIcon("/images/expanded-icon.png"));

		images.add(createImageIcon("/images/main-icon-16.png").getImage());
		images.add(createImageIcon("/images/main-icon-24.png").getImage());
		images.add(createImageIcon("/images/main-icon-32.png").getImage());
		images.add(createImageIcon("/images/main-icon-48.png").getImage());
		images.add(createImageIcon("/images/main-icon-64.png").getImage());
		images.add(createImageIcon("/images/main-icon-72.png").getImage());
		images.add(createImageIcon("/images/main-icon-96.png").getImage());
		images.add(createImageIcon("/images/main-icon-128.png").getImage());
		images.add(createImageIcon("/images/main-icon-512.png").getImage());

		frame.setIconImages(images);

		glassPane = new GlassPane(frame);

		frame.getRootPane().setGlassPane(glassPane);
		graphTree = new FilteredTree(new DefaultMutableTreeNode(), new NodeListener() {

			@Override
			public boolean keepNode(TreeNode node) {

				return true;

			}

		});

		graphTree.getTree().setRootVisible(false);
		graphTree.getTree().setRowHeight(22);

		graphTree.getTree().getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

		rowPanel = new JPanel(new BorderLayout());
		rowPanel.setBackground(new Color(243, 247, 250));

		rowPanel.setBorder(BorderFactory.createEtchedBorder());

		cellPane = new JEditorPane("text/html", null);
		cellPane.setEditable(false);

		JScrollPane scrollPane = new JScrollPane(cellPane);

		scrollPane.setBorder(BorderFactory.createEmptyBorder());

		rowPanel.add(scrollPane, BorderLayout.CENTER);
		graphTree.getTree().putClientProperty("JTree.lineStyle", "None");

		graphTree.getTree().setCellRenderer(new DefaultTreeCellRenderer() {

			@Override
			public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
					boolean isLeaf, int row, boolean focused) {
				Component component = super.getTreeCellRendererComponent(tree, value, selected, expanded, isLeaf, row,
						focused);

				if (!isLeaf || value.toString().equals("Subjects")) {
					setIcon(createImageIcon("/images/predicate-icon-16.png"));
				} else {
					setIcon(createImageIcon("/images/brkpd_obj.gif"));
				}

				return component;

			}

		});

		graphTree.getTree().addTreeSelectionListener(new TreeSelectionListener() {

			@Override
			public void valueChanged(TreeSelectionEvent e) {

				SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

					@Override
					protected Void doInBackground() throws Exception {
						DefaultMutableTreeNode node = (DefaultMutableTreeNode) graphTree.getTree()
								.getLastSelectedPathComponent();

						if (node == null || node.getParent() == null || node.getParent().toString().equals("Resources")) {

							glassPane.deactivate();

							return null;

						}

						if (node.getParent().toString().equals("Subjects")) {

							for (RDFNode rdfNode : graph.getVertices()) {
								PickedState<RDFNode> pickedVertexState = viewer.getPickedVertexState();

								pickedVertexState.pick(rdfNode, rdfNode.toString().equals(node.toString()));

							}

						} else if (node.getParent().toString().equals("Predicates")) {

							for (Statement statement : graph.getEdges()) {
								PickedState<Statement> pickedEdgeState = viewer.getPickedEdgeState();

								pickedEdgeState.pick(statement,
										statement.getPredicate().toString().equals(node.toString()));

							}

						}

						glassPane.deactivate();

						return null;

					}

				};

				worker.execute();

			}

		});

		JXButton graphButton = new JXButton(createImageIcon("/images/graph-icon-16.png"));
		JXButton circularButton = new JXButton(createImageIcon("/images/circular-icon-16.png"));
		JXButton latticeButton = new JXButton(createImageIcon("/images/square-icon-16.png"));

		ButtonGroup group = new ButtonGroup();

		group.add(graphButton);
		group.add(circularButton);
		group.add(latticeButton);

		JToolBar actionBar = new JToolBar();

		actionBar.setFloatable(false);
		actionBar.setRollover(true);

		actionBar.add(graphButton);
		actionBar.add(circularButton);
		actionBar.add(latticeButton);

		zoomInButton = new JXButton(createImageIcon("/images/zoom-in-icon-24.png"));
		zoomOutButton = new JXButton(createImageIcon("/images/zoom-out-icon-24.png"));

		JToolBar viewBar = new JToolBar();

		viewBar.setFloatable(false);
		viewBar.setRollover(true);

		viewBar.add(zoomInButton);
		viewBar.add(zoomOutButton);

		graphButtons = new ButtonManager(graphButton, circularButton, latticeButton, zoomInButton, zoomOutButton);

		JPanel emptyPanel = new JPanel();

		emptyPanel.setBackground(Color.WHITE);

		emptyPanel.setBorder(BorderFactory.createEtchedBorder());

		graphContainer = new JPanel(new BorderLayout());

		resetGraphContainer();

		graphButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				setupViewer(new ISOMLayout<RDFNode, Statement>(graph));

			}

		});

		circularButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setupViewer(new CircleLayout<RDFNode, Statement>(graph));
			}

		});

		latticeButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setupViewer(new LatticeLayout<RDFNode, Statement>(graph));
			}

		});

		satellitePane = new JPanel(new BorderLayout());

		satellitePane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 0, 2, 1),
				BorderFactory.createEtchedBorder()));
		satellitePane.setMinimumSize(new Dimension(64, 64));
		satellitePane.setPreferredSize(new Dimension(64, 64));

		JComponent component = new ListViewBuilder().border(new EmptyBorder(5, 5, 5, 0)).labelView(viewBar)
				.listView(graphContainer).filterView(satellitePane).listBarView(actionBar).build();

		component.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));

		cellPane.setBackground(new Color(243, 247, 250));
		cellPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
		cellPane.setFont(graphTree.getFont());

		MultiSplitLayout.Node modelRoot = MultiSplitLayout
				.parseModel("(ROW (LEAF name=left weight=0.2) (LEAF name=middle weight=0.6) (LEAF name=right weight=0.2))");

		mainSplitPane = new JXMultiSplitPane();

		mainSplitPane.getMultiSplitLayout().setModel(modelRoot);
		mainSplitPane.getMultiSplitLayout().setLayoutByWeight(true);

		mainSplitPane.add(graphTree, "left");
		mainSplitPane.add(component, "middle");
		mainSplitPane.add(rowPanel, "right");

		cellPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		mainSplitPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		frame.addWindowListener(new WindowListener() {

			@Override
			public void windowOpened(WindowEvent event) {
			}

			@Override
			public void windowIconified(WindowEvent e) {
			}

			@Override
			public void windowDeiconified(WindowEvent e) {
			}

			@Override
			public void windowDeactivated(WindowEvent e) {
			}

			@Override
			public void windowClosing(WindowEvent e) {
			}

			@Override
			public void windowClosed(WindowEvent e) {
			}

			@Override
			public void windowActivated(WindowEvent e) {
			}

		});

		frame.pack();
	}

	/**
	 * Get the 'Split' Pane
	 *
	 * @return the split pane
	 *
	 */
	public JComponent getSplitPane() {

		return mainSplitPane;

	}

	/**
	 * Create the Main Tool Bar
	 *
	 * @return a new created Tool Bar
	 *
	 */
	private JToolBar createToolBar() {
		JToolBar toolBar = new JToolBar();

		toolBar.setFloatable(false);
		toolBar.setRollover(true);

		JMenuBar bar = new JMenuBar();

		bar.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

		frame.setJMenuBar(bar);

		JMenu menuFile = new JMenu(local("MENU_FILE"));
		JMenu menuTools = new JMenu(local("MENU_TOOLS"));
		JMenu menuHelp = new JMenu(local("MENU_HELP"));

		bar.add(menuFile);
		bar.add(menuTools);
		bar.add(menuHelp);

		addAction(toolBar, menuFile, "Open", "/images/folder-open-icon-24.png", "/images/folder-open-icon-16.png",
				new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent event) {

						@SuppressWarnings("resource")
						OpenDialog dialog = new OpenDialog(frame, "Open RDF File", new OpenDialog.OpenAction() {

							@Override
							public boolean open(Closeable closeable, String uri) throws Exception {
								model = FileManager.get().loadModel(uri);
								graph = new DirectedModelGraph(model);

								graphTree.setRoot(new DefaultMutableTreeNode("Resources"));
								graphTree.getTree().setRootVisible(false);

								StmtIterator iStatement = model.listStatements();

								List<String> predicates = new LinkedList<String>();
								List<String> subjects = new LinkedList<String>();

								while (iStatement.hasNext()) {
									Statement statement = iStatement.nextStatement();

									if (!predicates.contains(statement.getPredicate().toString())) {

										predicates.add(statement.getPredicate().toString());

									}

									if (!statement.getSubject().isAnon()
											&& !subjects.contains(statement.getSubject().toString())) {

										subjects.add(statement.getSubject().toString());

									}

								}

								addNodes("Predicates", predicates);
								addNodes("Subjects", subjects);

								setupViewer(new ISOMLayout<RDFNode, Statement>(graph));

								((DefaultTreeModel) graphTree.getTree().getModel()).reload();

								Preferences preferences = Preferences.userRoot();

								Set<String> sortedUris = new LinkedHashSet<String>(Arrays.asList(StringUtils.split(
										Preferences.userRoot().get("snail-uris", ""), ",")));

								sortedUris.remove(uri);

								List<String> uris = new LinkedList<String>(sortedUris);

								uris.add(0, uri);

								preferences.put("snail-uris", StringUtils.join(uris.toArray(new String[0]), ','));

								enableGraphButtons(true);

								return true;
							}

						});

						dialog.setVisible(true);

					}

				});

		menuFile.addSeparator();

		addAction(menuFile, "Exit", "/images/exit-icon-16.png", new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				System.exit(0);
			}

		});

		addAction(toolBar, menuTools, "Measures", "/images/measure-icon-24.png", "/images/measure-icon-16.png",
				new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent event) {

						SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

							@Override
							protected Void doInBackground() throws Exception {

								try {
									JDialog dialog = new DetailDialog(frame, "Distance",
											new DistancePanel(model, graph));

									dialog.setLocationRelativeTo(frame);

									glassPane.deactivate();

									dialog.setVisible(true);

								} catch (Exception e) {

									TaskDialogs.showException(e);

								}

								return null;

							}
						};

						glassPane.activate("Please Wait");

						worker.execute();

					}

				});

		addAction(menuHelp, "Help", "/images/help-icon-16.png", new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				AboutDialog ab = new AboutDialog(frame, true);
				ab.setVersion("V1.1.0");
				ab.setVisible(true);

			}

		});

		toolBar.setBorder(new EmptyBorder(5, 5, 0, 5));

		enableGraphButtons(false);

		return toolBar;

	}

	/**
	 * Create and show the GUI
	 *
	 * @throws Exception
	 *             thrown if the GUI cannot be created
	 *
	 */
	private static void createAndShowGUI() throws Exception {

		System.setProperty("apple.laf.useScreenMenuBar", "true");

		try {

			UIManager.setLookAndFeel(new WindowsLookAndFeel());

		} catch (UnsupportedLookAndFeelException e) {

			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		}

		UIManager.put("Tree.collapsedIcon", createImageIcon("/images/collapsed-icon.png"));
		UIManager.put("Tree.expandedIcon", createImageIcon("/images/expanded-icon.png"));

		JFrame frame = new JFrame("Snail Navigator");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		Controller controller = new Controller(frame);

		frame.getContentPane().add(controller.createToolBar(), BorderLayout.NORTH);
		frame.getContentPane().add(controller.getSplitPane(), BorderLayout.CENTER);
		frame.getContentPane().add(controller.statusBar, BorderLayout.SOUTH);

		frame.pack();
		frame.setSize(1500, 900);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

	}

	private void setupViewer(final Layout<RDFNode, Statement> layout) {
		final Layout<RDFNode, Statement> layoutDecorator = new ExpandableLayoutDecorator<RDFNode, Statement>(layout);

		viewer = new VisualizationViewer<RDFNode, Statement>(layoutDecorator, new Dimension(960, 780));

		final SatelliteVisualizationViewer<RDFNode, Statement> satelliteViewer = new SatelliteVisualizationViewer<RDFNode, Statement>(
				viewer, new Dimension(68, 68));

		ScalingControl satelliteScaler = new CrossoverScalingControl();

		satelliteViewer.scaleToLayout(satelliteScaler);

		DefaultModalGraphMouse<RDFNode, Statement> modalMouse = new DefaultModalGraphMouse<RDFNode, Statement>();

		modalMouse.setMode(Mode.PICKING);

		modalMouse.setZoomAtMouse(true);

		viewer.setGraphMouse(modalMouse);

		viewer.setBackground(new Color(255, 255, 255));
		viewer.setBorder(BorderFactory.createEtchedBorder());

		GraphZoomScrollPane scrollPanel = new GraphZoomScrollPane(viewer);
		scrollPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), BorderFactory.createEmptyBorder(5, 5, 5, 5)));

		while (graphContainer.getComponentCount() > 0) {
			graphContainer.remove(0);
		}

		while (satellitePane.getComponentCount() > 0) {
			satellitePane.remove(0);
		}

		graphContainer.add(scrollPanel, BorderLayout.CENTER);
		graphContainer.updateUI();

		satellitePane.add(satelliteViewer);

		removeActionListeners(zoomInButton);
		removeActionListeners(zoomOutButton);
		
		zoomInButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
			
				satelliteScaler.scale(viewer, 1.1f, viewer.getCenter());
			
			}
			
		});
		
		zoomOutButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				satelliteScaler.scale(viewer, 1/1.1f, viewer.getCenter());
			}
			
		});
		
		final PickedState<RDFNode> pickedVertexState = viewer.getPickedVertexState();

		pickedVertexState.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {

				SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
					@Override
					protected Void doInBackground() throws Exception {

						synchronized (cellPane) {

							pickedVertexCounter.increment();

							if (pickedVertexCounter.intValue() != pickedVertexState.getPicked().size()) {

								return null;

							}

							String text = "<html><table>";

							for (RDFNode node : pickedVertexState.getPicked()) {

								if (node.isResource()) {
									text += "<tr><td width=\"5\" rowspan=\"2\" bgcolor=\"#D3EAF2\" color=\"#4A4A4A\" align=\"right\"><b><font size=\"1\">";
								} else if (node.isAnon()) {
									text += "<tr><td width=\"5\" rowspan=\"2\" bgcolor=\"#C8BFE7\" color=\"#4A4A4A\" align=\"right\"><b><font size=\"1\">";
								} else {
									text += "<tr><td width=\"5\" rowspan=\"2\" bgcolor=\"#7092BE\" color=\"#4A4A4A\" align=\"right\"><b><font size=\"1\">";
								}

								text += "&nbsp;</b></font></td></tr>";

								text += "<tr><td>";
								text += node.toString();
								text += "</td></tr>";

							}

							text += "</table></html>";

							cellPane.setText(text);

							glassPane.deactivate();

							pickedVertexCounter.setValue(0);

						}

						return null;

					}

				};

				if ((pickedVertexState.isPicked((RDFNode) e.getItem()) && (pickedVertexState.getPicked().size() == 1))) {

					glassPane.activate("Please Wait");
					pickedVertexCounter.setValue(0);

				}

				if (pickedVertexState.isPicked((RDFNode) e.getItem())) {
					worker.execute();
				}
				

			}

		});

		final PickedState<Statement> pickedEdgeState = viewer.getPickedEdgeState();

		pickedEdgeState.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {

				SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
					@Override
					protected Void doInBackground() throws Exception {

						synchronized (cellPane) {

							String text = "<html><table>";

							pickedEdgeCounter.increment();

							if (pickedEdgeCounter.intValue() != pickedEdgeState.getPicked().size()) {

								return null;

							}

							for (Statement statememt : pickedEdgeState.getPicked()) {

								text += "<tr><td width=\"5\" rowspan=\"4\" bgcolor=\"#D3EAF2\" color=\"#4A4A4A\" align=\"right\"><b><font size=\"1\">";
								text += "&nbsp;</b></font></td></tr>";

								text += "<tr><td>";
								text += statememt.getSubject().toString();
								text += "</td></tr><tr><td><b>";
								text += statememt.getPredicate().toString();
								text += "</b></td></tr><tr><td>";
								text += statememt.getObject().toString();
								text += "</td></tr>";

								text += "<tr><td></td></tr>";

							}

							text += "</table></html>";

							cellPane.setText(text);

						}

						glassPane.deactivate();

						pickedEdgeCounter.setValue(0);

						return null;

					}

				};

				if ((pickedEdgeState.isPicked((Statement) e.getItem()) && (pickedEdgeState.getPicked().size() == 1))) {

					glassPane.activate("Please Wait");
					pickedEdgeCounter.setValue(0);

				}

				if (pickedEdgeState.isPicked((Statement) e.getItem())) {
					worker.execute();
				}

			}

		});

		viewer.addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseMoved(MouseEvent arg0) {
			}

			@Override
			public void mouseDragged(MouseEvent arg0) {
				double width = viewer.getSize().width;
				double height = viewer.getSize().height;

				for (RDFNode node : layout.getGraph().getVertices()) {

					Point2D point = layout.transform(node);

					width = Math.max(width, point.getX());
					height = Math.max(height, point.getY());

				}

				if (width > viewer.getSize().width || height > viewer.getSize().height) {

					layoutDecorator.setSize(new Dimension((int) width + 24, (int) height + 24));

				}

			}

		});

	}

	private void resetGraphContainer() {
		JPanel panel = new JPanel();
		panel.setBackground(new Color(255, 255, 255));

		panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));

		graphContainer.add(panel, BorderLayout.CENTER);
		graphContainer.updateUI();

	}

	private void addNodes(String label, List<String> resources) {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(label);

		for (String resource : resources) {
			DefaultMutableTreeNode child = new DefaultMutableTreeNode(resource);

			graphTree.getTree().expandPath(new TreePath(child.getPath()));
			node.add(child);

		}

		((DefaultMutableTreeNode) graphTree.getTree().getModel().getRoot()).add(node);

	}

	/**
	 * Main Method
	 *
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					createAndShowGUI();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		});

	}

	/**
	 * Enable Graph Buttons
	 *
	 * @param enable
	 *            'true' enable graph buttons, 'false' disable graph buttons
	 *
	 */
	private void enableGraphButtons(boolean enable) {

		graphButtons.setEnabled(enable);

	}

	private void removeActionListeners(JXButton button) {
		
		for (ActionListener listener : button.getActionListeners()) {
			
			button.removeActionListener(listener);
			
		}
		
	}
	
	public static String local(String key) {
		try {

			return TEXTS.getString(key);

		} catch (Exception e) {
		}

		return "[" + key + "]";
	}

}
