/*
 * Created on 05-Mar-2005
 *
 *IG Adams
 */
package util;

import game.ConfigurationException;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author iadams
 *
 * Booch utility class providing helper functions for working with XML.
 */
public final class XmlUtils {

    /**
     * No-args private constructor, to prevent (meaningless) construction of one of these. 
     */
    private XmlUtils(){
    }
    /**
     * Extracts the value of a given attribute from a NodeNameMap. 
     * Returns null if no such attribute can be found.
     * 
     * @param nnp the NodeNameMap to search for the Attribute
     * @param attrName the name of the attribute who's value is desired
     * @return the named attribute's value or null if absent.
     */
    public static String extractStringAttribute(NamedNodeMap nnp, String attrName) {
        Node nameAttr = nnp.getNamedItem(attrName);
        if (nameAttr == null){
                return null;
        }
        return nameAttr.getNodeValue();        
    }

    /**
     * Opens and parses an xml file. Searches the root level of the file for an element
     * with the supplied name.
     * @param fileName the name of the file to open
     * @param elementName the name of the element to find
     * @return the named element in the named file
     * @throws ConfigurationException if there is any problem opening and parsing the file, or
     * if the file does not contain a top level element with the given name.
     */
    public static Element findElementInFile(String fileName, String elementName) throws ConfigurationException
    {
        Document doc;
        try {
            File theFile = new File(fileName);
            // Step 1: create a DocumentBuilderFactory and setNamespaceAware
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            // Step 2: create a DocumentBuilder
            DocumentBuilder db = dbf.newDocumentBuilder();
            // Step 3: parse the input file to get a Document object
            doc = db.parse(theFile);
        } catch (ParserConfigurationException e) {
            throw new ConfigurationException("Could not read/parse " + fileName, e);
        } catch (SAXException e) {
            throw new ConfigurationException("Could not read/parse " + fileName, e);
        } catch (IOException e) {
            throw new ConfigurationException("Could not read/parse " + fileName, e);
        }
    
        // Now find the named Element
        NodeList nodeList = doc.getChildNodes();
        for ( int iNode = 0; ( iNode < nodeList.getLength() ) ; iNode++)
        {
            Node childNode = nodeList.item(iNode);
            if (    (childNode != null) 
                 && (childNode.getNodeName().equals(elementName))
                 && (childNode.getNodeType() == Node.ELEMENT_NODE))
            {
                return (Element) childNode;
            }
        }
        throw new ConfigurationException("Could not find " + elementName + " in " + fileName);
    }

}
