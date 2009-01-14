package edu.umich.eecs.tac;

import se.sics.tasim.logtool.ParticipantInfo;
import se.sics.tasim.logtool.LogReader;
import se.sics.tasim.props.ServerConfig;
import se.sics.tasim.props.StartInfo;
import se.sics.isl.transport.Transportable;

import java.util.logging.Logger;
import java.util.Hashtable;
import java.util.ArrayList;
import java.io.IOException;
import java.text.ParseException;

import edu.umich.eecs.tac.props.RetailCatalog;
import edu.umich.eecs.tac.props.AuctionInfo;
import com.botbox.util.ArrayUtils;

/**
 * @author Patrick Jordan
 */
public class TACAASimulationInfo extends Parser {

    private static final Logger log = Logger.getLogger(TACAASimulationInfo.class.getName());

    private int simID;
    private int uniqueID;
    private String simType;
    private String simParams;
    private long startTime;
    private int simLength;

    private String serverName;
    private String serverVersion;

    private ServerConfig serverConfig;

    private AuctionInfo auctionInfo;
    private RetailCatalog retailCatalog;

    private Participant[] participants;
    private Hashtable participantTable;

    private int[] agentRoles;
    private Participant[][] agentsPerRole;
    private int agentRoleNumber;

    private int currentDate = 0;

    private boolean isParsingExtended = false;

    // Note: the context must have been set in the log reader before
    // this object is created!
    public TACAASimulationInfo(LogReader logReader) throws IOException, ParseException {
        super(logReader);

        ParticipantInfo[] infos = logReader.getParticipants();
        participants = new Participant[infos == null ? 0 : infos.length];
        participantTable = new Hashtable();
        for (int i = 0, n = participants.length; i < n; i++) {
            ParticipantInfo info = infos[i];
            if (info != null) {
                participants[i] = new Participant(info);
                participantTable.put(info.getAddress(), participants[i]);
            }
        }
        simID = logReader.getSimulationID();
        uniqueID = logReader.getUniqueID();
        simType = logReader.getSimulationType();
        simParams = logReader.getSimulationParams();
        startTime = logReader.getStartTime();
        simLength = logReader.getSimulationLength();
        serverName = logReader.getServerName();
        serverVersion = logReader.getServerVersion();

        start();

        // Extract the rest of the information
        Participant[] advertisers = getParticipantsByRole(ADVERTISER);
        /*if (advertisers != null) {
            Participant m = advertisers[0];
            StartInfo si = m.getStartInfo();
        } */
    }

    public String getServerName() {
        return serverName;
    }

    public String getServerVersion() {
        return serverVersion;
    }

    public int getUniqueID() {
        return uniqueID;
    }

    public int getSimulationID() {
        return simID;
    }

    public String getSimulationType() {
        return simType;
    }

    public long getStartTime() {
        return startTime;
    }

    public int getSimulationLength() {
        return simLength;
    }

    public ServerConfig getServerConfig() {
        return serverConfig;
    }

    public Participant getParticipant(int agentIndex) {
        Participant p;
        if (agentIndex >= participants.length || ((p = participants[agentIndex]) == null)) {
            throw new IllegalArgumentException("no participant " + agentIndex);
        }
        return p;
    }

    public Participant getParticipant(String address) {
        Participant p = (Participant) participantTable.get(address);
        if (p == null) {
            throw new IllegalArgumentException("no participant " + address);
        }
        return p;
    }

    public int getParticipantCount() {
        return participants.length;
    }

    public Participant[] getParticipants() {
        return participants;
    }

    public Participant[] getParticipantsByRole(int role) {
        int index = ArrayUtils.indexOf(agentRoles, 0, agentRoleNumber, role);
        if (index < 0) {
            if (agentRoles == null) {
                agentRoles = new int[5];
                agentsPerRole = new Participant[5][];
            } else if (agentRoleNumber == agentRoles.length) {
                agentRoles = ArrayUtils.setSize(agentRoles, agentRoleNumber + 5);
                agentsPerRole = (Participant[][]) ArrayUtils.setSize(agentsPerRole, agentRoleNumber + 5);
            }

            ArrayList list = new ArrayList();
            for (int i = 0, n = participants.length; i < n; i++) {
                Participant a = participants[i];
                if ((a != null) && (a.getInfo().getRole() == role)) {
                    list.add(a);
                }
            }

            index = agentRoleNumber;
            agentsPerRole[agentRoleNumber] = list.size() > 0 ? (Participant[]) list
                    .toArray(new Participant[list.size()]) : null;
            agentRoles[agentRoleNumber++] = role;
        }
        return agentsPerRole[index];
    }

    // -------------------------------------------------------------------
    // Parser callback handling
    // -------------------------------------------------------------------

    protected void messageToRole(int sender, int role, Transportable content) {
        for (int i = 0, n = participants.length; i < n; i++) {
            if (participants[i].getInfo().getRole() == role) {
                participants[i].messageReceived(currentDate, sender, content);
            }
        }

        if (sender != 0) {
            getParticipant(sender).messageSentToRole(currentDate, role, content);
        }
    }

    protected void message(int sender, int receiver, Transportable content) {
        // Ignore messages to the coordinator
        if (receiver != 0) {
            getParticipant(receiver).messageReceived(currentDate, sender, content);
            if (sender != 0) {
                getParticipant(sender).messageSent(currentDate, receiver, content);
            }
        }
    }

    protected void data(Transportable object) {
        if (object instanceof ServerConfig) {
            this.serverConfig = (ServerConfig) object;
        }
    }

    protected void dataUpdated(int agentIndex, int type, int value) {
        if (type == DU_BANK_ACCOUNT) {
            Participant p = getParticipant(agentIndex);
            p.setResult(value);
        }
    }

    protected void dataUpdated(int agentIndex, int type, long value) {
        if (type == DU_BANK_ACCOUNT) {
            Participant p = getParticipant(agentIndex);
            p.setResult(value);
        }
    }

    protected void dataUpdated(int agent, int type, float value) {
    }

    protected void dataUpdated(int agent, int type, String value) {
    }

    protected void dataUpdated(int agent, int type, Transportable content) {
    }

    protected void dataUpdated(int type, Transportable object) {
        if (object instanceof AuctionInfo) {
            this.auctionInfo = (AuctionInfo) object;
        } else if (object instanceof RetailCatalog) {
            this.retailCatalog = (RetailCatalog) object;
        }
    }

    protected void impressions(int agent, long amount) {
        Participant agentInfo = getParticipant(agent);
        agentInfo.addImpressions(amount);
    }

    protected void clicks(int agent, long amount) {
        Participant agentInfo = getParticipant(agent);
        agentInfo.addClicks(amount);
    }

    protected void conversions(int agent, long amount) {
        Participant agentInfo = getParticipant(agent);
        agentInfo.addConversions(amount);
    }


    protected void transaction(int from, int to, long amount) {
        Participant fromP = getParticipant(from);
        Participant toP = getParticipant(to);
        toP.addCost(amount);
        fromP.addRevenue(amount);
    }

    protected void nextDay(int date, long serverTime) {
        this.currentDate = date;
    }

    protected void unhandledNode(String nodeName) {
        // Ignore anything else for now
        log.warning("ignoring unhandled node '" + nodeName + '\'');
    }
} // TACSCMSimulationInfo
