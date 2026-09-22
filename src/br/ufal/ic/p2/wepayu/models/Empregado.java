package br.ufal.ic.p2.wepayu.models;

import br.ufal.ic.p2.wepayu.Exception.EmpregadoNaoExisteException;

import java.util.ArrayList;
import java.util.List;

public class Empregado {
    private String id;
    private String nome;
    private String endereco;
    private String tipo;
    private String salario;

    public Empregado(String nome, String endereco, String tipo, String salario) throws EmpregadoNaoExisteException {
        this.nome = nome;
        this.endereco = endereco;
        this.tipo = tipo;
        this.salario = salario;
    }

    public Empregado() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public String getEndereco() {
        return endereco;
    }

    public String getTipo() {
        return tipo;
    }

    public String getSalario() {
        return salario;
    }

    private List<CartaoDePonto> listaCartoes = new ArrayList<>();

    public void adicionarCartao(CartaoDePonto novoCartaoDePonto){
        listaCartoes.add(novoCartaoDePonto);
    }

    private List<Venda> listaVendas = new ArrayList<>();

    public void adicionarVenda(Venda novaVenda){
        listaVendas.add(novaVenda);
    }

}
