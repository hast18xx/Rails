/* $Header: /Users/blentz/rails_rcs/cvs/18xx/rails/ui/swing/hexmap/GUIHex.java,v 1.15 2008/03/17 17:50:12 evos Exp $*/
package rails.ui.swing.hexmap;


import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

import org.apache.log4j.Logger;

import rails.game.*;
import rails.game.model.ModelObject;
import rails.ui.swing.*;
import rails.ui.swing.elements.ViewObject;
import rails.util.Util;


/**
 * Base class that holds common components for GUIHexes of all orientations.
 */

public class GUIHex implements ViewObject
{

	public static final double SQRT3 = Math.sqrt(3.0);
	public static double NORMAL_SCALE = 1.0;
	public static double SELECTED_SCALE = 0.8;

	public static void setScale (double scale) {
		NORMAL_SCALE = scale;
		SELECTED_SCALE = 0.8 * scale;
	}

	protected MapHex model;
	protected GeneralPath innerHexagon;
	protected static final Color highlightColor = Color.red;
	protected Point center;

    protected HexMap hexMap; // Containing this hex
	protected String hexName;
	protected int currentTileId;
	protected int originalTileId;
	protected int currentTileOrientation;
	protected String tileFilename;
	protected TileI currentTile;

	protected GUITile currentGUITile = null;
	protected GUITile provisionalGUITile = null;
	protected boolean upgradeMustConnect;

	protected List<TokenI> offStationTokens;

	protected GUIToken provisionalGUIToken = null;

	protected double tileScale = NORMAL_SCALE;

	protected String toolTip = "";

	/**
	 * Stores the neighbouring views. This parallels the neighors field in
	 * MapHex, just on the view side.
	 *
	 * @todo check if we can avoid this
	 */
	private GUIHex[] neighbors = new GUIHex[6];

	// GUI variables
	double[] xVertex = new double[6];
	double[] yVertex = new double[6];
	double len;
	GeneralPath hexagon;
	Rectangle rectBound;
	int baseRotation = 0;

	/** Globally turns antialiasing on or off for all hexes. */
	static boolean antialias = true;
	/** Globally turns overlay on or off for all hexes */
	static boolean useOverlay = true;
	// Selection is in-between GUI and rails.game state.
	private boolean selected;

	protected static Logger log = Logger.getLogger(GUIHex.class.getPackage().getName());

	public GUIHex(HexMap hexMap, double cx, double cy, int scale, double xCoord, double yCoord)
	{
        this.hexMap = hexMap;

		if (MapManager.getTileOrientation() == MapHex.EW)
		{
			len = scale;
			xVertex[0] = cx + SQRT3 / 2 * scale;
			yVertex[0] = cy + 0.5 * scale;
			xVertex[1] = cx + SQRT3 * scale;
			yVertex[1] = cy;
			xVertex[2] = cx + SQRT3 * scale;
			yVertex[2] = cy - 1 * scale;
			xVertex[3] = cx + SQRT3 / 2 * scale;
			yVertex[3] = cy - 1.5 * scale;
			xVertex[4] = cx;
			yVertex[4] = cy - 1 * scale;
			xVertex[5] = cx;
			yVertex[5] = cy;

			baseRotation = 30; // degrees
		}
		else
		{
			len = scale / 3.0;
			xVertex[0] = cx;
			yVertex[0] = cy;
			xVertex[1] = cx + 2 * scale;
			yVertex[1] = cy;
			xVertex[2] = cx + 3 * scale;
			yVertex[2] = cy + SQRT3 * scale;
			xVertex[3] = cx + 2 * scale;
			yVertex[3] = cy + 2 * SQRT3 * scale;
			xVertex[4] = cx;
			yVertex[4] = cy + 2 * SQRT3 * scale;
			xVertex[5] = cx - 1 * scale;
			yVertex[5] = cy + SQRT3 * scale;

			baseRotation = 0;
		}

		hexagon = makePolygon(6, xVertex, yVertex, true);
		rectBound = hexagon.getBounds();

		center = new Point((int) ((xVertex[2] + xVertex[5]) / 2),
				(int) ((yVertex[0] + yVertex[3]) / 2));
		Point2D.Double center2D = new Point2D.Double((xVertex[2] + xVertex[5]) / 2.0,
				(yVertex[0] + yVertex[3]) / 2.0);

		final double innerScale = 0.8;
		AffineTransform at = AffineTransform.getScaleInstance(innerScale,
				innerScale);
		innerHexagon = (GeneralPath) hexagon.createTransformedShape(at);

		// Translate innerHexagon to make it concentric.
		Rectangle2D innerBounds = innerHexagon.getBounds2D();
		Point2D.Double innerCenter = new Point2D.Double(innerBounds.getX()
				+ innerBounds.getWidth() / 2.0, innerBounds.getY()
				+ innerBounds.getHeight() / 2.0);
		at = AffineTransform.getTranslateInstance(center2D.getX()
				- innerCenter.getX(), center2D.getY() - innerCenter.getY());
		innerHexagon.transform(at);

	}

