package ar.edu.uade.fieldcheck.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import ar.edu.uade.fieldcheck.data.local.entity.TemplateEntity
import ar.edu.uade.fieldcheck.data.local.entity.TemplateItemEntity
import ar.edu.uade.fieldcheck.data.local.entity.TemplateWithItems
import kotlinx.coroutines.flow.Flow

@Dao
abstract class TemplateDao {

    @Transaction
    @Query("SELECT * FROM templates ORDER BY name")
    abstract fun observeAll(): Flow<List<TemplateWithItems>>

    @Query("SELECT COUNT(*) FROM templates")
    abstract suspend fun count(): Int

    // La caché se reemplaza entera: se borran las plantillas (y sus ítems por cascada)
    // y se insertan las nuevas. Las inspecciones no se tocan porque no dependen de esta tabla.
    @Transaction
    open suspend fun replaceAll(templates: List<TemplateEntity>, items: List<TemplateItemEntity>) {
        deleteAll()
        insertTemplates(templates)
        insertItems(items)
    }

    @Query("DELETE FROM templates")
    protected abstract suspend fun deleteAll()

    @Insert
    protected abstract suspend fun insertTemplates(templates: List<TemplateEntity>)

    @Insert
    protected abstract suspend fun insertItems(items: List<TemplateItemEntity>)
}
