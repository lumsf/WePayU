package br.ufal.ic.p2.wepayu.Exception;

public class NaoHaEmpregadoComEsseNomeException extends RuntimeException {
    public NaoHaEmpregadoComEsseNomeException() {
        super("Nao ha empregado com esse nome.");
    }
}
