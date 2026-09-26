package br.ufal.ic.p2.wepayu.models;

import br.ufal.ic.p2.wepayu.Exception.*;

import java.util.ArrayList;
import java.util.List;

public class BancoDados {
    public List<Empregado> listaEmpregados = new ArrayList<>();
    private int contador = 0;

    public List<Empregado> getListaEmpregados(){ return listaEmpregados;}

    public void adicionarEmpregado(Empregado novoEmpregado){
        contador++;

        String id = "id" + contador;
        novoEmpregado.setId(id);

        listaEmpregados.add(novoEmpregado);

    }

    public void remover(String id) throws EmpregadoNaoExisteException {
        if(id == null || id.isEmpty()) throw new IdentificacaoDoEmpregadoNaoPodeSerNulaException();
        for (int j = 0; j < listaEmpregados.size(); j++) {
            if (listaEmpregados.get(j).getId().equals(id)) {
                listaEmpregados.remove(j);
                return;
            }
        }

        throw new EmpregadoNaoExisteException();
    }

    public Empregado buscar(String id) throws EmpregadoNaoExisteException{
        for(Empregado empregado : listaEmpregados){
            if(empregado.getId().equals(id)){
                return empregado;
            }
        }
        throw new EmpregadoNaoExisteException();
    }

    public String buscarPorNome(String nome, int indice){
        int contador = 0;
        for(Empregado empregado : listaEmpregados){
            if(empregado.getNome().equals(nome)){
                contador++;

                if(contador == indice){
                    return empregado.getId();
                }
            }
        }
        throw new NaoHaEmpregadoComEsseNomeException();
    }

    public void alteraEmpregado(String id, String atributo, String valor) throws EmpregadoNaoExisteException {
        if(id == null || id.isEmpty()) throw new IdentificacaoDoEmpregadoNaoPodeSerNulaException();
        Empregado e = buscar(id);
            if(atributo.equals("nome")){
                if(valor == null || valor.isEmpty()) throw new NomeNaoPodeSerNuloException();
                e.setNome(valor);
            }
            else if(atributo.equals("endereco")){
                if(valor == null || valor.isEmpty()) throw new EnderecoNaoPodeSerNuloException();
                e.setEndereco(valor);
            }
            else if(atributo.equals("salario")){
                e.setSalario(Validador.formatar(Validador.validarSalario(valor)));
            }
            else if(atributo.equals("comissao")){
                if(!e.getTipo().equals("comissionado")) throw new EmpregadoNaoComissionadoException();
                ((Comissionado) e).setComissao(Validador.formatar(Validador.validarComissao(valor)));
            }
            else if(atributo.equals("sindicalizado")){
                e.setSindicalizado(valor);
            }
            else{
                throw new AtributoNaoExisteException();
            }
    }

    public void alteraEmpregado(String id, String atributo, String valor, String idSindicato, String taxaSindical) throws EmpregadoNaoExisteException {
        Empregado empregado = buscar(id);

        if(atributo.equals("sindicalizado")){
            empregado.setSindicalizado(valor);
            empregado.setIdSindicato(idSindicato);
            empregado.setTaxaSindical(taxaSindical);
        }
        else{
            throw new AtributoNaoExisteException();
        }
    }

    public void alteraEmpregado(String id, String atributo, String valor, String banco, String agencia, String contaCorrente) throws EmpregadoNaoExisteException{
        Empregado empregado = buscar(id);

        if(atributo.equals("metodoPagamento") && valor.equals("banco")){
            empregado.setMetodoPagamento(valor);
            empregado.setBanco(banco);
            empregado.setAgencia(agencia);
            empregado.setContaCorrente(contaCorrente);
        }
        else{
            throw new AtributoNaoExisteException();
        }
    }

    public void mudaTipoEmpregado(String id, String novoTipo, String salario) throws EmpregadoNaoExisteException{
        Empregado antigo = buscar(id);

        Empregado novo;
        if(novoTipo.equals("horista")){
            novo = new Horista(antigo.getNome(), antigo.getEndereco(), novoTipo, salario);
        }
        else if(novoTipo.equals("assalariado")){
            novo = new Assalariado(antigo.getNome(), antigo.getEndereco(), novoTipo, salario);
        }
        else{
            throw new TipoInvalidoException();
        }

        copiarDadosComuns(antigo, novo);

        int idx = listaEmpregados.indexOf(antigo);
        listaEmpregados.set(idx, novo);
    }

    public void mudaTipoEmpregado(String id, String novoTipo, String salario, String comissao) throws EmpregadoNaoExisteException{
        Empregado antigo = buscar(id);

        Comissionado novo = new Comissionado(antigo.getNome(), antigo.getEndereco(), novoTipo, salario, comissao);

        copiarDadosComuns(antigo, novo);

        int idx = listaEmpregados.indexOf(antigo);
        listaEmpregados.set(idx, novo);
    }

    private void copiarDadosComuns(Empregado antigo, Empregado novo){
        novo.setId(antigo.getId());
        novo.setMetodoPagamento(antigo.getMetodoPagamento());
        novo.setBanco(antigo.getBanco());
        novo.setAgencia(antigo.getAgencia());
        novo.setContaCorrente(antigo.getContaCorrente());
        novo.setSindicalizado(antigo.getSindicalizado());
        novo.setIdSindicato(antigo.getIdSindicato());
        novo.setTaxaSindical(antigo.getTaxaSindical());
    }

    public void zerar(){
        listaEmpregados.clear();
        contador = 0;
    }

    public void lancaCartao(String id, String data, String horas) throws EmpregadoNaoExisteException{
        for(int j = 0; j < listaEmpregados.size(); j++){
            if(listaEmpregados.get(j).getId().equals(id)){
                CartaoDePonto cartao = new CartaoDePonto(data, horas);
                listaEmpregados.get(j).adicionarCartao(cartao);
                return;
            }
        }
        throw new EmpregadoNaoExisteException();
    }

    public void lancaVenda(String id, String data, String valor) throws EmpregadoNaoExisteException{
        for(int j = 0; j < listaEmpregados.size(); j++){
            if(listaEmpregados.get(j).getId().equals(id)){
                Venda venda = new Venda(data, valor);
                listaEmpregados.get(j).adicionarVenda(venda);
                return;
            }
        }
        throw new EmpregadoNaoExisteException();
    }

    public void lancaTaxaServico(String id, String data, String valor) throws EmpregadoNaoExisteException{
        for(int j = 0; j < listaEmpregados.size(); j++){
            if(listaEmpregados.get(j).getId().equals(id)){
                TaxaServico taxaServico = new TaxaServico(data, valor);
                listaEmpregados.get(j).adicionarTaxaServico(taxaServico);
                return;
            }
        }
        throw new EmpregadoNaoExisteException();
    }
}
