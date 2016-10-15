package org.snail.viewer.jung.graph;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.util.iterator.ClosableIterator;

/**
 *
 * @author pldms
 */
public class DirectedModelGraph implements DirectedGraph<RDFNode, Statement> {
	private final Model model;

	public DirectedModelGraph(final Model model) {
		this.model = model;
	}

	private static final <T> Collection<T> asCollection(final ClosableIterator<? extends T> it) {
		final Collection<T> toReturn = new HashSet<T>();

		while (it.hasNext()) {
			toReturn.add((T) it.next());
		}

		it.close();

		return toReturn;

	}

	public Collection<Statement> getInEdges(RDFNode vertex) {

		return asCollection(model.listStatements(null, null, vertex));

	}

	@SuppressWarnings("unchecked")
	public Collection<Statement> getOutEdges(RDFNode vertex) {

		return  (Collection<Statement>)((vertex.isLiteral()) ? Collections.emptyList() : asCollection(model.listStatements((Resource) vertex,
				null, (RDFNode) null)));

	}

	public Collection<RDFNode> getPredecessors(RDFNode vertex) {

		return DirectedModelGraph.<RDFNode> asCollection(model.listResourcesWithProperty(null, vertex));

	}

	@SuppressWarnings("unchecked")
	public Collection<RDFNode> getSuccessors(RDFNode vertex) {

		return  (Collection<RDFNode>)(((vertex.isLiteral()) ? Collections.emptyList() : asCollection(model.listObjectsOfProperty(
				(Resource) vertex, null))));

	}

	public int inDegree(RDFNode vertex) {

		return getInEdges(vertex).size();

	}

	public int outDegree(RDFNode vertex) {

		return getOutEdges(vertex).size();

	}

	public boolean isPredecessor(RDFNode v1, RDFNode v2) {

		return (v1.isLiteral()) ? false : model.contains((Resource) v1, null, v2);

	}

	public boolean isSuccessor(RDFNode v1, RDFNode v2) {

		return isPredecessor(v2, v1);

	}

	public int getPredecessorCount(RDFNode vertex) {

		return getPredecessors(vertex).size();

	}

	public int getSuccessorCount(RDFNode vertex) {

		return getSuccessors(vertex).size();

	}

	public RDFNode getSource(Statement directed_edge) {

		return directed_edge.getSubject();

	}

	public RDFNode getDest(Statement directed_edge) {

		return directed_edge.getObject();

	}

	public boolean isSource(RDFNode vertex, Statement edge) {

		return vertex.equals(edge.getSubject());

	}

	public boolean isDest(RDFNode vertex, Statement edge) {

		return vertex.equals(edge.getObject());

	}

	public boolean addEdge(Statement e, RDFNode v1, RDFNode v2) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public boolean addEdge(Statement e, RDFNode v1, RDFNode v2, EdgeType edgeType) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public Pair<RDFNode> getEndpoints(Statement edge) {

		return new Pair<RDFNode>(edge.getSubject(), edge.getObject());

	}

	public RDFNode getOpposite(RDFNode vertex, Statement edge) {

		return (edge.getSubject().equals(vertex)) ? edge.getObject() : edge.getSubject();

	}

	public Collection<Statement> getEdges() {

		return asCollection(model.listStatements());

	}

	public Collection<RDFNode> getVertices() {
		Collection<RDFNode> nodes = asCollection(model.listObjects());

		nodes.addAll(asCollection(model.listSubjects()));

		return nodes;

	}

	public boolean containsVertex(RDFNode vertex) {

		return (vertex.isResource() && model.contains((Resource) vertex, null, (RDFNode) null)) ? true : model
				.contains(null, null, vertex);

	}

	public boolean containsEdge(Statement edge) {

		return model.contains(edge);

	}

	public int getEdgeCount() {

		return (int) model.size();

	}

	public int getVertexCount() {

		return getVertices().size();

	}

	public Collection<RDFNode> getNeighbors(RDFNode vertex) {
		Collection<RDFNode> nodes = new HashSet<RDFNode>();

		nodes.addAll(getSuccessors(vertex));
		nodes.addAll(getPredecessors(vertex));

		return nodes;

	}

	public Collection<Statement> getIncidentEdges(RDFNode vertex) {
		Collection<Statement> most = asCollection(model.listStatements(null, null, vertex));

		if (vertex.isResource()) {
			most.addAll(asCollection(model.listStatements((Resource) vertex, null, (RDFNode) null)));
		}

		return most;

	}

	public Collection<RDFNode> getIncidentVertices(Statement edge) {

		return Arrays.asList(edge.getSubject(), edge.getObject());

	}

	public Statement findEdge(RDFNode v1, RDFNode v2) {
		Collection<Statement> collection = findEdgeSet(v1, v2);

		return (collection.isEmpty()) ? null : collection.iterator().next();

	}

	public Collection<Statement> findEdgeSet(RDFNode v1, RDFNode v2) {
		Collection<Statement> collection = new HashSet<Statement>();

		if (v1.isResource())
			collection.addAll(asCollection(model.listStatements((Resource) v1, null, v2)));
		if (v2.isResource())
			collection.addAll(asCollection(model.listStatements((Resource) v2, null, v1)));

		return collection;

	}

	public boolean addVertex(RDFNode vertex) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public boolean addEdge(Statement edge, Collection<? extends RDFNode> vertices) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public boolean addEdge(Statement edge, Collection<? extends RDFNode> vertices, EdgeType edge_type) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public boolean removeVertex(RDFNode vertex) {
		
		throw new UnsupportedOperationException("Not supported yet.");
	
	}

	public boolean removeEdge(Statement edge) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public boolean isNeighbor(RDFNode v1, RDFNode v2) {

		return this.getNeighbors(v1).contains(v2);

	}

	public boolean isIncident(RDFNode vertex, Statement edge) {

		return (edge.getSubject().equals(vertex) || edge.getObject().equals(vertex));
	
	}

	public int degree(RDFNode vertex) {

		return inDegree(vertex) + outDegree(vertex);

	}

	public int getNeighborCount(RDFNode vertex) {

		return this.getNeighbors(vertex).size();

	}

	public int getIncidentCount(Statement edge) {

		return (edge.getSubject().equals(edge.getObject())) ? 1 : 2;
	
	}

	public EdgeType getEdgeType(Statement edge) {

		return EdgeType.DIRECTED;

	}

	public EdgeType getDefaultEdgeType() {

		return EdgeType.DIRECTED;

	}

	@SuppressWarnings("unchecked")
	public Collection<Statement> getEdges(EdgeType edge_type) {

		return (Collection<Statement>) ((edge_type.equals(EdgeType.DIRECTED)) ? getEdges() : Collections.emptyList());

	}

	public int getEdgeCount(EdgeType edge_type) {

		return (edge_type.equals(EdgeType.DIRECTED)) ? (int) model.size() : 0;

	}

}