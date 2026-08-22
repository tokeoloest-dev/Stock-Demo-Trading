package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TradeOrderDao {
    @Query("SELECT * FROM orders ORDER BY timestamp DESC")
    fun getAllOrders(): Flow<List<TradeOrderEntity>>

    @Query("SELECT * FROM orders WHERE dateString = :dateString ORDER BY timestamp DESC")
    fun getOrdersByDate(dateString: String): Flow<List<TradeOrderEntity>>

    @Query("SELECT DISTINCT dateString FROM orders ORDER BY timestamp DESC")
    fun getAllOrderDates(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: TradeOrderEntity): Long

    @Update
    suspend fun updateOrder(order: TradeOrderEntity)

    @Query("DELETE FROM orders")
    suspend fun clearOrders()
}

@Dao
interface PositionDao {
    @Query("SELECT * FROM positions WHERE isOpen = 1 ORDER BY openedTimestamp DESC")
    fun getOpenPositions(): Flow<List<PositionEntity>>

    @Query("SELECT * FROM positions ORDER BY openedTimestamp DESC")
    fun getAllPositions(): Flow<List<PositionEntity>>

    @Query("SELECT * FROM positions WHERE id = :id LIMIT 1")
    suspend fun getPositionById(id: Long): PositionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosition(position: PositionEntity): Long

    @Update
    suspend fun updatePosition(position: PositionEntity)

    @Query("DELETE FROM positions")
    suspend fun clearPositions()
}

@Dao
interface WalletTransactionDao {
    @Query("SELECT * FROM wallet_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<WalletTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: WalletTransactionEntity): Long

    @Query("DELETE FROM wallet_transactions")
    suspend fun clearTransactions()
}

@Dao
interface DemoAccountDao {
    @Query("SELECT * FROM demo_account WHERE id = 1 LIMIT 1")
    fun getDemoAccount(): Flow<DemoAccountEntity?>

    @Query("SELECT * FROM demo_account WHERE id = 1 LIMIT 1")
    suspend fun getDemoAccountSync(): DemoAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(account: DemoAccountEntity)
}

@Dao
interface WatchlistDao {
    @Query("SELECT * FROM watchlist_items ORDER BY addedAt ASC")
    fun getAllWatchlistItems(): Flow<List<WatchlistItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToWatchlist(item: WatchlistItemEntity)

    @Query("DELETE FROM watchlist_items WHERE symbol = :symbol")
    suspend fun removeFromWatchlist(symbol: String)

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist_items WHERE symbol = :symbol)")
    fun isInWatchlist(symbol: String): Flow<Boolean>

    @Query("SELECT symbol FROM watchlist_items")
    suspend fun getAllSymbolsSync(): List<String>
}
