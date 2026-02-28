package br.uece.alunos.sisreserva.v1.domain.equipamento.useCase;

import br.uece.alunos.sisreserva.v1.domain.equipamento.EquipamentoRepository;
import br.uece.alunos.sisreserva.v1.domain.equipamento.specification.EquipamentoSpecification;
import br.uece.alunos.sisreserva.v1.dto.equipamento.EquipamentoRetornoDTO;
import br.uece.alunos.sisreserva.v1.infra.security.UsuarioAutenticadoService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/**
 * Caso de uso para obter equipamentos com filtros e paginação.
 * Aplica restrições de visualização baseadas no cargo do usuário autenticado.
 * Usuários externos só podem visualizar equipamentos multiusuário.
 */
@Slf4j
@Component
@AllArgsConstructor
public class ObterEquipamentos {

    private final EquipamentoRepository repository;
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    /**
     * Obtém equipamentos com filtros e paginação.
     * Aplica automaticamente restrição para usuários externos (apenas equipamentos multiusuário).
     * 
     * @param pageable Informações de paginação
     * @param id Filtro por ID do equipamento
     * @param tombamento Filtro por tombamento
     * @param status Filtro por status
     * @param tipoEquipamento Filtro por ID do tipo de equipamento
     * @param reservavel Filtro por equipamentos disponíveis para reserva
     * @return Página com os equipamentos encontrados
     */
    public Page<EquipamentoRetornoDTO> obter(Pageable pageable, String id, String tombamento, String status, String tipoEquipamento, Boolean reservavel) {
        boolean restringirApenasMultiusuario = usuarioAutenticadoService.deveAplicarRestricoesMultiusuario();
        if (restringirApenasMultiusuario) {
            var usuario = usuarioAutenticadoService.getUsuarioAutenticado();
            if (usuario != null) {
                log.info("[AUDIT] FILTRO_APLICADO - Usuário externo '{}' (ID: {}) listando equipamentos - Restrição multiusuario=true aplicada",
                        usuario.getEmail(), usuario.getId());
            }
        }
        var spec = EquipamentoSpecification.byFilters(
                id,
                tombamento,
                status,
                tipoEquipamento,
                null,
                reservavel,
                restringirApenasMultiusuario
        );
        var page = repository.findAll(spec, pageable);
        if (restringirApenasMultiusuario) {
            var usuario = usuarioAutenticadoService.getUsuarioAutenticado();
            if (usuario != null) {
                log.info("[AUDIT] RESULTADO_LISTAGEM - Usuário externo '{}' visualizou {} equipamentos multiusuário (total no sistema pode ser maior)",
                        usuario.getEmail(), page.getTotalElements());
            }
        }
        return page.map(EquipamentoRetornoDTO::new);
    }
}
