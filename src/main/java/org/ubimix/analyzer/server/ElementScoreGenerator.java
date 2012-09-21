package org.ubimix.analyzer.server;

import org.ubimix.analyzer.scores.ScoreGenerator;
import org.ubimix.analyzer.scores.ScoreGenerator.TagInfo;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Element;
import org.w3c.dom.Entity;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * @author kotelnikov
 */
public class ElementScoreGenerator {

    public static String getName(Node node) {
        String name = node.getNodeName();
        if (name == null) {
            name = node.getLocalName();
        }
        return name;
    }

    private ScoreGenerator<Element> fGenerator;

    public ElementScoreGenerator(ScoreGenerator<Element> generator) {
        fGenerator = generator;
    }

    public TagInfo<Element> beginTag(Element tag) {
        return fGenerator.beginTag(tag);
    }

    public TagInfo<Element> endTag() {
        return fGenerator.endTag();
    }

    public void onText(String content) {
        fGenerator.onText(content);
    }

    public void visit(Element tag) {
        beginTag(tag);
        NodeList children = tag.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child instanceof Element) {
                visit((Element) child);
            } else {
                String content = null;
                if (child instanceof CDATASection) {
                    CDATASection cdata = (CDATASection) child;
                    // content = cdata.getTextContent();
                    content = cdata.getData();
                } else if (child instanceof Text) {
                    Text text = (Text) child;
                    content = text.getTextContent();
                } else if (child instanceof Entity) {
                    Entity entity = (Entity) child;
                    content = entity.getTextContent();
                }
                if (content != null) {
                    onText(content);
                }
            }
        }
        endTag();
    }
}