package edu.umich.eecs.tac.viewer.role;

import edu.umich.eecs.tac.TACAAConstants;
import edu.umich.eecs.tac.props.Query;
import edu.umich.eecs.tac.props.QueryReport;
import edu.umich.eecs.tac.props.RetailCatalog;
import edu.umich.eecs.tac.props.SalesReport;
import edu.umich.eecs.tac.viewer.TACAASimulationPanel;
import edu.umich.eecs.tac.viewer.TACAAViewerConstants;
import edu.umich.eecs.tac.viewer.ViewAdaptor;
import static edu.umich.eecs.tac.viewer.ViewerChartFactory.createDifferenceChart;
import static edu.umich.eecs.tac.viewer.ViewerUtils.buildQuerySpace;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import se.sics.isl.transport.Transportable;
import se.sics.tasim.viewer.TickListener;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;
/**
 * @author Guha Balakrishnan
 */
public class AgentRevCostPanel extends JPanel {
    private int agent;
    private String advertiser;
    private XYSeriesCollection seriescollection;
    private XYSeries revSeries;
    private XYSeries costSeries;
    private int currentDay;
    private Set<Query> queries;
    private boolean showBorder;

    public AgentRevCostPanel(int agent, String advertiser,
                             TACAASimulationPanel simulationPanel, boolean showBorder) {

        setBackground(TACAAViewerConstants.CHART_BACKGROUND);
        revSeries = new XYSeries("Revenue");
        costSeries = new XYSeries("Cost");
        seriescollection = new XYSeriesCollection();

        this.showBorder = showBorder;
        this.agent = agent;
        this.advertiser = advertiser;
        simulationPanel.addTickListener(new DayListener());
        simulationPanel.addViewListener(new DataUpdateListener());
        initialize();
    }

    private void initialize() {
        setLayout(new GridLayout(1, 1));
        if (showBorder) {
            setBorder(BorderFactory.createTitledBorder("Revenue and Cost"));
        }
        queries = new HashSet<Query>();
        seriescollection.addSeries(revSeries);
        seriescollection.addSeries(costSeries);

        JFreeChart chart = createDifferenceChart(showBorder ? null : advertiser, seriescollection);
        ChartPanel chartpanel = new ChartPanel(chart, false);
        chartpanel.setMouseZoomable(true, false);
        add(chartpanel);
    }

    private void handleRetailCatalog(RetailCatalog retailCatalog) {
        queries.clear();

        buildQuerySpace(queries, retailCatalog);
    }


    protected class DayListener implements TickListener {

        public void tick(long serverTime) {
            AgentRevCostPanel.this.tick(serverTime);
        }

        public void simulationTick(long serverTime, int simulationDate) {
            AgentRevCostPanel.this.simulationTick(serverTime, simulationDate);
        }
    }

    protected void tick(long serverTime) {
    }

    protected void simulationTick(long serverTime, int simulationDate) {
        currentDay = simulationDate;
    }

    private double getDayCost(QueryReport report) {

        double result = 0;
        for(Query query : queries) {
            result = result + report.getCost(query);

        }
        return result;

    }

    private double getDayRevenue(SalesReport report) {
        double result = 0;
        for(Query query : queries) {
            result = result + report.getRevenue(query);

        }
        return result;
    }

    private class DataUpdateListener extends ViewAdaptor {

        public void dataUpdated(final int agent, final int type, final Transportable value) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (type == TACAAConstants.DU_QUERY_REPORT &&
                        value.getClass().equals(QueryReport.class) &&
                        agent == AgentRevCostPanel.this.agent) {

                        QueryReport queryReport = (QueryReport) value;

                        costSeries.addOrUpdate(currentDay, AgentRevCostPanel.this.getDayCost(queryReport));
                    }

                    if (type == TACAAConstants.DU_SALES_REPORT &&
                        value.getClass().equals(SalesReport.class) &&
                        agent == AgentRevCostPanel.this.agent) {

                        SalesReport salesReport = (SalesReport) value;

                        revSeries.addOrUpdate(currentDay, AgentRevCostPanel.this.getDayRevenue(salesReport));
                    }
                }
            });
        }

        public void dataUpdated(final int type, final Transportable value) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    Class valueType = value.getClass();
                    if (valueType == RetailCatalog.class) {
                        handleRetailCatalog((RetailCatalog) value);
                    }
                }
            });
        }
    }
}
