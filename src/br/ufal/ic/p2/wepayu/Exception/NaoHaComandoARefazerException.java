package br.ufal.ic.p2.wepayu.Exception;

public class NaoHaComandoARefazerException extends RuntimeException {
    public NaoHaComandoARefazerException() {
        super("Nao ha comando a refazer.");
    }
}
