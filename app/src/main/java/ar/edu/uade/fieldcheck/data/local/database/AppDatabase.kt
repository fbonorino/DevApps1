package ar.edu.uade.fieldcheck.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ar.edu.uade.fieldcheck.data.local.dao.EvidenceDao
import ar.edu.uade.fieldcheck.data.local.dao.InspectionDao
import ar.edu.uade.fieldcheck.data.local.dao.TemplateDao
import ar.edu.uade.fieldcheck.data.local.entity.EvidenceEntity
import ar.edu.uade.fieldcheck.data.local.entity.InspectionEntity
import ar.edu.uade.fieldcheck.data.local.entity.ItemResultEntity
import ar.edu.uade.fieldcheck.data.local.entity.TemplateEntity
import ar.edu.uade.fieldcheck.data.local.entity.TemplateItemEntity

// El esquema se exporta a app/schemas: cada cambio de versión necesita una migración escrita a mano.
// No usamos fallbackToDestructiveMigration porque borraría inspecciones que todavía no se sincronizaron.
@Database(
    entities = [
        TemplateEntity::class,
        TemplateItemEntity::class,
        InspectionEntity::class,
        ItemResultEntity::class,
        EvidenceEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun templateDao(): TemplateDao
    abstract fun inspectionDao(): InspectionDao
    abstract fun evidenceDao(): EvidenceDao

    companion object {
        fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "fieldcheck.db").build()
    }
}
