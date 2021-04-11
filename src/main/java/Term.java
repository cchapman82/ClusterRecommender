import java.util.ArrayList;
import java.util.Map;

/*
    Recommender System
        Hold and calculate information for terms.  It holds the term itself as well as the closest term in
        its cluster and the closest cluster to it.  It holds the silhouette value that tells how well term
        fits in cluster.  It holds the distance to the centroid of the cluster, the a(i) value for cohesion
        and the b(i) value for separation.  It has a list of the documents the term occurs in and the document
        frquency.
    Author: Christopher Chapman
    Version: 1.0-SNAPSHOT
 */
public class Term {
    // instance variables
    private String term;
    private String closestTerm;
    private int closestClusterId;
    private Double silhouette;
    private Double distance;
    private Double a;
    private Double b;
    private ArrayList<Integer> docs = new ArrayList<Integer>();
    private int docFrq;
    // getters and setters
    public String getTerm() {
        return term;
    }
    public void setTerm(String term) {
        this.term = term;
    }
    public ArrayList<Integer> getDocs() {
        return docs;
    }
    // sets the documents the term occurs in if the document is not already added
    public void setDocs(int i) {
        if (docs.contains(i))
            return;
        else {
            docs.add(i);
            setNumDocs();
        }
    }
    public Double getDistance() {
        return distance;
    }
    public void setDistance(Double d) {this.distance = d;}
    public int getDocFrq() {
        return docFrq;
    }
    public void setNumDocs() {
        this.docFrq = docs.size();
    }
    public void setA(Double a) {
        this.a = a;
    }
    public Double getA() {
        return a;
    }
    public void setB(Double b) {
        this.b = b;
    }
    public Double getB() {
        return b;
    }
    public void setSilhouette(Double silhouette) {
        this.silhouette = silhouette;
    }
    public Double getSilhouette() {
        return a;
    }
    public void setClosestTerm(String term) {
        this.closestTerm = term;
    }
    public Term getClosestTerm() {
        return ConfigManager.invertedIndex.get(closestTerm);
    }
    public void setClosestClusterId(int i) {
        this.closestClusterId = i;
    }
    // for documentation purposes
    @Override
    public String toString() {
        String s = "";
        for (int i : docs) {
            s += String.valueOf(i) + ",";
        }
        return "Term{" +
                "term='" + term + '\'' +
                ", closestTerm='" + closestTerm + '\'' +
                ", closestClusterId=" + closestClusterId +
                ", silhouette=" + silhouette +
                ", distance=" + distance +
                ", a=" + a +
                ", b=" + b +
                ", docs=" + s +
                ", docFrq=" + docFrq +
                '}' ;
    }
    // compute the cosine similarity distance from center of cluster
    public Double computeCosineToCentroid(Term t, Double[] ds) {
        Double dotp = 0.0;
        Double xs = 0.0;
        Double ys = 0.0;
        int i = 0;
        for (Map.Entry entry : ConfigManager.docs.entrySet()) {
            Document d = (Document) entry.getValue();
            if (t.getDocs().contains(d.getDocId())) {
                dotp += d.getNormWeights().get(t.getTerm()) * ds[i];
                xs += Math.pow(d.getNormWeights().get(t.getTerm()), 2);
                ys += Math.pow(ds[i], 2);
            } else {
                dotp += 0 * ds[i];
                xs += Math.pow(0, 2);
                ys += Math.pow(ds[i], 2);
            }
            i++;
        }
        Double denominator = Math.sqrt(xs * ys);
        if ( denominator == 0) {
            distance = 0.0;
        } else {
            distance = 1 - (dotp / denominator);
        }
        return distance;
    }
    // compute cosine similarity distance between a term and another term in cluster
    public Double computeCosine(Term t, Term tt) {
        Double dotp = 0.0;
        Double xs = 0.0;
        Double ys = 0.0;
        int i = 0;
        for (Map.Entry entry : ConfigManager.docs.entrySet()) {
            Document d = (Document) entry.getValue();
            if (t.getDocs().contains(d.getDocId())) {
                //  if both terms have weights in the same document
                if (tt.getDocs().contains(d.getDocId())) {
                    dotp += d.getNormWeights().get(t.getTerm()) * d.getNormWeights().get(tt.getTerm());
                    xs += Math.pow(d.getNormWeights().get(t.getTerm()), 2);
                    ys += Math.pow(d.getNormWeights().get(tt.getTerm()), 2);
                //  if the first term has weight in document, but second does not
                } else {
                    dotp += d.getNormWeights().get(t.getTerm()) * 0;
                    xs += Math.pow(d.getNormWeights().get(t.getTerm()), 2);
                    ys += Math.pow(0, 2);
                }

            } else {
                //  if first term does not have weight in document, but second does
                if (tt.getDocs().contains(d.getDocId())) {
                    dotp += 0 * d.getNormWeights().get(tt.getTerm());
                    xs += Math.pow(0, 2);
                    ys += Math.pow(d.getNormWeights().get(tt.getTerm()), 2);
                //  if neither term has weights in document
                } else {
                    dotp += 0 * 0;
                    xs += Math.pow(0, 2);
                    ys += Math.pow(0, 2);
                }
            }
            i++;
        }
        Double denominator = Math.sqrt(xs * ys);
        Double dist = 0.0;
        if ( denominator == 0) {
            dist = 0.0;
        } else {
            dist = 1 - (dotp / denominator);
        }
        return dist;
    }
}
