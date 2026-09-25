# NIDOVA

Prototipo experimental que monitorea las condiciones de incubación de nidos modelo de tortuga golfina y mitiga el riesgo térmico mediante microaspersión, para el Campamento Tortugero Don Manuel Orantes.

## Language

### Nidos

**Estación**:
Conjunto de nidos colocados bajo las mismas condiciones, junto con los sensores ambientales que comparten.
_Avoid_: Entorno, módulo, sitio

**Nido**:
Modelo físico de nido de tortuga golfina, instrumentado con sensores, dentro del entorno de pruebas. Nunca es un nido real.
_Avoid_: Nido real, nidada

**Experimento**:
Periodo con inicio y fin en una estación durante el cual se fijan la configuración y qué nido es control y cuál es tratado; la eficacia siempre se calcula por experimento. Una estación tiene como máximo un experimento activo.
_Avoid_: Prueba, corrida, ensayo

**Configuración**:
Conjunto de umbrales del estado térmico, duración del pulso, tiempo de reposo y límite de pulsos con el que opera un experimento; no cambia mientras el experimento está activo.
_Avoid_: Parámetros, ajustes, settings

**Rol**:
Papel de un nido dentro de un experimento: control o tratado. Un mismo nido puede tener roles distintos en experimentos distintos.
_Avoid_: Tipo de nido

**Nido control**:
Nido cuyo rol en un experimento es no recibir nunca microaspersión; sirve como referencia para medir la eficacia.
_Avoid_: Nido testigo, nido sin tratamiento

**Nido tratado**:
Nido cuyo rol en un experimento es recibir microaspersión cuando entra en riesgo térmico.
_Avoid_: Nido con riego, nido experimental

### Variables

**Temperatura de arena**:
Temperatura medida a la profundidad de los huevos dentro del nido; es la única variable que determina el estado térmico.
_Avoid_: Temperatura del nido, temperatura profunda, temperatura

**Temperatura superficial**:
Temperatura medida en la capa superior de la arena del nido; se registra para análisis pero nunca determina el estado térmico.
_Avoid_: Temperatura de arena (para referirse a esta)

**Humedad de arena**:
Contenido de humedad de la arena del nido; permite comprobar que la microaspersión realmente mojó el nido.
_Avoid_: Humedad (a secas), humedad del suelo

**Temperatura ambiental**:
Temperatura del aire en la estación; es la misma para todos sus nidos.
_Avoid_: Temperatura exterior, clima

**Humedad ambiental**:
Humedad relativa del aire en la estación; es la misma para todos sus nidos.
_Avoid_: Humedad (a secas), humedad del aire

**Radiación UV**:
Intensidad de radiación ultravioleta que recibe la estación; es la misma para todos sus nidos.
_Avoid_: Luz, radiación solar

**Lectura de nido**:
Medición de la temperatura de arena, la temperatura superficial y la humedad de arena de un nido en un instante, con la hora del microcontrolador.
_Avoid_: Registro, dato, muestra

**Lectura de estación**:
Medición de la temperatura ambiental, la humedad ambiental y la radiación UV de una estación en un instante, con la hora del microcontrolador.
_Avoid_: Lectura ambiental, registro, dato

### Riesgo térmico

**Estado térmico**:
Clasificación de un nido según su temperatura de arena: Normal (< 32 °C), Alerta (32–34 °C), Riesgo (≥ 34 °C) o Sin datos (lectura fuera del rango físico posible, por falla de sensor).
_Avoid_: Nivel de alerta, semáforo

**Riesgo térmico**:
Estado térmico en el que la temperatura de arena es ≥ 34 °C y los embriones sufren estrés por calor.
_Avoid_: Sobrecalentamiento, alerta

**Microaspersión**:
Riego automatizado de agua pulverizada sobre un nido tratado para bajar su temperatura de arena; ocurre siempre en pulsos y solo durante un experimento activo, nunca en estado Sin datos.
_Avoid_: Riego, aspersión, rociado

**Pulso**:
Una activación de la microaspersión con duración fija configurable.
_Avoid_: Disparo, ciclo, riego

**Tiempo de reposo**:
Espera mínima después de un pulso antes de volver a evaluar el estado térmico y permitir otro pulso, para que el agua se filtre en la arena.
_Avoid_: Cooldown, pausa, espera

**Límite de pulsos**:
Número máximo de pulsos que un nido tratado puede recibir por hora, para no inundarlo ni enfriarlo de más.
_Avoid_: Tope, máximo de riegos

**Tiempo en Riesgo**:
Minutos que un nido pasó en Riesgo térmico durante un experimento; es el indicador principal de eficacia al comparar el nido tratado contra el nido control.
_Avoid_: Eficacia (a secas), exposición

**Predicción de Riesgo**:
Estimación de que un nido entrará en Riesgo térmico dentro de un horizonte de tiempo, calculada a partir de las variables de la estación y la tendencia de la temperatura de arena. Es solo informativa: nunca activa la microaspersión.
_Avoid_: Pronóstico, alerta temprana

### Personas

**Visitante**:
Cualquier persona sin cuenta que consulta el tablero público en modo de solo lectura.
_Avoid_: Usuario, invitado, público

**Administrador**:
Persona del campamento o del equipo con sesión iniciada que gestiona estaciones, nidos y experimentos, revisa el historial de pulsos y exporta datos. Solo otro administrador puede darle de alta.
_Avoid_: Admin, operador, usuario

**Campamento**:
El Campamento Tortugero Don Manuel Orantes, cliente del proyecto.
_Avoid_: Cliente, tortuguero
