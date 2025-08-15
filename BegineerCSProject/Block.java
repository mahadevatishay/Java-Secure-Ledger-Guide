import java.util.ArrayList;
import java.util.Date;

public class Block {

    // The unique digital fingerprint of this block.
    public String hash;
    // The hash of the previous block in the chain.
    public String previousHash;
    // The list of transactions contained within this block.
    public ArrayList<Transaction> transactions = new ArrayList<>();
    // Timestamp of when the block was created.
    private long timeStamp;
    // A random number used in the mining process (Proof of Work).
    private int nonce;

    /**
     * Constructor for the Block class.
     * @param previousHash The hash of the block before this one in the chain.
     */
    public Block(String previousHash) {
        this.previousHash = previousHash;
        this.timeStamp = new Date().getTime();
        // Calculate the hash for this new block right away.
        this.hash = calculateHash();
    }

    /**
     * Calculates the unique hash for this block.
     * The hash is calculated based on the previous hash, the timestamp, the nonce,
     * and the Merkle root of the transactions.
     * @return A SHA-256 hash string.
     */
    public String calculateHash() {
        String calculatedhash = Transaction.applySha256(
                previousHash +
                Long.toString(timeStamp) +
                Integer.toString(nonce) +
                getMerkleRoot() // Include the transactions' root hash
        );
        return calculatedhash;
    }

    /**
     * Simulates "Proof of Work". This method will repeatedly calculate the block's hash,
     * incrementing the nonce, until it finds a hash that meets the difficulty requirement
     * (i.e., starts with a certain number of zeros).
     * @param difficulty The number of leading zeros required for the hash.
     */
    public void mineBlock(int difficulty) {
        String target = new String(new char[difficulty]).replace('\0', '0'); // Create a string with 'difficulty' number of zeros
        while (!hash.substring(0, difficulty).equals(target)) {
            nonce++;
            hash = calculateHash();
        }
        System.out.println("Block Mined!!! : " + hash);
    }

    /**
     * Adds a transaction to this block.
     * @param transaction The transaction to be added.
     * @return true if the transaction was added successfully, false otherwise.
     */
    public boolean addTransaction(Transaction transaction) {
        // Process transaction and check if valid, unless block is genesis block then ignore.
        if (transaction == null) return false;
        if ((!previousHash.equals("0"))) {
            if ((!transaction.verifySignature())) {
                System.out.println("Transaction failed to verify");
                return false;
            }
        }
        transactions.add(transaction);
        System.out.println("Transaction Successfully added to Block");
        return true;
    }

    /**
     * Calculates the Merkle Root for the list of transactions.
     * The Merkle Root is a hash that represents the entire set of transactions.
     * This is a simplified implementation for our project.
     * @return The Merkle Root hash as a string.
     */
    private String getMerkleRoot() {
        int count = transactions.size();
        ArrayList<String> previousTreeLayer = new ArrayList<>();
        for (Transaction transaction : transactions) {
            previousTreeLayer.add(transaction.transactionId);
        }
        ArrayList<String> treeLayer = previousTreeLayer;
        while (count > 1) {
            treeLayer = new ArrayList<>();
            for (int i = 1; i < previousTreeLayer.size(); i += 2) {
                treeLayer.add(Transaction.applySha256(previousTreeLayer.get(i - 1) + previousTreeLayer.get(i)));
            }
            count = treeLayer.size();
            previousTreeLayer = treeLayer;
        }
        String merkleRoot = (treeLayer.size() == 1) ? treeLayer.get(0) : "";
        return merkleRoot;
    }
}
