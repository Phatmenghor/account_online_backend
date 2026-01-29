package com.internal.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.util.Iterator;

public class XmlParser {

    public static String extractCif(Document doc) {
        try {
            XPath xpath = XPathFactory.newInstance().newXPath();
            xpath.setNamespaceContext(new NamespaceContext() {
                @Override
                public String getNamespaceURI(String prefix) {
                    switch (prefix) {
                        case "ns0":
                            return "http://temenos.com/OAOWAR";
                        default:
                            return null;
                    }
                }

                @Override
                public String getPrefix(String namespaceURI) {
                    return null;
                }

                @Override
                public Iterator getPrefixes(String namespaceURI) {
                    return null;
                }
            });

            Node node = (Node) xpath.evaluate(
                    "//ns0:OAOCUSTOMERCREATIONResponse/CUSTOMERType/@id",
                    doc,
                    XPathConstants.NODE
            );

            return node != null ? node.getNodeValue() : null;

        } catch (Exception e) {
            return null;
        }
    }

    public static String extractMnemonic(Document doc) {
        try {
            XPath xpath = XPathFactory.newInstance().newXPath();
            xpath.setNamespaceContext(new NamespaceContext() {
                @Override
                public String getNamespaceURI(String prefix) {
                    return switch (prefix) {
                        case "ns0" -> "http://temenos.com/OAOWAR";
                        case "ns3" -> "http://temenos.com/CUSTOMER";
                        default -> null;
                    };
                }

                @Override
                public String getPrefix(String namespaceURI) {
                    return null;
                }

                @Override
                public Iterator getPrefixes(String namespaceURI) {
                    return null;
                }
            });

            Node node = (Node) xpath.evaluate(
                    "//ns0:OAOCUSTOMERCREATIONResponse/CUSTOMERType/ns3:MNEMONIC",
                    doc,
                    XPathConstants.NODE
            );

            return node != null ? node.getTextContent() : null;

        } catch (Exception e) {
            return null;
        }
    }


}
