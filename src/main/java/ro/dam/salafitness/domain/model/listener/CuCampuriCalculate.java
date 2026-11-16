package ro.dam.salafitness.domain.model.listener;

public interface CuCampuriCalculate {

    void recalculeazaCampuriCalculate();

    // colectia LAZY de care depinde calculul (null daca nu exista)
    default String colectieNecesara() {
        return null;
    }
}