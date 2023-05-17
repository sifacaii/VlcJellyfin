package org.sifacai.vlcjellyfin.Dlna;

import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class XmlParser {
    private static String TAG = "XML解析器";

    private String getRspXML(String action, HashMap<String, String> map) {
        String rsp = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
                "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"" +
                " s:encodingstyle=\"http://schemas.xmlsoap.org/soap/encoding/\">" +
                "<s:Body>" +
                "<u:" + action + "Response xmlns:u=\"urn:schemas-upnp-org:service:AVTransport:1\">";

        if (map != null) {
            for (String key : map.keySet()) {
                rsp += "<" + key + ">" + map.get(key) + "</" + key + ">";
            }
        }

        rsp += "</u:" + action + "Response>" +
                "</s:Body>" +
                "</s:Envelope>";

        return rsp;
    }

    public static void ReadNode(XmlPullParser p, HashMap phm, String endTag) throws XmlPullParserException, IOException {
        int et = p.next();
        String tagName = p.getName();
        do {
            if (et == p.START_TAG) {
                HashMap chm = new HashMap();
                phm.put(tagName, chm);
                ReadNode(p, chm, tagName);
            } else if (et == p.TEXT) {
                phm.put("TEXT-VALUE", p.getText());
            } else if (et == p.END_TAG) {
                Log.d(TAG, "ReadNode: END:" + p.getName());
                if (tagName.equals(endTag)) return;
            }
            et = p.next();
            tagName = p.getName();
        } while (et != p.END_DOCUMENT);
    }

    public static DlnaDevice parseX(String xml) throws ParserConfigurationException, IOException, SAXException {
        DlnaDevice dd = new DlnaDevice();
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));

        NodeList nl = doc.getElementsByTagName("friendlyName");
        dd.friendlyName = nl.getLength() > 0 ? nl.item(0).getTextContent() : "";
        nl = doc.getElementsByTagName("modelName");
        dd.modelName = nl.getLength() > 0 ? nl.item(0).getTextContent() : "";
        nl = doc.getElementsByTagName("UDN");
        dd.UDN = nl.getLength() > 0 ? nl.item(0).getTextContent() : "";
        nl = doc.getElementsByTagName("deviceType");
        dd.deviceType = nl.getLength() > 0 ? nl.item(0).getTextContent() : "";


        nl = doc.getElementsByTagName("service");
        for (int i = 0; i < nl.getLength(); i++) {
            NodeList ns = nl.item(i).getChildNodes();
            DlnaService ds = new DlnaService();
            dd.DlnaServices.add(ds);
            for (int j = 0; j < ns.getLength(); j++) {
                Node nd = ns.item(j);
                switch (nd.getNodeName()){
                    case "serviceType":
                        ds.serviceType = nd.getTextContent();
                        break;
                    case "serviceId":
                        ds.serviceId = nd.getTextContent();
                        break;
                    case "controlURL":
                        ds.controlURL = nd.getTextContent();
                        break;
                    case "eventSubURL":
                        ds.eventSubURL = nd.getTextContent();
                        break;
                    case "SCPDURL":
                        ds.SCPDURL = nd.getTextContent();
                        break;
                }
            }
        }
        return dd;
    }
}
