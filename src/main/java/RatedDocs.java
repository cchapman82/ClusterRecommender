/*
    Recommender System

    Author: Christopher Chapman
    Version: 1.0-SNAPSHOT
 */

import java.util.ArrayList;

public class RatedDocs {
    private ArrayList<Integer> relList = new ArrayList<Integer>();
    private ArrayList<Integer> orderedList = new ArrayList();
    private Double threshold = 0.3;

    public ArrayList<Integer> getRelList() {
        return relList;
    }

    public void setRelList(ArrayList<Integer> relList) {
        this.relList = relList;
    }

    public ArrayList getOrderedList() {
        return orderedList;
    }

    public void setOrderedList(ArrayList orderedList) {
        this.orderedList = orderedList;
    }

    public Double getThreshold() {
        return threshold;
    }

    public void setThreshold(Double threshold) {
        this.threshold = threshold;
    }

    public void addDoc(int docId) {
        if (relList.contains(docId)) {
            //System.out.println("already added!");
            return;
        } else {
            relList.add(docId);
           //System.out.println("Relevant doc : " + docId + " added to relevant docs.");
        }
    }

   /* public void addDocOrdered() {
        Document d = Main.docs.get(docId);
        String[] terms = new String[q.getTermFrq().size()];
        Object[] objects = q.getTermFrq().keySet().toArray();
        for (int i = 0; i < terms.length; i++) {
            terms[i] = (String) objects[i];
        }
        HashMap<String, Double> termWeights = d.getNormWeights();
        Collection<String> c = termWeights.keySet();
        for (String s : terms) {
            if (c.contains(s)) {
                if (termWeights.get(s) >= threshold)
                    if (!orderedList.contains(docId))
                        orderedList.add(docId);
                    else
                        continue;
            }
        }
        for (Integer i : orderedList)
            System.out.println(i);
    }*/
}
