package br.ufal.ic.p2.wepayu.Exception;

public class DataInicialNaoPodeSerPosteriorAaDataFinalException extends RuntimeException {
    public DataInicialNaoPodeSerPosteriorAaDataFinalException() {
        super("Data inicial nao pode ser posterior aa data final.");
    }
}
