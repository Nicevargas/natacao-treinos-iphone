-- ==============================================================================
-- APP NATAÇÃO CRIATIVA - DENÚNCIA DE NOME NO RANKING
-- Arquivo: 20261009000001_denuncia_no_ranking.sql
--
-- A Apple (diretriz 1.2) exige, em app que mostra conteúdo de usuários, um jeito
-- de denunciar e de bloquear. No ranking o conteúdo é o nome que cada um escolhe.
--
-- - denunciar_nome_do_ranking(nome, motivo): grava a denúncia. O app não recebe
--   o id de ninguém: a função acha a pessoa pelo nome mostrado.
-- - ranking_nadadores(): igual à versão de 15/09, com uma linha a mais: quem
--   denuncia deixa de ver o nome denunciado.
-- - A tabela não é lida pela API. Quem modera olha no painel do Supabase
--   (Table Editor > denuncias_do_ranking) e, se for o caso, tira a pessoa do
--   ranking: UPDATE profiles SET ranking_publico = FALSE WHERE id = '<denunciado>';
--
-- Transação única com conferência: se algo falhar, nada muda.
-- ==============================================================================

BEGIN;

-- ------------------------------------------------------------------------------
-- 1. AS DENÚNCIAS
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.denuncias_do_ranking (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    denunciante UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    denunciado UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    -- O nome como estava na hora: a pessoa pode trocar depois.
    nome TEXT NOT NULL,
    motivo TEXT NOT NULL CHECK (motivo IN ('ofensivo', 'se_passa_por_outro', 'outro')),
    criada_em TIMESTAMPTZ NOT NULL DEFAULT now(),
    -- Preenchido à mão por quem modera, depois de olhar.
    resolvida_em TIMESTAMPTZ,
    CONSTRAINT denuncia_unica_por_par UNIQUE (denunciante, denunciado),
    CONSTRAINT denuncia_nao_e_de_si CHECK (denunciante <> denunciado)
);

CREATE INDEX IF NOT EXISTS denuncias_do_ranking_abertas
    ON public.denuncias_do_ranking (criada_em DESC) WHERE resolvida_em IS NULL;

-- Sem política nenhuma: pela API ninguém lê nem grava. Só a função abaixo.
ALTER TABLE public.denuncias_do_ranking ENABLE ROW LEVEL SECURITY;
REVOKE ALL ON TABLE public.denuncias_do_ranking FROM PUBLIC, anon, authenticated;

-- ------------------------------------------------------------------------------
-- 2. DENUNCIAR
-- Devolve quantas pessoas com esse nome foram denunciadas (0 = nome não está
-- mais no ranking). Denunciar de novo a mesma pessoa só atualiza o motivo.
-- ------------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION public.denunciar_nome_do_ranking(
    p_nome TEXT,
    p_motivo TEXT DEFAULT 'ofensivo'
)
RETURNS INT
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
DECLARE
    v_motivo TEXT := CASE WHEN p_motivo IN ('ofensivo', 'se_passa_por_outro', 'outro') THEN p_motivo ELSE 'outro' END;
    v_quantas INT;
BEGIN
    IF auth.uid() IS NULL THEN
        RAISE EXCEPTION 'Entre na sua conta para denunciar.' USING ERRCODE = '42501';
    END IF;

    INSERT INTO public.denuncias_do_ranking (denunciante, denunciado, nome, motivo)
    SELECT auth.uid(), p.id, trim(p.ranking_nome), v_motivo
    FROM public.profiles p
    WHERE p.ranking_publico
      AND p.ranking_nome IS NOT NULL
      AND trim(p.ranking_nome) = trim(p_nome)
      AND p.id <> auth.uid()
    ON CONFLICT ON CONSTRAINT denuncia_unica_por_par
    DO UPDATE SET motivo = EXCLUDED.motivo, nome = EXCLUDED.nome, criada_em = now(), resolvida_em = NULL;

    GET DIAGNOSTICS v_quantas = ROW_COUNT;
    RETURN v_quantas;
END;
$$;

REVOKE ALL ON FUNCTION public.denunciar_nome_do_ranking(TEXT, TEXT) FROM PUBLIC, anon;
GRANT EXECUTE ON FUNCTION public.denunciar_nome_do_ranking(TEXT, TEXT) TO authenticated;

