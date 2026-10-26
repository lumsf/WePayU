package br.ufal.ic.p2.wepayu.Exception;

import br.ufal.ic.p2.wepayu.Exception.WePayUException;

public class DescricaoDeAgendaInvalidaException extends WePayUException {
    public DescricaoDeAgendaInvalidaException() {
        super("Descricao de agenda invalida");
    }
}