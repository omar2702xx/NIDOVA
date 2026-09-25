# La eficacia se calcula por Experimento, no por nido

El rol de control o tratado no es un atributo fijo del nido: pertenece a un Experimento, que es un periodo con inicio y fin en una estación. Así se puede reemplazar un sensor, intercambiar nidos o comparar configuraciones (por ejemplo, pulsos de 10 s contra 20 s) sin mezclar datos que no son comparables. Todo cálculo de eficacia, incluido el Tiempo en Riesgo, se hace dentro de un experimento.

## Consequences

- La configuración (umbrales, pulso, reposo y límite de pulsos) se fija al iniciar el experimento y no se edita mientras está activo. Para cambiarla, se cierra el experimento y se abre otro.
- Una estación tiene como máximo un experimento activo. Sin experimento activo no hay microaspersión, pero las lecturas se siguen guardando.

## Considered Options

- **Rol fijo por nido**: más simple, pero cualquier cambio a mitad del semestre obligaba a dar de alta nidos "nuevos" y rompía la comparación histórica.
