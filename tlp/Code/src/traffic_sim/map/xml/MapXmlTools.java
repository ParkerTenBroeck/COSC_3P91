package traffic_sim.map.xml;

import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import traffic_sim.excpetions.CustomIntersectionLoadException;
import traffic_sim.excpetions.MapBuildingException;
import traffic_sim.map.RoadMap;
import traffic_sim.map.intersection.*;
import traffic_sim.vehicle.Vehicle;
import traffic_sim.vehicle.VehicleFactory;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;

public class MapXmlTools {
    private static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
    private static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";

    /**
     * @param node  The node whos children we want to count
     * @return  The number of non text children
     */
    public static int countNonTextChildren(Node node){
        int count = 0;
        for(int i = 0; i < node.getChildNodes().getLength(); i ++)
            if(!isTextNode(node.getChildNodes().item(i))) count ++;
        return count;
    }

    /**
     * @param node The node we want to check
     * @return  If the provided node is a text node
     */
    public static boolean isTextNode(Node node){
        return node.getNodeName().equals("#text");
    }

    /**
     * @param in    A inputstream of xml data
     * @return      The loaded map
     * @throws IOException  If the input stream can't be read properly
     * @throws SAXException If the provided map doesn't match the schema
     * @throws MapBuildingException If the provided file constructs an invalid map
     *inputstream @throws ParserConfigurationException If the schema is invalid
     * @throws CustomIntersectionLoadException  If the provided custom intersection throws an exception
     */
    public static RoadMap loadMap(InputStream in) throws IOException, SAXException, MapBuildingException, ParserConfigurationException, CustomIntersectionLoadException {

        var map = new RoadMap();

        var documentFactory = DocumentBuilderFactory.newInstance();
        documentFactory.setNamespaceAware(true);
        documentFactory.setValidating(true);

        documentFactory.setAttribute(JAXP_SCHEMA_LANGUAGE,XMLConstants.W3C_XML_SCHEMA_NS_URI);
        documentFactory.setAttribute(JAXP_SCHEMA_SOURCE,MapXmlTools.class.getResourceAsStream("/res/roadmap_schema.xsd"));

        var builder = documentFactory.newDocumentBuilder();
        builder.setErrorHandler(new ErrorHandler() {
            @Override
            public void warning(SAXParseException exception) throws SAXException {
                throw exception;
            }

            @Override
            public void error(SAXParseException exception) throws SAXException {
                throw exception;
            }

            @Override
            public void fatalError(SAXParseException exception) throws SAXException {
                throw exception;
            }
        });

        var document = builder.parse(in);

        var root = document.getFirstChild();
        for(int i = 0; i < root.getChildNodes().getLength(); i ++){
            var child = root.getChildNodes().item(i);
            switch (child.getNodeName().trim()){
                case "intersection" -> {
                    var id = child.getAttributes().getNamedItem("id").getNodeValue().trim();
                    var name = child.getAttributes().getNamedItem("name").getNodeValue().trim();
                    var x = Float.parseFloat(child.getAttributes().getNamedItem("x").getNodeValue().trim());
                    var y = Float.parseFloat(child.getAttributes().getNamedItem("y").getNodeValue().trim());

                    var kind = child.getChildNodes().item(0);
                    while(kind.getNodeName().equals("#text")){
                        kind = kind.getNextSibling();
                    }
                    Intersection intersection;
                    switch(kind.getNodeName().trim()){
                        case "default" -> intersection = new DefaultIntersection(name, x, y);
                        case "source" -> {
                            var source = new SourceIntersection(name, x, y);
                            
                            var facotries = new VehicleFactory[countNonTextChildren(kind)];
                            int factoriesIndex = 0;
                            for(int j = 0; j < kind.getChildNodes().getLength(); j ++){
                                if (isTextNode(kind.getChildNodes().item(j))) continue;
                                var factory = XmlTypeInstantiationFactory.generateFromXml(kind.getChildNodes().item(j));
                                facotries[factoriesIndex] = () -> (Vehicle) factory.create();
                                factoriesIndex ++;
                            }
                            source.factories(facotries);

                            intersection = source;
                        }
                        case "drain" -> intersection = new DrainIntersection(name, x, y);
                        case "timed" -> intersection = new TimedIntersection(name, x, y);
                        case "custom" -> {
                            var classPath = kind.getAttributes().getNamedItem("classPath").getNodeValue();
                            try{
                                var nodeClass = Class.forName(classPath);
                                intersection = constructIntersectionFromClass(nodeClass, name, x, y, kind);
                            }catch (ClassNotFoundException e) {
                                throw new CustomIntersectionLoadException(e, "Failed to get specified class path");
                            }
                        }
                        default -> throw new RuntimeException("Invalid node type '" + kind.getNodeName().trim()+"'");
                    }
                    map.addIntersection(id, intersection);
                }
                case "road" -> {
                    var id = child.getAttributes().getNamedItem("id").getNodeValue().trim();
                    var name = child.getAttributes().getNamedItem("name").getNodeValue().trim();
                    var from = child.getAttributes().getNamedItem("from").getNodeValue().trim();
                    var to = child.getAttributes().getNamedItem("to").getNodeValue().trim();
                    var lanes = Integer.parseInt(child.getAttributes().getNamedItem("lanes").getNodeValue());
                    map.linkIntersection(
                            map.getIntersectionById(from),
                            map.getIntersectionById(to),
                            id, name, lanes
                    );
                }
                case "turn" -> {
                    var name = child.getAttributes().getNamedItem("name").getNodeValue().trim();
                    var from_lane = Integer.parseInt(child.getAttributes().getNamedItem("from_lane").getNodeValue().trim());
                    var from = child.getAttributes().getNamedItem("from").getNodeValue().trim();
                    var to = child.getAttributes().getNamedItem("to").getNodeValue().trim();
                    var to_lane = Integer.parseInt(child.getAttributes().getNamedItem("to_lane").getNodeValue().trim());
                    map.addTurn(
                            map.getRoadById(from).getLane(from_lane),
                            map.getRoadById(to).getLane(to_lane),
                            name
                    );
                }
                case "#text" -> {}
                default -> throw new RuntimeException();
            }
        }

        return map;
    }

