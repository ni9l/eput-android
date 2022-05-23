package eput.android.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        KnownDevice::class,
        CustomizedPreference::class,
        Configuration::class],
    exportSchema = false,
    version = 1
)
@TypeConverters(eput.android.db.TypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun knownDeviceDao(): KnownDeviceDao
    abstract fun preferenceSelectionDao(): CustomizedPreferenceDao
    abstract fun configurationDao(): ConfigurationDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room
                .databaseBuilder(context, AppDatabase::class.java, "app_db")
                .build()
        }
    }
}