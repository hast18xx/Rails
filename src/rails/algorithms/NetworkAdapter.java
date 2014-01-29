package rails.algorithms;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rails.game.GameManager;
import rails.game.PublicCompany;

/**
 * This class coordinates the creation of company related graphs
 */
public class NetworkAdapter {

    protected static Logger log =
            LoggerFactory.getLogger(NetworkAdapter.class);

    private final GameManager gameManager;
    
    private NetworkGraph mapGraph;
    private NetworkGraph routeGraph;
    private NetworkGraph revenueGraph;
    private NetworkMultigraph multiGraph;

    private PublicCompany company;
    private boolean addHQ;
    
    private NetworkAdapter(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    public static NetworkAdapter create(GameManager gameManager) {
        return new NetworkAdapter(gameManager);
    }
    
    public NetworkGraph getMapGraph() {
        if (mapGraph == null) {
            mapGraph = NetworkGraph.createMapGraph(gameManager);
            log.info("MapGraph created");
        }
        return mapGraph;
    }
        
    public NetworkGraph getRouteGraph(PublicCompany company, boolean addHQ) {
        if (routeGraph == null || company != this.company || addHQ != this.addHQ) {
            routeGraph = NetworkGraph.createRouteGraph(getMapGraph(), company, addHQ);
            this.company = company;
            this.addHQ = addHQ;
            log.info("RouteGraph created");
        }
        return routeGraph;
    }
    
    public NetworkGraph getRevenueGraph(PublicCompany company,
            Collection<NetworkVertex> protectedVertices) {
        if (revenueGraph == null) {
            revenueGraph = NetworkGraph.createOptimizedGraph(getRouteGraph(company, false),
                    protectedVertices);
            log.info("RevenueGraph created");
        }
        
        return revenueGraph;
    }
    
    public NetworkMultigraph getMultigraph(PublicCompany company,
            Collection<NetworkVertex> protectedVertices) {
        if (multiGraph == null) {
            multiGraph = NetworkMultigraph.create(
                    getRevenueGraph(company, protectedVertices), protectedVertices);
            log.info("MultiGraph created");
        }
        return multiGraph;
    }
    
}
