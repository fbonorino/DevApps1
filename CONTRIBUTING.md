# Cómo trabajamos

La cátedra evalúa la evolución real del repositorio. La regla general es **commits chicos, frecuentes y de todos los integrantes**, no un volcado al final.

## Ramas

| Rama | Uso |
|---|---|
| `main` | Siempre compila y tiene solo trabajo revisado. No se commitea directo desde que arranca la implementación. |
| `feature/<rf>-<descripcion>` | Funcionalidad nueva. Ej.: `feature/rf02-completar-items`. |
| `fix/<descripcion>` | Corrección de un error. Ej.: `fix/sync-duplicados`. |
| `docs/<descripcion>` | Solo documentación. Ej.: `docs/diagrama-secuencia`. |
| `test/<descripcion>` | Solo tests. Ej.: `test/finalize-use-case`. |

Flujo:

```bash
git switch main && git pull
git switch -c feature/rf02-completar-items
# ...commits chicos...
git push -u origin feature/rf02-completar-items
# abrir el PR en GitHub
```

Las ramas se borran después del merge.

## Commits

Formato [Conventional Commits](https://www.conventionalcommits.org/es/), en español y en imperativo:

```
tipo(alcance): descripción corta

Cuerpo opcional: por qué se hizo el cambio, no qué líneas cambiaron.
```

| Tipo | Cuándo |
|---|---|
| `feat` | Funcionalidad nueva |
| `fix` | Corrección de un error |
| `test` | Agregar o corregir tests |
| `refactor` | Cambio interno sin cambiar comportamiento |
| `docs` | Documentación |
| `build` | Gradle, dependencias, versiones |
| `chore` | Mantenimiento (estructura, configuración) |

Alcances sugeridos: `ui`, `domain`, `data`, `room`, `api`, `sync`, `backend`, `docs`.

Ejemplos:

```
feat(domain): validar evidencia en ítems "No cumple" al finalizar
test(sync): verificar que un reintento no duplica la inspección
fix(ui): mantener el ítem actual al rotar la pantalla
```

Cada commit hace **una sola cosa** y deja el proyecto compilando.

## Pull requests

- Todo entra a `main` por PR, con **al menos una aprobación de otro integrante**.
- Título con el mismo formato que los commits.
- La descripción sigue la plantilla (`.github/pull_request_template.md`): qué cambia, qué requisito cubre y cómo se probó.
- PR chicos (idealmente menos de 400 líneas). Si crece, se divide.
- Se integra con **"Create a merge commit"** (no *squash*) para conservar los commits de cada integrante.
- Antes de pedir revisión: `./gradlew assembleDebug testDebugUnitTest` sin errores.

## Uso de IA

Se permite (sección 8 de la consigna). Antes de incorporar código asistido, quien lo sube tiene que poder responder: qué problema resuelve, qué capa toca, qué dependencias agrega, si hay una alternativa más simple, cómo se comprueba que funciona y qué pasa si falla.
