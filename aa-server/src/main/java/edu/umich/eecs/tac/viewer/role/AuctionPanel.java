package edu.umich.eecs.tac.viewer.role;

import edu.umich.eecs.tac.props.Query;
import edu.umich.eecs.tac.props.BidBundle;
import edu.umich.eecs.tac.viewer.ViewListener;
import edu.umich.eecs.tac.TACAAConstants;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.HashMap;

import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.Day;
import org.jfree.data.xy.XYDataset;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.plot.XYPlot;
import org.jfree.ui.RectangleInsets;
import se.sics.isl.transport.Transportable;
import se.sics.tasim.viewer.TickListener;
import com.botbox.util.ArrayUtils;

/**
 * @author Patrick Jordan
 */
public class AuctionPanel extends JComponent implements TACAAConstants {
    private Query query;

    private TimeSeriesCollection timeseriescollection;
    private Day currentDay;
    private PublisherTabPanel publisherPanel;
    private Map<String, TimeSeries> bidSeries;

    public AuctionPanel(Query query, PublisherTabPanel publisherPanel) {
        this.query = query;
        this.publisherPanel = publisherPanel;
        this.bidSeries = new HashMap<String, TimeSeries>();
        this.currentDay = new Day();
        initialize();

        publisherPanel.getSimulationPanel().addViewListener(new BidBundleListener());
        publisherPanel.getSimulationPanel().addTickListener(new DayListener());
    }


    protected void initialize() {
        setLayout(new GridLayout(1, 1));

        timeseriescollection = new TimeSeriesCollection();
        JFreeChart chart = createChart(timeseriescollection);
        ChartPanel chartpanel = new ChartPanel(chart, false);
        chartpanel.setMouseZoomable(true, false);

        add(chartpanel);

        // Participants will be added to the publisher panel before getting here.
        int count = publisherPanel.getAgentCount();

        for (int index = 0; index < count; index++) {
            if (publisherPanel.getRole(index) == TACAAConstants.ADVERTISER) {
                TimeSeries series = new TimeSeries(publisherPanel.getAgentName(index), Day.class);
                bidSeries.put(publisherPanel.getAgentName(index), series);
                timeseriescollection.addSeries(series);
            }
        }
    }


    private JFreeChart createChart(XYDataset xydataset) {
        JFreeChart jfreechart = ChartFactory.createTimeSeriesChart(String.format("Auction for (%s,%s)", getQuery().getManufacturer(), getQuery().getComponent()), "Day", "Bid [$]", xydataset, true, true, false);
        jfreechart.setBackgroundPaint(Color.white);
        XYPlot xyplot = (XYPlot) jfreechart.getPlot();
        xyplot.setBackgroundPaint(Color.lightGray);
        xyplot.setDomainGridlinePaint(Color.white);
        xyplot.setRangeGridlinePaint(Color.white);
        xyplot.setAxisOffset(new RectangleInsets(5D, 5D, 5D, 5D));
        xyplot.setDomainCrosshairVisible(true);
        xyplot.setRangeCrosshairVisible(true);
        xyplot.getDomainAxis().setVisible(false);
        
        org.jfree.chart.renderer.xy.XYItemRenderer xyitemrenderer = xyplot.getRenderer();
        if (xyitemrenderer instanceof XYLineAndShapeRenderer) {
            XYLineAndShapeRenderer xylineandshaperenderer = (XYLineAndShapeRenderer) xyitemrenderer;
            xylineandshaperenderer.setBaseShapesVisible(false);
        }
        return jfreechart;
    }

    public Query getQuery() {
        return query;
    }


    private class BidBundleListener implements ViewListener {

        public void dataUpdated(int agent, int type, int value) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void dataUpdated(int agent, int type, long value) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void dataUpdated(int agent, int type, float value) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void dataUpdated(int agent, int type, double value) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void dataUpdated(int agent, int type, String value) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void dataUpdated(int agent, int type, Transportable value) {
            if (type == TACAAConstants.DU_BIDS && value.getClass().equals(BidBundle.class)) {
                int index = publisherPanel.indexOfAgent(agent);
                String name = index < 0 ? null : publisherPanel.getAgentName(index);

                if (name != null) {
                    TimeSeries timeSeries = bidSeries.get(name);

                    if (timeSeries != null) {


                        BidBundle bundle = (BidBundle) value;

                        double bid = bundle.getBid(query);
                        if (!Double.isNaN(bid)) {
                            timeSeries.add(currentDay, bid);
                        }
                    }
                }
            }
        }

        public void dataUpdated(int type, Transportable value) {

        }

        public void participant(int agent, int role, String name, int participantID) {

        }
    }

    protected class DayListener implements TickListener {

        public void tick(long serverTime) {
            AuctionPanel.this.tick(serverTime);
        }

        public void simulationTick(long serverTime, int simulationDate) {
            AuctionPanel.this.simulationTick(serverTime, simulationDate);
        }
    }

    protected void tick(long serverTime) {
    }

    protected void simulationTick(long serverTime, int simulationDate) {
        currentDay = (Day) currentDay.next();
    }
}