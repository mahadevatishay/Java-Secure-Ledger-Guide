import java.security.*;
import java.util.ArrayList;
import java.util.Base64;

public class Transaction {

    public String transactionId; // A unique identifier for the transaction.
    public PublicKey sender; // The sender's public key (address).
    public PublicKey recipient; // The recipient's public key (address).
    public double value; // The amount to be transferred.
    public byte[] signature; // A digital signature to prevent tampering.

    public ArrayList<TransactionInput> inputs = new ArrayList<>();
    public ArrayList<TransactionOutput> outputs = new ArrayList<>();

    private static int sequence = 0; // A rough count of how many transactions have been generated.

    /**
     * Constructor for the Transaction class.
     * @param from The sender's public key.
     * @param to The recipient's public key.
     * @param value The amount to be transferred.
     * @param inputs The list of unspent transaction outputs to be used as inputs.
     */
    public Transaction(PublicKey from, PublicKey to, double value,  ArrayList<TransactionInput> inputs) {
        this.sender = from;
        this.recipient = to;
        this.value = value;
        this.inputs = inputs;
    }

    /**
     * Calculates the hash of the transaction, which will serve as its unique ID.
     */
    private String calculateHash() {
        sequence++; // Increase the sequence to avoid two identical transactions having the same hash.
        return applySha256(
                publicKeyToString(sender) +
                publicKeyToString(recipient) +
                Double.toString(value) + sequence
        );
    }

    /**
     * Generates a digital signature for the transaction data.
     * @param privateKey The sender's private key used for signing.
     */
    public void generateSignature(PrivateKey privateKey) {
        String data = publicKeyToString(sender) + publicKeyToString(recipient) + Double.toString(value);
        signature = applyECDSASig(privateKey, data);
    }

    /**
     * Verifies that the digital signature is valid.
     * @return true if the signature is valid, false otherwise.
     */
    public boolean verifySignature() {
        String data = publicKeyToString(sender) + publicKeyToString(recipient) + Double.toString(value);
        return verifyECDSASig(sender, data, signature);
    }

    /**
     * Processes the transaction by verifying the signature, checking inputs, creating outputs,
     * and updating the global list of unspent transactions (UTXOs).
     * @return true if the transaction is valid and processed successfully, false otherwise.
     */
    public boolean processTransaction() {

        if (!verifySignature()) {
            System.out.println("# Transaction Signature failed to verify");
            return false;
        }

        // Gather transaction inputs (Make sure they are unspent):
        for (TransactionInput i : inputs) {
            i.UTXO = Blockchain.UTXOs.get(i.transactionOutputId);
        }

        // Check if the transaction is valid:
        if (getInputsValue() < Blockchain.minimumTransaction) {
            System.out.println("# Transaction Inputs too small: " + getInputsValue());
            return false;
        }

        // Generate transaction outputs:
        double leftOver = getInputsValue() - value; // Get value of inputs then the left over change.
        transactionId = calculateHash();
        outputs.add(new TransactionOutput(this.recipient, value, transactionId)); // Send value to recipient.
        outputs.add(new TransactionOutput(this.sender, leftOver, transactionId)); // Send the left over 'change' back to the sender.

        // Add outputs to the unspent list
        for (TransactionOutput o : outputs) {
            Blockchain.UTXOs.put(o.id, o);
        }

        // Remove transaction inputs from UTXO lists as they are now spent:
        for (TransactionInput i : inputs) {
            if (i.UTXO == null) continue; // If Transaction can't be found, skip it.
            Blockchain.UTXOs.remove(i.UTXO.id);
        }

        return true;
    }

    /**
     * Calculates the total value of the transaction's inputs.
     * @return The sum of the input values.
     */
    public double getInputsValue() {
        double total = 0;
        for (TransactionInput i : inputs) {
            if (i.UTXO == null) continue; // If Transaction can't be found, skip it.
            total += i.UTXO.value;
        }
        return total;
    }

    /**
     * Calculates the total value of the transaction's outputs.
     * @return The sum of the output values.
     */
    public double getOutputsValue() {
        double total = 0;
        for (TransactionOutput o : outputs) {
            total += o.value;
        }
        return total;
    }
    
    // --- Static Helper Methods ---

    public static String applySha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] applyECDSASig(PrivateKey privateKey, String input) {
        try {
            Signature dsa = Signature.getInstance("ECDSA", "BC");
            dsa.initSign(privateKey);
            byte[] strByte = input.getBytes();
            dsa.update(strByte);
            return dsa.sign();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean verifyECDSASig(PublicKey publicKey, String data, byte[] signature) {
        try {
            Signature ecdsaVerify = Signature.getInstance("ECDSA", "BC");
            ecdsaVerify.initVerify(publicKey);
            ecdsaVerify.update(data.getBytes());
            return ecdsaVerify.verify(signature);
        } catch (Exception e) {
            return false;
        }
    }

    public static String publicKeyToString(Key key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }
}
