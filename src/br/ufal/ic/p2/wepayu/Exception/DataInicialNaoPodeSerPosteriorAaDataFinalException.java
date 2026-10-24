package br.ufal.ic.p2.wepayu.Exception;

public class DataInicialNaoPodeSerPosteriorAaDataFinalException extends WePayUException {
    public DataInicialNaoPodeSerPosteriorAaDataFinalException() {
        super("Data inicial nao pode ser posterior aa data final.");
    }
}
