# FieldCheck

App Android para hacer **inspecciones de campo guiadas por checklist**: por cada ítem se registra un resultado (Cumple / No cumple / N/A), una nota y fotos como evidencia. Funciona **100 % sin conexión** y sincroniza sola con un servidor cuando vuelve la señal.

Trabajo Práctico Obligatorio de **Desarrollo de Aplicaciones I** (UADE).

> **Estado actual:** Etapa 1, preentrega de análisis y diseño (entrega 09/10/2026). Toda la documentación está en [`docs/preentrega.md`](docs/preentrega.md).
>
> La implementación comienza en la Etapa 2, una vez aprobada la preentrega. Un prototipo exploratorio que se hizo para validar la arquitectura propuesta se conserva en la rama [`prototipo/arquitectura`](https://github.com/fieldcheck-uade/FieldCheck/tree/prototipo/arquitectura).

---

## Qué problema resuelve

Quien hace inspecciones en obra, planta o salas técnicas trabaja en lugares con mala señal. Hoy junta fotos en la galería, notas en papel o WhatsApp, y después reconstruye todo a mano en un Excel. FieldCheck permite registrar la inspección una sola vez, en el lugar, con la evidencia asociada a cada ítem.

Detalle completo en [`docs/preentrega.md`](docs/preentrega.md).

## Documentación

| Documento | Contenido |
|---|---|
| [`docs/preentrega.md`](docs/preentrega.md) | Los 17 entregables de la preentrega: problema, usuario, requisitos, offline first, tecnologías, roles. |
| [`docs/diagramas.md`](docs/diagramas.md) | Flujo de pantallas, arquitectura (datos e infraestructura), secuencia de sincronización y modelo de datos. |
| [Figma](https://www.figma.com/design/huvTo8nDgobJskL3ysZDSj/FieldCheck-%E2%80%94-Mockups--DevApps1-) | Wireframes y mockups de alta fidelidad con los estados de cada pantalla (exportados en [`docs/diseno/`](docs/diseno/)). |
| [`docs/pantallas.md`](docs/pantallas.md) | Especificación de pantallas y estados para Figma, y criterios de accesibilidad. |
| [`CONTRIBUTING.md`](CONTRIBUTING.md) | Ramas, commits y pull requests. |

## Instrucciones de ejecución

_Se completan en la Etapa 2._ La app se va a abrir y ejecutar desde Android Studio (Android 8.0, API 26, o superior) y el backend se va a levantar con `docker compose up` desde `backend/`.

## Arquitectura

MVVM con principios de Clean Architecture, en un único módulo `app` organizado en paquetes por capa (estructura prevista para la Etapa 2):

```
UI (Compose) → ViewModel → Caso de uso → Repositorio (interfaz) → Room / Retrofit
```

```
app/src/main/java/ar/edu/uade/fieldcheck/
├── presentation/          UI y estado de pantalla
│   ├── navigation/        rutas y NavHost
│   ├── theme/             colores, tipografía
│   ├── components/        componentes compartidos (banner offline, chips, etc.)
│   └── history/ newinspection/ execution/ review/ detail/   una carpeta por pantalla (P1..P5)
├── domain/                Kotlin puro, sin Android
│   ├── model/             modelos de dominio
│   ├── repository/        interfaces de repositorio
│   └── usecase/           casos de uso con reglas de negocio
├── data/
│   ├── local/             Room (database, dao, entity) y archivos de fotos
│   ├── remote/            Retrofit (api, dto)
│   ├── repository/        implementaciones de repositorio
│   ├── mapper/            Entity ↔ Dominio ↔ DTO
│   ├── sync/              SyncWorker (WorkManager)
│   └── connectivity/      monitor de red como Flow
└── di/                    AppContainer (inyección manual)
backend/                   API FastAPI (etapa de implementación)
docs/                      documentación de la preentrega
    diseno/                wireframes, mockups y estados de pantalla
```

Estrategia **offline first**: Room es la única fuente de verdad de la UI; las inspecciones se guardan localmente y WorkManager las sincroniza al haber red, de forma idempotente por `clientUuid`. Explicación completa en `docs/preentrega.md` (punto 14) y `docs/diagramas.md`.

## Tecnologías previstas

| Área | Tecnología |
|---|---|
| Lenguaje y UI | Kotlin 2.4, Jetpack Compose (BOM 2026.09), Material 3 |
| Navegación y estado | Navigation Compose (rutas tipadas), ViewModel, StateFlow, Coroutines |
| Persistencia | Room 2.8 (KSP) |
| Red | Retrofit 3, OkHttp 5, kotlinx.serialization |
| Segundo plano | WorkManager |
| Dispositivo | Cámara del sistema (`TakePicture` + `FileProvider`), ubicación (Fused Location, opcional), hoja de compartir |
| Imágenes | Coil 3 |
| Tests | JUnit4, MockK, Turbine, kotlinx-coroutines-test, MockWebServer, Room in-memory |
| Backend | FastAPI, SQLite, Docker Compose |

Justificación de cada una en `docs/preentrega.md`, punto 15.

## Funcionalidades y requisitos funcionales

| Requisito | Descripción | Estado |
|---|---|---|
| RF01 | Crear una inspección a partir de una plantilla (plantillas cacheadas) | Pendiente |
| RF02 | Completar ítems con resultado, nota y foto, sin conexión | Pendiente |
| RF03 | Sincronización automática con estado visible y reintento | Pendiente |
| RF04 | Historial, detalle y compartir resumen | Pendiente |

## Decisiones relevantes

- **Room como única fuente de verdad:** la UI nunca lee de la red, así que funciona igual con o sin conexión.
- **Sincronización al finalizar:** una inspección finalizada es de solo lectura, lo que casi elimina los conflictos; ante un conflicto, el servidor aplica *last-write-wins*.
- **Idempotencia por `clientUuid`:** reintentar un envío nunca duplica datos en el servidor.
- **Cámara del sistema en lugar de CameraX:** menos código y sin pedir el permiso `CAMERA`.
- **Inyección de dependencias manual:** sin Hilt ni Koin, para un proyecto de este tamaño.

## Limitaciones conocidas

- Sin login ni multiusuario: la app se autentica ante la API con una API key fija.
- Las plantillas se administran en el servidor, no se editan desde la app.
- No hay exportación a PDF: el resumen se comparte como texto, sin fotos adjuntas.
- Una inspección finalizada no se puede editar.

## Equipo

| Integrante | Rol principal |
|---|---|
| Franco Bonorino | Arquitectura y persistencia |
| Mercedes Rodriguez | UI / UX |
| Nicolás Rodriguez | Capacidades del dispositivo y testing |
| Nahuel Sanguineti | Sincronización y backend |
