/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wikipagerank;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.crypto.SealedObject;
import org.json.simple.parser.ParseException;

/**
 *
 * @author trinhhaison
 */
public class WikiPageRank {
    
    Map<Long, Set<Long>> adjacencyMatrix;
    Map<Long, Map<Long, Double>> adjacencyMatrixTranspose;
    Map<Long, Double> pageRankVector;
    long pageNumber;
    double d;

    public WikiPageRank(String seedTitle, long pageNumber, double d) throws IOException, UnsupportedEncodingException, ParseException {
        
        this.pageNumber = pageNumber;
        this.d = d;
        
        List<Long> queue = new ArrayList<>();
        PageInfo seedPage = new PageInfo(seedTitle);
        queue.add(seedPage.getPageID());
        
        long count = 0;
        long tempID;
        PageInfo tempPage;
        List<Long> linkIDs;
        Set<Long> tempSet;
        Map<Long, Double> tempMap;
        double transitionProbability;
        
        pageRankVector = new HashMap<>();
        pageRankVector.put(seedPage.getPageID(), 0.0);
        
        adjacencyMatrix = new HashMap<>();
        adjacencyMatrix.put(seedPage.getPageID(), new HashSet<>());
        
        adjacencyMatrixTranspose = new HashMap<>();
        adjacencyMatrixTranspose.put(seedPage.getPageID(), new HashMap<>());
        
        count++;
        
        while((count < pageNumber) && (!queue.isEmpty())){
            tempID = queue.remove(0);
            tempPage = new PageInfo(tempID);
            linkIDs = tempPage.getLinkedPageIDs();
            tempSet = adjacencyMatrix.get(tempID);
            
            for(Long linkID : linkIDs){
                
                tempSet.add(linkID);
                if(!pageRankVector.containsKey(linkID)){
                    
                    pageRankVector.put(linkID, 0.0);
                    adjacencyMatrix.put(linkID, new HashSet<>());
                    tempMap = new HashMap<>();
                    tempMap.put(tempID, 0.0);
                    adjacencyMatrixTranspose.put(linkID, tempMap);
                    queue.add(linkID);
                    
                    count++;
                    System.out.println(count);
                    if(count == pageNumber) break;
                }
                else{
                    tempMap = adjacencyMatrixTranspose.get(linkID);
                    tempMap.put(tempID, 0.0);
                }
            }
        }
        
        this.pageNumber = pageRankVector.size();
        double initialPageRankValue = 1.0/this.pageNumber;
        
        for(Map.Entry<Long, Set<Long>> entry : adjacencyMatrix.entrySet()){
            
            pageRankVector.put(entry.getKey(), initialPageRankValue);
            
            tempSet = entry.getValue();
            if(tempSet.size() > 0){
                transitionProbability = 1.0/tempSet.size();
                for(long linkID : tempSet){
                    adjacencyMatrixTranspose.get(linkID).put(entry.getKey(), transitionProbability);
                }
            }
            else{
                for(Map.Entry<Long, Double> subEntry : pageRankVector.entrySet()){
                    adjacencyMatrixTranspose.get(subEntry.getKey()).put(entry.getKey(), initialPageRankValue);
                }
            }
        }
        
        ObjectFileOperations.writeObjectToFile("/home/trinhhaison/NetBeansProjects/WikiPageRank/adjacency_matrix", adjacencyMatrix);
        ObjectFileOperations.writeObjectToFile("/home/trinhhaison/NetBeansProjects/WikiPageRank/adjacency_matrix_transpose", adjacencyMatrixTranspose);
    }
    
    public void calculatePageRankValues(long iterateNumber) throws IOException{
        
        int count = 0;
        Map<Long, Double> tempPageRankVector;
        Map<Long, Double> tempVector;
        double tempPageRankValue;
        double initialPageRankValue = 1.0/pageNumber;
        while(count < iterateNumber){
            tempPageRankVector = new HashMap<>();
            for(Map.Entry<Long, Double> entry : pageRankVector.entrySet()){
                tempVector = adjacencyMatrixTranspose.get(entry.getKey());
                tempPageRankValue = 0;
                for(Map.Entry<Long, Double> subEntry : pageRankVector.entrySet()){
                    if(tempVector.containsKey(subEntry.getKey())){
                        tempPageRankValue += d * tempVector.get(subEntry.getKey()) * subEntry.getValue() + (1.0 - d) * initialPageRankValue;
                    }
                    else{
                        tempPageRankValue += (1.0 - d) * initialPageRankValue;
                    }
                }
                tempPageRankVector.put(entry.getKey(), tempPageRankValue);
            }
            pageRankVector = tempPageRankVector;
            count++;
            System.out.println("iteration: " + count);
        }
        
        pageRankVector = PageInfo.sortByValue(pageRankVector);
        ObjectFileOperations.writeObjectToFile("/home/trinhhaison/NetBeansProjects/WikiPageRank/pagerank_vector", pageRankVector);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, UnsupportedEncodingException, ParseException {
        // TODO code application logic here
//        int a = 7;
//        double b = 1.0/a;
//        System.out.println(b);
        
        WikiPageRank pageRank = new WikiPageRank("Ludwig van Beethoven", 25000L, 0.9);
        pageRank.calculatePageRankValues(100);
        Map<Long, Double> pageRankVector = pageRank.pageRankVector;
        PageInfo pageInfo;
        
        for(Map.Entry<Long, Double> entry : pageRankVector.entrySet()){
            pageInfo = new PageInfo(entry.getKey());
            System.out.println(pageInfo.getPageTitle() + "  " + entry.getValue());
        }
    }
    
}
