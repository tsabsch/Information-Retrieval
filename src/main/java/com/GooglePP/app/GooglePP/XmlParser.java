package com.GooglePP.app.GooglePP;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.lucene.queryparser.xml.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A parser to parse a given reuters xml file
 *
 */
public class XmlParser {

	/**
	 * Removes the white space nodes generated by the xml parser
	 * 
	 * @param doc
	 *            the document from which these nodes should be removed
	 */
	private static void removeWhiteSpaceNodes(Document doc) {
		removeWhiteSpaceNodes(doc.getFirstChild());
	}

	/**
	 * Removes the white space nodes generated by the xml parser
	 * 
	 * @param Node
	 *            node the document from which these nodes should be removed
	 */
	private static void removeWhiteSpaceNodes(Node node) {
		// getting all the child nodes and iterate through all of them
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); ++i) {
			boolean removed = false;
			Node currentNode = children.item(i);
			// check if the node only consists of text
			if (currentNode.getNodeType() == Node.TEXT_NODE) {
				// remove the node if there is only white space
				removed = currentNode.getTextContent().trim().length() == 0;
				if (removed) {
					currentNode.getParentNode().removeChild(currentNode);
					--i;
				}
			}
			if (!removed) {
				// go recursively through all children if the nod was not
				// removed
				removeWhiteSpaceNodes(currentNode);
			}
		}
	}

	/**
	 * loading an xml document from a given path, and converts it to an
	 * org.w3c.dom.Document
	 * 
	 * @param filePath
	 *            the path of the xml file
	 * @return the parsed xml file
	 */
	private static Document loadDocument(String filePath) {
		File file = new File(filePath);
		Document doc = null;
		if (file.canRead()) {
			try {
				// loading the xml file via the DOMUtils from lucene
				FileInputStream iStream = new FileInputStream(file);
				InputStreamReader inputReader = new InputStreamReader(iStream);
				doc = DOMUtils.loadXML(inputReader);
				inputReader.close();
			} catch (IOException e) {
				System.err.println("Couldn't open: " + file.getAbsolutePath());
				e.printStackTrace();
				return null;
			}
		} else {
			System.err.println("Couldn't open: " + file.getAbsolutePath());
		}
		return doc;
	}

	/**
	 * parse the documents saved in a reuters xml file
	 * 
	 * @param filePath
	 *            the path to the xml file
	 * @return a list of the saved documents
	 */
	public static List<Doc> getDocs(String filePath) {
		// loading the xml file
		Document doc = loadDocument(filePath);
		// remove all white space nodes
		removeWhiteSpaceNodes(doc);
		// get the reuters nodes, where the documents are saved
		NodeList nodeList = doc.getChildNodes().item(0).getChildNodes();
		List<Doc> docs = new ArrayList<Doc>();
		// create a dateformat used to parse the date
		DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss.SS", Locale.ENGLISH);
		// iterate through every saved document
		for (int i = 0; i < nodeList.getLength(); ++i) {
			Node currentNode = nodeList.item(i);
			// get the id, which is saved as attribute of the reuters node
			int id = Integer.valueOf(currentNode.getAttributes().getNamedItem("NEWID").getNodeValue());
			Date date = null;
			String title = "";
			String text = "";
			NodeList children = currentNode.getChildNodes();

			for (int childIndex = 0; childIndex < children.getLength(); ++childIndex) {
				Node childNode = children.item(childIndex);
				if (childNode.getNodeName().equals("DATE")) {
					// parse the date with the dateformat defined before
					try {
						String dateString = childNode.getFirstChild().getNodeValue().trim();
						date = dateFormat.parse(dateString);
					} catch (ParseException e) {
						e.printStackTrace();
					}
				} else if (childNode.getNodeName().equals("TEXT")) {
					// parse the whole text of the document
					NodeList textChildren = childNode.getChildNodes();
					for (int textChildIndex = 0; textChildIndex < textChildren.getLength(); ++textChildIndex) {
						Node textChildNode = textChildren.item(textChildIndex);
						if (textChildNode.getNodeName().equals("TITLE")) {
							// get the document's title
							title = textChildNode.getFirstChild().getNodeValue();
						} else if (textChildNode.getNodeName().equals("BODY")) {
							// get the document's content
							text = textChildNode.getFirstChild().getNodeValue();
						}
					}
				}
			}
			docs.add(new Doc(id, date, title, text));
		}

		return docs;
	}

	public static void main(String[] args) {
		String filePath = "reut2-000.xml";
		List<Doc> docs = getDocs(filePath);
		for (int i = 0; i < docs.size(); ++i) {
			Doc currentDoc = docs.get(i);
			System.out.println("ID: " + currentDoc.getId());
			System.out.println("DATE: " + currentDoc.getDate());
			System.out.println("TITLE: " + currentDoc.getTitle());
			System.out.println("TEXT: " + currentDoc.getText().split("\n")[0]);
			System.out.println("####################################################################");
		}
	}
}
