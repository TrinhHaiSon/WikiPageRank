/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wikipagerank;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.Scanner;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author trinhhaison
 */
public class PageInfo {
    long pageID;
    String pageTitle;

    public PageInfo(String pageTitle) throws UnsupportedEncodingException, IOException, ParseException {
        this.pageTitle = pageTitle;
        String infoUrl = "https://en.wikipedia.org/w/api.php?action=query&prop=info&format=json&formatversion=2&titles="+URLEncoder.encode(pageTitle, "UTF-8");
        String json = getJSON(infoUrl);
        JSONObject jo = (JSONObject) new JSONParser().parse(json);
        JSONArray pages = (JSONArray)((Map)jo.get("query")).get("pages");
        Map page = (Map)pages.get(0);
        if(page.get("pageid") == null){
            this.pageID = -1;
        }
        else{
            this.pageID = (long) page.get("pageid");
        }
    }

    public PageInfo(long pageID) throws IOException, ParseException {
        this.pageID = pageID;
        String infoUrl = "https://en.wikipedia.org/w/api.php?action=query&prop=info&pageids="+pageID+"&inprop=url&format=json";
        String json = getJSON(infoUrl);
        JSONObject jo = (JSONObject) new JSONParser().parse(json);
        this.pageTitle = (String)((Map)((Map)((Map)jo.get("query")).get("pages")).get(String.valueOf(pageID))).get("title");
    }
    
    
    public List<Long> getLinkPageIDs() throws UnsupportedEncodingException, IOException, ParseException{
        String linksUrl = "https://en.wikipedia.org/w/api.php?action=query&titles="+URLEncoder.encode(pageTitle, "UTF-8")+"&prop=linkshere&lhlimit=5000&lhnamespace=0&format=json";
        String json = getJSON(linksUrl);
        JSONObject jo = (JSONObject) new JSONParser().parse(json);
        JSONArray linkPages = (JSONArray)((Map)((Map)((Map)jo.get("query")).get("pages")).get(String.valueOf(pageID))).get("linkshere");
        Map page;
        List<Long> linkPageIDs = new ArrayList<>();
        for(Object obj : linkPages){
            page = (Map)obj;
            linkPageIDs.add((long)page.get("pageid"));
//            System.out.println(page.get("title"));
//            System.out.println(page.get("pageid"));
        }
        
        return linkPageIDs;
    }
    
    public List<Long> getLinkedPageIDs() throws UnsupportedEncodingException, IOException, ParseException{
        String linksUrl = "https://en.wikipedia.org/w/api.php?action=query&titles="+URLEncoder.encode(pageTitle, "UTF-8")+"&prop=links&format=json&plnamespace=0&pllimit=5000";
        String json = getJSON(linksUrl);
        JSONObject jo = (JSONObject) new JSONParser().parse(json);
        JSONArray linkedPages = (JSONArray)((Map)((Map)((Map)jo.get("query")).get("pages")).get(String.valueOf(pageID))).get("links");
        Map page;
        PageInfo pageInfo;
        List<Long> linkedPageIDs = new ArrayList<>();
        
        for(Object obj : linkedPages){
            page = (Map)obj;
            pageInfo = new PageInfo((String)page.get("title"));
            if(pageInfo.getPageID() != -1){
                linkedPageIDs.add(pageInfo.getPageID());
                System.out.println(pageInfo.getPageTitle());
                System.out.println(pageInfo.getPageID());
            } 
        }
        
        return linkedPageIDs;
    }
            
            
    public static String streamToString(InputStream inputStream) {
        String text = new Scanner(inputStream, "UTF-8").useDelimiter("\\Z").next();
        return text;
    }
    
    public static String getJSON(String urlStr) throws MalformedURLException, IOException{
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setInstanceFollowRedirects(false);
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("charset", "utf-8");
        connection.connect();
        InputStream inStream = connection.getInputStream();
        String json = streamToString(inStream);
        connection.disconnect();
        return json;
    }

    public long getPageID() {
        return pageID;
    }

    public String getPageTitle() {
        return pageTitle;
    }
    
    public static Map<Long, Double> sortByValue(Map<Long, Double> unsortMap) {

        // 1. Convert Map to List of Map
        List<Map.Entry<Long, Double>> list =
                new LinkedList<Map.Entry<Long, Double>>(unsortMap.entrySet());

        // 2. Sort list with Collections.sort(), provide a custom Comparator
        //    Try switch the o1 o2 position for a different order
        Collections.sort(list, new Comparator<Map.Entry<Long, Double>>() {
            public int compare(Map.Entry<Long, Double> o1,
                               Map.Entry<Long, Double> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        // 3. Loop the sorted list and put it into a new insertion order Map LinkedHashMap
        Map<Long, Double> sortedMap = new LinkedHashMap<Long, Double>();
        for (Map.Entry<Long, Double> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        /*
        //classic iterator example
        for (Iterator<Map.Entry<String, Integer>> it = list.iterator(); it.hasNext(); ) {
            Map.Entry<String, Integer> entry = it.next();
            sortedMap.put(entry.getKey(), entry.getValue());
        }*/


        return sortedMap;
    }
    
     public static <K, V> void printMap(Map<K, V> map) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            System.out.println("Key : " + entry.getKey()
                    + " Value : " + entry.getValue());
        }
    }
    
    public static void main(String[] args) throws IOException, UnsupportedEncodingException, ParseException, ParseException {
//        PageInfo page = new PageInfo("Ludwig van Beethoven");
//        System.out.println(page.getPageTitle());
//        page.getLinkedPageIDs();

        Map<Long, Double> unsortMap = new HashMap<Long, Double>();
        unsortMap.put(100L, 10.3);
        unsortMap.put(20L, 100.67);
        unsortMap.put(30L, 6.34);
        unsortMap.put(50L, 20.0);
        unsortMap.put(60L, 1.3);
        unsortMap.put(70L, 7.23);
        unsortMap.put(80L, 8.67);
        unsortMap.put(90L, 99.34);
        unsortMap.put(110L, 50.15);
        unsortMap.put(120L, 2.0);
        unsortMap.put(130L, 9.18);
        System.out.println("Unsort Map......");
        printMap(unsortMap);

        System.out.println("\nSorted Map......By Value");
        Map<Long, Double> sortedMap = sortByValue(unsortMap);
        printMap(sortedMap);
    }
}
