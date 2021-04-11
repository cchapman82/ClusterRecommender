import opennlp.tools.stemmer.PorterStemmer;
import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.tokenize.Tokenizer;

import java.io.*;
import java.util.*;
/*
    Recommender System
        Configure and hold all the static variables and uses of those variables.
    Author: Christopher Chapman
    Version: 1.0-SNAPSHOT
 */
public class ConfigManager {

    // List of stop words from Time.stp
    public static ArrayList<String> stpWds = new ArrayList<String>();
    // HashMaps to hold Documents from Time
    public static HashMap<Integer, Document> docs = new HashMap<Integer, Document>();
    // Sorted Inverted Index
    public static TreeMap<String, Term> invertedIndex = new TreeMap<String, Term>();
    // clusters holding separated terms
    public static HashMap<Integer, Cluster> clusters = new HashMap<Integer, Cluster>();
    //  hold old clusters to see if they changed
    public static HashMap<Integer, ArrayList<Term>> oldClusters = new HashMap<Integer, ArrayList<Term>>();
    public static Cluster cluster = new Cluster();
    //  hold subjects with the highest silhouette value available to the user to choose fromm
    public static ArrayList<Term> available = new ArrayList<Term>();
    //  users allowed to access the system
    public static HashMap<String, UserProfile> users = new HashMap<String, UserProfile>();
    //  document term matrix for computing similarity values for rated documents
    public static HashMap<Integer, Double[]> docTermMatrix= new HashMap<Integer, Double[]>();
    private Scanner scanner = new Scanner(System.in);
    // getters and setters
    public void getStpWds() {
        File file;
        BufferedReader br;
        try {
            String in;
            String text = "";
            // read in stop words
            file = new File("TIME.STP");
            br = new BufferedReader(new FileReader(file));
            while ((in = br.readLine()) != null) {
                if (in.length() == 0)
                    continue;
                stpWds.add(in);
            }
        }  catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // read in documents from text file
    public void getDocuments() {
        File file;
        BufferedReader br;
        int flag = 0;
        Document doc = new Document();
        String in;
        String text ="";
        // read in documents
        try {
        file = new File("TIME.ALL");
        br = new BufferedReader(new FileReader(file));
        while ((in = br.readLine()) != null) {
            // beginning of new document or end of file
            if (in.contains("*TEXT") || in.contains("*STOP")) {
                // if hashMap has already been added to
                if (flag == 1) {
                    doc.setText(text);
                    docs.put(doc.getDocId(), doc);
                    // sets the documents term frequencies returned by the token/stem method
                    doc.setTermFrq(processPorterTokenizerDoc(text, doc.getDocId()));
                    // if end of document, break loop and set document text
                    if (in.contains("*STOP"))
                        break;
                    doc = new Document();
                    text = "";
                }
                // set document instance variables, for id, date and page
                String[] docInfo = in.split(" ");
                doc.setDocId(Integer.parseInt(docInfo[1]));
                doc.setDate(docInfo[2]);
                doc.setPage(Integer.parseInt(docInfo[4]));
            } else {
                // add line to document text
                text += in + " ";
                flag = 1;
            }
            }
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // data processing methods

    // remove stop words
    public static String[] processStpWds(String[] ss) {
        ArrayList<String> sss = new ArrayList<String>();
        // if words are stopwords do not add them to array list
        for (String s : ss) {
            if (stpWds.contains(s)) {
                continue;
            } else if (s.length() == 0) {
                continue;
            } else if (s == " ") {
                continue;
            } else {
                sss.add(s);
            }
        }
        ss = new String[sss.size()];
        for (int i = 0; i < ss.length; i++) {
            ss[i] = sss.get(i);
        }
        return ss;
    }
    //  tokenize and stem terms in documents
    public static HashMap processPorterTokenizerDoc(String s, int d) {
        HashMap<String, Integer> termFrqs = new HashMap<String, Integer>();
        Stemmer stemmer = new PorterStemmer();
        Tokenizer tokenizer = SimpleTokenizer.INSTANCE;
        // remove punctuation
        s = s.replaceAll("\\-", "");
        s = s.replaceAll("\\p{Punct}", " ");
        // tokenize document text
        String[] tokens = tokenizer.tokenize(s);
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            // stem document text
            stemmer.stem(token);
            tokens[i] = stemmer.toString();
        }
        // remove stop words
        tokens = processStpWds(tokens);
        // update terms and frequencies
        for (int i = 0; i < tokens.length; i++) {
            if (termFrqs.containsKey(tokens[i])) {
                int j = termFrqs.get(tokens[i])+1;
                termFrqs.replace(tokens[i], j);
            } else {
                termFrqs.putIfAbsent(tokens[i], 1);
            }
            // if query do not add to inverted index
            // add tokens to inverted index if document
            if (invertedIndex.containsKey(tokens[i])) {
                invertedIndex.get(tokens[i]).setDocs(d);
            } else {
                // do not add numbers
                try {
                    Integer.parseInt(tokens[i]);
                    continue;
                } catch (NumberFormatException e) {
                }
                Term t = new Term();
                t.setTerm(tokens[i]);
                t.setDocs(d);
                invertedIndex.put(tokens[i], t);
            }
        }
        return termFrqs;
    }
    //  asign terms to new clusters
    public void reallocate(){
        while (oldClusters.get(1).size() != clusters.get(1).getTerms().size()) {
            cluster.reallocateClusters();
        }
    }
    // assign random terms to cluster
    public void assignment() {
        cluster.randomAssignment();
    }
    //  get silhouette values calculated
    public void silhouette() {
        for (Map.Entry entry : clusters.entrySet()) {
            Cluster c = (Cluster) entry.getValue();
            c.calculateSilhouette();
        }
    }
    //  calculate weights
    public void calculateWeights() {
        // compute tfxidf weights for all documents.
        for (Map.Entry entry : ConfigManager.docs.entrySet()) {
            int id = (Integer) entry.getKey();
            Document doc = (Document) entry.getValue();
            doc.setWeights();
            doc.normalizeWeights();
        }
    }
    // read inverted index from text file and get previously calculated values
    public void getInvertedIndex() {
        File file = new File("terms.txt");
        String in = "";
        Term t = new Term();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            while((in = br.readLine()) != null) {
                String[] term = in.split(" ");
                String[] name = term[0].split("=");
                String n = name[1].replaceAll("\\p{Punct}", "");
                t.setTerm(n);
                name = term[1].split("=");
                t.setClosestTerm(name[1].replaceAll("\\p{Punct}", ""));
                name = term[2].split("=");
                t.setClosestClusterId(Integer.parseInt(name[1].replaceAll("\\,", "")));
                name = term[3].split("=");
                t.setSilhouette(Double.parseDouble(name[1].replaceAll("\\,", "")));
                name = term[4].split("=");
                t.setDistance(Double.parseDouble(name[1].replaceAll("\\,", "")));
                name = term[5].split("=");
                t.setA(Double.parseDouble(name[1].replaceAll("\\,", "")));
                name = term[6].split("=");
                t.setB(Double.parseDouble(name[1].replaceAll("\\,", "")));
                name = term[7].split("=");
                String[] docs = name[1].split(",");
                for (int i = 0; i < docs.length; i++) {
                    t.setDocs(Integer.parseInt(docs[i]));
                }
                invertedIndex.replace(t.getTerm(), t);
                t = new Term();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }

    }
    //  read previously determined clusters
    public void getClusters() {
        File file = new File("clusters.txt");
        String in = "";
        Cluster c = new Cluster();
        ArrayList<String> terms = new ArrayList<String>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            while ((in = br.readLine()) != null) {
                if ((in.contains("Cluster") || in.contains("Stop")) || in.equals("")) {
                    if (in.equals("") || in.equals("Stop")) {
                        for (String s : terms) {
                            c.setTerms(invertedIndex.get(s));
                        }
                        clusters.put(c.getClusterId(), c);
                        c = new Cluster();
                        terms = new ArrayList<>();
                    } else {
                        String[] name = in.split(" ");
                        c.setClusterId(Integer.parseInt(name[1]));
                    }

                } else {
                    terms.add(in);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //  read in previously created user profiles
    public void getUserProfiles() {
        File file = new File("users.txt");
        String in;
        UserProfile user;
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            while((in = br.readLine()) != null) {
                ArrayList<Term> terms = new ArrayList<Term>();
                ArrayList<Integer> nums = new ArrayList<Integer>();
                String name = "";
                String password = "";
                String[] ss = in.split(" ");
                String[] sss = ss[0].split("=");
                name = sss[1].replaceAll("\\p{Punct}", "");
                sss = ss[1].split("=");
                String[] ssss = sss[1].split("\\.");
                String n = "";
                if (ssss.length == 0) {
                    n = sss[1].replaceAll("\\p{Punct}", "");
                    terms.add(ConfigManager.invertedIndex.get(n));
                } else {
                    for (String s : ssss) {
                        s = s.replaceAll("\\p{Punct}", "");
                        if (!s.equals("")) {
                            terms.add(ConfigManager.invertedIndex.get(s));
                        }
                    }
                }
                sss = ss[2].split("=");
                ssss= sss[1].split("\\.");
                String m = "";
                if (ssss.length == 0) {
                    m = sss[1].replaceAll("\\p{Punct}", "");
                    if (!m.equals("")) {
                        nums.add(Integer.valueOf(m));
                    }
                } else {
                    for (String s : ssss) {
                        s = s.replaceAll("\\p{Punct}", "");
                        if (!s.equals("")) {
                            nums.add(Integer.valueOf(s));
                        }
                    }
                }
                sss = ss[3].split("=");
                password = sss[1].replaceAll("\\p{Punct}", "");
                user = new UserProfile(name, password);
                for (Term t : terms) {
                    user.setInterested(t);
                }
                for (Integer z : nums) {
                    user.addRatedDocs(z);
                }
                ConfigManager.users.put(user.getName(), user);
            }
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //  get term in cluster with the smallest silhouette value
    public void getCategories() {
        Term t = new Term();
        for(Map.Entry entry : ConfigManager.clusters.entrySet()) {
            Double maxSilhouette = -100.0;
            Cluster c = (Cluster) entry.getValue();
            if (c.getTerms().size() > 0) {
                for (Term term : c.getTerms()) {
                    if (term.getSilhouette() > maxSilhouette) {
                        t = term;
                        maxSilhouette = t.getDistance();
                    }
                }
            }
            if (t.getTerm() != null) {
                available.add(t);
                t = new Term();
            }
        }
    }
    // add User to users if not already added
    public void addUser(String name, String password) {
        UserProfile user = new UserProfile(name, password);
        String s = user.getName();
        if (users.containsKey(s)) {
            System.out.println("You already have a profile.");
        } else {
            users.put(s, user);
        }
    }
    //  get user info
    public HashMap<String, UserProfile> getUsers() {
        return users;
    }
    //  initialize user's profile
    public void setUpUser(String name) {
        System.out.println(name + " Please enter a password.");
        String in = scanner.nextLine();
        ArrayList<String> terms = new ArrayList<String>();
        addUser(name.toLowerCase(), in);
        System.out.println("Please enter what topics you are interested from the list below\n one at a time and press enter when finished");
        for (Term tm : available) {
            System.out.println(tm.getTerm());
        }
        while(!(in = scanner.nextLine()).isEmpty()) {
            String s = in.toUpperCase();
            terms.add(s);
        }
        UserProfile up = users.get(name.toLowerCase());
        for (String s : terms) {
            Term t = invertedIndex.get(s);
            up.setInterested(t);
        }
    }
    //  create document by term matrix to find similar documents to documents user deemed useful
    public void makeDocTermMatrix() {
        for (Map.Entry entry : docs.entrySet()) {
            Document d = (Document) entry.getValue();
            Double[] terms = new Double[invertedIndex.size()];
            int i = 0;
            for (Map.Entry mt : invertedIndex.entrySet()) {
                Term t = (Term) mt.getValue();
                if (t.getDocs().contains(d.getDocId())) {
                    terms[i] = d.getNormWeights().get(t.getTerm());
                } else {
                    terms[i] = 0.0;
                }
                i++;
            }
            docTermMatrix.put(d.getDocId(), terms);
        }
    }
    //  Compute cosine for useful documents
    public static Double computeCosine(Double[] xs, Double[] ys) {
        Double d = 0.0;
        Double dotP = 0.0;
        Double xsP = 0.0;
        Double ysP = 0.0;
        for (int i = 0; i < xs.length; i++) {
            dotP += xs[i] * ys[i];
            xsP += Math.pow(xs[i], 2);
            ysP += Math.pow(ys[i], 2);
        }
        Double xSP = Math.sqrt(xsP);
        Double ySP = Math.sqrt(ysP);
        d = dotP/(xSP * ySP);
        return d;
    }
}