	public MapHex getHexModel()
	{
		return this.model;
	}

	public void setHexModel(MapHex model)
	{
		this.model = model;
		currentTile = model.getCurrentTile();
		hexName = model.getName();
        currentTileId = model.getCurrentTile().getId();
        currentTileOrientation = model.getCurrentTileRotation();
    	currentGUITile = new GUITile(currentTileId, model);
		currentGUITile.setRotation(currentTileOrientation);
		setToolTip();

		if (StatusWindow.useObserver) {
			model.addObserver(this);
		}


	}

	public Rectangle getBounds()
	{
		return rectBound;
	}

	public void setBounds(Rectangle rectBound)
	{
		this.rectBound = rectBound;
	}

	public boolean contains(Point2D.Double point)
	{
		return (hexagon.contains(point));
	}

	public boolean contains(Point point)
	{
		return (hexagon.contains(point));
	}

	public boolean intersects(Rectangle2D r)
	{
		return (hexagon.intersects(r));
	}

	public void setSelected(boolean selected)
	{
		this.selected = selected;
		if (selected)
		{
			currentGUITile.setScale(SELECTED_SCALE);
		}
		else
		{
			currentGUITile.setScale(NORMAL_SCALE);
			provisionalGUITile = null;
		}
	}

	public boolean isSelected()
	{
		return selected;
	}

	static boolean getAntialias()
	{
		return antialias;
	}

	static void setAntialias(boolean enabled)
	{
		antialias = enabled;
	}

	static boolean getOverlay()
	{
		return useOverlay;
	}

	public static void setOverlay(boolean enabled)
	{
		useOverlay = enabled;
	}

	/**
	 * Return a GeneralPath polygon, with the passed number of sides, and the
	 * passed x and y coordinates. Close the polygon if the argument closed is
	 * true.
	 */
	static GeneralPath makePolygon(int sides, double[] x, double[] y,
			boolean closed)
	{
		GeneralPath polygon = new GeneralPath(GeneralPath.WIND_EVEN_ODD, sides);
		polygon.moveTo((float) x[0], (float) y[0]);
		for (int i = 1; i < sides; i++)
		{
			polygon.lineTo((float) x[i], (float) y[i]);
		}
		if (closed)
		{
			polygon.closePath();
		}

		return polygon;
	}

	public void setNeighbor(int i, GUIHex hex)
	{
		if (i >= 0 && i < 6)
		{
			neighbors[i] = hex;
			getHexModel().setNeighbor(i, hex.getHexModel());
		}
	}

	public GUIHex getNeighbor(int i)
	{
		if (i < 0 || i > 6)
		{
			return null;
		}
		else
		{
			return neighbors[i];
		}
	}

	public void paint(Graphics g)
	{
		Graphics2D g2 = (Graphics2D) g;

		if (getAntialias())
		{
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
		}
		else
		{
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_OFF);
		}

		Color terrainColor = Color.WHITE;
		if (isSelected())
		{
			g2.setColor(highlightColor);
			g2.fill(hexagon);

			g2.setColor(terrainColor);
			g2.fill(innerHexagon);

			g2.setColor(Color.black);
			g2.draw(innerHexagon);
		}

