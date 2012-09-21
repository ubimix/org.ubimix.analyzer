/* ************************************************************************** *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This file is licensed to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * ************************************************************************** */
package org.ubimix.analyzer.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;

/**
 * @author kotelnikov
 */
public class XMLUtil {

    public static void append(Document source, String path, Document target)
        throws XPathExpressionException,
        IOException {
        Element e = target.getDocumentElement();
        if (e == null) {
            e = target.createElement("result");
            target.appendChild(e);
        }
        append(source.getDocumentElement(), path, e);
    }

    public static void append(Element source, String path, Element target)
        throws XPathExpressionException,
        IOException {
        for (Node node : eval(source, path)) {
            node = copyNode(target, node);
        }
    }

    /**
     * @param target
     * @param node
     * @return
     * @throws Exception
     */
    public static Document copyDocument(Document document) throws Exception {
        Node n = document.getDocumentElement().cloneNode(true);
        Document result = newDocument();
        result.adoptNode(n);
        result.appendChild(n);
        return result;
    }

    /**
     * @param target
     * @param node
     * @return
     */
    public static Node copyNode(Element target, Node node) {
        Node n = node.cloneNode(true);
        Document targetDoc = target.getOwnerDocument();
        targetDoc.adoptNode(n);
        target.appendChild(n);
        return node;
    }

    public static Iterable<Node> eval(Document doc, String path)
        throws XPathExpressionException {
        return eval(doc.getDocumentElement(), path);
    }

    public static Iterable<Node> eval(Node element, String path)
        throws XPathExpressionException {
        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        // xpath.setNamespaceContext(new NamespaceContext() {
        //
        // /**
        // * @WARNING this code will work only if the namespace is present at
        // * each node of the xml dom and within the xpath queries.
        // * Otherwise you can fix like that:
        // *
        // * <pre>
        // * // FIXME: this is a
        // * hack!! if ("atom".equals(prefix)) return
        // * "http://www.w3.org/2005/Atom"; but it wont work with
        // * </pre>
        // *
        // * different namespaces
        // * @see
        // javax.xml.namespace.NamespaceContext#getNamespaceURI(java.lang.String)
        // */
        // public String getNamespaceURI(String prefix) {
        // String namespace = DOMUtil.lookupNamespaceURI(node, prefix);
        // return namespace;
        // }
        //
        // public String getPrefix(String namespaceURI) {
        // String prefix = node.lookupPrefix(namespaceURI);
        // return prefix;
        // }
        //
        // public Iterator<?> getPrefixes(final String namespaceURI) {
        // return new Iterator<String>() {
        // String ns = getPrefix(namespaceURI);
        //
        // public boolean hasNext() {
        // return ns != null;
        // }
        //
        // public String next() {
        // String r = ns;
        // ns = null;
        // return r;
        // }
        //
        // public void remove() {
        // }
        //
        // };
        // }
        // });
        XPathExpression expr = xpath.compile(path);
        final NodeList result = (NodeList) expr.evaluate(
            element,
            XPathConstants.NODESET);
        return new Iterable<Node>() {
            public Iterator<Node> iterator() {
                return new Iterator<Node>() {
                    int len = result.getLength();

                    int pos;

                    public boolean hasNext() {
                        return pos < len;
                    }

                    public Node next() {
                        if (pos >= len) {
                            return null;
                        }
                        return result.item(pos++);
                    }

                    public void remove() {
                        throw new IllegalAccessError();
                    }

                };
            }
        };
    }

    /**
     * Applies the specified XPath expression to the given node and returns the
     * first result.
     * 
     * @param node the root element; the given XPath expression is applied to
     *        this node
     * @param path the XPath expression
     * @return the first node corresponding to the specified XPath expression
     * @throws XPathExpressionException
     */
    public static Node evalFirst(Node node, String path)
        throws XPathExpressionException {
        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        XPathExpression expr = xpath.compile(path);
        final Node result = (Node) expr.evaluate(node, XPathConstants.NODE);
        return result;
    }

    public static Document format(
        Source xslSource,
        Document xml,
        URIResolver resolver)
        throws TransformerFactoryConfigurationError,
        TransformerConfigurationException,
        Exception,
        TransformerException {
        TransformerFactory factory = TransformerFactory.newInstance();
        DOMSource xmlSource = new DOMSource(xml);
        Templates t = factory.newTemplates(xslSource);
        Transformer transformer = t.newTransformer();
        transformer.setURIResolver(resolver);
        Document resultDoc = newDocument();
        DOMResult result = new DOMResult(resultDoc);
        transformer.transform(xmlSource, result);
        return resultDoc;
    }

