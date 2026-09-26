package br.ufal.ic.p2.wepayu.Exception;

public class MetodoDePagamentoInvalidoException extends RuntimeException {
    public MetodoDePagamentoInvalidoException() {
        super("Metodo de pagamento invalido.");
    }
}
