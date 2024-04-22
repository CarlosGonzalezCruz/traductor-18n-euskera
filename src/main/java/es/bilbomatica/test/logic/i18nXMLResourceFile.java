package es.bilbomatica.test.logic;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import es.bilbomatica.traductor.exceptions.WrongFormatException;

public class i18nXMLResourceFile implements i18nResourceFile {

    private static final String XPATH_I18N_CONTAINERS_ABSOLUTE = "//*[./i18n]";
    private static final String XPATH_I18N_ES_RELATIVE = "./i18n[@idioma = 'es']";
    private static final String XPATH_I18N_EU_RELATIVE = "./i18n[@idioma = 'eu']";
    private static final String NEW_I18N_ATTRIBUTE_LANGUAGE_KEY = "idioma";
    private static final String NEW_I18N_ATTRIBUTE_LANGUAGE_VALUE = "eu";

    private Map<String, String> properties;
    private String name;
    private Document xmlDocument;

    private i18nXMLResourceFile(Map<String, String> properties, String name, Document xmlDocument) {
        this.properties = properties;
        this.name = name;
        this.xmlDocument = xmlDocument;
    }

    public static i18nXMLResourceFile load(String filename, InputStream file) throws IOException, ParserConfigurationException, SAXException, XPathExpressionException, WrongFormatException {        
		InputStream stream = new BufferedInputStream(file);

        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        
        try {
            Document xmlDocument = builder.parse(stream);
            Map<String, String> ret = parseXMLIntoProperties(xmlDocument);

            return new i18nXMLResourceFile(ret, filename, xmlDocument);

        } catch(SAXParseException e) {
            throw new WrongFormatException(filename, I18nResourceFileType.XML.getName());

        }

    }

    @Override
    public String getName() {
        return this.name;
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
    public String getTranslatedName() {
        return this.name; // No hacer nada, intencionalmente
    }

    @Override
    public void writeToOutput(OutputStream stream) throws IOException {
        try {
            insertPropertiesIntoXML(xmlDocument, properties);
        } catch (XPathExpressionException e) {
            throw new IOException(e);
        }

        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance(); 
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(xmlDocument); 
            StreamResult result = new StreamResult(stream);
            transformer.transform(source, result);
        } catch (TransformerException e) {
            throw new IOException(e);
        } 
    }

    private static Map<String, String> parseXMLIntoProperties(Document xmlDocument) throws XPathExpressionException {
        XPath xPath = XPathFactory.newInstance().newXPath();
        Map<String, String> ret = new HashMap<>();

        NodeList nodes = (NodeList) xPath.compile(XPATH_I18N_CONTAINERS_ABSOLUTE).evaluate(xmlDocument, XPathConstants.NODESET);
        XPathExpression textFinder = xPath.compile(XPATH_I18N_ES_RELATIVE);

        for(int i = 0; i < nodes.getLength(); i++) {
            Node untranslatedText = (Node) textFinder.evaluate(nodes.item(i), XPathConstants.NODE);
            if(untranslatedText.getFirstChild() != null) {
                ret.put(String.valueOf(i), untranslatedText.getFirstChild().getNodeValue());
            }
        }

        return ret;
    }

    private static void insertPropertiesIntoXML(Document xmlDocument, Map<String, String> properties) throws XPathExpressionException {
        XPath xPath = XPathFactory.newInstance().newXPath();

        NodeList nodes = (NodeList) xPath.compile(XPATH_I18N_CONTAINERS_ABSOLUTE).evaluate(xmlDocument, XPathConstants.NODESET);
        XPathExpression textFinder = xPath.compile(XPATH_I18N_EU_RELATIVE);

        for(int i = 0; i < nodes.getLength(); i++) {
            Node translatedText = (Node) textFinder.evaluate(nodes.item(i), XPathConstants.NODE);
            if(translatedText == null) {
                translatedText = xmlDocument.createElement("i18n");
                ((Element) translatedText).setAttribute(NEW_I18N_ATTRIBUTE_LANGUAGE_KEY, NEW_I18N_ATTRIBUTE_LANGUAGE_VALUE);
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
