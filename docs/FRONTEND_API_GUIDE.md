# Guía de integración del frontend

Este documento describe el contrato actual de la API para el cliente web. Reemplaza
`https://<dominio-de-la-api>` con el dominio HTTPS de producción.

```text
API_BASE_URL=https://<dominio-de-la-api>
```

Todas las peticiones y respuestas usan JSON, excepto logout, que responde sin cuerpo.

## Endpoints

| Método | Ruta | Autenticación | Descripción |
|---|---|---|---|
| `POST` | `/api/jugadores` | Pública | Registrar jugador |
| `POST` | `/api/auth/login` | Pública | Iniciar sesión |
| `POST` | `/api/auth/refresh` | Cookie de refresh | Renovar access token y rotar refresh cookie |
| `POST` | `/api/auth/logout` | Cookie de refresh | Cerrar sesión y revocar refresh token |
| `GET` | `/api/jugadores?page=0&size=20` | Bearer JWT | Consultar ranking paginado |
| `GET` | `/api/jugadores/{id}` | Bearer JWT | Consultar perfil público |
| `GET` | `/api/jugadores/{id}/partidas` | Bearer JWT | Consultar las últimas 10 partidas |
| `PATCH` | `/api/jugadores/{id}/alias` | Bearer JWT del mismo jugador | Cambiar alias propio |
| `PATCH` | `/api/jugadores/{id}/contrasena` | Bearer JWT del mismo jugador | Cambiar contraseña propia |
| `POST` | `/api/partidas` | Bearer JWT | Guardar partida del jugador autenticado |

### Registrar jugador

`POST /api/jugadores`

```json
{
  "alias": "jugador1",
  "password": "una-clave-segura"
}
```

Responde `201 Created` con perfil (`id`, `alias`, `record`, `fechaRegistro`). La
contraseña nunca se devuelve. El alias debe tener hasta 80 caracteres y no estar
registrado.

### Iniciar sesión

`POST /api/auth/login`

```json
{
  "alias": "jugador1",
  "password": "una-clave-segura"
}
```

Responde `200 OK`, establece la cookie HttpOnly `refresh_token` y devuelve:

```json
{
  "accessToken": "<jwt>",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "jugador": {
    "id": 1,
    "alias": "jugador1",
    "record": null,
    "fechaRegistro": "2026-11-11T09:00:00"
  }
}
```

`expiresIn` está expresado en segundos. Mantén el access token en memoria cuando
sea posible; no guardes contraseñas ni refresh tokens en `localStorage`. El refresh
token es HttpOnly y JavaScript no debe intentar leerlo.

### Renovar sesión

`POST /api/auth/refresh`, sin body. Envía la cookie automáticamente mediante
`credentials: "include"`. Responde con el mismo formato del login, emite un nuevo
access token y rota la cookie de refresh.

El access token dura 15 minutos. El refresh puede continuar renovándose hasta 30
días de inactividad, con un máximo absoluto de 90 días desde el login. Después de
esos límites hay que iniciar sesión de nuevo.

**Importante:** debido a la rotación, serializa las renovaciones. Si varias
peticiones reciben `401` a la vez, comparte una sola promesa de refresh; no mandes
varias peticiones `/refresh` en paralelo. Reintenta la petición original como máximo
una vez con el access token nuevo. Si el refresh falla, borra el access token local
y solicita login.

Cambiar la contraseña revoca las sesiones de refresh de esa cuenta. El access
token existente puede seguir siendo válido hasta que venza, como máximo 15 minutos.

### Cerrar sesión

`POST /api/auth/logout`, sin body. Envía la cookie con `credentials: "include"`.
Responde `204 No Content`; elimina la cookie y revoca el refresh token actual.
Después, borra también el access token guardado en memoria en el cliente.

### Autenticación de rutas protegidas

Envía el access token en cada llamada protegida:

```http
Authorization: Bearer <accessToken>
```

Un token ausente, inválido o expirado produce `401 Unauthorized`. Los cambios de
alias y contraseña solo se permiten cuando el `{id}` coincide con el propietario
del token; de lo contrario la respuesta es `403 Forbidden`.

### Ranking y perfiles

`GET /api/jugadores?page=0&size=20` responde con una página de Spring que incluye
`content`, `totalElements`, `totalPages`, `number`, `size` y metadatos de página.
La página empieza en cero; `size` predeterminado es 20 y el máximo aplicado es 50.
El ranking ordena récord descendente; los jugadores sin récord quedan al final.

Cada elemento de `content` tiene esta forma:

```json
{
  "id": 1,
  "alias": "jugador1",
  "record": 1200,
  "fechaRegistro": "2026-11-11T09:00:00"
}
```

