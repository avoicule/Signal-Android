package org.thoughtcrime.securesms;


import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.thoughtcrime.securesms.database.BankAccountsDatabase;
import org.thoughtcrime.securesms.database.CursorRecyclerViewAdapter;
import org.thoughtcrime.securesms.database.DatabaseFactory;
import org.thoughtcrime.securesms.database.ThreadDatabase;
import org.thoughtcrime.securesms.database.model.ThreadRecord;
import org.thoughtcrime.securesms.mms.GlideRequests;
import org.thoughtcrime.securesms.util.Conversions;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * A CursorAdapter for building a list of conversation threads.
 *
 * @author Moxie Marlinspike
 */
class BankAccountListAdapter extends CursorRecyclerViewAdapter<BankAccountListAdapter.ViewHolder> {

    private static final int MESSAGE_TYPE_SWITCH_ARCHIVE = 1;
    private static final int MESSAGE_TYPE_THREAD         = 2;
    private static final int MESSAGE_TYPE_INBOX_ZERO     = 3;

    private final @NonNull BankAccountsDatabase bankAccountsDatabase;
    private final @NonNull  GlideRequests     glideRequests;
    private final @NonNull  Locale            locale;
    private final @NonNull  LayoutInflater    inflater;
    private final @Nullable ItemClickListener clickListener;
    private final @NonNull  MessageDigest     digest;

    private final Set<Long> batchSet  = Collections.synchronizedSet(new HashSet<Long>());
    private       boolean   batchMode = false;

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        public <V extends View & BindableBankAccountListItem> ViewHolder(final @NonNull V itemView)
        {
            super(itemView);
        }

        public BindableBankAccountListItem getItem() {
            return (BindableBankAccountListItem)itemView;
        }
    }


    @Override
    public long getItemId(@NonNull Cursor cursor) {
        BankAccountsDatabase.BancAccountInfo record  = getBankAccountRecord(cursor);

        return record.getId();
    }

    BankAccountListAdapter(@NonNull Context context,
                            @NonNull GlideRequests glideRequests,
                            @NonNull Locale locale,
                            @Nullable Cursor cursor,
                            @Nullable ItemClickListener clickListener)
    {
        super(context, cursor);
        try {
            this.glideRequests  = glideRequests;
            this.bankAccountsDatabase = DatabaseFactory.getBankAccountsDatabase(context);
            this.locale         = locale;
            this.inflater       = LayoutInflater.from(context);
            this.clickListener  = clickListener;
            this.digest         = MessageDigest.getInstance("SHA1");
            setHasStableIds(true);
        } catch (NoSuchAlgorithmException nsae) {
            throw new AssertionError("SHA-1 missing");
        }
    }

    @Override
    public ViewHolder onCreateItemViewHolder(ViewGroup parent, int viewType) {
        if (viewType == MESSAGE_TYPE_SWITCH_ARCHIVE) {
            BankAccountListItemAction action = (BankAccountListItemAction) inflater.inflate(R.layout.conversation_list_item_action,
                    parent, false);

            action.setOnClickListener(v -> {
                if (clickListener != null) clickListener.onSwitchToArchive();
            });

            return new ViewHolder(action);
        } else if (viewType == MESSAGE_TYPE_INBOX_ZERO) {
          //  return new ViewHolder((ConversationListItemInboxZero)inflater.inflate(R.layout.conversation_list_item_inbox_zero, parent, false));
            return null;//alin
        } else {
            final ConversationListItem item = (ConversationListItem)inflater.inflate(R.layout.conversation_list_item_view,
                    parent, false);

            item.setOnClickListener(view -> {
                if (clickListener != null) clickListener.onItemClick(item);
            });

            item.setOnLongClickListener(view -> {
                if (clickListener != null) clickListener.onItemLongClick(item);
                return true;
            });

           // return new ViewHolder(item);
            return null;//alin
        }
    }

    @Override
    public void onItemViewRecycled(ViewHolder holder) {
        holder.getItem().unbind();
    }

    @Override
    public void onBindItemViewHolder(ViewHolder viewHolder, @NonNull Cursor cursor) {
        viewHolder.getItem().bind(getBankAccountRecord(cursor), glideRequests, locale, batchSet, batchMode);
    }

    @Override
    public int getItemViewType(@NonNull Cursor cursor) {
        BankAccountsDatabase.BancAccountInfo record = getBankAccountRecord(cursor);

      //  if (threadRecord.getDistributionType() == ThreadDatabase.DistributionTypes.ARCHIVE) {
        //    return MESSAGE_TYPE_SWITCH_ARCHIVE;
       // } else if (threadRecord.getDistributionType() == ThreadDatabase.DistributionTypes.INBOX_ZERO) {
         //   return MESSAGE_TYPE_INBOX_ZERO;
        //} else {
         //   return MESSAGE_TYPE_THREAD;
        //}


        return 1;
    }

    private BankAccountsDatabase.BancAccountInfo getBankAccountRecord(@NonNull Cursor cursor) {
        return bankAccountsDatabase.readerFor(cursor).getCurrent();
    }

    void toggleThreadInBatchSet(long threadId) {
        if (batchSet.contains(threadId)) {
            batchSet.remove(threadId);
        } else if (threadId != -1) {
            batchSet.add(threadId);
        }
    }

    Set<Long> getBatchSelections() {
        return batchSet;
    }

    void initializeBatchMode(boolean toggle) {
        this.batchMode = toggle;
        unselectAllThreads();
    }

    private void unselectAllThreads() {
        this.batchSet.clear();
        this.notifyDataSetChanged();
    }

    void selectAllThreads() {
        for (int i = 0; i < getItemCount(); i++) {
          //  long threadId = getThreadRecord(getCursorAtPositionOrThrow(i)).getThreadId();
          //  if (threadId != -1) batchSet.add(threadId);
        }
        this.notifyDataSetChanged();
    }

    interface ItemClickListener {
        void onItemClick(ConversationListItem item);
        void onItemLongClick(ConversationListItem item);
        void onSwitchToArchive();
    }
}
