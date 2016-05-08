package org.snail.viewer.widget;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;
import javax.swing.JProgressBar;
import javax.swing.plaf.basic.BasicProgressBarUI;

class ProgressCircleUI extends BasicProgressBarUI {
	@Override
	public Dimension getPreferredSize(JComponent c) {
		Dimension d = super.getPreferredSize(c);
		int v = Math.max(d.width, d.height);
		d.setSize(v, v);
		return d;
	}

	@Override
	public void paint(Graphics g, JComponent c) {
		Insets b = progressBar.getInsets(); // area for border
		int barRectWidth = progressBar.getWidth() - b.right - b.left;
		int barRectHeight = progressBar.getHeight() - b.top - b.bottom;
		if (barRectWidth <= 0 || barRectHeight <= 0) {
			return;
		}

		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		double degree = 360 * progressBar.getPercentComplete();
		double sz = Math.min(barRectWidth, barRectHeight);
		double cx = b.left + barRectWidth * .5;
		double cy = b.top + barRectHeight * .5;
		double or = sz * .5;
		// double ir = or - 20;
		double ir = or * .5; // .8;
		Shape inner = new Ellipse2D.Double(cx - ir, cy - ir, ir * 2, ir * 2);
		Shape outer = new Ellipse2D.Double(cx - or, cy - or, sz, sz);
		Shape sector = new Arc2D.Double(cx - or, cy - or, sz, sz, 90 - degree, degree, Arc2D.PIE);

		Area foreground = new Area(sector);
		Area background = new Area(outer);
		Area hole = new Area(inner);

		foreground.subtract(hole);
		background.subtract(hole);

		g2.setPaint(new Color(0xDDDDDD));
		g2.fill(background);
		g2.setPaint(progressBar.getForeground());
		g2.fill(foreground);
		g2.dispose();

		// Deal with possible text painting
		if (progressBar.isStringPainted()) {
			paintString(g, b.left, b.top, barRectWidth, barRectHeight, 0, b);
		}

	}

}

class ProgressListener implements PropertyChangeListener {
	private final JProgressBar progressBar;

	ProgressListener(JProgressBar progressBar) {
		this.progressBar = progressBar;
		this.progressBar.setValue(0);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String strPropertyName = evt.getPropertyName();
		if ("progress".equals(strPropertyName)) {
			progressBar.setIndeterminate(false);
			int progress = (Integer) evt.getNewValue();
			progressBar.setValue(progress);
		}
	}
}
