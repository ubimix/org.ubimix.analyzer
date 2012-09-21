/**
 * 
 */
package org.ubimix.analyzer;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import junit.framework.TestCase;

import org.ubimix.analyzer.scores.ScoreGenerator;
import org.ubimix.analyzer.scores.ScoreGenerator.TagInfo;
import org.ubimix.analyzer.scores.impl.ScoreGeneratorFactory;
import org.ubimix.analyzer.server.ElementInfoProvider;
import org.ubimix.analyzer.server.ElementScoreGenerator;
import org.ubimix.analyzer.utils.HTMLUtils;
import org.ubimix.analyzer.utils.IOUtil;
import org.ubimix.analyzer.utils.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author kotelnikov
 */
public class FileAnalyzerSandbox extends TestCase {

    protected boolean fEnablePrint = true;

    protected File fHtmlDir;

    private ScoreGeneratorFactory<Element> fScoreGeneratorFactory = new ScoreGeneratorFactory<Element>(
        new ElementInfoProvider());

    protected File fXmlDir;

    public FileAnalyzerSandbox(String name) {
        super(name);
    }

    /**
     * Converts HTML files into clean XML files.
     * 
     * @param htmlDir directory containing the input files
     * @param xmlDir directory containing the cleaned files
     * @throws Exception
     */

    private void cleanupFiles(File htmlDir, File xmlDir) throws Exception {
        IOUtil.delete(xmlDir);
        xmlDir.mkdirs();
        File[] files = getFiles(htmlDir, ".html");
        for (File inFile : files) {
            String fileName = inFile.getName();
            fileName = fileName.substring(
                0,
                fileName.length() - ".html".length())
                + ".xml";
            File outFile = new File(xmlDir, fileName);

            print("Converting: " + inFile + " -> " + outFile + "...");
            String str = IOUtil.readString(inFile);
            Document doc = HTMLUtils.cleanupHTML(new StringReader(str));
            FileOutputStream out = new FileOutputStream(outFile);
            XMLUtil.serializeXML(doc, out);
            println("   OK");
        }
    }

    public File[] getFiles(File dir, final String ext) {
        File[] files = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(ext);
            }
        });
        if (files == null) {
            return new File[0];
        }
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        return files;
    }

    protected File getRootDir() {
        return new File("./");
    }

    protected void print(String string) {
        if (fEnablePrint) {
            System.out.print(string);
        }
    }

    protected void println(String string) {
        print(string + "\n");
    }

    private void println1(String string) {
        System.out.println(string);
    }

    /**
     * Initializes the input HTML and XML directories.
     * 
     * @see junit.framework.TestCase#setUp()
     */

    @Override
    protected void setUp() throws Exception {
        File rootDir = getRootDir();
        fXmlDir = new File(rootDir, "tmp-xml");
        fHtmlDir = new File(rootDir, "tmp-html");
    }

    public void testNormalizer() throws Exception {
        // Deletes empty directory
        fXmlDir.delete();
        if (!fXmlDir.exists()) {
            cleanupFiles(fHtmlDir, fXmlDir);
        }

        File[] list = getFiles(fXmlDir, ".xml");
        for (File file : list) {
            String xml = IOUtil.readString(file);
            Document doc = XMLUtil.readXML(new StringReader(xml));
            // HTMLUtils.cleanupHTML(new StringReader(xml));
            Element root = doc.getDocumentElement();
            Element body = (Element) root.getChildNodes().item(1);

            final List<TagInfo<Element>> tagInfoList = new ArrayList<TagInfo<Element>>();
            ScoreGenerator<Element> generator = fScoreGeneratorFactory
                .newScoreGenerator();
            ElementScoreGenerator util = new ElementScoreGenerator(generator) {
                @Override
                public TagInfo<Element> endTag() {
                    TagInfo<Element> info = super.endTag();
                    tagInfoList.add(info);
                    Element tag = info.getTag();
                    tag.setAttribute("score", ""
                        + info.getFullScore().getLevel(0));
                    return info;
                }
            };
            util.visit(body);
            Collections.sort(tagInfoList, new Comparator<TagInfo<Element>>() {
                @Override
                public int compare(TagInfo<Element> o1, TagInfo<Element> o2) {
                    return -o1.getFullScore().compareTo(o2.getFullScore(), 0);
                }
            });
            String fileName = file.getName();
            println1("====================================================");
            println1(fileName);
            println1("----------------------------------------------------");

            int len = 300;
            int count = 3;
            for (int i = 0; i < count; i++) {
                if (i > 0) {
                    println1("----------------------------------------------------");
                }
                TagInfo<Element> info = tagInfoList.get(i);
                println(toString(len, info));
            }
        }
    }

    public String toString(int len, TagInfo<Element> info) throws IOException {
        Element element = info.getTag();
        String str = XMLUtil.serializeXML(element);
        str = str.replaceAll("[\r\n \t]+", " ");
        int l = Math.min(len, str.length());
        str = str.substring(0, l);

        double fullScore = info.getFullScore().getLevel(0);
        str = String.format("%f", fullScore) + " - " + str;
        return str;
    }

}
//