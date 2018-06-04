package org.thoughtcrime.securesms;



import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import org.thoughtcrime.securesms.components.RatingManager;
import org.thoughtcrime.securesms.components.SearchToolbar;
import org.thoughtcrime.securesms.database.DatabaseFactory;
import org.thoughtcrime.securesms.database.MessagingDatabase.MarkedMessageInfo;
import org.thoughtcrime.securesms.lock.RegistrationLockDialog;
import org.thoughtcrime.securesms.notifications.MarkReadReceiver;
import org.thoughtcrime.securesms.notifications.MessageNotifier;
import org.thoughtcrime.securesms.permissions.Permissions;
import org.thoughtcrime.securesms.recipients.Recipient;
import org.thoughtcrime.securesms.search.SearchFragment;
import org.thoughtcrime.securesms.service.KeyCachingService;
import org.thoughtcrime.securesms.util.DynamicLanguage;
import org.thoughtcrime.securesms.util.DynamicNoActionBarTheme;
import org.thoughtcrime.securesms.util.DynamicTheme;
import org.thoughtcrime.securesms.util.TextSecurePreferences;

import java.util.List;

public class BankAccountListActivity extends PassphraseRequiredActionBarActivity
        implements ConversationListFragment.ConversationSelectedListener
{
    @SuppressWarnings("unused")
    private static final String TAG = BankAccountListActivity.class.getSimpleName();

    private final DynamicTheme    dynamicTheme    = new DynamicNoActionBarTheme();
    private final DynamicLanguage dynamicLanguage = new DynamicLanguage();

    private BankAccountListFragment  bankAccountListFragment;
    private SearchFragment           searchFragment;
    private SearchToolbar            searchToolbar;
    private ImageView                searchAction;
    private ViewGroup                fragmentContainer;

    @Override
    protected void onPreCreate() {
        dynamicTheme.onCreate(this);
        dynamicLanguage.onCreate(this);
    }

    @Override
    protected void onCreate(Bundle icicle, boolean ready) {
        setContentView(R.layout.bankaccount_list_activity);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        searchToolbar            = findViewById(R.id.search_toolbar);
        searchAction             = findViewById(R.id.search_action);
        fragmentContainer        = findViewById(R.id.fragment_container);
        bankAccountListFragment = initFragment(R.id.fragment_container, new BankAccountListFragment(), dynamicLanguage.getCurrentLocale());

        initializeSearchListener();

        RatingManager.showRatingDialogIfNecessary(this);
        RegistrationLockDialog.showReminderIfNecessary(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        dynamicTheme.onResume(this);
        dynamicLanguage.onResume(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuInflater inflater = this.getMenuInflater();
        menu.clear();

        inflater.inflate(R.menu.bank_accounts, menu);

        menu.findItem(R.id.menu_clear_passphrase).setVisible(!TextSecurePreferences.isPasswordDisabled(this));

        super.onPrepareOptionsMenu(menu);
        return true;
    }

    private void initializeSearchListener() {
        searchAction.setOnClickListener(v -> {
            Permissions.with(this)
                    .request(Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS)
                    .ifNecessary()
                    .onAllGranted(() -> searchToolbar.display(searchAction.getX() + (searchAction.getWidth() / 2),
                            searchAction.getY() + (searchAction.getHeight() / 2)))
                    .withPermanentDenialDialog(getString(R.string.ConversationListActivity_signal_needs_contacts_permission_in_order_to_search_your_contacts_but_it_has_been_permanently_denied))
                    .execute();
        });

        searchToolbar.setListener(new SearchToolbar.SearchListener() {
            @Override
            public void onSearchTextChange(String text) {
                String trimmed = text.trim();

                if (trimmed.length() > 0) {
                    if (searchFragment == null) {
                        searchFragment = SearchFragment.newInstance(dynamicLanguage.getCurrentLocale());
                        getSupportFragmentManager().beginTransaction()
                                .add(R.id.fragment_container, searchFragment, null)
                                .commit();
                    }
                    searchFragment.updateSearchQuery(trimmed);
                } else if (searchFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .remove(searchFragment)
                            .commit();
                    searchFragment = null;
                }
            }

            @Override
            public void onSearchClosed() {
                if (searchFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .remove(searchFragment)
                            .commit();
                    searchFragment = null;
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case R.id.menu_conversations:      handleConversations();    return true;
            case R.id.menu_new_group:         createGroup();           return true;
            case R.id.menu_settings:          handleDisplaySettings(); return true;
            case R.id.menu_my_bank:          handleDisplaySettings(); return true;
            case R.id.menu_clear_passphrase:  handleClearPassphrase(); return true;
            case R.id.menu_mark_all_read:     handleMarkAllRead();     return true;
            case R.id.menu_import_export:     handleImportExport();    return true;
            case R.id.menu_invite:            handleInvite();          return true;
            case R.id.menu_help:              handleHelp();            return true;
        }

        return false;
    }

    @Override
    public void onCreateConversation(long threadId, Recipient recipient, int distributionType, long lastSeen) {
        openConversation(threadId, recipient, distributionType, lastSeen, -1);
    }

    public void openConversation(long threadId, Recipient recipient, int distributionType, long lastSeen, int startingPosition) {
        searchToolbar.clearFocus();

        Intent intent = new Intent(this, ConversationActivity.class);
        intent.putExtra(ConversationActivity.ADDRESS_EXTRA, recipient.getAddress());
        intent.putExtra(ConversationActivity.THREAD_ID_EXTRA, threadId);
        intent.putExtra(ConversationActivity.DISTRIBUTION_TYPE_EXTRA, distributionType);
        intent.putExtra(ConversationActivity.TIMING_EXTRA, System.currentTimeMillis());
        intent.putExtra(ConversationActivity.LAST_SEEN_EXTRA, lastSeen);
        intent.putExtra(ConversationActivity.STARTING_POSITION_EXTRA, startingPosition);

        startActivity(intent);
        overridePendingTransition(R.anim.slide_from_right, R.anim.fade_scale_out);
    }

    @Override
    public void onSwitchToArchive() {
        Intent intent = new Intent(this, ConversationListArchiveActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        if (searchToolbar.isVisible()) searchToolbar.collapse();
        else                           super.onBackPressed();
    }

    private void createGroup() {
        Intent intent = new Intent(this, GroupCreateActivity.class);
        startActivity(intent);
    }

    private void handleDisplaySettings() {
        Intent preferencesIntent = new Intent(this, ApplicationPreferencesActivity.class);
        startActivity(preferencesIntent);
    }

    private void handleClearPassphrase() {
        Intent intent = new Intent(this, KeyCachingService.class);
        intent.setAction(KeyCachingService.CLEAR_KEY_ACTION);
        startService(intent);
    }

    private void handleImportExport() {
        startActivity(new Intent(this, ImportExportActivity.class));
    }

    private void handleConversations() {
        startActivity(new Intent(this, ConversationListActivity.class));
    }

    @SuppressLint("StaticFieldLeak")
    private void handleMarkAllRead() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Context                 context    = BankAccountListActivity.this;
                List<MarkedMessageInfo> messageIds = DatabaseFactory.getThreadDatabase(context).setAllThreadsRead();

                MessageNotifier.updateNotification(context);
                MarkReadReceiver.process(context, messageIds);

                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void handleInvite() {
        startActivity(new Intent(this, InviteActivity.class));
    }

    private void handleHelp() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://support.whispersystems.org")));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.ConversationListActivity_there_is_no_browser_installed_on_your_device, Toast.LENGTH_LONG).show();
        }
    }
}
