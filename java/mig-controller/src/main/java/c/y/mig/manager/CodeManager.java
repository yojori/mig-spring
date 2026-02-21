package c.y.mig.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import jakarta.annotation.PostConstruct;

@Component
public class CodeManager {

    private static Map<String, List<Map<String, String>>> codeMap = new HashMap<>();
    private static Map<String, String> singleCodeMap = new HashMap<>();

    @PostConstruct
    public void init() {
        try {
            // Load from webapp/WEB-INF/config/pageCode.xml
            // In Spring Boot, accessing WEB-INF directly can be tricky if running as JAR.
            // But since we are targeting a structure that supports JSPs, we assume standard
            // layout.
            // Ideally this should be in resources, but for porting we look where it was.

            // Try classpath resource first, or file system relative to run dir
            File xmlFile = new File("src/main/webapp/WEB-INF/config/pageCode.xml");
            if (!xmlFile.exists()) {
                // Fallback for runtime
                xmlFile = new File("WEB-INF/config/pageCode.xml");
            }

            if (xmlFile.exists()) {
                loadXml(xmlFile);
            } else {
                System.err.println("pageCode.xml not found!");
                throw new RuntimeException("pageCode.xml not found at " + xmlFile.getAbsolutePath());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadXml(File file) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(file);
        doc.getDocumentElement().normalize();

        NodeList nList = doc.getElementsByTagName("code");

        // The structure is <code-0001><option...></code-0001> inside <code>
        // Actually the example shows <code> <code-0001> ... </code-0001> </code>

        Node codeNode = nList.item(0); // <code> tag
        NodeList childNodes = codeNode.getChildNodes();

        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                String codeId = node.getNodeName(); // e.g., code-0001
                List<Map<String, String>> options = new ArrayList<>();

                NodeList optionList = node.getChildNodes();
                for (int j = 0; j < optionList.getLength(); j++) {
                    Node optNode = optionList.item(j);
                    if (optNode.getNodeType() == Node.ELEMENT_NODE && "option".equals(optNode.getNodeName())) {
                        Element optElement = (Element) optNode;
                        Map<String, String> optMap = new HashMap<>();
                        optMap.put("value", optElement.getAttribute("value"));
                        optMap.put("text", optElement.getTextContent());
                        options.add(optMap);
                    }
                }
                codeMap.put(codeId, options);
            }
        }
    }

    public static List<Map<String, String>> getCodeList(String codeId) {
        if (codeId != null && codeId.startsWith("pageCode.code.")) {
            codeId = codeId.substring("pageCode.code.".length());
        }
        return codeMap.getOrDefault(codeId, new ArrayList<>());
    }

    // Legacy helper
    public static String getSingleCode(String key) {
        return singleCodeMap.get(key);
    }
}
