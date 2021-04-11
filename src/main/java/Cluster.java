/*
    Recommender System
        Clustered Terms from document corpus to get the recommomendations for
        the user to set up initial user profile as well as calculate the cohesion and separation
        values for silhouette and the silhouette values.
    Author: Christopher Chapman
    Version: 1.0-SNAPSHOT
 */

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class Cluster {
    private int clusterId;
    private ArrayList<Term> terms = new ArrayList<Term>();
    private static Double[] centroids = new Double[ConfigManager.docs.size()];

    // getters and setters
    public int getClusterId() {
        return this.clusterId;
    }

    public void setClusterId(int clusterId) {
        this.clusterId = clusterId;
    }

    public ArrayList<Term> getTerms() {
        return this.terms;
    }

    public void setTerms(Term t) {
        this.terms.add(t);
    }

    public void clearTerms() {
        terms = new ArrayList<Term>();
    }

    public Double[] getCentroids() {
        return centroids;
    }


    // create centroids for cluster.
    public void createCentroids() {
        // divide by number of terms in cluster
        Double denominator = new Double(getTerms().size());
        Double numerator = 0.0;
        int i = 0;
        // for every document in corpus, add normalized weights to numerator
        for (Map.Entry entry : ConfigManager.docs.entrySet()) {
            int docId = (Integer) entry.getKey();
            for (Term t : terms) {
                // if document contains term, add weight
                if (t.getDocs().contains(docId)) {
                    numerator += ConfigManager.docs.get(docId).getNormWeights().get(t.getTerm());
                } else {
                    numerator += 0.0;
                }
            }
            centroids[i] = numerator / denominator;
            i++;
        }
    }

    // calculate the cohesion value for term in current cluster
    public void calculateA(Term t) {
        Double d = 0.0;
        Double minDist = 100.0;
        int i = 0;
        // for every term get cosine similarity distance
        for (Term tt : terms) {
            // do not compute for same term
            if (tt.equals(t)) {
                continue;
            } else {
                Double dt = t.computeCosine(t, tt);
                d += dt;
                //  assign closest term for recomendations
                if (dt < minDist) {
                    minDist = dt;
                    t.setClosestTerm(tt.getTerm());
                }
                i++;
            }
        }
        t.setA(d / i);
    }

    // calculate the separation value for silhouette
    public void calculateB(Term t) {
        Double minDist = 100.0;
        int j = 0;
        Double d = 0.0;
        // for every cluster calculate the minimum mean distance from term to cluster
        for (Map.Entry entry : ConfigManager.clusters.entrySet()) {
            Cluster c = (Cluster) entry.getValue();
            // do not calculate for the cluster containing term
            if (c.getClusterId() == clusterId) {
                continue;
                // do not calculate if cluster is empty
            } else if (c.getTerms().size() == 0) {
                continue;
            } else {
                // compute cosine similarity distance from term to every term in cluster
                for (Term tt : c.getTerms()) {
                    d += t.computeCosine(t, tt);
                }
            }
            //  get mean distance
            d = d / c.getTerms().size();
            // assign lowest distance and cluster to term
            if (d < minDist) {
                minDist = d;
                j = c.clusterId;
            }
        }
        t.setClosestClusterId(j);
        t.setB(minDist);
    }
    //  calculate silhouette coefficient to test cohesion and separation of clusters
    public void calculateSilhouette() {
        // for each term in cluster, calculate the silhouette
        for (Term term : terms) {
            calculateA(term);
            calculateB(term);
            Double a = term.getA();
            Double b = term.getB();
            Double denominator = 0.0;
            if (a > b) {
                denominator = a;
            } else {
                denominator = b;
            }
            term.setSilhouette((b - a) / denominator);
        }
    }
    // write silhouette values to txt file
    public static void writeSilhouette() {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter("silhouette.txt"));
            for (Map.Entry entry : ConfigManager.clusters.entrySet()) {
                Cluster c = (Cluster) entry.getValue();
                bw.write("Cluster " + c.clusterId + "\n");
                for (Term term : c.getTerms()) {
                    bw.flush();
                    bw.write("Silhouette for " + term.getTerm() + " = " + term.getSilhouette() + "\n");
                    bw.flush();
                }
            }
            bw.close();
        } catch(
        IOException e) {
            e.printStackTrace();
        }
    }
    // assign terms randomly to clusters prior to clustering
    public void randomAssignment() {
        // make clusters
        for (int i = 0; i < 11; i++) {
            Cluster c = new Cluster();
            c.setClusterId(i+1);
            ConfigManager.clusters.put(c.getClusterId(), c);
            ConfigManager.oldClusters.put(c.getClusterId(), new ArrayList<Term>());
        }
        // arbitrarily assign terms to clusters
        int i = 0;
        Collection<Map.Entry<String, Term>> terms = ConfigManager.invertedIndex.entrySet();
        Iterator<Map.Entry<String, Term>> iterator = terms.iterator();
        while (iterator.hasNext()) {
            for (int j = 0; j < ConfigManager.clusters.size(); j++, i++) {
                if (i == ConfigManager.invertedIndex.size())
                    break;
                ConfigManager.clusters.get(j+1).setTerms(ConfigManager.invertedIndex.get(iterator.next().getKey()));
            }
        }
        createCentroids();
    }
    // reallocate clusters until they converge
    public void reallocateClusters() {
        for (Map.Entry<Integer, Cluster> entry : ConfigManager.clusters.entrySet()) {
            Cluster c = entry.getValue();
            int id = entry.getKey();
            ConfigManager.oldClusters.put(id, c.getTerms());
            c.clearTerms();
        }
        Collection<Map.Entry<String, Term>> terms = ConfigManager.invertedIndex.entrySet();
        Iterator<Map.Entry<String, Term>> iterator = terms.iterator();
        int i = 0;
        while(iterator.hasNext()) {
            Term t = iterator.next().getValue();
            int nearest = 1;
            Double closest = 0.0;
            // compute cosine similarity distance to centroid
            for(Map.Entry entry : ConfigManager.clusters.entrySet()) {
                Double cosine = t.computeCosineToCentroid(t, ((Cluster) entry.getValue()).getCentroids());
                if (cosine >= closest)   {
                    closest = cosine;
                    nearest = ((Cluster) entry.getValue()).getClusterId();
                }
            }
            ConfigManager.clusters.get(nearest).setTerms(t);
            i++;
        }
        // recalculate centroids
        createCentroids();
    }
}
