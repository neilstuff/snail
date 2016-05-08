package org.snail.viewer.panel;

import java.awt.Color;
import java.awt.Dimension;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;

import org.apache.commons.collections15.Transformer;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.snail.viewer.swing.layout.RiverLayout;
import org.snail.viewer.widget.DetailDialog.AbstractDetailPanel;

import edu.uci.ics.jung.algorithms.shortestpath.DistanceStatistics;
import edu.uci.ics.jung.graph.Graph;

@SuppressWarnings("serial")
public class DistancePanel extends JPanel implements AbstractDetailPanel {

	public DistancePanel(Model model, final Graph<RDFNode, Statement> graph) throws Exception {

		setLayout(new RiverLayout());
		setBorder(BorderFactory.createEtchedBorder());

		JEditorPane contextView = new JEditorPane("text/html", null);

		contextView.setEditable(false);
		contextView.setBackground(new Color(243, 247, 250));
		contextView.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
		contextView.setFont(new JTree().getFont());

		StringBuilder builder = new StringBuilder();

		builder.append("<html>");
		builder.append("<table>");

		Transformer<RDFNode, Double> transformer = DistanceStatistics.averageDistances(graph);

		StmtIterator iterator = model.listStatements();

		Map<String, Double> map = new TreeMap<String, Double>();

		while (iterator.hasNext()) {
			Statement statement = iterator.next();

			map.put(statement.getSubject().toString(), transformer.transform(statement.getSubject()));

		}

		for (Entry<String, Double> entry : map.entrySet()) {
			setItem(builder, entry.getKey(), entry.getValue().toString());
		}

		builder.append("</table>");
		builder.append("</html>");

		contextView.setText(builder.toString());
		setLayout(new RiverLayout());
		setBorder(BorderFactory.createEtchedBorder());

		JScrollPane scrollPanel = new JScrollPane(contextView);

		scrollPanel.setPreferredSize(new Dimension(600, 400));
		scrollPanel.setMinimumSize(new Dimension(600, 400));

		add("br hfill vfill", scrollPanel);

	}

	private void setItem(StringBuilder builder, String label, String value) {

		builder.append("<tr><td width=\"120\"><b>");
		builder.append(label);
		builder.append("</b></td><td><i>");
		builder.append(value);
		builder.append("</i></td></tr>");

	}

	@Override
	public JPanel getPanel() {

		return this;

	}

	@Override
	public String getLabel() {

		return "Distance";

	}

	@Override
	public String getIcon() {

		return "/images/measure-icon-32.png";

	}

}
