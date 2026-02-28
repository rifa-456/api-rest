package br.uece.alunos.sisreserva.v1.domain.solicitacaoReserva.specification;

import br.uece.alunos.sisreserva.v1.domain.solicitacaoReserva.SolicitacaoReserva;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Specifications para consulta dinâmica da entidade SolicitacaoReserva.
 * Implementa filtros de dados e filtros de permissão baseados no cargo do usuário autenticado.
 * <p>
 * Regras de visualização:
 * <ul>
 *   <li>Admin: vê todas as reservas</li>
 *   <li>Gestor: vê as suas + dos espaços que gerencia + dos equipamentos desses espaços</li>
 *   <li>Secretaria: vê as suas + dos espaços da secretaria + dos equipamentos desses espaços</li>
 *   <li>Usuário interno/externo: vê apenas as suas</li>
 * </ul>
 */
public class SolicitacaoReservaSpecification {

    /**
     * Cria uma Specification baseada nos parâmetros passados.
     * Aplica filtros de dados (AND) e filtros de permissão (OR) baseados no cargo do usuário.
     *
     * @param id                         filtro por ID da solicitação
     * @param dataInicio                 filtro por data de início
     * @param dataFim                    filtro por data de fim
     * @param espacoId                   filtro por ID do espaço
     * @param equipamentoId              filtro por ID do equipamento
     * @param usuarioSolicitanteId       filtro por ID do usuário solicitante
     * @param statusCodigo               filtro por código do status
     * @param projetoId                  filtro por ID do projeto
     * @param isAdmin                    se o usuário é administrador (vê todas)
     * @param usuarioAutenticadoId       ID do usuário autenticado (para filtrar as suas)
     * @param espacosGerenciadosIds      IDs dos espaços que o usuário gerencia ou está na secretaria
     * @param equipamentosPermitidosIds  IDs dos equipamentos vinculados aos espaços gerenciados (permissão)
     * @param equipamentosDoespacoIds    IDs dos equipamentos ativos do espaço informado em
     *                                   {@code espacoDoEquipamentoId} (filtro de dados); lista
     *                                   vazia significa que o filtro não deve ser aplicado
     * @return Specification com os filtros aplicados
     */
    public static Specification<SolicitacaoReserva> byFilter(
            String id,
            LocalDate dataInicio,
            LocalDate dataFim,
            String espacoId,
            String equipamentoId,
            String usuarioSolicitanteId,
            Integer statusCodigo,
            String projetoId,
            boolean isAdmin,
            String usuarioAutenticadoId,
            List<String> espacosGerenciadosIds,
            List<String> equipamentosPermitidosIds,
            List<String> equipamentosDoespacoIds
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (id != null && !id.isBlank()) {
                predicates.add(cb.equal(root.get("id"), id));
            }
            if (dataInicio != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("dataInicio"), dataInicio));
            }
            if (dataFim != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("dataFim"), dataFim));
            }
            if (espacoId != null && !espacoId.isBlank()) {
                predicates.add(cb.equal(root.get("espaco").get("id"), espacoId));
            }
            if (equipamentoId != null && !equipamentoId.isBlank()) {
                predicates.add(cb.equal(root.get("equipamento").get("id"), equipamentoId));
            }
            if (usuarioSolicitanteId != null && !usuarioSolicitanteId.isBlank()) {
                predicates.add(cb.equal(root.get("usuarioSolicitante").get("id"), usuarioSolicitanteId));
            }
            if (statusCodigo != null) {
                predicates.add(cb.equal(root.get("status"), statusCodigo));
            }
            if (projetoId != null && !projetoId.isBlank()) {
                predicates.add(cb.equal(root.get("projeto").get("id"), projetoId));
            }

            if (equipamentosDoespacoIds != null && !equipamentosDoespacoIds.isEmpty()) {
                predicates.add(root.get("equipamento").get("id").in(equipamentosDoespacoIds));
            }

            if (!isAdmin) {
                List<Predicate> permissaoPredicates = new ArrayList<>();

                if (usuarioAutenticadoId != null) {
                    permissaoPredicates.add(
                            cb.equal(root.get("usuarioSolicitante").get("id"), usuarioAutenticadoId)
                    );
                }

                if (espacosGerenciadosIds != null && !espacosGerenciadosIds.isEmpty()) {
                    permissaoPredicates.add(
                            root.get("espaco").get("id").in(espacosGerenciadosIds)
                    );
                }

                if (equipamentosPermitidosIds != null && !equipamentosPermitidosIds.isEmpty()) {
                    permissaoPredicates.add(
                            root.get("equipamento").get("id").in(equipamentosPermitidosIds)
                    );
                }

                if (!permissaoPredicates.isEmpty()) {
                    predicates.add(cb.or(permissaoPredicates.toArray(new Predicate[0])));
                } else if (usuarioAutenticadoId != null) {
                    predicates.add(
                            cb.equal(root.get("usuarioSolicitante").get("id"), usuarioAutenticadoId)
                    );
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
