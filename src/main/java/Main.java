import opennlp.tools.stemmer.PorterStemmer;
import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.tokenize.Tokenizer;

import java.io.*;
import java.util.*;

/*
    Recommender System
        Present system and provide interface for user.  If silhouette values are not calculated, the system goes
        through to start everything, otherwise, reads documents and previously computed clusters.
    Author: Christopher Chapman
    Version: 1.0-SNAPSHOT
 */
public class Main {
    // instance variables
    public static ConfigManager mngr = new ConfigManager();
    public static Logger logger = new Logger();

    public static void main(String[] args) {
        UserProfile user = new UserProfile();
        File file = new File("silhouette.txt");
        Boolean exists = file.exists();
        Term t = new Term();
        String name = "";
        if (exists) {
            start();
            Scanner scanner = new Scanner(System.in);
            System.out.println("Welcome to Christopher Chapman's Recommender System.");
            System.out.println("Please enter user name.");
            String in = scanner.nextLine();
            name = in.toLowerCase();
            if (mngr.getUsers().containsKey(name)) {
                    System.out.println("Please enter password.");
                    in = scanner.nextLine();
                    if (mngr.getUsers().containsKey(name)) {
                        user = mngr.getUsers().get(name);
                    }
                    String password = user.getPassword();
                    if(password.equals(in)) {
                        System.out.println("Welcome back");
                    } else {
                        System.out.println("password incorrect");
                    }
                } else {
                    System.out.println("Please set up profile " + in);
                    mngr.setUpUser(in);
                }
            user = mngr.getUsers().get(name);

            int num = 0;
            while (true) {
                System.out.println("What else would you like to do ?");
                System.out.println("[1] to add simialr terms\n" +
                        "[2] to get docs for subjects in profile\n" +
                        "[3] to get documents related to rated documents\n[0] to end");
                num = scanner.nextInt();
                switch(num) {
                    case 1 :
                        user.addSimilarTerms();
                        continue;
                    case 2 :
                        ArrayList<Term> terms = user.getInterested();
                        ArrayList<Integer> docs = user.getDocs();
                        for (Term tt : terms) {
                            System.out.println("The documents associated with " + tt.getTerm() + " are :");
                            for (Integer i : docs) {
                                if (tt.getDocs().contains(i)) {
                                    System.out.println("DocumentID : " + i);
                                }
                            }
                        }
                        user.readRateDoc();
                        continue;
                    case 3 :
                        HashMap<Integer, ArrayList<Integer>> simDoc = new HashMap<Integer, ArrayList<Integer>>();
                        for (Integer i : user.getRatedDocs()) {
                            ArrayList<Integer> simDocs = user.getRatedSimilar(i);
                            simDoc.put(i, simDocs);
                        }
                        for (Map.Entry<Integer, ArrayList<Integer>> entry : simDoc.entrySet()) {
                            System.out.println("The similar documents to document " + entry.getKey() + " are:");
                            ArrayList<Integer> nums = entry.getValue();
                            for (Integer i : nums) {
                                System.out.println(i);
                            }
                        }
                        user.readRateDoc();
                        continue;
                    case 0 :
                        logger.writeUsers();
                        System.out.println("Thank you goodbye!!");
                        System.exit(0);
                }
            }

        } else{
            mngr.getStpWds();
            mngr.getDocuments();
            mngr.calculateWeights();
            mngr.assignment();
            mngr.reallocate();
            mngr.silhouette();
            logger.writeClusters();
            logger.writeTerms();
            logger.writeSihouette();
            logger.writeDocs();
        }
    }
    // read in data to not have to compute again
    public static void start(){
        mngr.getStpWds();
        mngr.getDocuments();
        mngr.calculateWeights();
        mngr.getInvertedIndex();
        mngr.getClusters();
        mngr.getCategories();
        mngr.getUserProfiles();
        mngr.makeDocTermMatrix();

    }
}