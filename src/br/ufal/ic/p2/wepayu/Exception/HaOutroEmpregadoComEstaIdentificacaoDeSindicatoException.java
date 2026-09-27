package br.ufal.ic.p2.wepayu.Exception;

public class HaOutroEmpregadoComEstaIdentificacaoDeSindicatoException extends RuntimeException {
    public HaOutroEmpregadoComEstaIdentificacaoDeSindicatoException() {
        super("Ha outro empregado com esta identificacao de sindicato");
    }
}
