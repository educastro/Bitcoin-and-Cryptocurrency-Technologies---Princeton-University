import java.util.ArrayList;

public class TxHandler {

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
	
	/**
	 * Creates a local and private variable to store the UtxoPool copy.
	 */
	private UTXOPool UTXOPool;
	
	/**
	 * Default constructor
	 */
    public TxHandler(UTXOPool utxoPool) {
        // IMPLEMENT THIS
        /**
         * makes a local copy of the UTXOPool
         */
    		this.UTXOPool = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        // IMPLEMENT THIS
    		/**
    		 * Instance to store all the already processed inputs in this transaction so we can check
    		 * which one was already processed and avoid double spending error. 
    		 */
    		UTXOPool processedUTXOPool = new UTXOPool();
    		double sumOfInputValues = 0;
    		double sumOfOutputValues = 0;
    		
    		for (int iterator = 0; iterator < tx.numInputs(); iterator++) {
    			/**
    			 * The input correspondent to the iterator position.
    			 */
    			Transaction.Input inputInstance = tx.getInput(iterator);
    			/**
    			 * Creates an UTXO instance with the input.prevTxHash (hash of the Transaction whose output is being used)
    			 * and input.outputIndex (used output's index in the previous transaction). This is important because every
    			 * new block depends on the previous one to be valid.
    			 */
    			UTXO UTXOInstance = new UTXO(inputInstance.prevTxHash, inputInstance.outputIndex);
    			/**
    		     * @return the transaction output corresponding to UTXO {@code utxo}, or null if {@code utxo} is
    		     *         not in the pool.
    		     */
    			Transaction.Output outputInstance = UTXOPool.getTxOutput(UTXOInstance);
    		
    			/**
    			 * Condition (1).
    			 * If the UTXOPoolInstance doesn't contain in the current UTXOPool then return false, that transaction 
    			 * it not valid.  
    			 */
    			
    			if (UTXOPool.contains(UTXOInstance) == false) {
    				return false;
    			}
    			
    			/**
    			 * Condition (2).
    			 * The Crypto has a method to verifySignature, so here we are providing the output.address with the
    			 * public address to the person who is receiving the money, the data to be signed from this transaction
    			 * and the signature from the transaction (private key)
    			 */
    			if (Crypto.verifySignature(outputInstance.address, tx.getRawDataToSign(iterator), inputInstance.signature) == false) {
    				return false;
    			}
    			
    			/**
    			 * Condition (3).
    			 * Check if this UTXO was already processed checking if it contains on the processedUTXOPool variable,
    			 * if it is not then code keeps working and this input instance is added to the processedUTXOPool.
    			 */
    			if (processedUTXOPool.contains(UTXOInstance) == true) {
    				return false;
    			} 
    				
    			processedUTXOPool.addUTXO(UTXOInstance, outputInstance);
    			
    			
    			sumOfInputValues = sumOfInputValues + outputInstance.value;
    			
    		}
    			/**
    			 * Condition (4).
    			 * Check if all of the the output values are non-negatives.
    			 */
    			for (int iterator2 = 0; iterator2 < tx.getOutputs().size(); iterator2++) {
    				Transaction.Output outputInstanceValue = tx.getOutput(iterator2);
    				if (outputInstanceValue.value < 0) {
    					return false;
    				}
    				sumOfOutputValues = sumOfOutputValues + outputInstanceValue.value;
    			}
    			
    			/**
    			 * Condition (5).
    			 * Check if the sum of all the input values in this tx transaction are equal or bigger 
    			 * than the sum of all the output values.
    			 */
    			if (sumOfInputValues >= sumOfOutputValues == false) {
    				return false;
    			}
    		
    		/**
    		 * If all the statements are true, then return true, it means that our transaction tx is
    		 * good to go.
    		 */
    		return true;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // IMPLEMENT THIS
    	
    		/**
    		 * If possibleTxs is null or empty than we get out the method.
    		 */
    		if ((possibleTxs == null) || (possibleTxs.length == 0)) {
    			return null;
    		}
    		
    		/**
    		 * ArrayList for all the accepted transactions
    		 */
    		ArrayList<Transaction> acceptedTransactions = new ArrayList<Transaction>();
    		
    		for (Transaction tx : possibleTxs) { 
    			if (isValidTx(tx)) {
    				acceptedTransactions.add(tx);
    			
    				/**
    				 * remove the inputs
    				 */
	    			for (int iterator3 = 0; iterator3 < tx.numInputs(); iterator3++) {
	    				Transaction.Input inputToRemove = tx.getInput(iterator3);
	    				UTXO UTXOToRemove = new UTXO(inputToRemove.prevTxHash, inputToRemove.outputIndex);
	    				UTXOPool.removeUTXO(UTXOToRemove);
	    			}
	    			/**
	    			 * add the outputs
	    			 */
	    			for (int iterator4 = 0; iterator4 < tx.numOutputs(); iterator4++) {
	    				Transaction.Output outputToAdd = tx.getOutput(iterator4);
	    				UTXO UTXOToAdd = new UTXO(tx.getHash(), iterator4);
	    				UTXOPool.addUTXO(UTXOToAdd, outputToAdd);
	    			}
    			
    			}
    		}
			
    		/**
    		 * Convert the acceptedTransactions ArrayList to the Transaction[] object and returns it
    		 */
    		Transaction[] returnTransactionObject = new Transaction[acceptedTransactions.size()];
    		
    		for (int iterator5 = 0; iterator5 < acceptedTransactions.size(); iterator5++) {
    			returnTransactionObject[iterator5] = acceptedTransactions.get(iterator5);
    		}
    	
    		return returnTransactionObject;
    		
    }

}
