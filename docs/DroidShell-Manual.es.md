# DroidShell

DroidShell es una aplicación de terminal para Android basada en Termux, con
identidad, prefijo, bootstrap y repositorio de paquetes propios.

## Estado Actual

- Package name: `com.droidshell.app`
- Nombre visible: `DroidShell`
- Prefijo: `/data/data/com.droidshell.app/files/usr`
- Home: `/data/data/com.droidshell.app/files/home`
- Repositorio apt: <https://codedev-404.github.io/termux-packages/apt/termux-droidshell/>
- Arquitectura probada: `aarch64`
- Node.js probado: `v26.4.0`
- npm probado: `11.19.0`

DroidShell puede coexistir con la aplicación Termux original porque utiliza un
package name y un directorio de datos distintos.

## Instalación

La APK arm64 actual se encuentra en la carpeta Descargas con el nombre:

`DroidShell-r2-automatico.apk`

También puede instalarse mediante adb:

```bash
adb install -r DroidShell-r2-automatico.apk
```

En el primer arranque, DroidShell instala automáticamente el bootstrap propio
y configura el repositorio apt de DroidShell. No es necesario editar
`sources.list` manualmente en instalaciones nuevas.

## Instalar Paquetes

Los comandos se ejecutan desde la terminal de DroidShell:

```bash
apt update
pkg install nodejs npm
node --version
npm --version
```

El repositorio propio contiene actualmente Node.js, npm, c-ares y sus
dependencias principales. Se irá ampliando con más paquetes compilados para
el prefijo DroidShell.

## OpenCode En Android

La versión actual de `opencode-ai` no declara compatibilidad con Android:

```text
Valid os: darwin, linux, win32
Actual os: android
```

Por eso este comando falla directamente en DroidShell:

```bash
npm install -g opencode-ai
```

El instalador oficial puede descargar un binario Linux arm64, pero ese binario
requiere el cargador glibc `/lib/ld-linux-aarch64.so.1`, que Android no
proporciona. Agregar el directorio a `PATH` no resuelve esa incompatibilidad.

### Camino Recomendado

El repositorio DroidShell ya incluye `proot`, `proot-distro`, Python y sus
dependencias. El flujo probado es ejecutar OpenCode dentro de Debian:

```bash
pkg install proot-distro
proot-distro install debian
proot-distro login debian
export DEBIAN_FRONTEND=noninteractive
apt update
apt install -y curl ca-certificates
curl -fsSL https://opencode.ai/install | bash
export PATH="$HOME/.opencode/bin:$PATH"
opencode --version
```

OpenCode se ejecuta con el cargador glibc de Debian, no directamente sobre
Android. En la prueba realizada dentro de DroidShell respondió correctamente
con la versión `1.18.18`.

Después se puede iniciar dentro de cualquier proyecto:

```bash
cd ~/mi-proyecto
opencode
```

Para configurar el proveedor de inteligencia artificial, usar `/connect`
dentro de OpenCode.

Documentación oficial: <https://opencode.ai/docs/>

## Mejoras Incluidas

### Aplicación principal

- Modo lectura para bloquear accidentalmente la entrada del terminal.
- Gesto horizontal para cambiar entre sesiones.
- Agrupación de sesiones por directorio de trabajo.
- Reverse wrap-around en el emulador.
- ECH respetando el margen derecho.
- Propiedades `session-change-swipe` y `session-drawer-grouping`.

### API

- Patrones de vibración con `.` para vibración corta y `-` para vibración
  larga.
- Grabación de vídeo desde la cámara.
- Grabación de pantalla mediante MediaProjection.

### Widgets y arranque

- Salida de tareas del widget en notificaciones.
- Logs de scripts de arranque en `~/.termux/boot/logs/`.
- Retraso y espera de red mediante `~/.termux/boot/boot.properties`.

Ejemplo de `boot.properties`:

```properties
boot-delay-ms=5000
wait-for-network=true
```

### Styling

- Importación de temas y fuentes mediante el selector de archivos Android.
- Soporte de fuentes `.ttf` y `.otf`.
- Vista previa de colores.

### proot-distro

- Comando `proot-distro update` para actualizar una imagen sin borrar el
  rootfs completo.
- Comando `proot-distro exec` para ejecutar comandos usando la configuración
  de una sesión activa.

## Repositorios

Repositorios principales del proyecto:

- App: <https://github.com/CodeDev-404/termux-app>
- Paquetes: <https://github.com/CodeDev-404/termux-packages>
- API: <https://github.com/CodeDev-404/termux-api>
- Boot: <https://github.com/CodeDev-404/termux-boot>
- Float: <https://github.com/CodeDev-404/termux-float>
- Styling: <https://github.com/CodeDev-404/termux-styling>
- Tasker: <https://github.com/CodeDev-404/termux-tasker>
- Widget: <https://github.com/CodeDev-404/termux-widget>
- X11: <https://github.com/CodeDev-404/termux-x11>
- proot-distro: <https://github.com/CodeDev-404/proot-distro>

## Compilación

La aplicación y los bootstraps se compilan principalmente mediante GitHub
Actions. El bootstrap requiere Linux, Docker, Android SDK y NDK.

El flujo general es:

1. Modificar los repositorios en una rama de trabajo.
2. Compilar el bootstrap con el workflow de termux-packages.
3. Publicar los archivos `bootstrap-*.zip` en un release.
4. Compilar la APK de DroidShell.
5. Firmar la APK con la clave privada de DroidShell.
6. Instalar y probar en Android.

## Firma

La app y todos sus plugins deben utilizar la misma clave de firma porque
comparten `sharedUserId`.

El keystore se guarda fuera del repositorio, en:

`~/droidshell-keystore/droidshell.jks`

Nunca se debe subir el keystore, sus contraseñas ni copias de la clave a
GitHub.

## Seguridad Y Mantenimiento

- No borrar los datos de DroidShell sin realizar antes un respaldo.
- No instalar plugins firmados con otra clave.
- Mantener actualizado el bootstrap y el repositorio apt propio.
- Revisar los scripts antes de ejecutar paquetes de terceros.
- Los cambios del proyecto se trabajan en ramas independientes.

## Licencia

DroidShell está basado en código de Termux y conserva las obligaciones de las
licencias de los componentes originales, principalmente GPLv3. Las licencias
de cada componente deben mantenerse al redistribuir la aplicación.