`GET /api/jugadores/{id}` devuelve el mismo perfil. `GET
/api/jugadores/{id}/partidas` devuelve un arreglo con hasta 10 partidas, de la más
reciente a la más antigua:

```json
[
  {
    "id": 27,
    "nombreJugador": "jugador1",
    "puntuacion": 1200,
    "fecha": "2026-11-11T10:00:00"
  }
]
```

### Cambiar alias

`PATCH /api/jugadores/{id}/alias`

```json
{
  "alias": "nuevoAlias"
}
```

El alias debe ser distinto al actual y no pertenecer a otro jugador.

### Cambiar contraseña

`PATCH /api/jugadores/{id}/contrasena`

```json
{
  "contrasena": "otra-clave-segura"
}
```

La nueva contraseña debe diferir de la actual. El servidor aplica BCrypt y revoca
las sesiones de refresh de la cuenta.

### Guardar partida y detectar récord

`POST /api/partidas`

```json
{
  "puntuacion": 1200
}
```

No mandes `jugadorId`: el servidor obtiene el jugador del JWT. La puntuación debe
ser un entero igual o mayor que cero. Responde `200 OK`:

```json
{
  "partida": {
    "id": 27,
    "nombreJugador": "jugador1",
    "puntuacion": 1200,
    "fecha": "2026-11-11T10:00:00"
  },
  "nuevoRecord": true,
  "recordAnterior": 950,
  "recordActual": 1200
}
```

Muestra la animación de récord cuando `nuevoRecord` sea `true`.

## Errores y mensajes para el usuario

Los errores procesados por el handler global se devuelven como Problem Details.
Usa `title` como título visible y `detail` como subtítulo; nunca muestres stack
traces ni el objeto completo como texto:

```json
{
  "type": "about:blank",
  "title": "Alias no disponible",
  "status": 409,
  "detail": "El alias ya está registrado por otro jugador."
}
```

Los campos más útiles para el frontend son `title`, `detail` y `status`. Algunos
ejemplos:

| Estado | Título común | Cuándo ocurre |
|---|---|---|
| `400` | `Error de validación` / `Solicitud inválida` / `Alias sin cambios` | Body inválido, datos incorrectos o alias igual al actual |
| `401` | `Credenciales inválidas` / `Sesión vencida` | Login incorrecto o refresh inválido/expirado/reutilizado |
| `403` | `Acceso denegado` | Intento de cambiar la cuenta de otro jugador |
| `404` | `Jugador no encontrado` | ID de jugador inexistente |
| `409` | `Alias no disponible` | Alias ya registrado |

Las respuestas de autenticación `401` generadas directamente por Spring Security
(por ejemplo, un Bearer expirado antes de llegar al controlador) pueden no tener
el mismo formato Problem Details. El cliente debe usar un mensaje fallback si
faltan `title` o `detail`, por ejemplo: “La sesión venció. Intenta continuar de
nuevo.”

No asumas que toda respuesta de error contiene JSON. Comprueba el status y analiza
el body de forma segura; ante un error de red o respuesta vacía, muestra un mensaje
genérico y conserva los detalles técnicos solo en logs de desarrollo.

## Estructura sugerida para el frontend

Centraliza el contrato y el transporte HTTP para evitar URLs y manejo de errores
duplicados por pantalla. Por ejemplo:

```text
src/
  api/
    httpClient.ts       # base URL, JSON, Bearer, errores y refresh único
    authApi.ts          # register, login, refresh, logout
    playersApi.ts       # ranking, perfil, alias, contraseña, partidas recientes
    gamesApi.ts         # guardar puntuación
    apiErrors.ts        # parseo de title/detail/status y error tipado
  auth/
    authStore.ts        # access token y perfil en memoria
    useAuth.ts          # estado/acciones de sesión para la UI
```

`httpClient` debe añadir `Authorization` a rutas protegidas, usar
`credentials: "include"` en login/refresh/logout y coordinar un refresh compartido
para evitar carreras. Si la renovación devuelve `401`, limpia la sesión y dirige al
login. Las pantallas deben consumir las funciones de `api/`, no llamar
`fetch` directamente.

## Requisito de despliegue cross-origin

Si el frontend está en otro origen, el backend debe permitir explícitamente el
origen HTTPS del frontend en CORS y habilitar credenciales; no se puede usar
`Access-Control-Allow-Origin: *` junto con cookies. La cookie actual es
`HttpOnly`, `SameSite=Strict`, `Secure` y tiene path `/api/auth`. Los subdominios
bajo el mismo sitio normalmente son same-site, pero siguen siendo distintos
orígenes para CORS. Si frontend y API están en sitios distintos, la política
`SameSite=Strict` puede impedir que el navegador envíe la cookie; coordina una
configuración de cookie/CORS segura con el backend antes de integrar.
