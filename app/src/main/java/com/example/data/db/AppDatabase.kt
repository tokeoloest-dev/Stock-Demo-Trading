package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TradeOrderEntity::class,
        PositionEntity::class,
        WalletTransactionEntity::class,
        DemoAccountEntity::class,
        WatchlistItemEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tradeOrderDao(): TradeOrderDao
    abstract fun positionDao(): PositionDao
    abstract fun walletTransactionDao(): WalletTransactionDao
    abstract fun demoAccountDao(): DemoAccountDao
    abstract fun watchlistDao(): WatchlistDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "stock_demo_trading.db"
                )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed initial demo account and watchlist
                        CoroutineScope(Dispatchers.IO).launch {
                            val database = getDatabase(context)
                            database.demoAccountDao().insertOrUpdate(
                                DemoAccountEntity(
                                    id = 1,
                                    balance = 1000000.0, // ₹10,00,000 initial demo funds
                                    marginUtilized = 0.0,
                                    totalRealizedPnL = 0.0,
                                    totalPointsDeducted = 0.0
                                )
                            )
                            database.walletTransactionDao().insertTransaction(
                                WalletTransactionEntity(
                                    type = "CREDIT",
                                    amount = 1000000.0,
                                    points = 0.0,
                                    description = "Welcome Demo Trading Capital Credit",
                                    dateString = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                                )
                            )
                            // Seed default watchlist symbols
                            listOf("NIFTY 50", "BANK NIFTY", "SENSEX", "RELIANCE", "TCS", "HDFCBANK", "INFY", "TATAMOTORS").forEach { sym ->
                                database.watchlistDao().addToWatchlist(WatchlistItemEntity(symbol = sym))
                            }
                        }
                    }
                })
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