    public static Document formatXML(Document xml, Document xsl)
        throws Exception {
        TransformerFactory factory = TransformerFactory.newInstance();
        DOMSource xslSource = new DOMSource(xsl);
        Templates t = factory.newTemplates(xslSource);
        Transformer transformer = t.newTransformer();
        return formatXML(xml, transformer);
    }

    public static void formatXML(Document doc, Document xsl, Writer writer)
        throws Exception {
        DOMSource xmlSource = new DOMSource(doc);
        DOMSource xslSource = new DOMSource(xsl);
        formatXML(xmlSource, xslSource, writer);
        writer.flush();
    }

    public static void formatXML(Document doc, Reader xslStream, Writer writer)
        throws Exception {
        DOMSource xmlSource = new DOMSource(doc);
        StreamSource xslSource = new StreamSource(xslStream);
        formatXML(xmlSource, xslSource, writer);
        writer.flush();
    }

    public static Document formatXML(Document xml, Transformer transformer)
        throws Exception {
        DOMSource xmlSource = new DOMSource(xml);
        Document resultDoc = newDocument();
        DOMResult result = new DOMResult(resultDoc);
        transformer.transform(xmlSource, result);
        return resultDoc;
    }

    public static Document formatXML(
        Document xml,
        URIResolver resolver,
        Document xsl) throws Exception {
        DOMSource xslSource = new DOMSource(xsl);
        return format(xslSource, xml, resolver);
    }

    public static void formatXML(
        Document xml,
        URIResolver resolver,
        Document xsl,
        Writer output) throws Exception {
        StreamResult result = new StreamResult(output);
        DOMSource xslSource = new DOMSource(xsl);
        TransformerFactory factory = TransformerFactory.newInstance();
        DOMSource xmlSource = new DOMSource(xml);
        Templates t = factory.newTemplates(xslSource);
        Transformer transformer = t.newTransformer();
        transformer.setURIResolver(resolver);
        transformer.transform(xmlSource, result);
    }

    public static Document formatXML(
        Document xml,
        URIResolver resolver,
        String... xsls) throws Exception {
        TransformerFactory factory = TransformerFactory.newInstance();
        for (String xsl : xsls) {
            DOMSource xmlSource = new DOMSource(xml);
            Source xslSource = resolver.resolve(xsl, "");
            Templates t = factory.newTemplates(xslSource);
            Transformer transformer = t.newTransformer();
            transformer.setURIResolver(resolver);
            Document resultDoc = newDocument();
            DOMResult result = new DOMResult(resultDoc);
            transformer.transform(xmlSource, result);
            xml = resultDoc;
        }
        return xml;
    }

    private static void formatXML(
        Source xmlSource,
        Source xslSource,
        Writer output) throws Exception {
        TransformerFactory factory = TransformerFactory.newInstance();
        Templates t = factory.newTemplates(xslSource);
        Transformer transformer = t.newTransformer();
        StreamResult result = new StreamResult(output);
        transformer.transform(xmlSource, result);
    }

    public static Element getChild(Element e, String path) throws Exception {
        Iterable<Node> list = eval(e, path);
        if (list == null) {
            return null;
        }
        Iterator<Node> iterator = list.iterator();
        return (Element) (iterator.hasNext() ? iterator.next() : null);
    }

