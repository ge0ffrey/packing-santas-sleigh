/*
 * Copyright 2013 wleite
 *
 * https://www.kaggle.com/c/packing-santas-sleigh/forums/t/6579/two-visualizations/36721#post36721
 */

package org.optaplannerdelirium.pss.swingui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JPanel;

import org.optaplannerdelirium.pss.domain.PresentAllocation;

public class Panel3D extends JPanel implements KeyListener {

    private double alfa = Math.PI / 4;
    private double beta = 0;
    private double zoom = 0.4;
    private double sinAlfa;
    private double cosAlfa;
    private double sinBeta;
    private double cosBeta;
    private Color[][] colors = new Color[15][3];

    private List<PresentAllocation> visualPresentAllocationList = new ArrayList<PresentAllocation>();
    private boolean updated = false;

    public Panel3D() {
        alfa += 2 * (Math.PI / 36);
        beta += 10 * (Math.PI / 36);
        updateSinCos();
        initColors();
        addKeyListener(this);
    }

    private void updateSinCos() {
        sinAlfa = Math.sin(alfa);
        sinBeta = Math.sin(beta);
        cosAlfa = Math.cos(alfa);
        cosBeta = Math.cos(beta);
    }

    private void initColors() {
        int alfa = 240;
        colors[0][0] = new Color(255, 40, 40, alfa);
        colors[1][0] = new Color(40, 255, 40, alfa);
        colors[2][0] = new Color(40, 40, 255, alfa);
        colors[3][0] = new Color(255, 255, 40, alfa);
        colors[4][0] = new Color(40, 255, 255, alfa);
        colors[5][0] = new Color(255, 40, 255, alfa);
        colors[6][0] = new Color(255, 128, 40, alfa);
        colors[7][0] = new Color(40, 128, 255, alfa);
        colors[8][0] = new Color(255, 40, 128, alfa);
        colors[9][0] = new Color(128, 255, 40, alfa);
        colors[10][0] = new Color(40, 255, 128, alfa);
        colors[11][0] = new Color(128, 40, 255, alfa);
        colors[12][0] = new Color(128, 40, 40, alfa);
        colors[13][0] = new Color(40, 128, 40, alfa);
        colors[14][0] = new Color(40, 40, 128, alfa);
        for (int i = 0; i < colors.length; i++) {
            Color c = colors[i][0];
            if (c == null) {
                continue;
            }
            colors[i][1] = darken(c, 40);
            colors[i][2] = darken(c, 80);
        }
    }

    private Color darken(Color c, int v) {
        return new Color(Math.max(0, c.getRed() - v), Math.max(0, c.getGreen() - v), Math.max(0, c.getBlue() - v), c.getAlpha());
    }

    public void setVisualPresentAllocationList(List<PresentAllocation> visualPresentAllocationList) {
        this.visualPresentAllocationList = visualPresentAllocationList;
        update();
    }

    public void paintComponent(Graphics g) {
        if (!updated) {
            super.paintComponent(g);
            return;
        }
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(getBackground());
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.GRAY);
        g2.setStroke(new BasicStroke(0.4f));

        Point2D c1 = to2D(1000, 1000, 0);
        Point2D c2 = to2D(0, 0, 480);
        Point2D c3 = to2D(1000, 0, 0);
        Point2D c4 = to2D(0, 1000, 0);
        g2.translate(getWidth() / 2 - (c3.getX() + c4.getX()) / 2, getHeight() / 2 - (c1.getY() + c2.getY()) / 2);

