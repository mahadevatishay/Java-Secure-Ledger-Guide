import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Blockchain {

    public static ArrayList<Block> blockchain = new ArrayList<>();
    public static HashMap<String, TransactionOutput> UTXOs = new HashMap<>();

    public static int difficulty = 3; // Difficulty for mining (number of leading zeros)
    public static double minimumTransaction = 0.1;
    public static Wallet walletA;
    public static Wallet walletB;
    public static Transaction genesisTransaction;

    public static void main(String[] args) {
    // Add BouncyCastle as a Security Provider
    Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

    // --- Initial Blockchain Setup ---
    ArrayList<Wallet> wallets = new ArrayList<>();
    walletA = new Wallet();
    walletB = new Wallet();
    wallets.add(walletA);
    wallets.add(walletB);
    Wallet coinbase = new Wallet();

    // Create genesis transaction, which sends 100 coins to walletA
    genesisTransaction = new Transaction(coinbase.publicKey, walletA.publicKey, 100f, null);
    genesisTransaction.generateSignature(coinbase.privateKey);
    genesisTransaction.transactionId = "0";
    genesisTransaction.outputs.add(new TransactionOutput(genesisTransaction.recipient, genesisTransaction.value, genesisTransaction.transactionId));
    UTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));

    System.out.println("Creating and Mining Genesis block... ");
    Block genesis = new Block("0");
    genesis.addTransaction(genesisTransaction);
    addBlock(genesis);

    // --- Interactive Loop ---
    Scanner scanner = new Scanner(System.in);
    while (true) {
        System.out.println("\n=================================");
        System.out.println("Choose an action:");
        System.out.println("1. Send Funds");
        System.out.println("2. Check Wallet Balance");
        System.out.println("3. Check Blockchain Validity");
        System.out.println("4. Exit");
        System.out.print("Enter your choice: ");

        String choice = scanner.nextLine();
        Block lastBlock = blockchain.get(blockchain.size() - 1);

        switch (choice) {
            case "1":
                try {
                    System.out.print("Enter sender wallet number (0 for A, 1 for B, etc.): ");
                    int senderIndex = Integer.parseInt(scanner.nextLine());
                    System.out.print("Enter recipient wallet number: ");
                    int recipientIndex = Integer.parseInt(scanner.nextLine());
                    System.out.print("Enter amount to send: ");
                    float amount = Float.parseFloat(scanner.nextLine());

                    if (senderIndex >= wallets.size() || recipientIndex >= wallets.size()) {
                        System.out.println("Invalid wallet number.");
                        continue;
                    }

                    Wallet sender = wallets.get(senderIndex);
                    Wallet recipient = wallets.get(recipientIndex);
                    
                    Block newBlock = new Block(lastBlock.hash);
                    System.out.println("\nAttempting to send " + amount + " from Wallet " + senderIndex + " to Wallet " + recipientIndex + "...");
                    newBlock.addTransaction(sender.sendFunds(recipient.publicKey, amount));
                    addBlock(newBlock);

                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter numbers only.");
                }
                break;

            case "2":
                try {
                    System.out.print("Enter wallet number to check balance (0 for A, 1 for B, etc.): ");
                    int walletIndex = Integer.parseInt(scanner.nextLine());
                     if (walletIndex >= wallets.size()) {
                        System.out.println("Invalid wallet number.");
                        continue;
                    }
                    System.out.println("Wallet " + walletIndex + " balance is: " + wallets.get(walletIndex).getBalance());
                } catch (NumberFormatException e) {
                     System.out.println("Invalid input. Please enter a number.");
                }
                break;

            case "3":
                isChainValid();
                break;

            case "4":
                System.out.println("Exiting...");
                scanner.close();
                System.exit(0);
                break;

            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }
}
        public static void addBlock(Block newBlock) {
        newBlock.mineBlock(difficulty);
        blockchain.add(newBlock);
        }
        public static Boolean isChainValid() {
        Block currentBlock;
        Block previousBlock;
        String hashTarget = new String(new char[difficulty]).replace('\0', '0');

        // Loop through the blockchain to check hashes:
        for (int i = 1; i < blockchain.size(); i++) {
            currentBlock = blockchain.get(i);
            previousBlock = blockchain.get(i - 1);

            // Compare the registered hash and the calculated hash:
            if (!currentBlock.hash.equals(currentBlock.calculateHash())) {
                System.out.println("Current Hashes not equal");
                return false;
            }

            // Compare the previous hash and the registered previous hash:
            if (!previousBlock.hash.equals(currentBlock.previousHash)) {
                System.out.println("Previous Hashes not equal");
                return false;
            }
        
            // Check if the hash is solved (has the required number of zeros)
            if(!currentBlock.hash.substring(0, difficulty).equals(hashTarget)) {
                System.out.println("This block hasn't been mined");
                return false;
            }
        }
        System.out.println("Blockchain is valid");
        return true;
    }
}