package edu.umich.eecs.tac.viewer.role;

import edu.umich.eecs.tac.viewer.TACAASimulationPanel;
import edu.umich.eecs.tac.viewer.ViewListener;
import edu.umich.eecs.tac.TACAAConstants;

import java.awt.*;
import java.util.Map;
import java.util.HashMap;

import se.sics.isl.transport.Transportable;

/**
 * @author Patrick Jordan
 */
public class AdvertiserTabPanel extends SimulationTabPanel {
    private Map<String,AdvertiserAgentPanel> agentPanels;

    public AdvertiserTabPanel(TACAASimulationPanel simulationPanel) {
        super(simulationPanel);

        agentPanels = new HashMap<String, AdvertiserAgentPanel>();

        simulationPanel.addViewListener(new ParticipantListener());

        initialize();
    }

    private void initialize() {
        setLayout(new GridLayout(2,4));
    }

    private class ParticipantListener implements ViewListener {

        public void dataUpdated(int agent, int type, int value) {
        }

        public void dataUpdated(int agent, int type, long value) {
        }

        public void dataUpdated(int agent, int type, float value) {
        }

        public void dataUpdated(int agent, int type, double value) {
        }

        public void dataUpdated(int agent, int type, String value) {
        }

        public void dataUpdated(int agent, int type, Transportable value) {
        }

        public void dataUpdated(int type, Transportable value) {
        }

        public void participant(int agent, int role, String name, int participantID) {
            if(!agentPanels.containsKey(name) && role == TACAAConstants.ADVERTISER) {
                AdvertiserAgentPanel agentPanel = new AdvertiserAgentPanel(agent,name,AdvertiserTabPanel.this);

                agentPanels.put(name, agentPanel);

                add(agentPanel);
            }
        }
    }
}