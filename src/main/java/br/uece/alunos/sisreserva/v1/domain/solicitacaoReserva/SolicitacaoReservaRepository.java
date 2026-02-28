package br.uece.alunos.sisreserva.v1.domain.solicitacaoReserva;

import br.uece.alunos.sisreserva.v1.dto.espaco.ReservasPorMesProjection;
import br.uece.alunos.sisreserva.v1.dto.espaco.ReservasPorUsuarioProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SolicitacaoReservaRepository extends JpaRepository<SolicitacaoReserva, String>,  JpaSpecificationExecutor<SolicitacaoReserva>{
    @Query("SELECT sr FROM SolicitacaoReserva sr WHERE sr.usuarioSolicitante.id = :usuarioId")
    List<SolicitacaoReserva> findByUsuarioSolicitanteId(String usuarioId);

    @Query("SELECT sr FROM SolicitacaoReserva sr WHERE sr.espaco.id = :espacoId")
    List<SolicitacaoReserva> findByEspacoId(String espacoId);

    @Query("SELECT sr FROM SolicitacaoReserva sr WHERE sr.status = :status")
    List<SolicitacaoReserva> findByStatus(StatusSolicitacao status);

    @Query("SELECT sr FROM SolicitacaoReserva sr WHERE sr.projeto.id = :projetoId")
    List<SolicitacaoReserva> findByProjetoId(String projetoId);

    /**
     * Busca os IDs dos projetos vinculados a reservas de espaços específicos.
     * Útil para filtrar projetos que gestores/secretarias podem visualizar.
     * 
     * @param espacosIds Lista de IDs dos espaços
     * @return Lista com os IDs dos projetos vinculados a reservas desses espaços
     */
    @Query("""
        SELECT DISTINCT sr.projeto.id FROM SolicitacaoReserva sr
        WHERE sr.espaco.id IN :espacosIds
          AND sr.projeto IS NOT NULL
    """)
    List<String> findProjetosIdsVinculadosAosEspacos(List<String> espacosIds);

    /**
     * Busca os IDs dos projetos vinculados a reservas de equipamentos específicos.
     * Útil para filtrar projetos que gestores/secretarias podem visualizar.
     * 
     * @param equipamentosIds Lista de IDs dos equipamentos
     * @return Lista com os IDs dos projetos vinculados a reservas desses equipamentos
     */
    @Query("""
        SELECT DISTINCT sr.projeto.id FROM SolicitacaoReserva sr
        WHERE sr.equipamento.id IN :equipamentosIds
          AND sr.projeto IS NOT NULL
    """)
    List<String> findProjetosIdsVinculadosAosEquipamentos(List<String> equipamentosIds);

    @Query("SELECT sr FROM SolicitacaoReserva sr WHERE sr.dataInicio >= :startDate AND sr.dataFim <= :endDate")
    List<SolicitacaoReserva> findByPeriodo(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT sr FROM SolicitacaoReserva sr WHERE sr.status = :status AND sr.espaco.id = :espacoId")
    List<SolicitacaoReserva> findByStatusAndEspacoId(StatusSolicitacao status, String espacoId);

    @Query("""
        SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END
        FROM SolicitacaoReserva s
        WHERE s.espaco.id = :espacoId
        AND s.status = br.uece.alunos.sisreserva.v1.domain.solicitacaoReserva.StatusSolicitacao.APROVADO
        AND s.dataInicio < :dataFim
        AND s.dataFim > :dataInicio
    """)
    boolean existsByEspacoIdAndPeriodoConflitanteAprovado(String espacoId, LocalDateTime dataInicio, LocalDateTime dataFim);

    @Query("SELECT sr FROM SolicitacaoReserva sr ORDER BY sr.createdAt DESC")
    Page<SolicitacaoReserva> findAllPageable(Pageable pageable);

    @Query("SELECT sr FROM SolicitacaoReserva sr WHERE sr.id = :id")
    Optional<SolicitacaoReserva> findById(String id);

    @Query("""
        SELECT sr FROM SolicitacaoReserva sr
        LEFT JOIN FETCH sr.usuarioSolicitante
        LEFT JOIN FETCH sr.espaco
        LEFT JOIN FETCH sr.equipamento
        LEFT JOIN FETCH sr.projeto
        WHERE sr.id = :id
    """)
    Optional<SolicitacaoReserva> findByIdWithRelations(String id);

    @Query("""
        SELECT s FROM SolicitacaoReserva s
        WHERE s.status = br.uece.alunos.sisreserva.v1.domain.solicitacaoReserva.StatusSolicitacao.APROVADO
          AND s.dataInicio >= :dataInicio
          AND s.dataInicio < :dataFim
        ORDER BY s.dataInicio ASC
    """)
    List<SolicitacaoReserva> findReservasAprovadasPorPeriodo(LocalDateTime dataInicio, LocalDateTime dataFim);

    @Query("""
        SELECT s FROM SolicitacaoReserva s
        WHERE s.status = br.uece.alunos.sisreserva.v1.domain.solicitacaoReserva.StatusSolicitacao.APROVADO
          AND s.dataInicio >= :dataInicio
          AND s.dataInicio < :dataFim
          AND s.espaco.id = :espacoId
        ORDER BY s.dataInicio ASC
    """)
    List<SolicitacaoReserva> findReservasAprovadasPorPeriodoEEspaco(LocalDateTime dataInicio, LocalDateTime dataFim, String espacoId);

    /**
     * Busca todas as reservas filhas de uma reserva pai.
     * 
     * @param reservaPaiId ID da reserva pai
     * @return lista de reservas filhas
     */
    @Query("SELECT sr FROM SolicitacaoReserva sr WHERE sr.reservaPaiId = :reservaPaiId ORDER BY sr.dataInicio ASC")
    List<SolicitacaoReserva> findByReservaPaiId(String reservaPaiId);

    /**
     * Busca todas as reservas (pai e filhas) de um grupo de recorrência.
     * 
     * @param reservaPaiId ID da reserva pai
     * @return lista contendo a reserva pai e todas as filhas
     */
    @Query("""
        SELECT sr FROM SolicitacaoReserva sr 
        WHERE sr.id = :reservaPaiId OR sr.reservaPaiId = :reservaPaiId 
        ORDER BY sr.dataInicio ASC
    """)
    List<SolicitacaoReserva> findReservasPaiEFilhas(String reservaPaiId);

    /**
     * Conta quantas reservas filhas existem para uma reserva pai.
     * 
     * @param reservaPaiId ID da reserva pai
     * @return número de reservas filhas
     */
    @Query("SELECT COUNT(sr) FROM SolicitacaoReserva sr WHERE sr.reservaPaiId = :reservaPaiId")
    Long countByReservaPaiId(String reservaPaiId);

    /**
     * Conta reservas de um espaço em um mês/ano específico (query agregada otimizada).
     * 
     * @param espacoId ID do espaço
     * @param mes mês (1-12)
     * @param ano ano
     * @return projeção com totais de reservas solicitadas e confirmadas
     */
    @Query("""
        SELECT 
            CAST(MONTH(sr.dataInicio) AS int) as mes,
            CAST(YEAR(sr.dataInicio) AS int) as ano,
            COUNT(sr) as totalReservas,
            SUM(CASE WHEN sr.status = br.uece.alunos.sisreserva.v1.domain.solicitacaoReserva.StatusSolicitacao.APROVADO THEN 1 ELSE 0 END) as reservasConfirmadas
        FROM SolicitacaoReserva sr
        WHERE sr.espaco.id = :espacoId
          AND MONTH(sr.dataInicio) = :mes
          AND YEAR(sr.dataInicio) = :ano
        GROUP BY MONTH(sr.dataInicio), YEAR(sr.dataInicio)
    """)
    Optional<ReservasPorMesProjection> contarReservasPorEspacoEMes(
        @Param("espacoId") String espacoId, 
        @Param("mes") int mes, 
        @Param("ano") int ano
    );

    /**
     * Agrupa e conta reservas de um espaço por mês/ano (query agregada otimizada).
     * 
     * @param espacoId ID do espaço
     * @return lista de projeções com totais agrupados por mês/ano, ordenada por quantidade decrescente
     */
    @Query("""
        SELECT 
            CAST(MONTH(sr.dataInicio) AS int) as mes,
            CAST(YEAR(sr.dataInicio) AS int) as ano,
            COUNT(sr) as totalReservas,
            SUM(CASE WHEN sr.status = br.uece.alunos.sisreserva.v1.domain.solicitacaoReserva.StatusSolicitacao.APROVADO THEN 1 ELSE 0 END) as reservasConfirmadas
        FROM SolicitacaoReserva sr
        WHERE sr.espaco.id = :espacoId
        GROUP BY MONTH(sr.dataInicio), YEAR(sr.dataInicio)
        ORDER BY COUNT(sr) DESC
    """)
    List<ReservasPorMesProjection> contarReservasPorEspacoAgrupadoPorMes(@Param("espacoId") String espacoId);

    /**
     * Agrupa e conta reservas de um espaço por usuário (query agregada otimizada).
     * 
     * @param espacoId ID do espaço
     * @return lista de projeções com totais agrupados por usuário, ordenada por quantidade decrescente
     */
    @Query("""
        SELECT 
            sr.usuarioSolicitante.id as usuarioId,
            sr.usuarioSolicitante.nome as usuarioNome,
            COUNT(sr) as totalReservas,
            SUM(CASE WHEN sr.status = br.uece.alunos.sisreserva.v1.domain.solicitacaoReserva.StatusSolicitacao.APROVADO THEN 1 ELSE 0 END) as reservasConfirmadas
        FROM SolicitacaoReserva sr
        WHERE sr.espaco.id = :espacoId
        GROUP BY sr.usuarioSolicitante.id, sr.usuarioSolicitante.nome
        HAVING SUM(CASE WHEN sr.status = br.uece.alunos.sisreserva.v1.domain.solicitacaoReserva.StatusSolicitacao.APROVADO THEN 1 ELSE 0 END) > 0
        ORDER BY SUM(CASE WHEN sr.status = br.uece.alunos.sisreserva.v1.domain.solicitacaoReserva.StatusSolicitacao.APROVADO THEN 1 ELSE 0 END) DESC
    """)
    List<ReservasPorUsuarioProjection> contarReservasPorEspacoAgrupadoPorUsuario(@Param("espacoId") String espacoId);

    /**
     * Verifica se existe conflito de horários para um equipamento específico.
     * Considera apenas reservas aprovadas.
     * 
     * @param equipamentoId ID do equipamento
     * @param dataInicio data e hora de início
     * @param dataFim data e hora de fim
     * @return true se houver conflito, false caso contrário
     */
    @Query("""
        SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END
        FROM SolicitacaoReserva s
        WHERE s.equipamento.id = :equipamentoId
        AND s.status = br.uece.alunos.sisreserva.v1.domain.solicitacaoReserva.StatusSolicitacao.APROVADO
        AND s.dataInicio < :dataFim
        AND s.dataFim > :dataInicio
    """)
    boolean existsByEquipamentoIdAndPeriodoConflitanteAprovado(
        @Param("equipamentoId") String equipamentoId, 
        @Param("dataInicio") LocalDateTime dataInicio, 
        @Param("dataFim") LocalDateTime dataFim
    );

    /**
     * Busca reservas de um equipamento específico.
     * 
     * @param equipamentoId ID do equipamento
     * @return lista de reservas do equipamento
     */
    @Query("SELECT sr FROM SolicitacaoReserva sr WHERE sr.equipamento.id = :equipamentoId ORDER BY sr.dataInicio DESC")
    List<SolicitacaoReserva> findByEquipamentoId(@Param("equipamentoId") String equipamentoId);

    /**
     * Busca reservas aprovadas de um equipamento em um período específico.
     * 
     * @param equipamentoId ID do equipamento
     * @param dataInicio data e hora de início do período
     * @param dataFim data e hora de fim do período
     * @return lista de reservas aprovadas no período
     */
    @Query("""
        SELECT s FROM SolicitacaoReserva s
        WHERE s.equipamento.id = :equipamentoId
          AND s.status = br.uece.alunos.sisreserva.v1.domain.solicitacaoReserva.StatusSolicitacao.APROVADO
          AND s.dataInicio >= :dataInicio
          AND s.dataInicio < :dataFim
        ORDER BY s.dataInicio ASC
    """)
    List<SolicitacaoReserva> findReservasAprovadasPorPeriodoEEquipamento(
        @Param("dataInicio") LocalDateTime dataInicio, 
        @Param("dataFim") LocalDateTime dataFim, 
        @Param("equipamentoId") String equipamentoId
    );

    /**
     * Verifica se o usuário já possui uma solicitação de reserva ativa (pendente ou aprovada)
     * para o mesmo espaço ou equipamento no período informado. Considera conflitos quando há 
     * sobreposição de horários para o mesmo recurso (espaço ou equipamento).
     * 
     * @param usuarioId ID do usuário solicitante
     * @param espacoId ID do espaço (null se for reserva de equipamento)
     * @param equipamentoId ID do equipamento (null se for reserva de espaço)
     * @param dataInicio data e hora de início da nova reserva
     * @param dataFim data e hora de fim da nova reserva
     * @return true se já existe uma solicitação do usuário para o mesmo recurso no período, false caso contrário
     */
    @Query("""
        SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END
        FROM SolicitacaoReserva s
        WHERE s.usuarioSolicitante.id = :usuarioId
        AND (s.status = br.uece.alunos.sisreserva.v1.domain.solicitacaoReserva.StatusSolicitacao.PENDENTE
             OR s.status = br.uece.alunos.sisreserva.v1.domain.solicitacaoReserva.StatusSolicitacao.APROVADO)
        AND (
            (:espacoId IS NOT NULL AND s.espaco.id = :espacoId)
            OR (:equipamentoId IS NOT NULL AND s.equipamento.id = :equipamentoId)
        )
        AND s.dataInicio < :dataFim
        AND s.dataFim > :dataInicio
    """)
    boolean existsByUsuarioIdAndPeriodoConflitante(
        @Param("usuarioId") String usuarioId,
        @Param("espacoId") String espacoId,
        @Param("equipamentoId") String equipamentoId,
        @Param("dataInicio") LocalDateTime dataInicio,
        @Param("dataFim") LocalDateTime dataFim
    );

    /**
     * Busca todas as solicitações pendentes que conflitam com um intervalo de tempo específico
     * para o mesmo espaço ou equipamento da solicitação aprovada.
     *
     * 
     * @param solicitacaoAprovadaId ID da solicitação que foi aprovada (para excluí-la dos resultados)
     * @param espacoId ID do espaço (null se for reserva de equipamento)
     * @param equipamentoId ID do equipamento (null se for reserva de espaço)
     * @param dataInicio data e hora de início da reserva aprovada
     * @param dataFim data e hora de fim da reserva aprovada
     * @return lista de solicitações pendentes que conflitam com o período
     */
    @Query("""
        SELECT s FROM SolicitacaoReserva s
        LEFT JOIN FETCH s.usuarioSolicitante
        LEFT JOIN FETCH s.espaco
        LEFT JOIN FETCH s.equipamento
        WHERE s.id != :solicitacaoAprovadaId
        AND s.status = br.uece.alunos.sisreserva.v1.domain.solicitacaoReserva.StatusSolicitacao.PENDENTE
        AND (
            (:espacoId IS NOT NULL AND s.espaco.id = :espacoId)
            OR (:equipamentoId IS NOT NULL AND s.equipamento.id = :equipamentoId)
        )
        AND s.dataInicio < :dataFim
        AND s.dataFim > :dataInicio
    """)
    List<SolicitacaoReserva> findSolicitacoesPendentesConflitantes(
        @Param("solicitacaoAprovadaId") String solicitacaoAprovadaId,
        @Param("espacoId") String espacoId,
        @Param("equipamentoId") String equipamentoId,
        @Param("dataInicio") LocalDateTime dataInicio,
        @Param("dataFim") LocalDateTime dataFim
    );

    // ==================== QUERIES AGREGADAS PARA ESTATÍSTICAS DE EQUIPAMENTO ====================

    /**
     * Conta reservas de um equipamento em um mês específico (query agregada otimizada).
     * 
     * @param equipamentoId ID do equipamento
     * @param mes mês (1-12)
     * @param ano ano
     * @return projeção com totais do mês, vazia se não houver reservas
     */
    @Query("""
        SELECT 
            CAST(MONTH(sr.dataInicio) AS int) as mes,
            CAST(YEAR(sr.dataInicio) AS int) as ano,
            COUNT(sr) as totalReservas,
            SUM(CASE WHEN sr.status = br.uece.alunos.sisreserva.v1.domain.solicitacaoReserva.StatusSolicitacao.APROVADO THEN 1 ELSE 0 END) as reservasConfirmadas
        FROM SolicitacaoReserva sr
        WHERE sr.equipamento.id = :equipamentoId
          AND MONTH(sr.dataInicio) = :mes
          AND YEAR(sr.dataInicio) = :ano
        GROUP BY MONTH(sr.dataInicio), YEAR(sr.dataInicio)
    """)
    Optional<ReservasPorMesProjection> contarReservasPorEquipamentoEMes(
        @Param("equipamentoId") String equipamentoId, 
        @Param("mes") int mes, 
        @Param("ano") int ano
    );

    /**
     * Agrupa e conta reservas de um equipamento por mês/ano (query agregada otimizada).
     * 
     * @param equipamentoId ID do equipamento
     * @return lista de projeções com totais agrupados por mês/ano, ordenada por quantidade decrescente
     */
    @Query("""
        SELECT 
            CAST(MONTH(sr.dataInicio) AS int) as mes,
            CAST(YEAR(sr.dataInicio) AS int) as ano,
            COUNT(sr) as totalReservas,
            SUM(CASE WHEN sr.status = br.uece.alunos.sisreserva.v1.domain.solicitacaoReserva.StatusSolicitacao.APROVADO THEN 1 ELSE 0 END) as reservasConfirmadas
        FROM SolicitacaoReserva sr
        WHERE sr.equipamento.id = :equipamentoId
        GROUP BY MONTH(sr.dataInicio), YEAR(sr.dataInicio)
        ORDER BY COUNT(sr) DESC
    """)
    List<ReservasPorMesProjection> contarReservasPorEquipamentoAgrupadoPorMes(@Param("equipamentoId") String equipamentoId);

    /**
     * Agrupa e conta reservas de um equipamento por usuário (query agregada otimizada).
     * 
     * @param equipamentoId ID do equipamento
     * @return lista de projeções com totais agrupados por usuário, ordenada por quantidade decrescente
     */
    @Query("""
        SELECT 
            sr.usuarioSolicitante.id as usuarioId,
            sr.usuarioSolicitante.nome as usuarioNome,
            COUNT(sr) as totalReservas,
            SUM(CASE WHEN sr.status = br.uece.alunos.sisreserva.v1.domain.solicitacaoReserva.StatusSolicitacao.APROVADO THEN 1 ELSE 0 END) as reservasConfirmadas
        FROM SolicitacaoReserva sr
        WHERE sr.equipamento.id = :equipamentoId
        GROUP BY sr.usuarioSolicitante.id, sr.usuarioSolicitante.nome
        HAVING SUM(CASE WHEN sr.status = br.uece.alunos.sisreserva.v1.domain.solicitacaoReserva.StatusSolicitacao.APROVADO THEN 1 ELSE 0 END) > 0
        ORDER BY SUM(CASE WHEN sr.status = br.uece.alunos.sisreserva.v1.domain.solicitacaoReserva.StatusSolicitacao.APROVADO THEN 1 ELSE 0 END) DESC
    """)
    List<ReservasPorUsuarioProjection> contarReservasPorEquipamentoAgrupadoPorUsuario(@Param("equipamentoId") String equipamentoId);

    // ==================== QUERIES PARA ESTATÍSTICAS POR PERÍODO ====================

    /**
     * Agrupa e conta reservas de um espaço por mês em um período específico.
     * 
     * @param espacoId ID do espaço
     * @param mesInicial mês inicial (1-12)
     * @param anoInicial ano inicial
     * @param mesFinal mês final (1-12)
     * @param anoFinal ano final
     * @return lista de projeções com totais agrupados por mês, ordenada cronologicamente
     */
    @Query("""
        SELECT 
            CAST(MONTH(sr.dataInicio) AS int) as mes,
            CAST(YEAR(sr.dataInicio) AS int) as ano,
            COUNT(sr) as totalReservas,
            SUM(CASE WHEN sr.status = br.uece.alunos.sisreserva.v1.domain.solicitacaoReserva.StatusSolicitacao.APROVADO THEN 1 ELSE 0 END) as reservasConfirmadas
        FROM SolicitacaoReserva sr
        WHERE sr.espaco.id = :espacoId
          AND (YEAR(sr.dataInicio) > :anoInicial OR (YEAR(sr.dataInicio) = :anoInicial AND MONTH(sr.dataInicio) >= :mesInicial))
          AND (YEAR(sr.dataInicio) < :anoFinal OR (YEAR(sr.dataInicio) = :anoFinal AND MONTH(sr.dataInicio) <= :mesFinal))
        GROUP BY MONTH(sr.dataInicio), YEAR(sr.dataInicio)
        ORDER BY YEAR(sr.dataInicio), MONTH(sr.dataInicio)
    """)
    List<ReservasPorMesProjection> contarReservasPorEspacoNoPeriodo(
        @Param("espacoId") String espacoId,
        @Param("mesInicial") int mesInicial,
        @Param("anoInicial") int anoInicial,
        @Param("mesFinal") int mesFinal,
        @Param("anoFinal") int anoFinal
    );

    /**
     * Agrupa e conta reservas de um equipamento por mês em um período específico.
     * 
     * @param equipamentoId ID do equipamento
     * @param mesInicial mês inicial (1-12)
     * @param anoInicial ano inicial
     * @param mesFinal mês final (1-12)
     * @param anoFinal ano final
     * @return lista de projeções com totais agrupados por mês, ordenada cronologicamente
     */
    @Query("""
        SELECT 
            CAST(MONTH(sr.dataInicio) AS int) as mes,
            CAST(YEAR(sr.dataInicio) AS int) as ano,
            COUNT(sr) as totalReservas,
            SUM(CASE WHEN sr.status = br.uece.alunos.sisreserva.v1.domain.solicitacaoReserva.StatusSolicitacao.APROVADO THEN 1 ELSE 0 END) as reservasConfirmadas
        FROM SolicitacaoReserva sr
        WHERE sr.equipamento.id = :equipamentoId
          AND (YEAR(sr.dataInicio) > :anoInicial OR (YEAR(sr.dataInicio) = :anoInicial AND MONTH(sr.dataInicio) >= :mesInicial))
          AND (YEAR(sr.dataInicio) < :anoFinal OR (YEAR(sr.dataInicio) = :anoFinal AND MONTH(sr.dataInicio) <= :mesFinal))
        GROUP BY MONTH(sr.dataInicio), YEAR(sr.dataInicio)
        ORDER BY YEAR(sr.dataInicio), MONTH(sr.dataInicio)
    """)
    List<ReservasPorMesProjection> contarReservasPorEquipamentoNoPeriodo(
        @Param("equipamentoId") String equipamentoId,
        @Param("mesInicial") int mesInicial,
        @Param("anoInicial") int anoInicial,
        @Param("mesFinal") int mesFinal,
        @Param("anoFinal") int anoFinal
    );

    /**
     * Agrupa e conta reservas de um espaço por usuário em um período específico.
     * 
     * @param espacoId ID do espaço
     * @param mesInicial mês inicial (1-12)
     * @param anoInicial ano inicial
     * @param mesFinal mês final (1-12)
     * @param anoFinal ano final
     * @return lista de projeções com totais agrupados por usuário, ordenada por quantidade decrescente
     */
    @Query("""
        SELECT 
            sr.usuarioSolicitante.id as usuarioId,
            sr.usuarioSolicitante.nome as usuarioNome,
            COUNT(sr) as totalReservas,
            SUM(CASE WHEN sr.status = br.uece.alunos.sisreserva.v1.domain.solicitacaoReserva.StatusSolicitacao.APROVADO THEN 1 ELSE 0 END) as reservasConfirmadas
        FROM SolicitacaoReserva sr
        WHERE sr.espaco.id = :espacoId
          AND (YEAR(sr.dataInicio) > :anoInicial OR (YEAR(sr.dataInicio) = :anoInicial AND MONTH(sr.dataInicio) >= :mesInicial))
          AND (YEAR(sr.dataInicio) < :anoFinal OR (YEAR(sr.dataInicio) = :anoFinal AND MONTH(sr.dataInicio) <= :mesFinal))
        GROUP BY sr.usuarioSolicitante.id, sr.usuarioSolicitante.nome
        HAVING SUM(CASE WHEN sr.status = br.uece.alunos.sisreserva.v1.domain.solicitacaoReserva.StatusSolicitacao.APROVADO THEN 1 ELSE 0 END) > 0
        ORDER BY SUM(CASE WHEN sr.status = br.uece.alunos.sisreserva.v1.domain.solicitacaoReserva.StatusSolicitacao.APROVADO THEN 1 ELSE 0 END) DESC
    """)
    List<ReservasPorUsuarioProjection> contarReservasPorEspacoEUsuarioNoPeriodo(
        @Param("espacoId") String espacoId,
        @Param("mesInicial") int mesInicial,
        @Param("anoInicial") int anoInicial,
        @Param("mesFinal") int mesFinal,
        @Param("anoFinal") int anoFinal
    );

    /**
     * Agrupa e conta reservas de um equipamento por usuário em um período específico.
     * 
     * @param equipamentoId ID do equipamento
     * @param mesInicial mês inicial (1-12)
     * @param anoInicial ano inicial
     * @param mesFinal mês final (1-12)
     * @param anoFinal ano final
     * @return lista de projeções com totais agrupados por usuário, ordenada por quantidade decrescente
     */
    @Query("""
        SELECT 
            sr.usuarioSolicitante.id as usuarioId,
            sr.usuarioSolicitante.nome as usuarioNome,
            COUNT(sr) as totalReservas,
            SUM(CASE WHEN sr.status = br.uece.alunos.sisreserva.v1.domain.solicitacaoReserva.StatusSolicitacao.APROVADO THEN 1 ELSE 0 END) as reservasConfirmadas
        FROM SolicitacaoReserva sr
        WHERE sr.equipamento.id = :equipamentoId
          AND (YEAR(sr.dataInicio) > :anoInicial OR (YEAR(sr.dataInicio) = :anoInicial AND MONTH(sr.dataInicio) >= :mesInicial))
          AND (YEAR(sr.dataInicio) < :anoFinal OR (YEAR(sr.dataInicio) = :anoFinal AND MONTH(sr.dataInicio) <= :mesFinal))
        GROUP BY sr.usuarioSolicitante.id, sr.usuarioSolicitante.nome
        HAVING SUM(CASE WHEN sr.status = br.uece.alunos.sisreserva.v1.domain.solicitacaoReserva.StatusSolicitacao.APROVADO THEN 1 ELSE 0 END) > 0
        ORDER BY SUM(CASE WHEN sr.status = br.uece.alunos.sisreserva.v1.domain.solicitacaoReserva.StatusSolicitacao.APROVADO THEN 1 ELSE 0 END) DESC
    """)
    List<ReservasPorUsuarioProjection> contarReservasPorEquipamentoEUsuarioNoPeriodo(
        @Param("equipamentoId") String equipamentoId,
        @Param("mesInicial") int mesInicial,
        @Param("anoInicial") int anoInicial,
        @Param("mesFinal") int mesFinal,
        @Param("anoFinal") int anoFinal
    );

    /**
     * Agrupa e conta TODAS as reservas (incluindo não aprovadas) de um espaço por usuário em um período específico.
     * Diferente de contarReservasPorEspacoEUsuarioNoPeriodo, esta query não filtra usuários sem reservas aprovadas.
     * 
     * @param espacoId ID do espaço
     * @param mesInicial mês inicial (1-12)
     * @param anoInicial ano inicial
     * @param mesFinal mês final (1-12)
     * @param anoFinal ano final
     * @return lista de projeções com totais agrupados por usuário, ordenada por total de solicitações
     */
    @Query("""
        SELECT 
            sr.usuarioSolicitante.id as usuarioId,
            sr.usuarioSolicitante.nome as usuarioNome,
            COUNT(sr) as totalReservas,
            SUM(CASE WHEN sr.status = br.uece.alunos.sisreserva.v1.domain.solicitacaoReserva.StatusSolicitacao.APROVADO THEN 1 ELSE 0 END) as reservasConfirmadas
        FROM SolicitacaoReserva sr
        WHERE sr.espaco.id = :espacoId
          AND (YEAR(sr.dataInicio) > :anoInicial OR (YEAR(sr.dataInicio) = :anoInicial AND MONTH(sr.dataInicio) >= :mesInicial))
          AND (YEAR(sr.dataInicio) < :anoFinal OR (YEAR(sr.dataInicio) = :anoFinal AND MONTH(sr.dataInicio) <= :mesFinal))
        GROUP BY sr.usuarioSolicitante.id, sr.usuarioSolicitante.nome
        ORDER BY COUNT(sr) DESC
    """)
    List<ReservasPorUsuarioProjection> contarTodosUsuariosPorEspacoNoPeriodo(
        @Param("espacoId") String espacoId,
        @Param("mesInicial") int mesInicial,
        @Param("anoInicial") int anoInicial,
        @Param("mesFinal") int mesFinal,
        @Param("anoFinal") int anoFinal
    );

    /**
     * Agrupa e conta TODAS as reservas (incluindo não aprovadas) de um equipamento por usuário em um período específico.
     * Diferente de contarReservasPorEquipamentoEUsuarioNoPeriodo, esta query não filtra usuários sem reservas aprovadas.
     * 
     * @param equipamentoId ID do equipamento
     * @param mesInicial mês inicial (1-12)
     * @param anoInicial ano inicial
     * @param mesFinal mês final (1-12)
     * @param anoFinal ano final
     * @return lista de projeções com totais agrupados por usuário, ordenada por total de solicitações
     */
    @Query("""
        SELECT 
            sr.usuarioSolicitante.id as usuarioId,
            sr.usuarioSolicitante.nome as usuarioNome,
            COUNT(sr) as totalReservas,
            SUM(CASE WHEN sr.status = br.uece.alunos.sisreserva.v1.domain.solicitacaoReserva.StatusSolicitacao.APROVADO THEN 1 ELSE 0 END) as reservasConfirmadas
        FROM SolicitacaoReserva sr
        WHERE sr.equipamento.id = :equipamentoId
          AND (YEAR(sr.dataInicio) > :anoInicial OR (YEAR(sr.dataInicio) = :anoInicial AND MONTH(sr.dataInicio) >= :mesInicial))
          AND (YEAR(sr.dataInicio) < :anoFinal OR (YEAR(sr.dataInicio) = :anoFinal AND MONTH(sr.dataInicio) <= :mesFinal))
        GROUP BY sr.usuarioSolicitante.id, sr.usuarioSolicitante.nome
        ORDER BY COUNT(sr) DESC
    """)
    List<ReservasPorUsuarioProjection> contarTodosUsuariosPorEquipamentoNoPeriodo(
        @Param("equipamentoId") String equipamentoId,
        @Param("mesInicial") int mesInicial,
        @Param("anoInicial") int anoInicial,
        @Param("mesFinal") int mesFinal,
        @Param("anoFinal") int anoFinal
    );

    /**
     * Busca o mês com mais reservas de um espaço em um período específico.
     * 
     * @param espacoId ID do espaço
     * @param mesInicial mês inicial (1-12)
     * @param anoInicial ano inicial
     * @param mesFinal mês final (1-12)
     * @param anoFinal ano final
     * @return projeção com o mês que tem mais reservas no período
     */
    @Query("""
        SELECT 
            CAST(MONTH(sr.dataInicio) AS int) as mes,
            CAST(YEAR(sr.dataInicio) AS int) as ano,
            COUNT(sr) as totalReservas,
            SUM(CASE WHEN sr.status = br.uece.alunos.sisreserva.v1.domain.solicitacaoReserva.StatusSolicitacao.APROVADO THEN 1 ELSE 0 END) as reservasConfirmadas
        FROM SolicitacaoReserva sr
        WHERE sr.espaco.id = :espacoId
          AND (YEAR(sr.dataInicio) > :anoInicial OR (YEAR(sr.dataInicio) = :anoInicial AND MONTH(sr.dataInicio) >= :mesInicial))
          AND (YEAR(sr.dataInicio) < :anoFinal OR (YEAR(sr.dataInicio) = :anoFinal AND MONTH(sr.dataInicio) <= :mesFinal))
        GROUP BY MONTH(sr.dataInicio), YEAR(sr.dataInicio)
        ORDER BY SUM(CASE WHEN sr.status = br.uece.alunos.sisreserva.v1.domain.solicitacaoReserva.StatusSolicitacao.APROVADO THEN 1 ELSE 0 END) DESC
    """)
    List<ReservasPorMesProjection> contarMesComMaisReservasPorEspacoNoPeriodo(
        @Param("espacoId") String espacoId,
        @Param("mesInicial") int mesInicial,
        @Param("anoInicial") int anoInicial,
        @Param("mesFinal") int mesFinal,
        @Param("anoFinal") int anoFinal
    );

    /**
     * Busca o mês com mais reservas de um equipamento em um período específico.
     * 
     * @param equipamentoId ID do equipamento
     * @param mesInicial mês inicial (1-12)
     * @param anoInicial ano inicial
     * @param mesFinal mês final (1-12)
     * @param anoFinal ano final
     * @return projeção com o mês que tem mais reservas no período
     */
    @Query("""
        SELECT 
            CAST(MONTH(sr.dataInicio) AS int) as mes,
            CAST(YEAR(sr.dataInicio) AS int) as ano,
            COUNT(sr) as totalReservas,
            SUM(CASE WHEN sr.status = br.uece.alunos.sisreserva.v1.domain.solicitacaoReserva.StatusSolicitacao.APROVADO THEN 1 ELSE 0 END) as reservasConfirmadas
        FROM SolicitacaoReserva sr
        WHERE sr.equipamento.id = :equipamentoId
          AND (YEAR(sr.dataInicio) > :anoInicial OR (YEAR(sr.dataInicio) = :anoInicial AND MONTH(sr.dataInicio) >= :mesInicial))
          AND (YEAR(sr.dataInicio) < :anoFinal OR (YEAR(sr.dataInicio) = :anoFinal AND MONTH(sr.dataInicio) <= :mesFinal))
        GROUP BY MONTH(sr.dataInicio), YEAR(sr.dataInicio)
        ORDER BY SUM(CASE WHEN sr.status = br.uece.alunos.sisreserva.v1.domain.solicitacaoReserva.StatusSolicitacao.APROVADO THEN 1 ELSE 0 END) DESC
    """)
    List<ReservasPorMesProjection> contarMesComMaisReservasPorEquipamentoNoPeriodo(
        @Param("equipamentoId") String equipamentoId,
        @Param("mesInicial") int mesInicial,
        @Param("anoInicial") int anoInicial,
        @Param("mesFinal") int mesFinal,
        @Param("anoFinal") int anoFinal
    );

    /**
     * Busca o mês com mais reservas de um espaço em um ano específico.
     * 
     * @param espacoId ID do espaço
     * @param ano ano
     * @return projeção com o mês que tem mais reservas no ano
     */
    @Query("""
        SELECT 
            CAST(MONTH(sr.dataInicio) AS int) as mes,
            CAST(YEAR(sr.dataInicio) AS int) as ano,
            COUNT(sr) as totalReservas,
            SUM(CASE WHEN sr.status = br.uece.alunos.sisreserva.v1.domain.solicitacaoReserva.StatusSolicitacao.APROVADO THEN 1 ELSE 0 END) as reservasConfirmadas
        FROM SolicitacaoReserva sr
        WHERE sr.espaco.id = :espacoId
          AND YEAR(sr.dataInicio) = :ano
        GROUP BY MONTH(sr.dataInicio), YEAR(sr.dataInicio)
        ORDER BY SUM(CASE WHEN sr.status = br.uece.alunos.sisreserva.v1.domain.solicitacaoReserva.StatusSolicitacao.APROVADO THEN 1 ELSE 0 END) DESC
    """)
    List<ReservasPorMesProjection> contarMesComMaisReservasPorEspacoNoAno(
        @Param("espacoId") String espacoId,
        @Param("ano") int ano
    );

    /**
     * Busca o mês com mais reservas de um equipamento em um ano específico.
     * 
     * @param equipamentoId ID do equipamento
     * @param ano ano
     * @return projeção com o mês que tem mais reservas no ano
     */
    @Query("""
        SELECT 
            CAST(MONTH(sr.dataInicio) AS int) as mes,
            CAST(YEAR(sr.dataInicio) AS int) as ano,
            COUNT(sr) as totalReservas,
            SUM(CASE WHEN sr.status = br.uece.alunos.sisreserva.v1.domain.solicitacaoReserva.StatusSolicitacao.APROVADO THEN 1 ELSE 0 END) as reservasConfirmadas
        FROM SolicitacaoReserva sr
        WHERE sr.equipamento.id = :equipamentoId
          AND YEAR(sr.dataInicio) = :ano
        GROUP BY MONTH(sr.dataInicio), YEAR(sr.dataInicio)
        ORDER BY SUM(CASE WHEN sr.status = br.uece.alunos.sisreserva.v1.domain.solicitacaoReserva.StatusSolicitacao.APROVADO THEN 1 ELSE 0 END) DESC
    """)
    List<ReservasPorMesProjection> contarMesComMaisReservasPorEquipamentoNoAno(
        @Param("equipamentoId") String equipamentoId,
        @Param("ano") int ano
    );
}