    /**
     * Creates and returns an new document builder factory. This method tries to
     * configure the namespace support for the builder. If the underlying parser
     * does not support namespaces then this method returns a simple
     * DocumentBuilder object.
     * 
     * @return a new document builder
     * @throws ParserConfigurationException
     */
    private static DocumentBuilder getDocumentBuilder(boolean isNamespaceAware)
        throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(isNamespaceAware); // never forget this!
        try {
            factory.setFeature(
                "http://xml.org/sax/features/namespaces",
                isNamespaceAware);
        } catch (Throwable t) {
            // Just skip it...
        }
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder;
    }

    public static String getText(Node node) throws IOException {
        StringWriter writer = new StringWriter();
        if (!serializeXmlNode(node, writer, false)) {
            return null;
        }
        return writer.toString();
    }

    /**
     * Creates and returns a new empty DOM document.
     * 
     * @return a newly created DOM document
     * @throws ParserConfigurationException
     */
    public static Document newDocument() throws Exception {
        DocumentBuilder builder = getDocumentBuilder(true);
        return builder.newDocument();
    }

    public static Document parseMetadataMap(Map<String, String> metainfo)
        throws Exception {
        DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
        DocumentBuilder bd = fact.newDocumentBuilder();
        Document doc = bd.newDocument();
        Element rt = doc.createElement("ROOT");
        doc.appendChild(rt);
        for (String key : metainfo.keySet()) {
            Element e1 = doc.createElement("param");
            e1.setAttribute("key", key);
            String value = metainfo.get(key);
            e1.setAttribute("value", value);
            rt.appendChild(e1);
        }
        return doc;
    }

    /**
     * Parses the given input stream and returns the corresponding desirialized
     * XML document.
     * 
     * @param input the input stream containing the serialized XML document
     * @return the deserialized DOM document
     * @throws Exception
     */
    public static Document readXML(InputStream input) throws Exception {
        return readXML(new InputStreamReader(input, "UTF-8"), true);
    }

    /**
     * Parses the given input stream and returns the corresponding desirialized
     * XML document.
     * 
     * @param input the input stream containing the serialized XML document
     * @return the deserialized DOM document
     * @throws Exception
     */
    public static Document readXML(InputStream input, boolean isNamespaceAware)
        throws Exception {
        return readXML(new InputStreamReader(input, "UTF-8"), isNamespaceAware);
    }

    public static Document readXML(Reader reader) throws Exception {
        return readXML(reader, true);
    }

    /**
     * Parses the given input stream and returns the corresponding desirialized
     * XML document.
     * 
     * @param reader the reader containing the serialized XML document
     * @param isNamespaceAware
     * @return the deserialized DOM document
     * @throws Exception
     */
    public static Document readXML(Reader reader, boolean isNamespaceAware)
        throws Exception {
        try {
            DocumentBuilder builder = getDocumentBuilder(isNamespaceAware);
            InputSource source = new InputSource(reader);
            Document doc = builder.parse(source);
            return doc;
        } finally {
            reader.close();
        }
    }

    /**
     * @param doc
     * @param out
     * @throws IOException
     */
    public static void serializeXML(Document doc, OutputStream out)
        throws IOException {
        serializeXML(doc.getDocumentElement(), out);
    }

    public static void serializeXML(Document doc, Writer writer)
        throws IOException {
        serializeXML(doc.getDocumentElement(), writer, true);
    }

    public static void serializeXML(
        Document doc,
        Writer writer,
        boolean addXmlDeclaration) throws IOException {
        serializeXML(doc.getDocumentElement(), writer, addXmlDeclaration);
    }

    public static void serializeXML(Element doc, OutputStream out)
        throws IOException {
        Writer writer = new OutputStreamWriter(out, "UTF-8");
        serializeXML(doc, writer, true);
    }

    private static void serializeXML(
        Element doc,
        Writer writer,
        boolean addXmlDeclaration) throws IOException {
        try {
            DOMImplementationRegistry registry = DOMImplementationRegistry
                .newInstance();
            DOMImplementationLS impl = (DOMImplementationLS) registry
                .getDOMImplementation("LS");
            LSSerializer serializer = impl.createLSSerializer();
            DOMConfiguration config = serializer.getDomConfig();
            config.setParameter("xml-declaration", addXmlDeclaration);
            // config.setParameter("format-pretty-print", true);
            // config.setParameter("normalize-characters", true);
            LSOutput out = impl.createLSOutput();
            out.setCharacterStream(writer);
            serializer.write(doc, out);
        } catch (Throwable e) {
            throw new IOException(e.getMessage());
        }
    }

    public static String serializeXML(Node node) throws IOException {
        StringWriter writer = new StringWriter();
        if (!serializeXmlNode(node, writer, true)) {
            return null;
        }
        return writer.toString();
    }

    private static boolean serializeXmlNode(
        Node node,
        Writer writer,
        boolean includeNode) throws IOException {
        if (node == null) {
            return false;
        }
        short type = node.getNodeType();
        boolean result = true;
        switch (type) {
            case Node.ATTRIBUTE_NODE: {
                String text = ((Attr) node).getValue();
                writer.write(text);
                break;
            }
            case Node.TEXT_NODE: {
                String text = ((Text) node).getData();
                writer.write(text);
                break;
            }
            case Node.ELEMENT_NODE: {
                Element element = (Element) node;
                if (includeNode) {
                    serializeXML(element, writer, false);
                } else {
                    Node child = element.getFirstChild();
                    while (child != null) {
                        serializeXmlNode(child, writer, true);
                        child = child.getNextSibling();
                    }
                }
                break;
            }
            case Node.DOCUMENT_NODE: {
                Document doc = (Document) node;
                serializeXmlNode(doc.getDocumentElement(), writer, includeNode);
                break;
            }
            default:
                result = false;
                break;
        }
        return result;
    }

}