		paintOverlay(g2);
		paintStationTokens(g2);
		paintOffStationTokens(g2);

		FontMetrics fontMetrics = g2.getFontMetrics();
		if (getHexModel().getTileCost() > 0 /*&& originalTileId == currentTileId*/)
		{
			g2.drawString(Bank.format(getHexModel().getTileCost()),
					rectBound.x + (rectBound.width - fontMetrics.stringWidth(
								Integer.toString(getHexModel().getTileCost())))	* 3 / 5,
					rectBound.y + ((fontMetrics.getHeight() + rectBound.height) * 9 / 15));
		}

		Map<PublicCompanyI, City> homes;
		if ((homes = getHexModel().getHomes()) != null)
		{
		    StringBuffer b = new StringBuffer();
		    for (Iterator<PublicCompanyI> it = homes.keySet().iterator(); it.hasNext(); ) {

		        PublicCompanyI co = it.next();

				if (!co.hasStarted() && !co.hasFloated())
				{
				    if (b.length() > 0) b.append(",");
				    b.append(co.getName());
				}
		    }
		    String label = b.toString();
			g2.drawString(label,
					rectBound.x + (rectBound.width - fontMetrics.stringWidth(label)) * 1 / 2,
					rectBound.y + ((fontMetrics.getHeight() + rectBound.height) * 3 / 10));
		}

