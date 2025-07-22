import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

public class ExampleTesting {
    private Repository repo1;
    private Repository repo2;

    // Occurs before each of the individual test cases
    // (creates new repos and resets commit ids)
    @BeforeEach
    public void setUp() {
        repo1 = new Repository("repo1");
        repo2 = new Repository("repo2");
        Repository.Commit.resetIds();
    }

    @Test
    @DisplayName("EXAMPLE TEST - getHistory()")
    public void getHistory() throws InterruptedException {
        // Initialize commit messages
        String[] commitMessages = new String[]{"Initial commit.",
                                               "Updated method documentation.",
                                               "Removed unnecessary object creation."};
        commitAll(repo1, commitMessages);
        testHistory(repo1, 1, commitMessages);
    }

    @Test
    @DisplayName("EXAMPLE TEST - drop() (empty case)")
    public void testDropEmpty() {
        assertFalse(repo1.drop("123"));
    }

    @Test
    @DisplayName("EXAMPLE TEST - drop() (front case)")
    public void testDropFront() throws InterruptedException {
        // Initialize commit messages
        commitAll(repo1, new String[]{"First commit"});      // ID "0"
        commitAll(repo2, new String[]{"Added unit tests."}); // ID "1"

        // Assert that repo1 successfully dropped "0"
        assertTrue(repo1.drop("0"));
        assertEquals(repo1.getRepoSize(), 0);
        
        // Assert that repo2 does not drop "0" but drops "1"
        // (Note that the commit ID increments regardless of the repository!)
        assertFalse(repo2.drop("0"));
        assertTrue(repo2.drop("1"));
        assertEquals(repo2.getRepoSize(), 0);
    }

    @Test
    @DisplayName("EXAMPLE TEST - synchronize() (one: [1, 2], two: [3, 4])")
    public void testSynchronizeOne() throws InterruptedException {
        // Initialize commit messages
        commitAll(repo1, new String[]{"One", "Two"});
        commitAll(repo2, new String[]{"Three", "Four"});
        
        // Make sure both repos got exactly 2 commits each
        assertEquals(2, repo1.getRepoSize());
        assertEquals(2, repo2.getRepoSize());

        // Synchronize repo2 into repo1
        repo1.synchronize(repo2);
        assertEquals(4, repo1.getRepoSize());
        assertEquals(0, repo2.getRepoSize());

        // Make sure the history of repo1 is correctly synchronized
        testHistory(repo1, 4, new String[]{"One", "Two", "Three", "Four"});
    }

    // Commits all of the provided messages into the provided repo, making sure timestamps
    // are correctly sequential (no ties). If used, make sure to include
    //      'throws InterruptedException'
    // much like we do with 'throws FileNotFoundException'. Example useage:
    //
    // repo1:
    //      head -> null
    // To commit the messages "one", "two", "three", "four"
    //      commitAll(repo1, new String[]{"one", "two", "three", "four"})
    // This results in the following after picture
    // repo1:
    //      head -> "four" -> "three" -> "two" -> "one" -> null
    //
    // YOU DO NOT NEED TO UNDERSTAND HOW THIS METHOD WORKS TO USE IT! (this is why documentation
    // is important!)
    public void commitAll(Repository repo, String[] messages) throws InterruptedException {
        // Commit all of the provided messages
        for (String message : messages) {
            int size = repo.getRepoSize();
            repo.commit(message);
            
            // Make sure exactly one commit was added to the repo
            assertEquals(size + 1, repo.getRepoSize(),
                         String.format("Size not correctly updated after commiting message [%s]",
                                       message));

            // Sleep to guarantee that all commits have different time stamps
            Thread.sleep(2);
        }
    }

    // Makes sure the given repositories history is correct up to 'n' commits, checking against
    // all commits made in order. Example useage:
    //
    // repo1:
    //      head -> "four" -> "three" -> "two" -> "one" -> null
    //      (Commits made in the order ["one", "two", "three", "four"])
    // To test the getHistory() method up to n=3 commits this can be done with:
    //      testHistory(repo1, 3, new String[]{"one", "two", "three", "four"})
    // Similarly, to test getHistory() up to n=4 commits you'd use:
    //      testHistory(repo1, 4, new String[]{"one", "two", "three", "four"})
    //
    // YOU DO NOT NEED TO UNDERSTAND HOW THIS METHOD WORKS TO USE IT! (this is why documentation
    // is important!)
    public void testHistory(Repository repo, int n, String[] allCommits) {
        int totalCommits = repo.getRepoSize();
        assertTrue(n <= totalCommits,
                   String.format("Provided n [%d] too big. Only [%d] commits",
                                 n, totalCommits));
        
        String[] nCommits = repo.getHistory(n).split("\n");
        
        assertTrue(nCommits.length <= n,
                   String.format("getHistory(n) returned more than n [%d] commits", n));
        assertTrue(nCommits.length <= allCommits.length,
                   String.format("Not enough expected commits to check against. " +
                                 "Expected at least [%d]. Actual [%d]",
                                 n, allCommits.length));
        
        for (int i = 0; i < n; i++) {
            String commit = nCommits[i];

            // Old commit messages/ids are on the left and the more recent commit messages/ids are
            // on the right so need to traverse from right to left
            int backwardsIndex = totalCommits - 1 - i;
            String commitMessage = allCommits[backwardsIndex];

            assertTrue(commit.contains(commitMessage),
                       String.format("Commit [%s] doesn't contain expected message [%s]",
                                     commit, commitMessage));
            assertTrue(commit.contains("" + backwardsIndex),
                       String.format("Commit [%s] doesn't contain expected id [%d]",
                                     commit, backwardsIndex));
        }
    }
}
