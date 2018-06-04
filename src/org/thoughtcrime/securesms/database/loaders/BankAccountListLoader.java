package org.thoughtcrime.securesms.database.loaders;




import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;

import org.thoughtcrime.securesms.contacts.ContactAccessor;
import org.thoughtcrime.securesms.database.Address;
import org.thoughtcrime.securesms.database.DatabaseFactory;
import org.thoughtcrime.securesms.database.ThreadDatabase;
import org.thoughtcrime.securesms.util.AbstractCursorLoader;

import java.util.LinkedList;
import java.util.List;

public class BankAccountListLoader extends AbstractCursorLoader {

    public BankAccountListLoader(Context context, String filter) {
        super(context);
    }

    @Override
    public Cursor getCursor() {
        return  DatabaseFactory.getBankAccountsDatabase(context).getBankAccountsList();
    }



}
