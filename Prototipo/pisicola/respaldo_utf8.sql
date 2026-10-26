--
-- PostgreSQL database dump
--

\restrict 3PE2gbUSUDbdW4rt7ccj7J5b2wVJnQ0uydTinjn0E9oMA6Zk3inwMfZ2nW2gqiV

-- Dumped from database version 18.6
-- Dumped by pg_dump version 18.6

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: alerta; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.alerta (
    id_alerta integer NOT NULL,
    id_lectura integer NOT NULL,
    tipo_alerta character varying(50) NOT NULL,
    mensaje_enviado text NOT NULL,
    fecha_emision timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.alerta OWNER TO postgres;

--
-- Name: alerta_id_alerta_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.alerta_id_alerta_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.alerta_id_alerta_seq OWNER TO postgres;

--
-- Name: alerta_id_alerta_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.alerta_id_alerta_seq OWNED BY public.alerta.id_alerta;


--
-- Name: lectura; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.lectura (
    id_lectura integer NOT NULL,
    id_sensor integer NOT NULL,
    id_parametro integer NOT NULL,
    valor numeric(8,2) NOT NULL,
    fecha_hora timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.lectura OWNER TO postgres;

--
-- Name: lectura_id_lectura_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.lectura_id_lectura_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.lectura_id_lectura_seq OWNER TO postgres;

--
-- Name: lectura_id_lectura_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.lectura_id_lectura_seq OWNED BY public.lectura.id_lectura;


--
-- Name: mantenimiento; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.mantenimiento (
    id_mantenimiento integer NOT NULL,
    tipo_actividad character varying(50) NOT NULL,
    fecha_programada date NOT NULL,
    estado character varying(20) DEFAULT 'Pendiente'::character varying,
    observaciones text
);


ALTER TABLE public.mantenimiento OWNER TO postgres;

--
-- Name: mantenimiento_id_mantenimiento_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.mantenimiento_id_mantenimiento_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.mantenimiento_id_mantenimiento_seq OWNER TO postgres;

--
-- Name: mantenimiento_id_mantenimiento_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.mantenimiento_id_mantenimiento_seq OWNED BY public.mantenimiento.id_mantenimiento;


--
-- Name: parametro; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.parametro (
    id_parametro integer NOT NULL,
    nombre character varying(50) NOT NULL,
    tipo_medicion character varying(20) NOT NULL,
    umbral_min numeric(5,2),
    umbral_max numeric(5,2)
);


ALTER TABLE public.parametro OWNER TO postgres;

--
-- Name: parametro_id_parametro_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.parametro_id_parametro_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.parametro_id_parametro_seq OWNER TO postgres;

--
-- Name: parametro_id_parametro_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.parametro_id_parametro_seq OWNED BY public.parametro.id_parametro;


--
-- Name: sensor; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sensor (
    id_sensor integer NOT NULL,
    ubicacion character varying(50) NOT NULL,
    descripcion character varying(255)
);


ALTER TABLE public.sensor OWNER TO postgres;

--
-- Name: sensor_id_sensor_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.sensor_id_sensor_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.sensor_id_sensor_seq OWNER TO postgres;

--
-- Name: sensor_id_sensor_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.sensor_id_sensor_seq OWNED BY public.sensor.id_sensor;


--
-- Name: alerta id_alerta; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.alerta ALTER COLUMN id_alerta SET DEFAULT nextval('public.alerta_id_alerta_seq'::regclass);


--
-- Name: lectura id_lectura; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.lectura ALTER COLUMN id_lectura SET DEFAULT nextval('public.lectura_id_lectura_seq'::regclass);


--
-- Name: mantenimiento id_mantenimiento; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.mantenimiento ALTER COLUMN id_mantenimiento SET DEFAULT nextval('public.mantenimiento_id_mantenimiento_seq'::regclass);


--
-- Name: parametro id_parametro; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.parametro ALTER COLUMN id_parametro SET DEFAULT nextval('public.parametro_id_parametro_seq'::regclass);


--
-- Name: sensor id_sensor; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sensor ALTER COLUMN id_sensor SET DEFAULT nextval('public.sensor_id_sensor_seq'::regclass);


--
-- Data for Name: alerta; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.alerta (id_alerta, id_lectura, tipo_alerta, mensaje_enviado, fecha_emision) FROM stdin;
1	2	Umbral Riesgoso	ALERTA SMS: El pH (17.0) está fuera del límite seguro (6.5 - 8.5).	2026-10-25 16:26:17.835362
2	3	Umbral Riesgoso	ALERTA SMS: El pH (6.0) está fuera del límite seguro (6.5 - 8.5).	2026-10-25 16:27:34.542095
3	5	Umbral Riesgoso	ALERTA SMS: El Temperatura (1.0) está fuera del límite seguro (24.0 - 30.0).	2026-10-25 16:30:19.681676
4	7	Umbral Riesgoso	ALERTA SMS: El pH (9.0) está fuera del límite seguro (6.5 - 8.5).	2026-10-25 16:41:35.012088
5	8	Umbral Riesgoso	ALERTA SMS: El pH (3.0) está fuera del límite seguro (6.5 - 8.5).	2026-10-25 16:54:05.97312
\.


--
-- Data for Name: lectura; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.lectura (id_lectura, id_sensor, id_parametro, valor, fecha_hora) FROM stdin;
1	1	1	8.00	2026-10-25 16:12:08.284975
2	1	1	17.00	2026-10-25 16:26:17.767765
3	1	1	6.00	2026-10-25 16:27:34.485953
4	1	1	7.00	2026-10-25 16:27:45.555011
5	1	2	1.00	2026-10-25 16:30:19.533133
6	2	1	7.00	2026-10-25 16:41:13.110435
7	2	1	9.00	2026-10-25 16:41:34.976054
8	1	1	3.00	2026-10-25 16:54:05.894965
\.


--
-- Data for Name: mantenimiento; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.mantenimiento (id_mantenimiento, tipo_actividad, fecha_programada, estado, observaciones) FROM stdin;
1	Limpieza	2026-10-26	Pendiente	
\.


--
-- Data for Name: parametro; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.parametro (id_parametro, nombre, tipo_medicion, umbral_min, umbral_max) FROM stdin;
1	pH	quimico	6.50	8.50
2	Temperatura	fisico	24.00	30.00
\.


--
-- Data for Name: sensor; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.sensor (id_sensor, ubicacion, descripcion) FROM stdin;
1	Estanque 1	\N
2	Estanque 2	\N
\.


--
-- Name: alerta_id_alerta_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.alerta_id_alerta_seq', 5, true);


--
-- Name: lectura_id_lectura_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.lectura_id_lectura_seq', 8, true);


--
-- Name: mantenimiento_id_mantenimiento_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.mantenimiento_id_mantenimiento_seq', 1, true);


--
-- Name: parametro_id_parametro_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.parametro_id_parametro_seq', 2, true);


--
-- Name: sensor_id_sensor_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.sensor_id_sensor_seq', 2, true);


--
-- Name: alerta alerta_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.alerta
    ADD CONSTRAINT alerta_pkey PRIMARY KEY (id_alerta);


--
-- Name: lectura lectura_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.lectura
    ADD CONSTRAINT lectura_pkey PRIMARY KEY (id_lectura);


--
-- Name: mantenimiento mantenimiento_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.mantenimiento
    ADD CONSTRAINT mantenimiento_pkey PRIMARY KEY (id_mantenimiento);


--
-- Name: parametro parametro_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.parametro
    ADD CONSTRAINT parametro_pkey PRIMARY KEY (id_parametro);


--
-- Name: sensor sensor_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sensor
    ADD CONSTRAINT sensor_pkey PRIMARY KEY (id_sensor);


--
-- Name: alerta alerta_id_lectura_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.alerta
    ADD CONSTRAINT alerta_id_lectura_fkey FOREIGN KEY (id_lectura) REFERENCES public.lectura(id_lectura);


--
-- Name: lectura lectura_id_parametro_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.lectura
    ADD CONSTRAINT lectura_id_parametro_fkey FOREIGN KEY (id_parametro) REFERENCES public.parametro(id_parametro);


--
-- Name: lectura lectura_id_sensor_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.lectura
    ADD CONSTRAINT lectura_id_sensor_fkey FOREIGN KEY (id_sensor) REFERENCES public.sensor(id_sensor);


--
-- PostgreSQL database dump complete
--

\unrestrict 3PE2gbUSUDbdW4rt7ccj7J5b2wVJnQ0uydTinjn0E9oMA6Zk3inwMfZ2nW2gqiV

