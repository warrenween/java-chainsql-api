package java8.example;

import static java8.example.Print.*;

import com.peersafe.base.client.Client;
import com.peersafe.base.client.responses.Response;
import com.peersafe.base.client.transactions.AccountTxPager;
import com.peersafe.base.client.transport.impl.JavaWebSocketTransportImpl;
import com.peersafe.base.core.coretypes.AccountID;
import com.peersafe.base.core.types.known.tx.result.TransactionResult;

/**
 * This example shows how to page through some old transactions
 * affecting the BitStamp account.
 */
public class AccountTx {
    static final AccountID bitStamp =
            AccountID.fromAddress("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh");

    public static void main(String[] args) {
        new Client(new JavaWebSocketTransportImpl())
                .connect("ws://192.168.0.197:6007");
    }

    public AccountTx(Client client) {
        client.accountTxPager(bitStamp)
                .maxRetriesPerPage(5)
                .onPage(this::onPage)
                .onError(this::onError)
                .forward(true)
                .minLedger(6000000)
                .maxLedger(6001000)
                .pageSize(200)
                .request();
    }

    private void onError(Response response) {
        printErr("Oh noes! We had an error");
        // MessageFormat gets confused by the json `{`
        printErr("{0}", response.message.toString(2));
        System.exit(1);
    }

    private void onPage(AccountTxPager.Page page) {
        // There was a rippled bug at time of writing, where each page's
        // ledger span wasn't set properly. Hopefully fixed by `now` :)
        print("Found {0} transactions between {1} and {2}",
                page.size(), page.ledgerMin(), page.ledgerMax());

        page.transactionResults().forEach(this::onTransaction);

        if (page.hasNext()) {
            print("requesting next page!");
            page.requestNext();
        } else {
            print("got all transactions!");
            System.exit(0);
        }
    }

    private void onTransaction(TransactionResult result) {
        print("Found a transaction!\n{0}", result.toJSON().toString(2));
    }
}
