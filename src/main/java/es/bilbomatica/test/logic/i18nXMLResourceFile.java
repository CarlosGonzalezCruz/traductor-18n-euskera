package es.bilbomatica.test.logic;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class i18nXMLResourceFile implements i18nResourceFile {

    private Map<String, String> properties;
    private String path;
    private Document xmlDocument;

    private i18nXMLResourceFile(Map<String, String> properties, String path, Document xmlDocument) {
        this.properties = properties;
        this.path = path;
        this.xmlDocument = xmlDocument;
    }

    public static i18nXMLResourceFile load(String path) throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();           
		InputStream stream = loader.getResourceAsStream(path);

        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        Document xmlDocument = builder.parse(stream);

		Map<String, String> ret = parseXMLIntoProperties(xmlDocument);

        return new i18nXMLResourceFile(ret, path, xmlDocument);
    }

    @Override
    public String getName() {
        return this.path;
    }

    @Override
    public int size() {
        return this.properties.size();
    }

    @Override
    public Map<String, String> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    @Override
    public Optional<String> getProperty(String key) {
        return Optional.ofNullable(this.properties.get(key));
    }

    @Override
    public void updateProperties(Map<String, String> newProperties) {
        this.properties.clear();
        this.properties.putAll(newProperties);
    }

    @Override
    public void save() throws IOException {
        File file = new File(Thread.currentThread().getContextClassLoader().getResource(".").getFile() + "/" + this.path);
        file.createNewFile(); // Por si no existe

        try {
            insertPropertiesIntoXML(xmlDocument, properties);
        } catch (XPathExpressionException e) {
            throw new IOException(e);
        }

        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance(); 
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(xmlDocument); 
            StreamResult result = new StreamResult(file); 
            transformer.transform(source, result); 
        } catch (TransformerException e) {
            throw new IOException(e);
        } 
    }

    private static Map<String, String> parseXMLIntoProperties(Document xmlDocument) throws XPathExpressionException {
        XPath xPath = XPathFactory.newInstance().newXPath();
        Map<String, String> ret = new HashMap<>();

        NodeList nodes = (NodeList) xPath.compile("//*/texto").evaluate(xmlDocument, XPathConstants.NODESET);
        XPathExpression textFinder = xPath.compile("./i18n[@idioma = 'es']");

        for(int i = 0; i < nodes.getLength(); i++) {
            Node untranslatedText = (Node) textFinder.evaluate(nodes.item(i), XPathConstants.NODE);
            ret.put(String.valueOf(i), untranslatedText.getFirstChild().getNodeValue());
        }

        return ret;
    }

    private static void insertPropertiesIntoXML(Document xmlDocument, Map<String, String> properties) throws XPathExpressionException {
        XPath xPath = XPathFactory.newInstance().newXPath();

        NodeList nodes = (NodeList) xPath.compile("//*/texto").evaluate(xmlDocument, XPathConstants.NODESET);
        XPathExpression textFinder = xPath.compile("./i18n[@idioma = 'eu']");

        for(int i = 0; i < nodes.getLength(); i++) {
            Node translatedText = (Node) textFinder.evaluate(nodes.item(i), XPathConstants.NODE);
            if(translatedText == null) {
                translatedText = xmlDocument.createElement("i18n");
                ((Element) translatedText).setAttribute("idioma", "eu");
                nodes.item(i).appendChild(xmlDocument.createTextNode("\t"));
                nodes.item(i).appendChild(translatedText);
                nodes.item(i).appendChild(xmlDocument.createTextNode("\n\t"));
            }
            Node textElement = xmlDocument.createTextNode(properties.get(String.valueOf(i)));

            // Borra todos los hijos para sobreescribir el texto
            while(translatedText.hasChildNodes()) {
                translatedText.removeChild(translatedText.getFirstChild());
            }

            translatedText.appendChild(textElement);
        }
    }
}