        for (int i = 0; i <= 1000; i += 40) {
            Point2D a = to2D(i, 0, 0);
            Point2D b = to2D(i, 0, 480);
            g2.draw(new Line2D.Double(a, b));
            Point2D c = to2D(i, 1000, 0);
            g2.draw(new Line2D.Double(a, c));
            Point2D d = to2D(0, i, 0);
            Point2D e = to2D(0, i, 480);
            g2.draw(new Line2D.Double(d, e));
            Point2D f = to2D(1000, i, 0);
            g2.draw(new Line2D.Double(d, f));
            Point2D h = to2D(0, 0, i);
            Point2D j = to2D(1000, 0, i);
            if (i <= 480) {
                g2.draw(new Line2D.Double(h, j));
            }
            Point2D k = to2D(0, 1000, i);
            if (i <= 480) {
                g2.draw(new Line2D.Double(h, k));
            }
        }
        g2.setStroke(new BasicStroke(0.4f));
        for (PresentAllocation presentAllocation : visualPresentAllocationList) {
            if (presentAllocation.getRotation() == null) {
                continue;
            }
            Point2D a = to2D(presentAllocation.getCalculatedX(), presentAllocation.getCalculatedY(), presentAllocation.getCalculatedZ() + presentAllocation.getZLength());
            Point2D k = to2D(presentAllocation.getCalculatedX() + presentAllocation.getXLength(), presentAllocation.getCalculatedY(), presentAllocation.getCalculatedZ() + presentAllocation.getZLength());
            Point2D c = to2D(presentAllocation.getCalculatedX() + presentAllocation.getXLength(), presentAllocation.getCalculatedY() + presentAllocation.getYLength(), presentAllocation.getCalculatedZ() + presentAllocation.getZLength());
            Point2D d = to2D(presentAllocation.getCalculatedX(), presentAllocation.getCalculatedY() + presentAllocation.getYLength(), presentAllocation.getCalculatedZ() + presentAllocation.getZLength());
            Point2D f = to2D(presentAllocation.getCalculatedX() + presentAllocation.getXLength(), presentAllocation.getCalculatedY(), presentAllocation.getCalculatedZ());
            Point2D h = to2D(presentAllocation.getCalculatedX() + presentAllocation.getXLength(), presentAllocation.getCalculatedY() + presentAllocation.getYLength(), presentAllocation.getCalculatedZ());
            Point2D j = to2D(presentAllocation.getCalculatedX(), presentAllocation.getCalculatedY() + presentAllocation.getYLength(), presentAllocation.getCalculatedZ());
            Color[] color = colors[presentAllocation.getId().intValue() % colors.length];
            Path2D p0 = poly(a, k, c, d);
            Path2D p1 = poly(j, h, c, d);
            Path2D p2 = poly(f, h, c, k);
            g2.setColor(color[0]);
            g2.fill(p0);
            g2.setColor(color[1]);
            g2.fill(p1);
            g2.setColor(color[2]);
            g2.fill(p2);
            g2.setColor(color[0]);
            g2.draw(p0);
            g2.draw(p1);
            g2.draw(p2);
        }
        g2.translate(0, 0);
    }

    private Path2D poly(Point2D a, Point2D b, Point2D c, Point2D d) {
        Path2D p = new Path2D.Double();
        p.moveTo(a.getX(), a.getY());
        p.lineTo(b.getX(), b.getY());
        p.lineTo(c.getX(), c.getY());
        p.lineTo(d.getX(), d.getY());
        p.closePath();
        return p;
    }

    public void update() {
        Collections.sort(visualPresentAllocationList, new Comparator<PresentAllocation>() {
            public int compare(PresentAllocation a, PresentAllocation b) {
                if (a.getRotation() == null && b.getRotation() == null) {
                    return a.getId() < b.getId() ? -1 : a.getId() > b.getId() ? 1 : 0;
                } else if (a.getRotation() == null) {
                    return -1;
                } else if (b.getRotation() == null) {
                    return 1;
                }
                if (a.getCalculatedX() >= b.getCalculatedX() + b.getXLength()) {
                    return 1;
                }
                if (b.getCalculatedX() >= a.getCalculatedX() + a.getXLength()) {
                    return -1;
                }

                if (a.getCalculatedY() >= b.getCalculatedY() + b.getYLength()) {
                    return 1;
                }
                if (b.getCalculatedY() >= a.getCalculatedY() + a.getYLength()) {
                    return -1;
                }

                if (a.getCalculatedZ() >= b.getCalculatedZ() + b.getZLength()) {
                    return 1;
                }
                if (b.getCalculatedZ() >= a.getCalculatedZ() + a.getZLength()) {
                    return -1;
                }
                return 0;
            }
        });
        updated = true;
        repaint();
        requestFocusInWindow();
    }

    private Point2D to2D(int x, int y, int z) {
        return new Point2D.Double((x * sinAlfa - y * cosAlfa) * zoom, ((x * cosAlfa + y * sinAlfa) * cosBeta - z * sinBeta) * zoom);
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyReleased(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            beta += Math.PI / 36;
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            beta -= Math.PI / 36;
        } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            alfa -= Math.PI / 36;
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            alfa += Math.PI / 36;
        } else if (e.getKeyCode() == KeyEvent.VK_A) {
            zoom *= 1.1;
        } else if (e.getKeyCode() == KeyEvent.VK_Z) {
            zoom /= 1.1;
        } else if (e.getKeyCode() == KeyEvent.VK_O) {
            alfa = Math.PI / 4;
            beta = Math.PI / 3;
            zoom = 0.4;
        } else {
            return;
        }
        if (beta < 0) {
            beta = 0;
        } else if (beta > Math.PI / 2) {
            beta = Math.PI / 2;
        }
        if (alfa < 0) {
            alfa = 0;
        } else if (alfa > Math.PI / 2) {
            alfa = Math.PI / 2;
        }
        updateSinCos();
        repaint();
    }
}
