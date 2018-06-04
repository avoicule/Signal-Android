package org.thoughtcrime.securesms.database;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.annimon.stream.Stream;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteStatement;

import org.thoughtcrime.securesms.ApplicationContext;
import org.thoughtcrime.securesms.database.documents.IdentityKeyMismatch;
import org.thoughtcrime.securesms.database.documents.IdentityKeyMismatchList;
import org.thoughtcrime.securesms.database.helpers.SQLCipherOpenHelper;
import org.thoughtcrime.securesms.database.model.MessageRecord;
import org.thoughtcrime.securesms.database.model.SmsMessageRecord;
import org.thoughtcrime.securesms.jobs.TrimThreadJob;
import org.thoughtcrime.securesms.recipients.Recipient;
import org.thoughtcrime.securesms.sms.IncomingGroupMessage;
import org.thoughtcrime.securesms.sms.IncomingTextMessage;
import org.thoughtcrime.securesms.sms.OutgoingTextMessage;
import org.thoughtcrime.securesms.util.JsonUtils;
import org.thoughtcrime.securesms.util.TextSecurePreferences;
import org.whispersystems.jobqueue.JobManager;
import org.whispersystems.libsignal.util.guava.Optional;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class BankAccountsDatabase extends Database {

    public  static final String TABLE_NAME = "bank_accounts";

    private static final String ID        = "_id";
    private static final String BANK_NAME    = "bank_name";
    private static final String BANK_ACCOUNT   = "bank_account";
    private static final String STATUS    = "status";
    private static final String PRIMARY    = "primaryAccount";
    private static final String REFRESH_TOKEN    = "refresh_token";
    private static final String LAST_LOGIN = "last_login";
    private static final String TIMESTAMP = "timestamp";



    public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + ID + " INTEGER PRIMARY KEY, "                          +
            BANK_NAME + " TEXT, " + BANK_ACCOUNT + " TEXT, "  + STATUS + " INTEGER, " +
            PRIMARY + " INTEGER, "+REFRESH_TOKEN +" TEXT, " + LAST_LOGIN + " INTEGER, " + TIMESTAMP + " INTEGER);";

    public static final String[] CREATE_INDEXES = {
            "CREATE INDEX IF NOT EXISTS BANK_ACCOUNTS_INDEX_NAME ON " + TABLE_NAME + " (" + BANK_NAME + ");",
    };

    public BankAccountsDatabase(Context context, SQLCipherOpenHelper databaseHelper) {
        super(context, databaseHelper);
    }

    public void insert(String bankName,String bankAccount,  int status, long timestamp) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues(4);
        values.put(BANK_NAME, bankName);
        values.put(BANK_ACCOUNT, bankAccount);
        values.put(STATUS, status);
        values.put(TIMESTAMP, timestamp);
        values.put(LAST_LOGIN, timestamp);
        values.put(PRIMARY, 1);
        values.put(REFRESH_TOKEN, "refresh token");
        db.insert(TABLE_NAME, null, values);

    }

    public void updateStatus( long Id, int status, long timestamp) {
        SQLiteDatabase db     = databaseHelper.getWritableDatabase();
        ContentValues  values = new ContentValues(2);
        values.put(STATUS, status);
        values.put(TIMESTAMP, timestamp);

        db.update(TABLE_NAME, values, Id + " = ? AND " + STATUS + " =?", new String[] {String.valueOf(Id),  String.valueOf(status)});
    }

    public @NonNull List<BankAccountsDatabase.BancAccountInfo> getBankAccountInfo(long Id) {
        SQLiteDatabase         db      = databaseHelper.getReadableDatabase();
        List<BankAccountsDatabase.BancAccountInfo> results = new LinkedList<>();

        try (Cursor cursor = db.query(TABLE_NAME, null, ID + " = ?", new String[] {String.valueOf(Id)}, null, null, null)) {
            while (cursor != null && cursor.moveToNext()) {
                results.add(new BankAccountsDatabase.BancAccountInfo(cursor.getString(cursor.getColumnIndexOrThrow(BANK_NAME)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(STATUS)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(TIMESTAMP)),
                        cursor.getString(cursor.getColumnIndexOrThrow(BANK_ACCOUNT)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(LAST_LOGIN)),
                        cursor.getString(cursor.getColumnIndexOrThrow(REFRESH_TOKEN)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(PRIMARY))));
            }
        }

        return results;
    }

    public int getAccountCount() {
        SQLiteDatabase         db      = databaseHelper.getReadableDatabase();

        int count=0;

        try (Cursor mCount= db.rawQuery("select count(*) from " +TABLE_NAME , null)) {
            mCount.moveToFirst();
            count= mCount.getInt(0);
            mCount.close();
        }
        return count;
    }

    void deleteRowsForMessage(String bankName) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.delete(TABLE_NAME, BANK_NAME + " = ?", new String[] {String.valueOf(bankName)});
    }

    void deleteAllRows() {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
    }

    public static class BancAccountInfo {
        private final String bankName;
        private final int     status;
        private final long    timestamp;
        private final String   bankAccount;
        private final int  primary;
        private final String refreshToken;
        private final long lastLogin;

        public BancAccountInfo(String bankName, int status, long timestamp,String accountNumber,long lastLogin,String refreshToken,int primary) {
            this.bankName = bankName;
            this.status = status;
            this.timestamp = timestamp;
            this.bankAccount=accountNumber;
            this.primary=primary;
            this.refreshToken=refreshToken;
            this.lastLogin=lastLogin;
        }


        public String getBankName() {
            return bankName;
        }
        public String getBankAccount(){return bankAccount;}

        public int getStatus() {
            return status;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
}



