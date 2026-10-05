package ar.edu.uade.fieldcheck.data.mapper

import ar.edu.uade.fieldcheck.data.local.entity.TemplateWithItems
import ar.edu.uade.fieldcheck.domain.model.Template
import ar.edu.uade.fieldcheck.domain.model.TemplateItem
import org.junit.Assert.assertEquals
import org.junit.Test

class TemplateMapperTest {

    private val template = Template(
        id = "tpl-obra",
        name = "Control semanal de obra",
        version = 3,
        items = listOf(
            TemplateItem("tpl-obra-1", "Vallado completo", 1),
            TemplateItem("tpl-obra-2", "Cartelería visible", 2),
        ),
        downloadedAt = 123L,
    )

    @Test
    fun `domain to entity and back keeps the same template`() {
        val entity = TemplateWithItems(
            template = template.toEntity(),
            items = template.items.map { it.toEntity(template.id) },
        )

        assertEquals(template, entity.toDomain())
        assertEquals(listOf("tpl-obra", "tpl-obra"), entity.items.map { it.templateId })
    }

    @Test
    fun `items come out ordered by position`() {
        val entity = TemplateWithItems(
            template = template.toEntity(),
            items = template.items.reversed().map { it.toEntity(template.id) },
        )

        assertEquals(listOf(1, 2), entity.toDomain().items.map { it.position })
    }
}
