# El microcontrolador decide la microaspersión, no el servidor

El microcontrolador lee la temperatura de arena, calcula el estado térmico y activa la microaspersión por su cuenta; el servidor solo recibe lecturas y eventos, los guarda, los grafica y le envía umbrales configurados. Así el nido tratado sigue protegido aunque se caiga el Wi-Fi, la PC o la plataforma web.

## Considered Options

- **El servidor decide y envía la orden de regreso**: se descartó porque cualquier fallo de red o de servidor dejaría el nido sin mitigación, justo cuando más calor hace.
