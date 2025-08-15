import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Wallet {

    // The private key, used to sign transactions. It must be kept secret.
    public PrivateKey privateKey;
    // The public key, which acts as the wallet's address. It can be shared publicly.
    public PublicKey publicKey;

    // A map to keep track of unspent transaction outputs (UTXOs) owned by this wallet.
    // This is managed locally for the wallet's convenience.
    public HashMap<String, TransactionOutput> UTXOs = new HashMap<>();

    /**
     * Constructor for the Wallet class.
     * It generates a new public/private key pair upon creation.
     */
    public Wallet() {
        generateKeyPair();
    }

    /**
     * Generates a new Elliptic Curve (EC) key pair.
     */
    public void generateKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA", "BC");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");
            
            keyGen.initialize(ecSpec, random);
            KeyPair keyPair = keyGen.generateKeyPair();
            
            privateKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Calculates the balance of the wallet by summing up the value of all its UTXOs.
     * It scans the main blockchain's UTXO list to find outputs belonging to this wallet.
     * @return The total balance of the wallet.
     */
    public double getBalance() {
        double total = 0;
        for (Map.Entry<String, TransactionOutput> item : Blockchain.UTXOs.entrySet()) {
            TransactionOutput UTXO = item.getValue();
            if (UTXO.isMine(publicKey)) { // Check if the output belongs to this wallet
                UTXOs.put(UTXO.id, UTXO); // Add it to our list of unspent transactions.
                total += UTXO.value;
            }
        }
        return total;
    }

    /**
     * Creates and returns a new transaction from this wallet.
     * @param _recipient The public key of the recipient.
     * @param _value The amount to send.
     * @return The newly created transaction, or null if funds are insufficient.
     */
    public Transaction sendFunds(PublicKey _recipient, double _value) {
        if (getBalance() < _value) {
            System.out.println("# Not enough funds to send transaction. Transaction Discarded.");
            return null;
        }
        
        // Create a list of inputs for the new transaction from the wallet's UTXOs
        ArrayList<TransactionInput> inputs = new ArrayList<>();

        double total = 0;
        for (Map.Entry<String, TransactionOutput> item : UTXOs.entrySet()) {
            TransactionOutput UTXO = item.getValue();
            total += UTXO.value;
            inputs.add(new TransactionInput(UTXO.id));
            if (total > _value) break;
        }

        Transaction newTransaction = new Transaction(publicKey, _recipient, _value, inputs);
        newTransaction.generateSignature(privateKey);

        // Remove the spent UTXOs from this wallet's list
        for (TransactionInput input : inputs) {
            UTXOs.remove(input.transactionOutputId);
        }
        return newTransaction;
    }
}
