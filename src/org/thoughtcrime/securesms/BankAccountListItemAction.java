package org.thoughtcrime.securesms;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.thoughtcrime.securesms.database.BankAccountsDatabase;
import org.thoughtcrime.securesms.database.model.ThreadRecord;
import org.thoughtcrime.securesms.mms.GlideRequests;
import org.thoughtcrime.securesms.util.ViewUtil;

import java.util.Locale;
import java.util.Set;

public class BankAccountListItemAction extends LinearLayout implements BindableBankAccountListItem{

    private TextView description;

    public BankAccountListItemAction(Context context) {
        super(context);
    }

    public BankAccountListItemAction(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public BankAccountListItemAction(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        this.description = ViewUtil.findById(this, R.id.description);
    }

    @Override
    public void bind(@NonNull BankAccountsDatabase.BancAccountInfo record, @NonNull GlideRequests glideRequests, @NonNull Locale locale, @NonNull Set<Long> selectedThreads, boolean batchMode) {
        this.description.setText(getContext().getString(R.string.ConversationListItemAction_archived_conversations_d,record.getId())); //era threadRecord.getCount()
    }

    @Override
    public void unbind() {

    }
}