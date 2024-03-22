package traffic_sim.map.xml;

import org.w3c.dom.Node;

public interface XmlTypeInstantiationFactory {
    Object create();

    /**
     * @param node  The node to build a class instance from
     * @return  The class the node desires
     */
    private static Class<?> getClassFromXml(Node node){
        switch (node.getNodeName()) {
            case "byte" -> {
                return byte.class;
            }
            case "short" -> {
                return short.class;
            }
            case "int" -> {
                return int.class;
            }
            case "long" -> {
                return long.class;
            }
            case "float" -> {
                return float.class;
            }
            case "double" -> {
                return double.class;
            }
            case "char" -> {
                return char.class;
            }
            case "String" -> {
                return String.class;
            }
            case "Object" -> {
                String classPath;
                if (node.getAttributes().getNamedItem("argumentClassPath") != null){
                    classPath = node.getAttributes().getNamedItem("argumentClassPath").getNodeValue();
                }else{
                    classPath = node.getAttributes().getNamedItem("classPath").getNodeValue();
                }
                try {
                    return Class.forName(classPath);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            default -> throw new RuntimeException("Invalid type kind: " + node.getNodeName());
        }
    }

    /** Generate a instantiation factory for a class with a specific constructor
     *
     * @param node  The node that represents the class constructor
     * @return  A factory that can create any number of instances of the specified class from the provided node
     */
    static XmlTypeInstantiationFactory generateFromXml(Node node){
        if(node.getNodeName().equals("Object")){
            try{
                var classPath = node.getAttributes().getNamedItem("classPath").getNodeValue();
                var clazz = Class.forName(classPath);

                var children = new XmlTypeInstantiationFactory[MapXmlTools.countNonTextChildren(node)];
                var childTypes = new Class<?>[children.length];
                int childTypeIndex = 0;
                for(int i = 0; i < node.getChildNodes().getLength(); i ++){
                    if(MapXmlTools.isTextNode(node.getChildNodes().item(i)))continue;
                    childTypes[childTypeIndex] = getClassFromXml(node.getChildNodes().item(i));
                    children[childTypeIndex] = generateFromXml(node.getChildNodes().item(i));
                    childTypeIndex ++;
                }

                var constructor = clazz.getConstructor(childTypes);


                return () -> {
                    var items = new Object[children.length];
                    for(int i = 0; i < items.length; i ++){
                        items[i] = children[i].create();
                    }
                    try{
                        return constructor.newInstance(items);
                    }catch (Exception e){
                        throw new RuntimeException(e);
                    }
                };
            }catch (Exception e){
                throw new RuntimeException(e);
            }
        }
        final String nodeVal = node.getChildNodes().item(0).getNodeValue();

        switch (node.getNodeName()){
            case "byte" -> {
                var v = Byte.parseByte(nodeVal);
                return () -> v;
            }
            case "short" -> {
                var v = Short.parseShort(nodeVal);
                return () -> v;
            }
            case "int" -> {
                var v = Integer.parseInt(nodeVal);
                return () -> v;
            }
            case "long" -> {
                var v = Long.parseLong(nodeVal);
                return () -> v;
            }
            case "float" -> {
                var v = Float.parseFloat(nodeVal);
                return () -> v;
            }
            case "double" -> {
                var v = Double.parseDouble(nodeVal);
                return () -> v;
            }
            case "char" -> {
                return () -> nodeVal.charAt(0);
            }
            case "String" -> {
                return () -> nodeVal;
            }
            default -> throw new RuntimeException("Invalid type kind: " + node.getNodeName());

        }
    }
}
