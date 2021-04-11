import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

/*
    Recommender System
        User profile to hold the terms the user is interested in the cluster, the documents that the user
        decided was useful as well as infromation for login.
    Author: Christopher Chapman
    Version: 1.0-SNAPSHOT
 */

public class UserProfile {
    //  instance variables
    private ArrayList<Term> interested = new ArrayList<Term>();
    private ArrayList<Integer> rDocs = new ArrayList<Integer>();
    private String name;
    private String password;
    private Scanner scanner = new Scanner(System.in);
    // constructors
    public UserProfile(String name, String password) {
        this.name = name; this.password = password;
    }
    public UserProfile() {}
    //  getters and setters
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    //  add subject the user is interested if not already present
    public void setInterested(Term t) {
        if (!interested.contains(t)) {
            interested.add(t);
        }
    }
    public ArrayList<Term> getInterested() {
        return interested;
    }
    //  get other documents associated with a particular term
    public ArrayList<Integer> getDocs() {
        ArrayList<Integer> docs = new ArrayList<Integer>();
        for(Term t : interested) {
            ArrayList<Integer> nDocs = t.getDocs();
            for (Integer i : nDocs) {
                docs.add(i);
            }
        }
        return docs;
    }
    //  show terms associated with the terms the user is interested in based on smallest distance in same cluster
    //  and give the opportunity for the user to add to their profile
    public void addSimilarTerms() {
        ArrayList<Term> terms = new ArrayList<Term>();
        for (Term t : interested) {
            terms.add(t.getClosestTerm());
        }
        System.out.println("These are terms that are related to terms in your profile");
        for (Term t : terms) {
            System.out.println(t.getTerm());
        }
        System.out.println("Enter terms you would like to add to profile");
        ArrayList<String> ss = new ArrayList<String>();
        String in;
        while(!(in = scanner.nextLine()).isEmpty()) {
            String s = in.toUpperCase();
            ss.add(s);
        }
        for (String s : ss) {
            Term t = ConfigManager.invertedIndex.get(s);
            setInterested(t);
        }
    }
    //  add documents to rated list that the user deemed helpful
    public void addRatedDocs(int i) {
        if (rDocs.contains(i)) {
            System.out.println("Document is already in rated documents.");
            return;
        } else {
            rDocs.add(i);
            System.out.println("Document added to rated documents.");
        }
    }
    public ArrayList<Integer> getRatedDocs() {
        return rDocs;
    }
    //  get documents that are similar to the documents user has deemed helpful based on
    //  cosine similarity to chosen document
    public ArrayList<Integer> getRatedSimilar(int i) {
        ArrayList<Integer> toSend = new ArrayList<Integer>();
        TreeMap<Double, Integer> simDocs = new TreeMap<Double, Integer>();
        Double[] doc = ConfigManager.docTermMatrix.get(i);
        Double csine = 0.0;
        for (Map.Entry entry : ConfigManager.docTermMatrix.entrySet()) {
            int j = (Integer) entry.getKey();
            if (i == j) {
                continue;
            } else {
                csine = ConfigManager.computeCosine(doc, (Double[]) entry.getValue());
            }
            simDocs.putIfAbsent(csine, (Integer) entry.getKey());
        }
        int k = 0;
        for (Map.Entry entry : simDocs.entrySet()) {
            if (k < 5) {
                toSend.add((Integer) entry.getValue());
            }
            k++;
        }
        return toSend;
    }
    //  allow a user to read the document presented and allow them to rate it
    public void readRateDoc() {
        System.out.println("Would you like to view the Documents?\n enter [1] for yes or [0] for no.");
        int num = scanner.nextInt();
        if (num == 1) {
            System.out.println("Which document?");
            num = scanner.nextInt();
            int doc = num;
            System.out.println(ConfigManager.docs.get(num));
            System.out.println("Was the Document useful?\n enter [1] for yes or [0] for no");
            num = scanner.nextInt();
            if (num == 1 ) {
                addRatedDocs(doc);
            } else {
                System.out.println("OK");
            }
        } else {
            System.out.println("OK");
        }
    }
    // Documentation purposes
    @Override
    public String toString() {
        String s = "";
        for(Term t : interested) {
            s += t.getTerm() + ".";
        }
        String ss = "";
        for (Integer i : rDocs) {
            ss += String.valueOf(i) + ".";
        }
        return "UserProfile{" +
                "name='" + name + '\'' +
                ", interested=" + s + '\'' +
                ", ratedDocs=" + ss + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
