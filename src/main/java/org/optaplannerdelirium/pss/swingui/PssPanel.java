/*
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplannerdelirium.pss.swingui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.optaplanner.core.impl.solution.Solution;
import org.optaplanner.examples.common.swingui.SolutionPanel;
import org.optaplannerdelirium.pss.domain.Sleigh;

public class PssPanel extends SolutionPanel {

    private Pss3DPanel pss3DPanel;
    private JTextField fromIndexField;
    private JTextField toIndexField;

    public PssPanel() {
        setLayout(new BorderLayout());
        JPanel northPanel = createNorthPanel();
        add(northPanel, BorderLayout.NORTH);
        pss3DPanel = new Pss3DPanel();
        add(pss3DPanel, BorderLayout.CENTER);
    }

    private JPanel createNorthPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.add(new JLabel("Index from"));
        fromIndexField = new JTextField("0", 7);
        panel.add(fromIndexField);
        panel.add(new JLabel("to"));
        toIndexField = new JTextField("100", 7);
        panel.add(toIndexField);
        JButton refreshButton = new JButton("Refresh");
        panel.add(refreshButton);
        ActionListener actionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Sleigh sleigh = (Sleigh) solutionBusiness.getSolution();
                refreshVisualPresentAllocations(sleigh);
            }
        };
        fromIndexField.addActionListener(actionListener);
        toIndexField.addActionListener(actionListener);
        refreshButton.addActionListener(actionListener);
        return panel;
    }

    public void resetPanel(Solution solution) {
        Sleigh sleigh = (Sleigh) solution;
        refreshVisualPresentAllocations(sleigh);
    }

    public void refreshVisualPresentAllocations(Sleigh sleigh) {
        int fromIndex = Integer.parseInt(fromIndexField.getText());
        int toIndex = Integer.parseInt(toIndexField.getText());
        pss3DPanel.setVisualPresentAllocationList(sleigh.getPresentAllocationList().subList(fromIndex, toIndex));
    }

}