-- ------------------------------------------------------------------------------
-- 3. O RANKING, ESCONDENDO DE CADA UM QUEM ELE DENUNCIOU
-- ------------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION public.ranking_nadadores(
    p_periodo TEXT DEFAULT 'mes',
    p_faixa TEXT DEFAULT NULL,
    p_sexo TEXT DEFAULT NULL,
    p_horario TEXT DEFAULT NULL,
    p_cidade TEXT DEFAULT NULL,
    p_local TEXT DEFAULT NULL,
    p_limite INT DEFAULT 100
)
RETURNS TABLE (posicao INT, nome TEXT, pontos INT, metros INT, treinos INT, semanas INT, sou_eu BOOLEAN)
LANGUAGE sql
STABLE
SECURITY DEFINER
SET search_path = public
AS $$
    WITH base AS (
        SELECT
            (now() AT TIME ZONE 'America/Sao_Paulo')::date AS hoje,
            extract(year FROM (now() AT TIME ZONE 'America/Sao_Paulo'))::int AS ano_atual
    ),
    janela AS (
        SELECT
            hoje,
            ano_atual,
            CASE p_periodo
                WHEN 'semana' THEN date_trunc('week', hoje)::date
                WHEN 'mes' THEN date_trunc('month', hoje)::date
                WHEN 'ano' THEN date_trunc('year', hoje)::date
                ELSE DATE '1900-01-01'
            END AS inicio
        FROM base
    ),
    participantes AS (
        SELECT p.id, trim(p.ranking_nome) AS nome
        FROM public.profiles p, janela j
        WHERE auth.uid() IS NOT NULL
          AND p.ranking_publico
          AND p.ranking_nome IS NOT NULL
          -- Quem eu denunciei some do ranking para mim (é o "bloquear").
          AND NOT EXISTS (
              SELECT 1 FROM public.denuncias_do_ranking d
              WHERE d.denunciante = auth.uid() AND d.denunciado = p.id
          )
          AND (p_sexo IS NULL OR p.sexo = p_sexo)
          AND (p_cidade IS NULL OR lower(trim(p.cidade)) = lower(trim(p_cidade)))
          AND (p_local IS NULL OR lower(trim(p.local_treino)) = lower(trim(p_local)))
          AND (
              p_faixa IS NULL
              OR (p.ano_nascimento IS NOT NULL AND CASE p_faixa
                  WHEN 'ate-17' THEN j.ano_atual - p.ano_nascimento <= 17
                  WHEN '18-29' THEN j.ano_atual - p.ano_nascimento BETWEEN 18 AND 29
                  WHEN '30-39' THEN j.ano_atual - p.ano_nascimento BETWEEN 30 AND 39
                  WHEN '40-49' THEN j.ano_atual - p.ano_nascimento BETWEEN 40 AND 49
                  WHEN '50-59' THEN j.ano_atual - p.ano_nascimento BETWEEN 50 AND 59
                  WHEN '60+' THEN j.ano_atual - p.ano_nascimento >= 60
                  ELSE FALSE
              END)
          )
    ),
    feitos AS (
        SELECT t.user_id, t.metros_feitos, t.metros_planejados, t.data_treino
        FROM public.treinos_realizados t
        JOIN participantes pa ON pa.id = t.user_id
        CROSS JOIN janela j
        WHERE t.data_treino BETWEEN j.inicio AND j.hoje
          AND (
              p_horario IS NULL
              OR CASE p_horario
                  WHEN 'manha' THEN extract(hour FROM t.created_at AT TIME ZONE 'America/Sao_Paulo') BETWEEN 5 AND 11
                  WHEN 'tarde' THEN extract(hour FROM t.created_at AT TIME ZONE 'America/Sao_Paulo') BETWEEN 12 AND 17
                  WHEN 'noite' THEN extract(hour FROM t.created_at AT TIME ZONE 'America/Sao_Paulo') NOT BETWEEN 5 AND 17
                  ELSE FALSE
              END
          )
    ),
    somas AS (
        SELECT
            user_id,
            (sum(metros_feitos / 100
                 + CASE WHEN metros_planejados > 0 AND metros_feitos >= metros_planejados THEN 10 ELSE 0 END)
             + 20 * count(DISTINCT date_trunc('week', data_treino)))::int AS pontos,
            sum(metros_feitos)::int AS metros,
            count(*)::int AS treinos,
            count(DISTINCT date_trunc('week', data_treino))::int AS semanas
        FROM feitos
        GROUP BY user_id
    )
    SELECT
        (rank() OVER (ORDER BY s.pontos DESC, s.metros DESC))::int AS posicao,
        pa.nome,
        s.pontos,
        s.metros,
        s.treinos,
        s.semanas,
        pa.id = auth.uid() AS sou_eu
    FROM somas s
    JOIN participantes pa ON pa.id = s.user_id
    ORDER BY posicao, pa.nome
    LIMIT least(greatest(COALESCE(p_limite, 100), 1), 200);
$$;

REVOKE ALL ON FUNCTION public.ranking_nadadores(TEXT, TEXT, TEXT, TEXT, TEXT, TEXT, INT) FROM PUBLIC, anon;
GRANT EXECUTE ON FUNCTION public.ranking_nadadores(TEXT, TEXT, TEXT, TEXT, TEXT, TEXT, INT) TO authenticated;

-- ------------------------------------------------------------------------------
-- 4. CONFERÊNCIA: a chave pública não denuncia, não vê o ranking nem as denúncias.
-- ------------------------------------------------------------------------------
DO $conferencia$
BEGIN
    SET LOCAL ROLE anon;

    BEGIN
        PERFORM public.denunciar_nome_do_ranking('x');
        RAISE EXCEPTION 'BRECHA: a chave pública consegue denunciar. Nada foi alterado.';
    EXCEPTION WHEN insufficient_privilege THEN
        NULL;
    END;

    BEGIN
        PERFORM * FROM public.ranking_nadadores();
        RAISE EXCEPTION 'BRECHA: a chave pública vê o ranking. Nada foi alterado.';
    EXCEPTION WHEN insufficient_privilege THEN
        NULL;
    END;

    BEGIN
        PERFORM 1 FROM public.denuncias_do_ranking;
        RAISE EXCEPTION 'BRECHA: a chave pública lê as denúncias. Nada foi alterado.';
    EXCEPTION WHEN insufficient_privilege THEN
        NULL;
    END;

    RESET ROLE;

    SET LOCAL ROLE authenticated;
    BEGIN
        PERFORM 1 FROM public.denuncias_do_ranking;
        RAISE EXCEPTION 'BRECHA: um usuário comum lê as denúncias. Nada foi alterado.';
    EXCEPTION WHEN insufficient_privilege THEN
        NULL;
    END;
    RESET ROLE;
END
$conferencia$;

COMMIT;
