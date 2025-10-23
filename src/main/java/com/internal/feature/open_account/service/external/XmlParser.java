package com.internal.feature.open_account.service.external;

import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

@Slf4j
public class XmlParser {

    private XmlParser() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Extract CIF from T24 customer creation response
     * Looks for <CUSTOMERType id="123456789">
     */
    public static String extractCif(Document document) {
        try {
            // Try to find CUSTOMERType element
            NodeList customerNodes = document.getElementsByTagName("CUSTOMERType");
            if (customerNodes.getLength() > 0) {
                Element customerElement = (Element) customerNodes.item(0);
                String cif = customerElement.getAttribute("id");
                if (cif != null && !cif.trim().isEmpty()) {
                    log.info("Extracted CIF: {}", cif);
                    return cif.trim();
                }
            }
            
            // Alternative: Try to find CUSTOMER element
            customerNodes = document.getElementsByTagName("CUSTOMER");
            if (customerNodes.getLength() > 0) {
                Element customerElement = (Element) customerNodes.item(0);
                String cif = customerElement.getAttribute("id");
                if (cif != null && !cif.trim().isEmpty()) {
                    log.info("Extracted CIF from CUSTOMER: {}", cif);
                    return cif.trim();
                }
            }
            
            // Alternative: Try to find id element directly
            NodeList idNodes = document.getElementsByTagName("id");
            if (idNodes.getLength() > 0) {
                String cif = idNodes.item(0).getTextContent();
                if (cif != null && !cif.trim().isEmpty()) {
                    log.info("Extracted CIF from id element: {}", cif);
                    return cif.trim();
                }
            }
            
            log.warn("No CIF found in XML response");
            return null;
            
        } catch (Exception e) {
            log.error("Failed to extract CIF from XML: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Extract account number from T24 account creation response
     * Looks for <Status><transactionId>KH0012011123456789</transactionId></Status>
     */
    public static String extractAccountNumber(Document document) {
        try {
            // Try to find Status element with transactionId
            NodeList statusNodes = document.getElementsByTagName("Status");
            for (int i = 0; i < statusNodes.getLength(); i++) {
                Element statusElement = (Element) statusNodes.item(i);
                
                // Check if this is an ACCOUNT application
                NodeList appNodes = statusElement.getElementsByTagName("application");
                if (appNodes.getLength() > 0 && "ACCOUNT".equals(appNodes.item(0).getTextContent())) {
                    NodeList transIdNodes = statusElement.getElementsByTagName("transactionId");
                    if (transIdNodes.getLength() > 0) {
                        String accountNumber = transIdNodes.item(0).getTextContent();
                        log.info("Extracted account number: {}", accountNumber);
                        return accountNumber;
                    }
                }
            }
            
            // Alternative: Direct transactionId lookup
            NodeList transIdNodes = document.getElementsByTagName("transactionId");
            if (transIdNodes.getLength() > 0) {
                String accountNumber = transIdNodes.item(0).getTextContent();
                log.info("Extracted account number from transactionId: {}", accountNumber);
                return accountNumber;
            }
            
            // Alternative: Try to find ACCOUNTType element
            NodeList accountNodes = document.getElementsByTagName("ACCOUNTType");
            if (accountNodes.getLength() > 0) {
                Element accountElement = (Element) accountNodes.item(0);
                String accountNumber = accountElement.getAttribute("id");
                if (accountNumber != null && !accountNumber.trim().isEmpty()) {
                    log.info("Extracted account number from ACCOUNTType: {}", accountNumber);
                    return accountNumber.trim();
                }
            }
            
            log.warn("No account number found in XML response");
            return null;
            
        } catch (Exception e) {
            log.error("Failed to extract account number from XML: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Check if XML response contains error
     */
    public static boolean hasError(Document document) {
        try {
            NodeList errorNodes = document.getElementsByTagName("error");
            if (errorNodes.getLength() > 0) {
                return true;
            }
            
            NodeList faultNodes = document.getElementsByTagName("faultstring");
            if (faultNodes.getLength() > 0) {
                return true;
            }
            
            return false;
        } catch (Exception e) {
            log.error("Error checking for XML errors: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extract error message from XML response
     */
    public static String extractErrorMessage(Document document) {
        try {
            NodeList errorNodes = document.getElementsByTagName("error");
            if (errorNodes.getLength() > 0) {
                return errorNodes.item(0).getTextContent();
            }
            
            NodeList faultNodes = document.getElementsByTagName("faultstring");
            if (faultNodes.getLength() > 0) {
                return faultNodes.item(0).getTextContent();
            }
            
            return "Unknown error";
        } catch (Exception e) {
            log.error("Failed to extract error message: {}", e.getMessage());
            return "Error parsing response";
        }
    }

    /**
     * Pretty print XML document for debugging
     */
    public static String documentToString(Document document) {
        try {
            javax.xml.transform.TransformerFactory tf = javax.xml.transform.TransformerFactory.newInstance();
            javax.xml.transform.Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(javax.xml.transform.OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(javax.xml.transform.OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            java.io.StringWriter writer = new java.io.StringWriter();
            transformer.transform(new javax.xml.transform.dom.DOMSource(document), 
                                new javax.xml.transform.stream.StreamResult(writer));
            return writer.toString();
        } catch (Exception e) {
            log.error("Failed to convert document to string: {}", e.getMessage());
            return document.toString();
        }
    }
}