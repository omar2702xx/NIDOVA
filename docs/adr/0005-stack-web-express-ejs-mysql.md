# Stack web: Node.js + Express + EJS + MySQL, autohospedado

La plataforma web usa Node.js con Express, vistas EJS, sesiones con express-session, MySQL y Chart.js, y corre en la PC del laboratorio. Lo difícil del proyecto es el hardware y el experimento, no la web, así que elegimos lo más predecible para un semestre. Este stack además cumple tal cual los entregables ("HTML, CSS y JavaScript", "utiliza sesiones", herramienta CASE con MySQL Workbench). Va autohospedado porque el registro pide una base de datos local y porque el servidor ejecuta el módulo de Haskell como programa externo.

## Considered Options

- **Next.js**: se descartó porque nadie del equipo domina React, las sesiones no vienen incluidas y el JSX podría no contar como el "HTML, CSS y JavaScript" que pide la Etapa 2. Se puede reconsiderar para el tablero si sobra tiempo.
- **Despliegue en Vercel u otro servicio sin servidor fijo**: incompatible con la BD local y con ejecutar el binario de Haskell.
