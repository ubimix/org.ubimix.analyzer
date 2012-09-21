package org.ubimix.analyzer.server;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.ubimix.analyzer.scores.impl.AbstractTagScoreDetector.ITagInfoProvider;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * @author kotelnikov
 */
public class ElementInfoProvider implements ITagInfoProvider<Element> {

    public static String getName(Node node) {
        String name = node.getNodeName();
        if (name == null) {
            name = node.getLocalName();
        }
        return name;
    }

    @Override
    public Iterable<Map.Entry<String, String>> getTagAttributes(Element tag) {
        final NamedNodeMap attrs = tag.getAttributes();
        return new Iterable<Map.Entry<String, String>>() {
            @Override
            public Iterator<Entry<String, String>> iterator() {
                return new Iterator<Entry<String, String>>() {

                    private int fPos;

                    @Override
                    public boolean hasNext() {
                        return fPos < attrs.getLength();
                    }

                    @Override
                    public Entry<String, String> next() {
                        if (fPos >= attrs.getLength()) {
                            return null;
                        }
                        final Attr attr = (Attr) attrs.item(fPos++);
                        return new Entry<String, String>() {

                            @Override
                            public String getKey() {
                                String name = getName(attr);
                                return name;
                            }

                            @Override
                            public String getValue() {
                                String value = attr.getValue();
                                return value;
                            }

                            @Override
                            public String setValue(String value) {
                                throw new NoSuchMethodError();
                            }
                        };
                    }

                    @Override
                    public void remove() {
                        if (fPos < attrs.getLength()) {
                            Attr attr = (Attr) attrs.item(fPos);
                            Node parent = attr.getParentNode();
                            parent.removeChild(attr);
                        }
                    }
                };
            }
        };
    }

    @Override
    public String getTagName(Element tag) {
        return getName(tag);
    }

}