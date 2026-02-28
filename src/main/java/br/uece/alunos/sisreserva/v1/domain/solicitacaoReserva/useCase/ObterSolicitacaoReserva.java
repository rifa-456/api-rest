package br.uece.alunos.sisreserva.v1.domain.solicitacaoReserva.useCase;

import br.uece.alunos.sisreserva.v1.domain.equipamentoEspaco.EquipamentoEspacoRepository;
import br.uece.alunos.sisreserva.v1.domain.gestorEspaco.GestorEspacoRepository;
import br.uece.alunos.sisreserva.v1.domain.secretariaEspaco.SecretariaEspacoRepository;
import br.uece.alunos.sisreserva.v1.domain.solicitacaoReserva.SolicitacaoReserva;
import br.uece.alunos.sisreserva.v1.domain.solicitacaoReserva.SolicitacaoReservaRepository;
import br.uece.alunos.sisreserva.v1.domain.solicitacaoReserva.specification.SolicitacaoReservaSpecification;
import br.uece.alunos.sisreserva.v1.dto.solicitacaoReserva.SolicitacaoReservaRetornoDTO;
import br.uece.alunos.sisreserva.v1.infra.security.UsuarioAutenticadoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Caso de uso responsável por obter solicitações de reserva com filtros.
 * Aplica automaticamente filtros de permissão baseados no cargo do usuário autenticado.
 * <p>
 * Regras de visualização:
 * <ul>
 *   <li>Admin: vê todas as reservas</li>
 *   <li>Gestor: vê as suas + reservas dos espaços que gerencia + equipamentos desses espaços</li>
 *   <li>Secretaria: vê as suas + reservas dos espaços da secretaria + equipamentos desses espaços</li>
 *   <li>Usuário interno/externo: vê apenas as suas próprias reservas</li>
 * </ul>
 */
@Component
public class ObterSolicitacaoReserva {

    @Autowired
    private SolicitacaoReservaRepository solicitacaoReservaRepository;

    @Autowired
    private UsuarioAutenticadoService usuarioAutenticadoService;

    @Autowired
    private GestorEspacoRepository gestorEspacoRepository;

    @Autowired
    private SecretariaEspacoRepository secretariaEspacoRepository;

    @Autowired
    private EquipamentoEspacoRepository equipamentoEspacoRepository;

    /**
     * Obtém solicitações de reserva aplicando filtros de dados e permissões.
     * Automaticamente restringe a visualização baseado no cargo do usuário autenticado.
     *
     * @param pageable              configuração de paginação
     * @param id                    filtro por ID
     * @param dataInicio            filtro por data de início
     * @param dataFim               filtro por data de fim
     * @param espacoId              filtro por espaço
     * @param equipamentoId         filtro por equipamento
     * @param usuarioSolicitanteId  filtro por usuário solicitante
     * @param statusCodigo          filtro por status
     * @param projetoId             filtro por projeto
     * @param espacoDoEquipamentoId filtra reservas de equipamentos pertencentes ao espaço informado
     * @return página de solicitações de reserva
     */
    public Page<SolicitacaoReservaRetornoDTO> obterSolicitacaoReserva(
            Pageable pageable,
            String id,
            LocalDate dataInicio,
            LocalDate dataFim,
            String espacoId,
            String equipamentoId,
            String usuarioSolicitanteId,
            Integer statusCodigo,
            String projetoId,
            String espacoDoEquipamentoId
    ) {
        Map<String, Object> filtros = new HashMap<>();
        if (id != null)                     filtros.put("id", id);
        if (dataInicio != null)             filtros.put("dataInicio", dataInicio);
        if (dataFim != null)                filtros.put("dataFim", dataFim);
        if (espacoId != null)               filtros.put("espacoId", espacoId);
        if (equipamentoId != null)          filtros.put("equipamentoId", equipamentoId);
        if (usuarioSolicitanteId != null)   filtros.put("usuarioSolicitanteId", usuarioSolicitanteId);
        if (statusCodigo != null)           filtros.put("statusCodigo", statusCodigo);
        if (projetoId != null)              filtros.put("projetoId", projetoId);
        if (espacoDoEquipamentoId != null)  filtros.put("espacoDoEquipamentoId", espacoDoEquipamentoId);

        return execute(filtros, pageable).map(SolicitacaoReservaRetornoDTO::new);
    }

