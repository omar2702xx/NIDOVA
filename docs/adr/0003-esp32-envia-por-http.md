# El ESP32 envía las lecturas por HTTP, sin MQTT

El microcontrolador es un ESP32 que envía lecturas y eventos de microaspersión al servidor por HTTP a través de Wi-Fi. MQTT es el protocolo "típico" de IoT, pero agrega un broker que el equipo tendría que instalar, operar y aprender durante un solo semestre. HTTP se depura con las mismas herramientas que el backend web.

## Considered Options

- **MQTT con broker (Mosquitto)**: se descartó por la complejidad adicional; se puede reconsiderar si el sistema pasa a campo con varias estaciones.
- **Arduino por USB/serial con programa puente**: se descartó porque ata la estación físicamente a la PC.
