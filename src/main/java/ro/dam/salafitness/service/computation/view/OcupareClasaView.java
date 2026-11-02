package ro.dam.salafitness.service.computation.view;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** V2 - v_ocupare_clase */
public record OcupareClasaView(Long idProgram, String clasa, LocalDateTime dataOraInceput, String salaStudio,
                               int capacitateMaxima, int locuriOcupate, int locuriLibere, BigDecimal gradOcuparePct) {
}
