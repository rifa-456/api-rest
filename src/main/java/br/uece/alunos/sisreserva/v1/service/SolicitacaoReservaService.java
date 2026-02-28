package br.uece.alunos.sisreserva.v1.service;

import br.uece.alunos.sisreserva.v1.dto.solicitacaoReserva.AtualizarStatusSolicitacaoDTO;
import br.uece.alunos.sisreserva.v1.dto.solicitacaoReserva.RecorrenciaInfoDTO;
import br.uece.alunos.sisreserva.v1.dto.solicitacaoReserva.SolicitacaoReservaDTO;
import br.uece.alunos.sisreserva.v1.dto.solicitacaoReserva.SolicitacaoReservaRetornoDTO;
import br.uece.alunos.sisreserva.v1.dto.solicitacaoReserva.HorariosOcupadosPorMesDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

/**
 * Interface de serviço para gerenciamento de solicitações de reserva.
 *
 * <p>Define os métodos de negócio para criação, consulta e gerenciamento
 * de reservas, incluindo suporte a reservas recorrentes.</p>
 *
 * @author Sistema de Reservas UECE
 */
public interface SolicitacaoReservaService {

    /**
     * Cria uma nova solicitação de reserva.
     * Suporta tanto reservas únicas quanto recorrentes.
     *
     * @param data dados da solicitação
     * @return dados da reserva criada (reserva pai em caso de recorrência)
     */
    SolicitacaoReservaRetornoDTO criarSolicitacaoReserva(SolicitacaoReservaDTO data);

    /**
     * Busca solicitações de reserva com filtros opcionais.
     *
     * @param pageable               configuração de paginação
     * @param id                     filtro por ID
     * @param dataInicio             filtro por data de início
     * @param dataFim                filtro por data de fim
     * @param espacoId               filtro por espaço
     * @param equipamentoId          filtro por equipamento
     * @param usuarioSolicitanteId   filtro por usuário solicitante
     * @param status                 filtro por status
     * @param projetoId              filtro por projeto
     * @param espacoDoEquipamentoId  filtra reservas de equipamentos pertencentes ao espaço informado
     * @return página de reservas encontradas
     */
    Page<SolicitacaoReservaRetornoDTO> obterSolicitacaoReserva(
            Pageable pageable,
            String id,
            LocalDate dataInicio,
            LocalDate dataFim,
            String espacoId,
            String equipamentoId,
            String usuarioSolicitanteId,
            Integer status,
            String projetoId,
            String espacoDoEquipamentoId
    );

    /**
     * Atualiza o status de uma solicitação de reserva.
     *
     * @param id   identificador da reserva
     * @param data dados de atualização
     * @return dados da reserva atualizada
     */
    SolicitacaoReservaRetornoDTO atualizarStatus(String id, AtualizarStatusSolicitacaoDTO data);

    /**
     * Obtém os horários ocupados em um mês específico para um espaço.
     *
     * @param mes      mês (1-12)
     * @param ano      ano
     * @param espacoId identificador do espaço
     * @return horários ocupados agrupados por dia
     */
    HorariosOcupadosPorMesDTO obterHorariosOcupadosPorMes(Integer mes, Integer ano, String espacoId);

    /**
     * Obtém informações completas sobre uma reserva recorrente.
     *
     * @param reservaId ID da reserva (pode ser pai ou filha)
     * @return informações da recorrência incluindo todas as ocorrências
     */
    RecorrenciaInfoDTO obterRecorrenciaInfo(String reservaId);
}