		if (getHexModel().isBlocked())
		{
			List<PrivateCompanyI> privates = Game.getCompanyManager()
					.getAllPrivateCompanies();
			for (PrivateCompanyI p : privates)
			{
				List<MapHex> blocked = p.getBlockedHexes();
				for (MapHex hex : blocked)
				{
					if (getHexModel().equals(hex))
					{
						g2.drawString("(" + p.getName() + ")",
								rectBound.x + (rectBound.width - fontMetrics.stringWidth("("
												+ p.getName() + ")")) * 1 / 2,
								rectBound.y + ((fontMetrics.getHeight() + rectBound.height) * 7 / 15));
					}
				}
			}
		}

	}

	private void paintOverlay(Graphics2D g2)
	{
		if (provisionalGUITile != null)
		{
			provisionalGUITile.paintTile(g2, center.x, center.y);
		}
		else
		{
			currentGUITile.paintTile(g2, center.x, center.y);
		}
	}

	private void paintStationTokens(Graphics2D g2)
	{
		if (getHexModel().getCities().size() > 1)
		{
			paintSplitStations(g2);
			return;
		}

		int numTokens = getHexModel().getTokens(1).size();
		List<TokenI> tokens = getHexModel().getTokens(1);

		for (int i = 0; i < tokens.size(); i++)
		{
			PublicCompanyI co = ((BaseToken)tokens.get(i)).getCompany();
			Point origin = getTokenOrigin2(numTokens, i, 1, 0);
			drawBaseToken(g2, co, origin);
		}
	}

	private void paintSplitStations(Graphics2D g2)
	{
		int numStations = getHexModel().getCities().size();
		int numTokens;
		List<TokenI> tokens;
		Point origin;
		PublicCompanyI co;

		for (int i = 0; i < numStations; i++)
		{
			tokens = getHexModel().getTokens(i+1);
            numTokens = tokens.size();

			for (int j = 0; j < tokens.size(); j++)
			{
				origin = getTokenOrigin2(numTokens, j, numStations, i);
				co = ((BaseToken)tokens.get(j)).getCompany();
				drawBaseToken(g2, co, origin);
			}
		}
	}

	private static int[] offStationTokenX = new int[] {-20, 0}; // Unclear why x=-10,y=-10 puts it at the center.
	private static int[] offStationTokenY = new int[] {-20, 0};

	private void paintOffStationTokens(Graphics2D g2)
	{
		List<TokenI> tokens = getHexModel().getTokens();
		if (tokens == null) return;

		int i = 0;
		for (TokenI token : tokens) {

			Point origin = new Point (center.x + offStationTokenX[i], center.y + offStationTokenY[i]);
			if (token instanceof BaseToken) {

				PublicCompanyI co = ((BaseToken)token).getCompany();
				drawBaseToken (g2, co, origin);

			} else if (token instanceof BonusToken) {

				drawBonusToken(g2, (BonusToken) token, origin);
			}
			if (++i > 1) break;
		}
	}

	private void drawBaseToken(Graphics2D g2, PublicCompanyI co, Point origin)
	{
		Dimension size = new Dimension(40, 40);

		GUIToken token = new GUIToken(co.getFgColour(),
				co.getBgColour(),
				co.getName(),
				origin.x,
				origin.y,
				15);
		token.setBounds(origin.x, origin.y, size.width, size.height);

		token.drawToken(g2);
	}

	private void drawBonusToken(Graphics2D g2, BonusToken bt, Point origin)
	{
		Dimension size = new Dimension(40, 40);

		GUIToken token = new GUIToken(Color.BLACK,
				Color.WHITE,
				"+"+bt.getValue(),
				origin.x,
				origin.y,
				15);
		token.setBounds(origin.x, origin.y, size.width, size.height);

		token.drawToken(g2);
	}

	/*
	 * Beware! Here be dragons! And nested switch/case statements! The horror!
	 *
	 * NOTE: CurrentFoo starts at 0 TotalFoo starts at 1
	 */
	private Point getTokenOrigin(int numTokens, int currentToken,
			int numStations, int currentStation)
	{
		Point p = new Point();

		switch (numStations)
		{
			// Single city, variable number of token spots.
			// This is the most common scenario.
			case 1:
				switch (numTokens)
				{
					// Single dot, basic hex
					case 1:
						p.x = (center.x - 9);
						p.y = (center.y - 9);
						return p;
					// Two dots, common green hex upgrade
					case 2:
						// First token
						if (currentToken == 0)
						{
							p.x = (center.x - 5);
							p.y = (center.y - 9);
							return p;
						}
						// Second Token
						else
						{
							p.x = (center.x - 17);
							p.y = (center.y - 9);
							return p;
						}
					// Three dots, common brown hex upgrade
					case 3:
						// First token
						if (currentToken == 0)
						{
							p.x = (center.x - 14);
							p.y = (center.y - 3);
							return p;
						}
						// Second Token
						else if (currentToken == 1)
						{
							p.x = (center.x - 5);
							p.y = (center.y - 3);
							return p;
						}
						// Third Token
						else
						{
							p.x = (center.x - 9);
							p.y = (center.y - 14);
							return p;
						}
					// Four dots, slightly less common brown hex upgrade
					case 4:
					case 5:
					case 6:
					default:
						return center;
				}
			// Big Cities, two stations.
			// usually only one or two token spots per station
			case 2:
				// First Station... (left side)
				if (currentStation == 0)
				{
					switch (numTokens)
					{
						case 1:
							p.x = (center.x - 14);
							p.y = (center.y + 3);
							return p;
						case 2:
							// First token
							if (currentToken == 0)
							{
								p.x = (center.x - 20);
								p.y = (center.y - 3);
								return p;
							}
							// Second Token
							else
							{
								p.x = (center.x - 10);
								p.y = (center.y + 9);
								return p;
							}
						default:
							return center;
					}
				}
				// Second Station... (right side)
				else
				{
					switch (numTokens)
					{
						case 1:
							p.x = (center.x - 1);
							p.y = (center.y - 20);
							return p;
						case 2:
							// First token
							if (currentToken == 0)
							{
								p.x = (center.x - 6);
								p.y = (center.y - 23);
								return p;
							}
							// Second Token
							else
							{
								p.x = (center.x + 6);
								p.y = (center.y - 12);
								return p;
							}
						default:
							return center;
					}
				}
			case 3:
			// TODO: We'll deal with the 3 station scenario later... much later.

			// Known cases: 3 single token stations,
			// 2 double token station and a single token station

			    // Only do the 3 single token stations case now.
			    // Coordinates sort of work for 18EU B/V tiles
			    switch (currentStation) {
			    case 0:
			        p.x = center.x - 14;
			        p.y = center.y + 8;
			        return p;
			    case 1:
			        p.x = center.x - 16;
			        p.y = center.y - 18;
			        return p;
			    case 2:
			        p.x = center.x + 3;
			        p.y = center.y - 9;
			        return p;
			    default:
			        return center;
			    }
			default:
				return center;
		}
	}

	public void rotateTile()
	{
		if (provisionalGUITile != null)
		{
			provisionalGUITile.rotate(1, currentGUITile, upgradeMustConnect);
		}
	}

    private Point getTokenOrigin2(int numTokens, int currentToken,
            int numStations, int stationNumber)
    {
        Point p = new Point(center.x - 8, center.y - 8);

        int cityNumber = stationNumber + 1;
        Station station = model.getCity(cityNumber).getRelatedStation();

        // Find the correct position on the tile
        double x = 0;
        double y = 0;
        double xx, yy;
        int positionCode = station.getPosition();
        if (positionCode != 0) {
            y = 14;
            double r = Math.toRadians(30 * (positionCode / 50));
            xx = x * Math.cos(r) + y * Math.sin(r);
            yy = y * Math.cos(r) - x * Math.sin(r);
            x = xx;
            y = yy;
        }

        // Correct for the number of base slots and the token number
        switch (station.getBaseSlots()) {
        case 2:
            x += -8 + 16 * currentToken;
            break;
        case 3:
            if (currentToken < 2) {
                x += -8 + 16 * currentToken;
                y += 8;
            } else {
                y -= 8;
            }
            break;
        case 4:
            x += -8 + 16 * currentToken % 2;
            y += 8 - 16 * currentToken / 2;
        }

        // Correct for the tile base and actual rotations
        int rotation = model.getCurrentTileRotation();
        double r = Math.toRadians(baseRotation
                + 60 * rotation);
        xx = x * Math.cos(r) + y * Math.sin(r);
        yy = y * Math.cos(r) - x * Math.sin(r);
        x = xx;
        y = yy;

        p.x += x;
        p.y -= y;

        //log.debug("New origin for hex "+getName()+" tile #"+model.getCurrentTile().getId()
        //        + " city "+cityNumber+" pos="+positionCode+" token "+currentToken+": x="+x+" y="+y);

        return p;
    }

	// Added by Erik Vos
	/**
	 * @return Returns the name.
	 */
	public String getName()
	{
		return hexName;
	}

	/**
	 * @return Returns the currentTile.
	 */
	public TileI getCurrentTile()
	{
		return currentTile;
	}

	/**
	 * @param currentTileOrientation
	 *            The currentTileOrientation to set.
	 */
	public void setTileOrientation(int tileOrientation)
	{
		this.currentTileOrientation = tileOrientation;
	}

	public String getToolTip()
	{
		return toolTip;
	}

	protected void setToolTip()
	{
		StringBuffer tt = new StringBuffer("<html>");
		tt.append("<b>Hex</b>: ").append(hexName);
		String name = model.getCityName();
		if (Util.hasValue(name)) {
		    tt.append(" (").append(name).append(")");
		}
		// The next line is a temporary development aid, that can be removed
		// later.
		/*
		tt.append("  <small>(")
				.append(model.getX())
				.append(",")
				.append(model.getY())
				.append(")</small>");
				*/
		tt.append("<br><b>Tile</b>: ").append(currentTile.getId());
		// TEMPORARY
		tt.append("<small> rot="+currentTileOrientation+"</small>");
		if (currentTile.hasStations())
		{
		    //for (Station st : currentTile.getStations())
		    Station st;
		    int cityNumber;
		    for (City city : model.getCities())
			{
		        cityNumber = city.getNumber();
		        st = city.getRelatedStation();
				tt.append("<br>  ").append(st.getType())
				  .append(" ").append(cityNumber) //.append("/").append(st.getNumber())
				  .append(" (").append(model.getConnectionString(cityNumber))
				  .append("): value ");
				if (model.hasOffBoardValues()) {
				    tt.append(model.getCurrentOffBoardValue()).append(" [");
				    int[] values = model.getOffBoardValues();
				    for (int i=0; i<values.length; i++) {
				        if (i>0) tt.append(",");
				        tt.append(values[i]);
				    }
				    tt.append("]");
				} else {
				    tt.append(st.getValue());
				}
				if (st.getBaseSlots() > 0)
				{
					tt.append(", ").append(st.getBaseSlots()).append(" slots");
					List<TokenI> tokens = model.getTokens(cityNumber);
					if (tokens.size() > 0) {
    					tt.append(" (");
    					int oldsize = tt.length();
    					for (TokenI token : tokens) {
    					    if (tt.length() > oldsize) tt.append(",");
    					    tt.append(token.getName());
    					}
    					tt.append(")");
					}
				}
				// TEMPORARY
				tt.append(" <small>pos="+st.getPosition()+"</small>");
			}
		}
		String upgrades = currentTile.getUpgradesString(model);
		if (upgrades.equals(""))
		{
			tt.append("<br>No upgrades");
		}
		else
		{
			tt.append("<br><b>Upgrades</b>: ").append(upgrades);
			if (model.getTileCost() > 0)
				tt.append("<br>Upgrade cost: "
						+ Bank.format(model.getTileCost()));
		}

		if (getHexModel().getDestinations() != null) {
			tt.append("<br><b>Destination</b>:");
			for (PublicCompanyI dest : getHexModel().getDestinations()) {
			    tt.append (" ");
                tt.append(dest.getName());
			}
		}
		toolTip = tt.toString();
	}

	public boolean dropTile(int tileId, boolean upgradeMustConnect)
	{
		this.upgradeMustConnect = upgradeMustConnect;

		provisionalGUITile = new GUITile(tileId, model);
		/* Check if we can find a valid orientation of this tile */
		if (provisionalGUITile.rotate(0, currentGUITile, upgradeMustConnect))
		{
			/* If so, accept it */
			provisionalGUITile.setScale(SELECTED_SCALE);
			toolTip = "Click to rotate";
			return true;
		}
		else
		{
			/* If not, refuse it */
			provisionalGUITile = null;
			return false;
		}

	}

	public void removeTile()
	{
		provisionalGUITile = null;
		setSelected(false);
		setToolTip();
	}

	public boolean canFixTile () {
		return provisionalGUITile != null;
	}

	public TileI getProvisionalTile () {
		return provisionalGUITile.getTile();
	}

	public int getProvisionalTileRotation() {
		return provisionalGUITile.getRotation();
	}

	public void fixTile () {

		setSelected (false);
		setToolTip();
	}

	public void removeToken()
	{
		provisionalGUIToken = null;
		setSelected(false);
		setToolTip();
	}

    public void fixToken () {
        setSelected (false);
        setToolTip();
    }

	/** Needed to satisfy the ViewObject interface. Currently not used. */
	public void deRegister()
	{
		if (model != null && StatusWindow.useObserver)
			model.deleteObserver(this);
	}

	public ModelObject getModel() {
	    return model;
	}

    // Required to implement Observer pattern.
    // Used by Undo/Redo
	public void update (Observable observable, Object notificationObject) {

	    if (notificationObject instanceof String) {
	        // The below code so far only deals with tile lay undo/redo.
	        // Tokens still to do
	        String[] elements = ((String)notificationObject).split("/");
	        currentTileId = Integer.parseInt(elements[0]);
	        currentTileOrientation = Integer.parseInt(elements[1]);
	        currentGUITile = new GUITile(currentTileId, model);
	        currentGUITile.setRotation(currentTileOrientation);
	        currentTile = currentGUITile.getTile();

			hexMap.repaint(getBounds());

	        provisionalGUITile = null;

	        log.debug ("GUIHex "+model.getName()+" updated: new tile "+currentTileId+"/"+currentTileOrientation);
			GameUIManager.instance.orWindow.updateStatus();
	    } else {
	    	hexMap.repaint(getBounds());
	    }
	}

}