    /**
     *
     * @param clazz The class of the intersection we want to construct
     * @param name  The name of the new intersection
     * @param x     The x coord of the new intersection
     * @param y     The y coord of the new intersection
     * @param node  The node responsible for making this class
     * @return      A intersection of type clazz
     * @throws CustomIntersectionLoadException  If the provided clazz wasn't an intersection or couldn't be constructed for some reason.
     */
    private static Intersection constructIntersectionFromClass(Class<?> clazz, String name, float x, float y, Node node) throws CustomIntersectionLoadException{
        try{
            var construct = clazz.getConstructor(String.class, float.class, float.class, Node.class);
            var value = construct.newInstance(name, x, y, node);
            if (value instanceof Intersection intersection){
                return intersection;
            }else{
                throw new CustomIntersectionLoadException(new ClassCastException(), "Loaded class is not an intersection");
            }
        }catch (NoSuchMethodException ignore){

        }catch (InvocationTargetException | RuntimeException | IllegalAccessException | InstantiationException e) {
            throw new CustomIntersectionLoadException(e, "Failed to run custom intersection constructor");
        }

        try{
            var construct = clazz.getConstructor(String.class, float.class, float.class);
            var value = construct.newInstance(name, x, y);
            if (value instanceof Intersection intersection){
                return intersection;
            }else{
                throw new CustomIntersectionLoadException(new ClassCastException(), "Loaded class is not an intersection");
            }
        }catch (NoSuchMethodException | InvocationTargetException | RuntimeException | IllegalAccessException | InstantiationException e) {
            throw new CustomIntersectionLoadException(e, "Failed to run custom intersection constructor");
        }
    }

    /**
     * Outputs the provided map formatted as a xml file to the output steam
     *
     * @param map   The map to save
     * @param out   THe place to write the output
     * @throws IOException  IO error occurred when trying to write the data to the provided output stream
     */
    public static void saveMap(RoadMap map, OutputStreamWriter out) throws IOException {
        out.write("<roadmap>\n");
        for(var intersection : map.getIntersections()){
            out.write("  <intersection ");
            // id
            out.write("id='"+map.getIntersectionId(intersection)+"' ");
            out.write("name='"+intersection.getName()+"' ");
            out.write("x='"+intersection.getX()+"' ");
            out.write("y='"+intersection.getY()+"'>");
            if (intersection instanceof DefaultIntersection){
                out.write("<default/>");
            }else if (intersection instanceof DrainIntersection){
                out.write("<drain/>");
            }else if (intersection instanceof SourceIntersection){
                out.write("<source/>");
            }else if(intersection instanceof TimedIntersection){
                out.write("<timed/>");
            }else{
                out.write("\n    <custom classPath='"+intersection.getClass().getName()+"'>");
                out.write("\n    </custom>\n  ");
            }
            out.write("</intersection>\n");
        }
        out.write('\n');

        for(var road : map.getRoads()){
            out.write("  <road ");
            out.write("id='"+map.getRoadId(road)+"' ");
            out.write("name='"+road.getName()+"' ");
            out.write("lanes='"+road.getNumLanes()+"' ");
            out.write("from='"+map.getIntersectionId(map.roadStarts(road))+"' ");
            out.write("to='"+map.getIntersectionId(map.roadEnds(road))+"'/>");
            out.write('\n');
        }
        out.write('\n');


        for(var intersection : map.getIntersections()){
            for(var turns : intersection.getAllTurns().entrySet()){
                for(var turn : turns.getValue()){
                    out.write("  <turn ");
                    out.write("from='"+map.getRoadId(turns.getKey().road())+"' ");
                    out.write("from_lane='"+turns.getKey().getLane()+"' ");
                    out.write("name='"+turn.getName()+"' ");
                    out.write("to='"+map.getRoadId(turn.getLane().road())+"' ");
                    out.write("to_lane='"+turn.getLane().getLane()+"'/>");
                    out.write('\n');
                }
            }
        }

        out.write("</roadmap>");

        out.flush();
    }
}
