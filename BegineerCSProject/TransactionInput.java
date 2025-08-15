public class TransactionInput {
    // Reference to the TransactionOutput -> transactionId
    public String transactionOutputId; 
    // The unspent transaction output that this input is referencing
    public TransactionOutput UTXO; 

    /**
     * Constructor for the TransactionInput class.
     * @param transactionOutputId The ID of the transaction output to be used as an input.
     */
    public TransactionInput(String transactionOutputId) {
        this.transactionOutputId = transactionOutputId;
    }
}
