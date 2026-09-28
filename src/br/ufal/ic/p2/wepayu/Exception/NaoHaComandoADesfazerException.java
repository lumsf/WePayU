package br.ufal.ic.p2.wepayu.Exception;

public class NaoHaComandoADesfazerException extends RuntimeException {
    public NaoHaComandoADesfazerException() {
        super("Nao ha comando a desfazer.");
    }
}
