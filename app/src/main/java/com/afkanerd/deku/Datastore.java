package com.afkanerd.deku;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.AutoMigration;
import androidx.room.Database;
import androidx.room.DatabaseConfiguration;
import androidx.room.Delete;
import androidx.room.DeleteColumn;
import androidx.room.DeleteTable;
import androidx.room.InvalidationTracker;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.AutoMigrationSpec;
import androidx.sqlite.db.SupportSQLiteOpenHelper;

import com.afkanerd.deku.DefaultSMS.DAO.ThreadsConfigurationsDao;
import com.afkanerd.deku.DefaultSMS.Models.Archive;
import com.afkanerd.deku.DefaultSMS.Models.Conversations.Conversation;
import com.afkanerd.deku.DefaultSMS.DAO.ConversationDao;
import com.afkanerd.deku.DefaultSMS.Models.Conversations.ThreadedConversations;
import com.afkanerd.deku.DefaultSMS.Models.Conversations.ConversationsThreadsEncryption;
import com.afkanerd.deku.DefaultSMS.DAO.ConversationsThreadsEncryptionDao;
import com.afkanerd.deku.DefaultSMS.Models.ThreadsConfigurations;
import com.afkanerd.deku.QueueListener.GatewayClients.GatewayClient;
import com.afkanerd.deku.QueueListener.GatewayClients.GatewayClientDAO;
import com.afkanerd.deku.QueueListener.GatewayClients.GatewayClientProjectDao;
import com.afkanerd.deku.QueueListener.GatewayClients.GatewayClientProjects;
import com.afkanerd.deku.Router.GatewayServers.GatewayServer;
import com.afkanerd.deku.Router.GatewayServers.GatewayServerDAO;
//import com.afkanerd.deku.QueueListener.GatewayClients.GatewayClient;
//import com.afkanerd.deku.QueueListener.GatewayClients.GatewayClientDAO;
//import com.afkanerd.deku.Router.GatewayServers.GatewayServer;
//import com.afkanerd.deku.Router.GatewayServers.GatewayServerDAO;

//@Database(entities = {GatewayServer.class, Archive.class, GatewayClient.class,
//        ThreadedConversations.class, Conversation.class}, version = 9)

@Database(entities = {
        Archive.class,
        GatewayServer.class,
        GatewayClientProjects.class,
        Conversation.class,
        GatewayClient.class,
        ThreadsConfigurations.class},
        version = 21,
        autoMigrations = {
        @AutoMigration(from = 9, to = 10),
        @AutoMigration(from = 10, to = 11),
        @AutoMigration(from = 11, to = 12),
        @AutoMigration(from = 12, to = 13),
        @AutoMigration(from = 13, to = 14),
        @AutoMigration(from = 14, to = 15),
        @AutoMigration(from = 15, to = 16),
        @AutoMigration(from = 16, to = 17, spec = Datastore.Migrate16To17.class),
        @AutoMigration(from = 17, to = 18),
        @AutoMigration(from = 18, to = 19),
        @AutoMigration(from = 19, to = 20, spec = Datastore.Migrate19To20.class),
        @AutoMigration(from = 20, to = 21, spec = Datastore.Migrate20To21.class)
})


public abstract class Datastore extends RoomDatabase {
    private static volatile Datastore datastore;

    public static synchronized Datastore getDatastore(Context context) {
        if(datastore == null) {
            datastore = create(context);
        }
        return datastore;
    }

    private static Datastore create(final Context context) {
        return Room.databaseBuilder(context, Datastore.class, databaseName)
                .enableMultiInstanceInvalidation()
                .build();
    }

    public static String databaseName = "SMSWithoutBorders-Messaging-DB";

    public abstract GatewayServerDAO gatewayServerDAO();

    public abstract GatewayClientDAO gatewayClientDAO();
    public abstract GatewayClientProjectDao gatewayClientProjectDao();

    public abstract ConversationDao conversationDao();

    public abstract ThreadsConfigurationsDao threadsConfigurationsDao();

    @Override
    public void clearAllTables() {

    }

    @NonNull
    @Override
    protected InvalidationTracker createInvalidationTracker() {
        return null;
    }

    @NonNull
    @Override
    protected SupportSQLiteOpenHelper createOpenHelper(@NonNull DatabaseConfiguration databaseConfiguration) {
        return null;
    }

    @DeleteTable(tableName = "CustomKeyStore")
    static class Migrate16To17 implements AutoMigrationSpec { }

    @DeleteColumn.Entries({
            @DeleteColumn(tableName = "Conversation", columnName = "isBlocked"),
            @DeleteColumn(tableName = "Conversation", columnName = "isMute"),
            @DeleteColumn(tableName = "Conversation", columnName = "isSecured")
    })
    @DeleteTable.Entries(
            @DeleteTable(tableName = "ThreadedConversations")
    )
    static class Migrate19To20 implements AutoMigrationSpec { }

    @DeleteTable.Entries(
            @DeleteTable(tableName = "ConversationsThreadsEncryption")
    )
    static class Migrate20To21 implements AutoMigrationSpec { }
}
