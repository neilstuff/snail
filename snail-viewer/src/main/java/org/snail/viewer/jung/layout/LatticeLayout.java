package org.snail.viewer.jung.layout;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.graph.Graph;



/**
 * A {@code Layout} implementation that positions vertices equally spaced on a regular rectangle.
 *
 * @author Neil Brittliff
 * 
 */
public class LatticeLayout<V, E> extends AbstractLayout<V,E> {

	private List<V> vertex_ordered_list;

	/**
	 * Creates an instance for the specified graph.
	 */
	public LatticeLayout(Graph<V,E> g) {
		super(g);
	}

	/**
	 * Sets the order of the vertices in the layout according to the ordering
	 * specified by {@code comparator}.
	 */
	public void setVertexOrder(Comparator<V> comparator) {
	    if (vertex_ordered_list == null) {
	        vertex_ordered_list = new ArrayList<V>(getGraph().getVertices());
	    }
	    
	    Collections.sort(vertex_ordered_list, comparator);
	}

    /**
     * Sets the order of the vertices in the layout according to the ordering
     * of {@code vertex_list}.
     */
	public void setVertexOrder(List<V> vertex_list) {
		
	    if (!vertex_list.containsAll(getGraph().getVertices())) 
	        throw new IllegalArgumentException("Supplied list must include " +
	        		"all vertices of the graph");
	    
	    this.vertex_ordered_list = vertex_list;
	
	}
	
	public void reset() {
		initialize();
	}

	public void initialize() {
		Dimension d = getSize();
		
		if (d != null) {
			
		    if (vertex_ordered_list == null) {
		        setVertexOrder(new ArrayList<V>(getGraph().getVertices()));
		    }

			int iVertex = 0;
			
			int columns =  (int)((d.getWidth() - 10)/50);
			int row =  (int)(d.getHeight()/(getGraph().getVertices().size()/columns));
			int iRow = 20;
			
			for (V v : vertex_ordered_list) {
				
				Point2D coord = transform(v);

				if (iVertex >= columns) {
					
					iVertex = 0;
					
					iRow += row;
					
				} 
				
				coord.setLocation(iVertex * 50 + 30, iRow);
				
				iVertex += 1;
				
			}
			
		}
		
	}

}