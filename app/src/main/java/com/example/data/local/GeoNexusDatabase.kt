package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [SpatialAssetEntity::class, InspectionTicketEntity::class],
    version = 1,
    exportSchema = false
)
abstract class GeoNexusDatabase : RoomDatabase() {
    abstract fun spatialAssetDao(): SpatialAssetDao
    abstract fun inspectionTicketDao(): InspectionTicketDao

    companion object {
        @Volatile
        private var INSTANCE: GeoNexusDatabase? = null

        fun getDatabase(context: Context): GeoNexusDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GeoNexusDatabase::class.java,
                    "geonexus_spatial.db"
                ).fallbackToDestructiveMigration(true)
                 .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
