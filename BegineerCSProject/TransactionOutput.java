import java.security.PublicKey;

public class TransactionOutput {
    public String id;
    // The public key of the new owner of these coins.
    public PublicKey recipient; 
    // The amount of coins they own.
    public double value; 
    // The id of the transaction this output was created in.
    public String parentTransactionId;

    /**
     * Constructor for the TransactionOutput class.
     * @param recipient The public key of the recipient.
     * @param value The amount of funds.
     * @param parentTransactionId The ID of the parent transaction.
     */
    public TransactionOutput(PublicKey recipient, double value, String parentTransactionId) {
        this.recipient = recipient;
        this.value = value;
        this.parentTransactionId = parentTransactionId;
        // The ID is calculated from the recipient, value, and parent transaction ID.
        this.id = Transaction.applySha256(Transaction.publicKeyToString(recipient) + Double.toString(value) + parentTransactionId);
    }

    /**
     * Checks if the coins belong to the specified public key.
     * @param publicKey The public key to check against.
     * @return true if the output belongs to the key, false otherwise.
     */
    public boolean isMine(PublicKey publicKey) {
        return (publicKey == recipient);
    }
}
