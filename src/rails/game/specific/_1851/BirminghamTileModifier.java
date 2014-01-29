package rails.game.specific._1851;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jgrapht.graph.SimpleGraph;

import rails.algorithms.NetworkEdge;
import rails.algorithms.NetworkGraph;
import rails.algorithms.NetworkGraphModifier;
import rails.algorithms.NetworkVertex;
import rails.game.GameManager;
import rails.game.GameManager;
import rails.game.MapHex;
import rails.game.RailsRoot;

public class BirminghamTileModifier implements NetworkGraphModifier {

    protected static Logger log =
        LoggerFactory.getLogger(BirminghamTileModifier.class);

    public void modifyGraph(NetworkGraph mapGraph) {
        
        // TODO (Rails 2.0): Add root reference to modifiers
        SimpleGraph<NetworkVertex, NetworkEdge> graph = mapGraph.getGraph();
        RailsRoot root = RailsRoot.getInstance();
        
        // 1. check Phase
        // this is a violation of the assumption that the track network only dependents on the map configuration
        // but not on other things (like phases)
        int phaseIndex = root.getPhaseManager().getCurrentPhase().getIndex(); 
        if (phaseIndex >= 2 ) {
            log.debug("Birmingham active, index of phase = " + phaseIndex);
            return;
        }
        
        // 2. retrieve Birmingham vertices ...
        MapHex birmingHex = root.getMapManager().getHex("J12");
        Set<NetworkVertex> birmingVertices = NetworkVertex.getVerticesByHex(graph.vertexSet(), birmingHex);

        // 3 ... and remove them from the graph
        graph.removeAllVertices(birmingVertices);
        log.debug("Birmingham inactive, index of phase = " + phaseIndex);
        
    }

}
