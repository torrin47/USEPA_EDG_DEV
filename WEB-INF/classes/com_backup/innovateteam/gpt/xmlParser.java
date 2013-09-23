package com.innovateteam.gpt;

import java.util.Iterator;
import java.util.HashMap;
import java.util.ArrayList;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import java.io.StringReader;
import org.xml.sax.InputSource;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

/**
 *
 * @author Deewen
 */
class node {

    private int index;
    private Node self;
    private ArrayList children = new ArrayList();
    private String val;
    private String name;

    public node(Node self) {
        this.self = self;
        this.name = self.getNodeName();
        this.val = "";
    }

    public void setVal(String val) {
        this.val = val;
    }

    public String getVal() {
        return this.val;
    }

    public String getName() {
        return this.name;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return this.index;
    }

    public Node getNode() {
        return this.self;
    }

    public void addChild(node child) {
        this.children.add(child);
    }

    public ArrayList getChildren() {
        return this.children;
    }
}

class xmlParser {

    private String xml = null;
    private Document doc = null;
    private ArrayList xpaths;
    private HashMap xpathVal;
    private String rootNodeName = null;

    public xmlParser(String xml) throws Exception {
        this.xpaths = new ArrayList();
        this.xpathVal = new HashMap();
        this.doc = null;
        this.xml = xml;

        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        InputSource is = new InputSource();
        is.setCharacterStream(new StringReader(this.xml));

        this.doc = db.parse(is);
    }

    public boolean isLeafNode(Node node) {
        NodeList subNodes = node.getChildNodes();
        int len = subNodes.getLength();
        for (int i = 0; i < len; i++) {
            if (subNodes.item(i).getNodeType() == Node.ELEMENT_NODE && subNodes.item(i).hasChildNodes()) {
                return false;
            }
        }
        return true;
    }

    public Node getRootNode() {
        Node root = null;
        NodeList list = this.doc.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            if (list.item(i).getNodeType() == Node.ELEMENT_NODE) {
                root = list.item(i);
                break;
            }
        }
        return root;
    }

    public int getChildIndex(node parent, node child) {
        int returnIndex = 0;
        String tmp;
        String childTagName = child.getNode().getNodeName();
        ArrayList children = parent.getChildren();
        Iterator it = children.iterator();
        while (it.hasNext()) {
            node n = (node) it.next();
            tmp = n.getNode().getNodeName();
            if (tmp.equalsIgnoreCase(childTagName)) {
                returnIndex++;
            }
        }
        return returnIndex + 1;
    }

    public void traverseChild(node parent) {
        Node node = parent.getNode();
        NodeList nodes_i = node.getChildNodes();
        for (int i = 0; i < nodes_i.getLength(); i++) {
            Node nNode = nodes_i.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                node child = new node(nNode);
                child.setIndex(this.getChildIndex(parent, child));
                parent.addChild(child);

                if (!(this.isLeafNode(nNode))) {

                    this.traverseChild(child);
                } else {

                    child.setVal(nNode.getTextContent());
                }

            }
        }
    }

    public node traverseNodes() {
        Node root = this.getRootNode();
        if (root != null) {
            node nodeRoot = new node(root);
            nodeRoot.setIndex(1);
            this.traverseChild(nodeRoot);

            return nodeRoot;
        }
        return null;
    }
}