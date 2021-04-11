import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
/*
    Recommender System
        Right information to a file to not have to go through and calculate all over again
    Author: Christopher Chapman
    Version: 1.0-SNAPSHOT
 */
public class Logger {

    public void writeClusters() {
        // write clusters to txt file
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter("clusters.txt"));
            for(Map.Entry<Integer, Cluster> entry : ConfigManager.clusters.entrySet()) {
                ArrayList<Term> terms = entry.getValue().getTerms();
                bw.write("Cluster " + entry.getKey() + "\n");
                bw.flush();
                for (Term t : terms) {
                    bw.write(t.getTerm() + "\n");
                    bw.flush();
                }
                bw.write("\n");
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //  write terms to file
    public void writeTerms() {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter("terms.txt"));
            for(Map.Entry<String, Term> entry : ConfigManager.invertedIndex.entrySet()) {
                Term term = entry.getValue();
                bw.write( term.toString()+ "\n");
                bw.flush();
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //  write documents and associated data to file
    public static void writeDocs() {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter("docs.txt"));
            for (Map.Entry entry : ConfigManager.docs.entrySet()) {
                Document d = (Document) entry.getValue();
                bw.write(d.toString() + "\n");
                bw.flush();
            }
            bw.close();
        } catch(
                IOException e) {
            e.printStackTrace();
        }
    }
    // wtite silhouette values for all terms in cluster
    public static void writeSihouette() {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter("silhouette.txt"));
            for (Map.Entry entry : ConfigManager.clusters.entrySet()) {
                Cluster c = (Cluster) entry.getValue();
                bw.write("Cluster " + c.getClusterId() + "\n");
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
    // write existing profiles to disk to read on start up
    public static void writeUsers() {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter("users.txt"));
            for (Map.Entry entry : ConfigManager.users.entrySet()) {
                bw.write(entry.getValue().toString() + "\n");
            }
            bw.close();
        } catch (IOException e) {
                e.printStackTrace();
            }
    }
}
