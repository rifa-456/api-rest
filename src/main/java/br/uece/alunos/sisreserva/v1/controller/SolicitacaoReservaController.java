package br.uece.alunos.sisreserva.v1.controller;

import br.uece.alunos.sisreserva.v1.dto.solicitacaoReserva.AtualizarStatusSolicitacaoDTO;
import br.uece.alunos.sisreserva.v1.dto.solicitacaoReserva.SolicitacaoReservaDTO;
import br.uece.alunos.sisreserva.v1.dto.solicitacaoReserva.SolicitacaoReservaRetornoDTO;
import br.uece.alunos.sisreserva.v1.dto.solicitacaoReserva.HorariosOcupadosPorMesDTO;
import br.uece.alunos.sisreserva.v1.dto.solicitacaoReserva.RecorrenciaInfoDTO;
import br.uece.alunos.sisreserva.v1.dto.utils.ApiResponseDTO;
import br.uece.alunos.sisreserva.v1.service.SolicitacaoReservaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/solicitacao-reserva")
@Tag(name = "Rotas de solicitação de reserva mapeadas no controller")
public class SolicitacaoReservaController {

    @Autowired
    private SolicitacaoReservaService solicitacaoReservaService;

    @PostMapping
    @Transactional
    public ResponseEntity<ApiResponseDTO<SolicitacaoReservaRetornoDTO>> criarSolicitacaoReserva(
            @RequestBody @Valid SolicitacaoReservaDTO data) {
        var solicitacaoRetornoDTO = solicitacaoReservaService.criarSolicitacaoReserva(data);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDTO.success(solicitacaoRetornoDTO));
    }

    @GetMapping
    public ResponseEntity<ApiResponseDTO<Page<SolicitacaoReservaRetornoDTO>>> obterSolicitacoesPaginadas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "16") int size,
            @RequestParam(defaultValue = "createdAt") String sortField,
            @RequestParam(defaultValue = "desc") String sortOrder,
            @RequestParam(required = false) String id,
            @RequestParam(required = false) LocalDate dataInicio,
            @RequestParam(required = false) LocalDate dataFim,
            @RequestParam(required = false) String espacoId,
            @RequestParam(required = false) String equipamentoId,
            @RequestParam(required = false) String usuarioSolicitanteId,
            @RequestParam(required = false) Integer statusCodigo,
            @RequestParam(required = false) String projetoId,
            @Parameter(description = "Filtra reservas de equipamentos pertencentes ao espaço informado")
            @RequestParam(required = false) String espacoDoEquipamentoId
    ) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortOrder), sortField));
        var solicitacoesPaginadas = solicitacaoReservaService.obterSolicitacaoReserva(
                pageable, id, dataInicio, dataFim, espacoId, equipamentoId,
                usuarioSolicitanteId, statusCodigo, projetoId, espacoDoEquipamentoId
        );
        return ResponseEntity.ok(ApiResponseDTO.success(solicitacoesPaginadas));
    }

    @PutMapping("/{id}/status")
    @Transactional
    public ResponseEntity<ApiResponseDTO<SolicitacaoReservaRetornoDTO>> atualizarStatus(
            @PathVariable String id,
            @RequestBody @Valid AtualizarStatusSolicitacaoDTO data) {
        var solicitacaoAtualizada = solicitacaoReservaService.atualizarStatus(id, data);
        return ResponseEntity.ok(ApiResponseDTO.success(solicitacaoAtualizada));
    }

    @GetMapping("/horarios-ocupados")
    public ResponseEntity<ApiResponseDTO<HorariosOcupadosPorMesDTO>> obterHorariosOcupados(
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) Integer ano,
            @RequestParam(required = false) String espacoId) {
        var horariosOcupados = solicitacaoReservaService.obterHorariosOcupadosPorMes(mes, ano, espacoId);
        return ResponseEntity.ok(ApiResponseDTO.success(horariosOcupados));
    }

    /**
     * Obtém informações completas sobre uma reserva recorrente.
     *
     * <p>Retorna a reserva pai e todas as reservas filhas (ocorrências) geradas pela recorrência.</p>
     *
     * @param id identificador da reserva (pode ser reserva pai ou filha)
     * @return informações completas da recorrência
     */
    @GetMapping("/{id}/recorrencia")
    public ResponseEntity<ApiResponseDTO<RecorrenciaInfoDTO>> obterRecorrenciaInfo(@PathVariable String id) {
        var recorrenciaInfo = solicitacaoReservaService.obterRecorrenciaInfo(id);
        return ResponseEntity.ok(ApiResponseDTO.success(recorrenciaInfo));
    }
}
