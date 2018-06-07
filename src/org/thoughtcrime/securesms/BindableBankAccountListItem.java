package org.thoughtcrime.securesms;

import android.support.annotation.NonNull;

import org.thoughtcrime.securesms.crypto.MasterSecret;
import org.thoughtcrime.securesms.database.BankAccountsDatabase;
import org.thoughtcrime.securesms.database.model.ThreadRecord;
import org.thoughtcrime.securesms.mms.GlideRequests;

import java.util.Locale;
import java.util.Set;

public interface BindableBankAccountListItem extends Unbindable {

    public void bind(@NonNull BankAccountsDatabase.BancAccountInfo thread,
                     @NonNull GlideRequests glideRequests, @NonNull Locale locale,
                     @NonNull Set<Long> selectedBankAccounts, boolean batchMode);
}
