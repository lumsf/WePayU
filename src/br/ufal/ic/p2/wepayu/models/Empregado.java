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
    private String metodoPagamento;
    private String sindicalizado;
    private String idSindicato;
    private String taxaSindical;
    private String banco;
    private String agencia;
    private String contaCorrente;

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
    public void setNome(String nome){ this.nome = nome; }

    public String getEndereco() {
        return endereco;
    }
    public void setEndereco(String endereco){ this.endereco = endereco; }

    public String getTipo() {
        return tipo;
    }
    public void setTipo(String tipo){ this.tipo = tipo; }

    public String getSalario() {
        return salario;
    }
    public void setSalario(String salario){ this.salario = salario; }

    public String getMetodoPagamento(){ return metodoPagamento; }
    public void setMetodoPagamento(String metodoPagamento){ this.metodoPagamento = metodoPagamento; }

    public String getSindicalizado(){ return sindicalizado; }
    public void setSindicalizado(String sindicalizado){ this.sindicalizado = sindicalizado; }

    public String getIdSindicato(){ return idSindicato; }
    public void setIdSindicato(String idSindicato){ this.idSindicato = idSindicato; }

    public String getTaxaSindical(){ return taxaSindical; }
    public void setTaxaSindical(String taxaSindical){ this.taxaSindical = taxaSindical; }

    public String getBanco(){ return banco; }
    public void setBanco(String banco){ this.banco = banco; }

    public String getAgencia(){ return agencia; }
    public void setAgencia(String agencia){ this.agencia = agencia; }

    public String getContaCorrente(){ return contaCorrente; }
    public void setContaCorrente(String contaCorrente){ this.contaCorrente = contaCorrente; }

    private List<CartaoDePonto> listaCartoes = new ArrayList<>();

    public void adicionarCartao(CartaoDePonto novoCartaoDePonto){
        listaCartoes.add(novoCartaoDePonto);
    }

    private List<Venda> listaVendas = new ArrayList<>();

    public void adicionarVenda(Venda novaVenda){
        listaVendas.add(novaVenda);
    }

    private List<TaxaServico> listaTaxaServico = new ArrayList<>();

    public void adicionarTaxaServico(TaxaServico novaTaxaServico){
        listaTaxaServico.add(novaTaxaServico);
    }

}
