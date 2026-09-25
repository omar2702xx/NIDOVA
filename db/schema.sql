-- NIDOVA - Esquema de base de datos (MySQL 8.0)
-- Vocabulario: ver CONTEXT.md. Decisiones: ver docs/adr/.

CREATE DATABASE IF NOT EXISTS nidova
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

USE nidova;

-- ---------------------------------------------------------------
-- Personas
-- ---------------------------------------------------------------

CREATE TABLE administrador (
  id               INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  nombre           VARCHAR(100) NOT NULL,
  correo           VARCHAR(150) NOT NULL,
  contrasena_hash  VARCHAR(255) NOT NULL,
  activo           BOOLEAN      NOT NULL DEFAULT TRUE,
  creado_por       INT UNSIGNED NULL,          -- NULL solo para el primer administrador
  creado_en        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uq_administrador_correo UNIQUE (correo),
  CONSTRAINT fk_administrador_creado_por
    FOREIGN KEY (creado_por) REFERENCES administrador (id)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------
-- Estaciones y nidos
-- ---------------------------------------------------------------

CREATE TABLE estacion (
  id           INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  nombre       VARCHAR(100) NOT NULL,
  descripcion  VARCHAR(255) NULL,
  token_hash   CHAR(64)     NOT NULL,          -- SHA-256 del token con el que se autentica el ESP32
  creado_en    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uq_estacion_nombre UNIQUE (nombre)
) ENGINE = InnoDB;

CREATE TABLE nido (
  id                     INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  estacion_id            INT UNSIGNED     NOT NULL,
  nombre                 VARCHAR(50)      NOT NULL,
  profundidad_sensor_cm  DECIMAL(4,1)     NOT NULL,   -- profundidad de los huevos
  creado_en              DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uq_nido_estacion_nombre UNIQUE (estacion_id, nombre),
  CONSTRAINT fk_nido_estacion
    FOREIGN KEY (estacion_id) REFERENCES estacion (id),
  CONSTRAINT ck_nido_profundidad CHECK (profundidad_sensor_cm > 0)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------
-- Experimentos (ADR-0006)
-- ---------------------------------------------------------------

CREATE TABLE experimento (
  id                  INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  estacion_id         INT UNSIGNED      NOT NULL,
  nombre              VARCHAR(100)      NOT NULL,
  descripcion         VARCHAR(500)      NULL,
  inicio              DATETIME          NOT NULL,
  fin                 DATETIME          NULL,          -- NULL = experimento activo
  creado_por          INT UNSIGNED      NOT NULL,

  -- Configuración: se fija al iniciar y no cambia mientras está activo
  umbral_alerta_c     DECIMAL(4,1)      NOT NULL DEFAULT 32.0,
  umbral_riesgo_c     DECIMAL(4,1)      NOT NULL DEFAULT 34.0,
  duracion_pulso_s    SMALLINT UNSIGNED NOT NULL DEFAULT 10,
  tiempo_reposo_s     SMALLINT UNSIGNED NOT NULL DEFAULT 300,
  limite_pulsos_hora  TINYINT UNSIGNED  NOT NULL DEFAULT 6,

  -- Máximo un experimento activo por estación: esta columna vale
  -- estacion_id mientras fin es NULL y NULL al cerrarse; el índice
  -- único ignora los NULL.
  estacion_activa_id  INT UNSIGNED
    AS (IF(fin IS NULL, estacion_id, NULL)) STORED,

  CONSTRAINT uq_experimento_activo UNIQUE (estacion_activa_id),
  CONSTRAINT fk_experimento_estacion
    FOREIGN KEY (estacion_id) REFERENCES estacion (id),
  CONSTRAINT fk_experimento_creado_por
    FOREIGN KEY (creado_por) REFERENCES administrador (id),
  CONSTRAINT ck_experimento_fechas     CHECK (fin IS NULL OR fin > inicio),
  CONSTRAINT ck_experimento_umbrales   CHECK (umbral_alerta_c < umbral_riesgo_c),
  CONSTRAINT ck_experimento_pulso      CHECK (duracion_pulso_s > 0),
  CONSTRAINT ck_experimento_limite     CHECK (limite_pulsos_hora > 0)
) ENGINE = InnoDB;

-- Rol de cada nido dentro de un experimento
CREATE TABLE experimento_nido (
  experimento_id  INT UNSIGNED               NOT NULL,
  nido_id         INT UNSIGNED               NOT NULL,
  rol             ENUM('control', 'tratado') NOT NULL,
  PRIMARY KEY (experimento_id, nido_id),
  CONSTRAINT fk_experimento_nido_experimento
    FOREIGN KEY (experimento_id) REFERENCES experimento (id),
  CONSTRAINT fk_experimento_nido_nido
    FOREIGN KEY (nido_id) REFERENCES nido (id)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------
-- Lecturas (cada 30 s, con la hora del ESP32)
-- ---------------------------------------------------------------

CREATE TABLE lectura_nido (
  id                  BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  nido_id             INT UNSIGNED  NOT NULL,
  medido_en           DATETIME      NOT NULL,   -- hora del ESP32 (NTP)
  recibido_en         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  temp_arena_c        DECIMAL(5,2)  NULL,       -- NULL si el sensor no respondió
  temp_superficial_c  DECIMAL(5,2)  NULL,
  humedad_arena_pct   DECIMAL(5,2)  NULL,
  estado_termico      ENUM('normal', 'alerta', 'riesgo', 'sin_datos') NOT NULL,
  -- Evita duplicados cuando el ESP32 reenvía su búfer tras una caída de red
  CONSTRAINT uq_lectura_nido UNIQUE (nido_id, medido_en),
  CONSTRAINT fk_lectura_nido_nido
    FOREIGN KEY (nido_id) REFERENCES nido (id)
) ENGINE = InnoDB;

CREATE TABLE lectura_estacion (
  id                 BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  estacion_id        INT UNSIGNED  NOT NULL,
  medido_en          DATETIME      NOT NULL,
  recibido_en        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  temp_ambiental_c   DECIMAL(5,2)  NULL,
  humedad_ambiental_pct DECIMAL(5,2) NULL,     -- humedad relativa del aire
  radiacion_uv       DECIMAL(5,2)  NULL,        -- índice UV
  CONSTRAINT uq_lectura_estacion UNIQUE (estacion_id, medido_en),
  CONSTRAINT fk_lectura_estacion_estacion
    FOREIGN KEY (estacion_id) REFERENCES estacion (id)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------
-- Microaspersión
-- ---------------------------------------------------------------

CREATE TABLE pulso (
  id                    BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  experimento_id        INT UNSIGNED      NOT NULL,
  nido_id               INT UNSIGNED      NOT NULL,   -- siempre un nido tratado
  inicio                DATETIME          NOT NULL,   -- hora del ESP32
  duracion_s            SMALLINT UNSIGNED NOT NULL,
  temp_arena_inicio_c   DECIMAL(5,2)      NOT NULL,
  recibido_en           DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uq_pulso UNIQUE (nido_id, inicio),
  CONSTRAINT fk_pulso_experimento_nido
    FOREIGN KEY (experimento_id, nido_id)
    REFERENCES experimento_nido (experimento_id, nido_id)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------
-- Predicción de Riesgo (ADR-0004, solo informativa)
-- ---------------------------------------------------------------

CREATE TABLE prediccion_riesgo (
  id               BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  nido_id          INT UNSIGNED      NOT NULL,
  calculado_en     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP,
  horizonte_min    SMALLINT UNSIGNED NOT NULL,
  probabilidad     DECIMAL(5,4)      NOT NULL,   -- 0.0000 a 1.0000
  version_modelo   VARCHAR(30)       NOT NULL,
  CONSTRAINT fk_prediccion_nido
    FOREIGN KEY (nido_id) REFERENCES nido (id),
  CONSTRAINT ck_prediccion_probabilidad CHECK (probabilidad BETWEEN 0 AND 1)
) ENGINE = InnoDB;

CREATE INDEX ix_prediccion_nido_fecha ON prediccion_riesgo (nido_id, calculado_en);
