package com.peersafe.base.client.subscriptions.ledger;

import com.peersafe.base.client.Client;
import com.peersafe.base.core.types.known.tx.result.TransactionResult;
import com.peersafe.base.core.types.shamap.TransactionTree;

import java.text.MessageFormat;

import static com.peersafe.base.client.subscriptions.ledger.LedgerSubscriber.log;

public class PendingLedger {
    static private final String logMessage = "({0}/{4}) {1}/{2} tx {3}";
    private Object[] logParameters() {
        return new Object[]{
                ledger_index,
                clearedTransactions,
                expectedTxns,
                transactions.hash(),
                status};
    }

    /**
     * Set status.
     * @param status status.
     */
    public void setStatus(Status status) {
        this.status = status;
        logStateChange();
    }

    public static enum Status {
        pending,
        checkingHeader,
        fillingIn,
        cleared
    }

    public Status status;
    TransactionTree transactions;
    // set to -1 when we don't know how many to expect
    // this is just useful for debugging purposes
    int expectedTxns = -1;
    int clearedTransactions = 0;

    long ledger_index;
    private Client client;

    /**
     * PendingLedger
     * @param ledger_index ledger index.
     * @param clientInstance client instance.
     */
    public PendingLedger(long ledger_index, Client clientInstance) {
        this.ledger_index = ledger_index;

        transactions = new TransactionTree();
        client = clientInstance;
        status = Status.pending;
    }

    /**
     * notifyTransaction
     * @param tr tr.
     */
    public void notifyTransaction(TransactionResult tr) {
        if (!transactions.hasLeaf(tr.hash)) {
            clearedTransactions++;
            transactions.addTransactionResult(tr);
            client.onTransactionResult(tr);
            logStateChange();
        }
    }

    private void logStateChange() {
        log(logMessage, logParameters());
    }

    /**
     * transactionHash
     * @return return value.
     */
    public String transactionHash() {
        return transactions.hash().toHex();
    }

    boolean transactionHashEquals(String transaction_hash) {
        return transaction_hash.equals(transactionHash());
    }

    @Override
    public String toString() {
        Object[] arguments = logParameters();
        return MessageFormat.format("PendingLedger: " + logMessage, arguments);
    }
}
