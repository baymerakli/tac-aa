package edu.umich.eecs.tac.sim;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;
import edu.umich.eecs.tac.props.*;

import java.util.Map;
import java.util.HashMap;

import se.sics.tasim.sim.SimulationAgent;

/**
 * @author Patrick Jordan, Ben Cassell
 */
public class DefaultSalesAnalystTest {
    DefaultSalesAnalyst salesAnalyst;
    AgentRepository repository;
    SalesReportSender salesReportSender;
    Map<String, AdvertiserInfo> advertiserInfo;


    String alice;

    private AuctionInfo auctionInfo;

    @Before
    public void setup() {
        auctionInfo = new AuctionInfo();
        
        repository = new SimpleAgentRepository();
        salesReportSender = new SimpleSalesReportSender();

        advertiserInfo = new HashMap<String, AdvertiserInfo>();
        AdvertiserInfo info  = new AdvertiserInfo();
        info.setDistributionWindow(2);
        info.setDistributionCapacity(10);
        
        alice = "alice";
        advertiserInfo.put(alice, info);

        salesAnalyst = new DefaultSalesAnalyst(repository, salesReportSender, 1);

    }

    @Test
    public void testConstructor() {
        assertNotNull(salesAnalyst);
    }

    @Test
    public void testAddAccount() {
        assertEquals(salesAnalyst.size(), 0);
        salesAnalyst.addAccount(alice);
        assertEquals(salesAnalyst.size(), 1);
    }

    @Test
    public void testConversions() {
        salesAnalyst.addAccount(alice);

        assertEquals(salesAnalyst.getRecentConversions(alice),0.0);

        salesAnalyst.addConversions(alice,new Query(),7,2.0);

        assertEquals(salesAnalyst.getRecentConversions(alice),7.0);

        salesAnalyst.sendSalesReportToAll();

        assertEquals(salesAnalyst.getRecentConversions(alice),7.0);

        salesAnalyst.addConversions(alice,new Query(),5,2.0);

        assertEquals(salesAnalyst.getRecentConversions(alice),12.0);

        salesAnalyst.sendSalesReportToAll();
        
        assertEquals(salesAnalyst.getRecentConversions(alice),5.0);
    }

    public class SimpleAgentRepository implements AgentRepository {
        public RetailCatalog getRetailCatalog() {
            return null;
        }

        public Map<String, AdvertiserInfo> getAdvertiserInfo() {
            return advertiserInfo;
        }

        public SimulationAgent[] getPublishers() {
            return new SimulationAgent[0];
        }

        public SimulationAgent[] getUsers() {
            return new SimulationAgent[0];
        }

        public SalesAnalyst getSalesAnalyst() {
            return salesAnalyst;
        }

        public AuctionInfo getAuctionInfo() {
            return auctionInfo;
        }


        public int getNumberOfAdvertisers() {
            return advertiserInfo.size();
        }


        public String[] getAdvertiserAddresses() {
            return advertiserInfo.keySet().toArray(new String[0]);
        }
    }

    public class SimpleSalesReportSender implements SalesReportSender {
        public void sendSalesReport(String advertiser, SalesReport report) {
        }

       public void broadcastConversions(String advertiser, int conversions) {
//           assertEquals(conversions,1);
        }
    }
}
