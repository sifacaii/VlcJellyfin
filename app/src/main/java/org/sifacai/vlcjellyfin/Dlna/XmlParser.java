package org.sifacai.vlcjellyfin.Dlna;

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;


public class XmlParser {
    private static String TAG = "XML解析器";

    public static void readServiceValue(XmlPullParser parser, DlnaService service, String tagName) throws XmlPullParserException, IOException {
        String value = parser.nextText();
        if (tagName.equals("servicetype")) service.serviceType = value;
        if (tagName.equals("serviceid")) service.serviceId = value;
        if (tagName.equals("controlurl")) service.controlURL = value;
        if (tagName.equals("scpdurl")) service.SCPDURL = value;
        if (tagName.equals("eventsuburl")) service.eventSubURL = value;
    }

    public static DlnaService readService(XmlPullParser parser) throws XmlPullParserException, IOException {
        DlnaService ds = new DlnaService();
        String tagName = "";
        int eventType = parser.next();
        while (!tagName.equals("service") && eventType != XmlPullParser.END_DOCUMENT) {
            tagName = parser.getName();
            if (tagName == null) tagName = "";
            if (eventType == XmlPullParser.START_TAG) {
                tagName = tagName.toLowerCase();
                readServiceValue(parser, ds, tagName);
            }
            eventType = parser.next();
        }
        return ds;
    }

    public static void readDevice(XmlPullParser parser, DlnaDevice de, String tagName) throws XmlPullParserException, IOException {
        int et = parser.next();
        if(et == XmlPullParser.END_TAG) return;
        String value = parser.getText();
        value = value == null ? "" : value.trim();
        if (tagName.equals("friendlyname")) de.friendlyName = value;
        if (tagName.equals("devicetype")) de.deviceType = value;
        if (tagName.equals("modelname")) de.modelName = value;
        if (tagName.equals("udn")) de.UDN = value;
    }

    public static DlnaDevice ParseXML2(String xml) throws XmlPullParserException, IOException {
        XmlPullParser xmlPullParser = Xml.newPullParser();
        xmlPullParser.setInput(new StringReader(xml));

        DlnaDevice device = new DlnaDevice();

        int eventType = xmlPullParser.getEventType();
        String tagName = "";
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                tagName = xmlPullParser.getName().toLowerCase();
                if (tagName.equals("service")) {
                    DlnaService service = readService(xmlPullParser);
                    device.DlnaServices.add(service);
                } else {
                    readDevice(xmlPullParser, device, tagName);
                }
            }
            eventType = xmlPullParser.next();
        }
        return device;
    }

    public static DlnaDevice ParseXML(String xml) throws XmlPullParserException, IOException {
        XmlPullParser xmlPullParser = Xml.newPullParser();
        xmlPullParser.setInput(new StringReader(xml));

        DlnaDevice device = new DlnaDevice();

        int eventType = xmlPullParser.getEventType();
        String tagName = "";
        DlnaService service = null;
        String icon = "";
        while (eventType != XmlPullParser.END_DOCUMENT) {
            String value = "";
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    tagName = xmlPullParser.getName().toLowerCase();
                    Log.d(TAG, "ParseXML: tagName:" + tagName);
                    if (tagName.equals("service")) service = new DlnaService();
                    if (tagName.equals("icon")) icon = "";
                    break;
                case XmlPullParser.TEXT:

                    break;
                case XmlPullParser.END_TAG:
                    value = xmlPullParser.getText();
                    value = value == null ? "" : value.trim();
                    Log.d(TAG, "ParseXML: tagName:" + tagName + " value:" + value);
                    if (tagName.equals("friendlyname")) {
                        device.friendlyName = value;
                        //Log.d(TAG, "ParseXML: friendlyname" + value);
                    }
                    if (tagName.equals("devicetype")) device.deviceType = value;
                    if (tagName.equals("modelname")) device.modelName = value;
                    if (tagName.equals("udn")) device.UDN = value;

                    if (tagName.equals("url")) icon = value;

                    if (tagName.equals("servicetype")) service.serviceType = value;
                    if (tagName.equals("serviceid")) service.serviceId = value;
                    if (tagName.equals("controlurl")) service.controlURL = value;
                    if (tagName.equals("scpdurl")) service.SCPDURL = value;
                    if (tagName.equals("eventsuburl")) service.eventSubURL = value;

                    String endTag = xmlPullParser.getName().toLowerCase();
                    Log.d(TAG, "ParseXML: endTag:" + endTag);
                    if (endTag.equals("service"))
                        device.DlnaServices.add(service);
                    if (endTag.equals("icon")) device.icon.add(icon);
                    break;
            }
            eventType = xmlPullParser.next();
        }
        return device;
    }

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
}