    /**
     * Executa a consulta aplicando filtros de dados e permissões.
     * Determina automaticamente as permissões do usuário autenticado.
     */
    private Page<SolicitacaoReserva> execute(Map<String, Object> filtros, Pageable pageable) {
        var usuario = usuarioAutenticadoService.getUsuarioAutenticado();
        boolean isAdmin = usuarioAutenticadoService.isAdmin();
        String usuarioId = usuario != null ? usuario.getId() : null;
        List<String> espacosPermitidos = obterEspacosPermitidos(usuarioId, isAdmin);
        List<String> equipamentosPermitidos = obterEquipamentosPermitidos(espacosPermitidos, isAdmin);
        List<String> equipamentosDoespacoFiltro = resolverEquipamentosDoEspaco(
                (String) filtros.get("espacoDoEquipamentoId")
        );
        return solicitacaoReservaRepository.findAll(
                SolicitacaoReservaSpecification.byFilter(
                        (String) filtros.get("id"),
                        (LocalDate) filtros.get("dataInicio"),
                        (LocalDate) filtros.get("dataFim"),
                        (String) filtros.get("espacoId"),
                        (String) filtros.get("equipamentoId"),
                        (String) filtros.get("usuarioSolicitanteId"),
                        (Integer) filtros.get("statusCodigo"),
                        (String) filtros.get("projetoId"),
                        isAdmin,
                        usuarioId,
                        espacosPermitidos,
                        equipamentosPermitidos,
                        equipamentosDoespacoFiltro
                ),
                pageable
        );
    }

    /**
     * Obtém a lista de IDs dos espaços que o usuário tem permissão para visualizar reservas.
     * Combina espaços gerenciados (gestor) e espaços secretariados.
     *
     * @param usuarioId ID do usuário autenticado
     * @param isAdmin   se o usuário é administrador
     * @return lista de IDs dos espaços permitidos (vazia se for apenas usuário comum)
     */
    private List<String> obterEspacosPermitidos(String usuarioId, boolean isAdmin) {
        if (isAdmin || usuarioId == null) {
            return List.of();
        }

        List<String> espacosGerenciados = gestorEspacoRepository
                .findEspacosIdsGerenciadosByUsuarioId(usuarioId);

        List<String> espacosSecretariados = secretariaEspacoRepository
                .findEspacosIdsSecretariadosByUsuarioId(usuarioId);

        return Stream.concat(espacosGerenciados.stream(), espacosSecretariados.stream())
                .distinct()
                .toList();
    }

    /**
     * Obtém a lista de IDs dos equipamentos vinculados aos espaços que o usuário gerencia.
     * Permite que gestores/secretarias vejam reservas de equipamentos dos espaços que gerenciam.
     *
     * @param espacosIds lista de IDs dos espaços gerenciados
     * @param isAdmin    se o usuário é administrador
     * @return lista de IDs dos equipamentos vinculados (vazia se admin ou sem espaços)
     */
    private List<String> obterEquipamentosPermitidos(List<String> espacosIds, boolean isAdmin) {
        if (isAdmin || espacosIds == null || espacosIds.isEmpty()) {
            return List.of();
        }
        return equipamentoEspacoRepository.findEquipamentosIdsVinculadosAosEspacos(espacosIds);
    }

    /**
     * Resolve o parâmetro {@code espacoDoEquipamentoId} em uma lista de IDs de equipamentos
     * ativos vinculados ao espaço informado. Retorna lista vazia quando o parâmetro é nulo,
     * indicando à Specification que o filtro não deve ser aplicado.
     *
     * <p>Reutiliza {@link EquipamentoEspacoRepository#findEquipamentosIdsVinculadosAosEspacos}
     * para manter consistência com o critério de "ativo" (dataRemocao IS NULL).</p>
     *
     * @param espacoDoEquipamentoId ID do espaço cujos equipamentos devem ser filtrados, ou null
     * @return lista de IDs dos equipamentos ativos do espaço, ou lista vazia se não filtrar
     */
    private List<String> resolverEquipamentosDoEspaco(String espacoDoEquipamentoId) {
        if (espacoDoEquipamentoId == null || espacoDoEquipamentoId.isBlank()) {
            return List.of();
        }
        return equipamentoEspacoRepository
                .findEquipamentosIdsVinculadosAosEspacos(List.of(espacoDoEquipamentoId));
    }
}