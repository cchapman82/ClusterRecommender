import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/*
    Recommender System
        Document information including id number, the document text, terms and their frequencies in document,
        the tfxidf weight associated with terms and the normalized term weights.
    Author: Christopher Chapman
    Version: 1.0-SNAPSHOT
 */
public class Document {
    // instance variables
    private int docId;
    private String text;
    // raw term frequencies
    private HashMap<String, Integer> termFrq = new HashMap<String, Integer>();
    //  tfxidf weights
    private HashMap<String, Double> weights = new HashMap<String, Double>();
    //  normalized weights
    private HashMap<String, Double> normWeights = new HashMap<String, Double>();
    Double normTfidf;
    private String date;
    private int page;

    // getters and setters
    public int getDocId() {
        return docId;
    }
    public void setDocId(int docId) {
        this.docId = docId;
    }
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }
    public HashMap getTermFrq() {
        return termFrq;
    }
    public void setTermFrq(HashMap termFrq) {
        this.termFrq = termFrq;
    }
    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }
    public int getPage() {
        return page;
    }
    public void setPage(int page) {
        this.page = page;
    }
    public HashMap<String, Double> getNormWeights() {
        return normWeights;
    }
    // calculate tfxidf weights
    public void setWeights() {
        // set N
        int totDocs = ConfigManager.docs.size();
        normTfidf = 0.0;
        for (Map.Entry entry : termFrq.entrySet()) {
                String term = (String) entry.getKey();
                // set n for entry term
                int frq = (Integer) entry.getValue();
                if (ConfigManager.invertedIndex.containsKey(term)) {
                    int numDocs = ConfigManager.invertedIndex.get(term).getDocFrq();
                    // calucate the inverse document frequency
                    Double inverse = Math.log(Double.valueOf(totDocs/numDocs))/Math.log(2);
                    // set tfxidf
                    Double tfidf = frq * inverse;
                    // for every term make denominator for normalizing weights
                    normTfidf += (Math.pow(frq, 2) * Math.pow(inverse, 2));
                    weights.put(term, tfidf);
                }
        }
    }
    // claculate normalized tfxidf weights
    public void normalizeWeights() {
        for (Map.Entry entry : weights.entrySet()) {
            String term = (String) entry.getKey();
            if (weights.containsKey(term)) {
                Double value = (Double) entry.getValue();
                Double normValue = value/Math.sqrt(normTfidf);
                normWeights.put(term, normValue);
            }
        }
    }
    //  Documentation purposes
    public String toString() {
        String s = "";
        String ss = "";
        String sss= "";
        for (Map.Entry entry : termFrq.entrySet()) {
            s += entry.toString()  + ",";
        }
        for (Map.Entry entry : weights.entrySet()) {
            ss += entry.toString()  + ",";
        }
        for (Map.Entry entry : normWeights.entrySet()) {
            sss += entry.toString()  + ",";
        }
        return "Document{" +
                "docId='" + docId + '\'' +
                "date='" + date + '\'' +
                "page='" + page + '\'' +
                ", text='" + text + '\'' +
                ", termFrq'" + s + '\'' +
                ", weights'" + ss + '\'' +
                ", normWeights'" + sss + '\'' +
                ", normTfidf='" + normTfidf + '\'' +
                '}';
    }
    
}
