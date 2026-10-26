package br.ufal.ic.p2.wepayu.Exception;

import br.ufal.ic.p2.wepayu.Exception.WePayUException;

public class AgendaDePagamentoNaoDisponivelException extends WePayUException {
    public AgendaDePagamentoNaoDisponivelException() {
        super("Agenda de pagamento nao esta disponivel");
    }
}