package net.sf.rails.game.specific._18Scan;

import net.sf.rails.game.PublicCompany;
import net.sf.rails.game.RailsItem;
import net.sf.rails.game.financial.StockSpace;

public class PublicCompany_18Scan extends PublicCompany {

    public PublicCompany_18Scan (RailsItem parent, String id) {
        super(parent, id);
    }

    @Override
    public void start(StockSpace startSpace) {

        // From phase 5, full capitalization applies
        if (getRoot().getPhaseManager().hasReachedPhase("5")
                && "Major".equalsIgnoreCase(getType().getId())) {
            this.capitalisation = CAPITALISE_FULL;
        }

        super.start(startSpace);
    }


        @Override
    public boolean canGenerateRevenue() {
        // A check for all safety, only minors should get here
        if ("Minor".equalsIgnoreCase(getType().getId())) {
            // Even without trains, 18Scan minors yield some money
            return true;
        } else {
            return canRunTrains();
        }
    }

}
