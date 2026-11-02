package ro.dam.salafitness.service.computation.view;

import java.time.LocalDate;

/** V1 - v_abonamente_active */
public record AbonamentActivView(Long idAbonamentClient, Integer idUtilizator, String client, String tipAbonament,
                                 LocalDate dataInceput, LocalDate dataSfarsit, long zileRamase) {
}
