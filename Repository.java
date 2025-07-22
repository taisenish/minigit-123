//Taise Nish
//05/01/2024
//CSE 123 B 
//TA: Evan Wu
//P1: Repository
//Implements a simplified of a version control system similar to Git, using linked-lists.
//Collaboarated with Andy Prempeh

import java.text.SimpleDateFormat;
import java.util.*;


public class Repository {
    
    private Commit head;
    private int size;
    private String name;

    //Constructor which creates a new, empty repository with the specified name.
    //Throws an illegal argument exception if the name is null or empty. Parameter is a string 
    //called name
    public Repository(String name){
        if(name == null || name.isEmpty()){
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        this.name = name;
    }

    //return the ID of the current head of this repository.If the head is null,
    // return null. Returns type is string since the ID is a string .
    public String getRepoHead(){
        if(head == null){
            return null;
        }
        return head.id;
    }

    //Returns the number of commits in the repository. Return type is an integer
    public int getRepoSize() {
        return size;
    }

    //Returns a string representation of this repository. If there are no commits in this 
    //repository, a message containing ' no commits' is returned. Return type is 
    //toString.
    public String toString() {
        if (head == null) {
            return name + " - No commits";
        }

        String result = name + " - Current head: " + head.toString();
        return result;
    }

    //Returns true if the commit with ID targetId is in 
    //the repository, false if not.Return type is a boolean value. Parameter
    //is a string which represents the target ID.
    public boolean contains(String targetId) {
        Commit current = head;
        while (current != null) {
            if (current.id.equals(targetId)) {
                return true;
            }
            current = current.past;
        }
        return false;
    }

    //Return a string consisting of the String representations of the most recent 
    //n commits in this repository.If there are no commits in this repository,an empty 
    //string is returned.If n is non-positive,an IllegalArgumentException is thrown.
    //Paramter is an integer n which represents the  number of commits thatb should be returned
    //Return type is a string.
    public String getHistory(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("Number of commits must be positive");
        }

        LinkedList<String> history = new LinkedList<>();
        Commit current = head;

        // Traverse to the nth commit
        for (int i = 0; i < n && current != null; i++) {
            history.addFirst(current.toString());
            current = current.past;
        }

        String result = "";

        for (String commit : history) {
            result = commit + "\n" + result;
        }
        return result.trim(); // Trim any leading or trailing whitespace
    }

    //Create a new commit with the given message, add it to this repository.The new commit
    //The return type is a stirng since the ID of the new commit is returned.Paramter includes
    //a string representing the message that the commit should be created with.
    public String commit(String message){
        Commit newCommit = new Commit(message, head);
        head = newCommit;
        
        size++;

        return newCommit.id;
    }

    //Remove the commit with ID targetId from this repository, maintaining the 
    //rest of the history.Rteurn type is a boolean value since true is retuned if 
    //the commit was successfully dropped, and false if there is no 
    //commit that matches the given ID.
    public boolean drop(String targetId){
        if (head == null) {
            return false; // Repository is empty
        }

        if (head.id.equals(targetId)) {
            head = head.past;
            size--;

            return true;
        }

        Commit current = head;

        while (current != null && current.past != null && 
                !current.past.id.equals(targetId)) {
                    current = current.past;
        }

        if (current != null && current.past != null) {
            current.past = current.past.past;
            size--;

            return true;
        }
        return false;// Commit not found
    }


    //Takes all the commits in the other repository and moves them
    // into this repository, combining the two repository histories such that
    // chronological order is preserved. Paramter is a repository object called other 
    //which represents the other repository
    public void synchronize(Repository other) {
        
        if (other.head == null) {
            return;
        }

        
        if (this.head == null) {
            head = other.head;
            size=other.size;
            other.head = null; 
            other.size = 0; 
        }

        Commit otherCurr = other.head;
        Commit prev = null;
        Commit curr = head;

        while (otherCurr != null) {
            while(curr!= null && curr.timeStamp > otherCurr.timeStamp){
                prev = curr;
                curr = curr.past;
            }
            Commit otherNext = otherCurr.past;

            if(prev == null){
                other.head = otherNext;
                otherCurr.past = head;
                head = otherCurr;
            }

            else{
                prev.past = otherCurr;
                otherCurr.past = curr;
            }
        
            size += 1;
            otherCurr = otherNext;
            curr = head;
            prev = null;
        }

        // Reset other repository
        other.head = null;
        other.size = 0;    
    }

  /**
     * DO NOT MODIFY
     * A class that represents a single commit in the repository.
     * Commits are characterized by an identifier, a commit message,
     * and the time that the commit was made. A commit also stores
     * a reference to the immediately previous commit if it exists.
     *
     * Staff Note: You may notice that the comments in this 
     * class openly mention the fields of the class. This is fine 
     * because the fields of the Commit class are public. In general, 
     * be careful about revealing implementation details!
     */
    public static class Commit {

        public Object commit;

        private static int currentCommitID;

        /**
         * The time, in milliseconds, at which this commit was created.
         */
        public final long timeStamp;

        /**
         * A unique identifier for this commit.
         */
        public final String id;

        /**
         * A message describing the changes made in this commit.
         */
        public final String message;

        /**
         * A reference to the previous commit, if it exists. Otherwise, null.
         */
        public Commit past;

        /**
         * Constructs a commit object. The unique identifier and timestamp
         * are automatically generated.
         * @param message A message describing the changes made in this commit.
         * @param past A reference to the commit made immediately before this
         *             commit.
         */
        public Commit(String message, Commit past) {
            this.id = "" + currentCommitID++;
            this.message = message;
            this.timeStamp = System.currentTimeMillis();
            this.past = past;
        }

        /**
         * Constructs a commit object with no previous commit. The unique
         * identifier and timestamp are automatically generated.
         * @param message A message describing the changes made in this commit.
         */
        public Commit(String message) {
            this(message, null);
        }

        /**
         * Returns a string representation of this commit. The string
         * representation consists of this commit's unique identifier,
         * timestamp, and message, in the following form:
         *      "[identifier] at [timestamp]: [message]"
         * @return The string representation of this collection.
         */
        @Override
        public String toString() {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
            Date date = new Date(timeStamp);

            return id + " at " + formatter.format(date) + ": " + message;
        }

        /**
        * Resets the IDs of the commit nodes such that they reset to 0.
        * Primarily for testing purposes.
        */
        public static void resetIds() {
            Commit.currentCommitID = 0;
        }
    }
}
