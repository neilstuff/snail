package org.snail.viewer.jung.graph;

import org.apache.commons.collections15.Transformer;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.shared.PrefixMapping;

/**
 *
 * @author pldms
 */
public class Transformers {

	public final static NodeT NODE = new NodeT();
	public final static EdgeT EDGE = new EdgeT();

	private final static String toString(Resource resource) {
		if (resource.isAnon())
			return "[]";
		PrefixMapping pmap = resource.getModel();
		String qname = pmap.qnameFor(resource.getURI());
		
		return (qname != null)  ? qname : "<" + resource.getURI() + ">";
	
	}

	public static class NodeT implements Transformer<RDFNode, String> {

		public String transform(RDFNode input) {
			if (input.isLiteral())
				return input.toString();
			else
				return Transformers.toString((Resource) input);
		}

	}

	public static class EdgeT implements Transformer<Statement, String> {

		public String transform(Statement input) {
			return Transformers.toString(input.getPredicate());
		}

	}
	
}
