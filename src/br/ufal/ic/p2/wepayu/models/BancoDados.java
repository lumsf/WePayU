package br.ufal.ic.p2.wepayu.models;

import br.ufal.ic.p2.wepayu.Exception.*;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.math.BigDecimal;
import java.time.LocalDate;

public class BancoDados {
    public List<Empregado> listaEmpregados = new ArrayList<>();
    private int contador = 0;

    private Stack<EstadoBancoDados> pilhaUndo = new Stack<>();
    private Stack<EstadoBancoDados> pilhaRedo = new Stack<>();

    public List<Empregado> getListaEmpregados(){ return listaEmpregados;}

    private void salvarEstado() throws Exception {

        EstadoBancoDados estado =
                new EstadoBancoDados(listaEmpregados, contador);

        pilhaUndo.push(estado);
        pilhaRedo.clear();
    }

    public void adicionarEmpregado(Empregado novoEmpregado) throws Exception {
        salvarEstado();

        contador++;

        String id = "id" + contador;
        novoEmpregado.setId(id);

        listaEmpregados.add(novoEmpregado);
    }

    public void remover(String id) throws Exception{
        if(id == null || id.isEmpty()) throw new IdentificacaoDoEmpregadoNaoPodeSerNulaException();
        for (int j = 0; j < listaEmpregados.size(); j++) {
            if (listaEmpregados.get(j).getId().equals(id)) {
                salvarEstado();
                listaEmpregados.remove(j);
                return;
            }
        }

        throw new EmpregadoNaoExisteException();
    }

    public Empregado buscar(String id) throws Exception{
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

    public void alteraEmpregado(String id, String atributo, String valor) throws Exception {
        if(id == null || id.isEmpty()) throw new IdentificacaoDoEmpregadoNaoPodeSerNulaException();

        Empregado e = buscar(id);

        if(atributo.equals("nome")){
            if(valor == null || valor.isEmpty()) throw new NomeNaoPodeSerNuloException();
            salvarEstado();
            e.setNome(valor);
        }
        else if(atributo.equals("endereco")){
            if(valor == null || valor.isEmpty()) throw new EnderecoNaoPodeSerNuloException();
            salvarEstado();
            e.setEndereco(valor);
        }
        else if(atributo.equals("salario")){
            salvarEstado();
            e.setSalario(Validador.formatar(Validador.validarSalario(valor)));
        }
        else if(atributo.equals("comissao")){
            if(!e.getTipo().equals("comissionado")) throw new EmpregadoNaoComissionadoException();
            salvarEstado();
            ((Comissionado) e).setComissao(Validador.formatar(Validador.validarComissao(valor)));
        }
        else if(atributo.equals("sindicalizado")){
            if (!valor.equals("true") && !valor.equals("false")) throw new ValorDeveSerTrueOuFalseException();

            salvarEstado();
            e.setSindicalizado(valor);

            if (valor.equals("false")) {
                e.setIdSindicato(null);
                e.setTaxaSindical(null);
                return;
            }
        }
        else if(atributo.equals("metodoPagamento")){
            if(!valor.equals("emMaos") && !valor.equals("banco") && !valor.equals("correios")){
                throw new MetodoDePagamentoInvalidoException();
            }
            if(valor.equals("banco")) throw new BancoNaoPodeSerNuloException();

            salvarEstado();
            e.setMetodoPagamento(valor);

            if(valor.equals("emMaos") || valor.equals("correios")){
                e.setBanco(null);
                e.setAgencia(null);
                e.setContaCorrente(null);
            }
        }
        else{
            throw new AtributoNaoExisteException();
        }
    }

    public void alteraEmpregado(String id, String atributo, String valor, String idSindicato,
                                String taxaSindical) throws Exception {
        Empregado empregado = buscar(id);

        if (!atributo.equals("sindicalizado")) throw new AtributoNaoExisteException();
        if (!valor.equals("true") && !valor.equals("false")) throw new ValorDeveSerTrueOuFalseException();

        if (valor.equals("false")) {
            salvarEstado();
            empregado.setSindicalizado("false");
            empregado.setIdSindicato(null);
            empregado.setTaxaSindical(null);
            return;
        }

        if (idSindicato == null || idSindicato.isEmpty()){
            throw new IdentificacaoDoSindicatoNaoPodeSerNulaException();
        }
        if (taxaSindical == null || taxaSindical.isEmpty()) throw new TaxaSindicalNulaException();

        String taxaFormatada = Validador.formatar(Validador.validarTaxaSindical(taxaSindical));

        for (int j = 0; j < listaEmpregados.size(); j++) {
            Empregado outro = listaEmpregados.get(j);

            if (!outro.getId().equals(id) && outro.getSindicalizado().equals("true")
                    && outro.getIdSindicato() != null
                    && outro.getIdSindicato().equals(idSindicato))
                throw new HaOutroEmpregadoComEstaIdentificacaoDeSindicatoException();
        }

        salvarEstado();

        empregado.setSindicalizado("true");
        empregado.setIdSindicato(idSindicato);
        empregado.setTaxaSindical(taxaFormatada);
    }

    public void alteraEmpregado(String id, String atributo, String valor, String banco, String agencia,
                                String contaCorrente) throws Exception{
        Empregado empregado = buscar(id);

        if(!atributo.equals("metodoPagamento")) throw new AtributoNaoExisteException();
        if(!valor.equals("banco") && !valor.equals("emMaos")) throw new MetodoDePagamentoInvalidoException();

        if(valor.equals("emMaos")){
            salvarEstado();
            empregado.setMetodoPagamento("emMaos");
            empregado.setBanco(null);
            empregado.setAgencia(null);
            empregado.setContaCorrente(null);
            return;
        }

        if(banco == null || banco.isEmpty()) throw new BancoNaoPodeSerNuloException();
        if(agencia == null || agencia.isEmpty()) throw new AgenciaNaoPodeSerNuloException();
        if(contaCorrente == null || contaCorrente.isEmpty()) throw new ContaCorrenteNaoPodeSerNuloException();

        salvarEstado();
        empregado.setMetodoPagamento("banco");
        empregado.setBanco(banco);
        empregado.setAgencia(agencia);
        empregado.setContaCorrente(contaCorrente);
    }

    public void mudaTipoEmpregado(String id, String novoTipo, String salario) throws Exception{
        Empregado antigo = buscar(id);

        if(!novoTipo.equals("horista") && !novoTipo.equals("assalariado")) throw new TipoInvalidoException();

        String salarioFormatado;

        if(salario == null){
            salarioFormatado = Validador.formatar(Validador.validarSalario(antigo.getSalario()));
        }
        else{
            salarioFormatado = Validador.formatar(Validador.validarSalario(salario));
        }

        Empregado novo;

        if(novoTipo.equals("horista")){
            novo = new Horista(antigo.getNome(), antigo.getEndereco(), novoTipo, salarioFormatado);
        }
        else if(novoTipo.equals("assalariado")){
            novo = new Assalariado(antigo.getNome(), antigo.getEndereco(), novoTipo, salarioFormatado);
        }
        else{
            throw new TipoInvalidoException();
        }

        copiarDadosComuns(antigo, novo);

        salvarEstado();

        int idx = listaEmpregados.indexOf(antigo);
        listaEmpregados.set(idx, novo);
    }

    public void mudaTipoEmpregado(String id, String novoTipo, String salario, String comissao) throws Exception{
        Empregado antigo = buscar(id);

        if(!novoTipo.equals("comissionado")) throw new TipoInvalidoException();

        String salarioFormatado = Validador.formatar(Validador.validarSalario(antigo.getSalario()));

        String comissaoFormatada = Validador.formatar(Validador.validarComissao(comissao));

        Comissionado novo = new Comissionado(antigo.getNome(), antigo.getEndereco(), novoTipo,
                salarioFormatado, comissaoFormatada);

        copiarDadosComuns(antigo, novo);

        salvarEstado();

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
        novo.setDataUltimoPagamento(antigo.getDataUltimoPagamento());
    }

    public void zerar() throws Exception {
        salvarEstado();
        listaEmpregados.clear();
        contador = 0;
    }

    public void lancaCartao(String id, String data, String horas) throws Exception{
        if(id == null || id.isEmpty()) throw new IdentificacaoDoEmpregadoNaoPodeSerNulaException();

        Empregado empregado = buscar(id);

        if(!(empregado instanceof Horista)) throw new EmpregadoNaoEhHoristaException();

        validarData(data);

        BigDecimal quantidadeHoras;

        try{
            quantidadeHoras = new BigDecimal(horas.replace(",", "."));
        }
        catch (Exception e) {
            throw new HorasDevemSerPositivasException();
        }

        if(quantidadeHoras.compareTo(BigDecimal.ZERO) <= 0) throw new HorasDevemSerPositivasException();

        salvarEstado();

        CartaoDePonto cartao = new CartaoDePonto(data, horas);
        empregado.adicionarCartao(cartao);
    }

    private LocalDate validarData(String data){
        try{
            String[] partes = data.split("/");

            if(partes.length != 3) throw new DataInvalidaException();

            int dia = Integer.parseInt(partes[0]);
            int mes = Integer.parseInt(partes[1]);
            int ano = Integer.parseInt(partes[2]);

            return LocalDate.of(ano, mes, dia);
        }
        catch(Exception e){
            if(e instanceof DataInvalidaException) throw (DataInvalidaException) e;
            throw new DataInvalidaException();
        }
    }

    public void lancaVenda(String id, String data, String valor) throws Exception{
        if(id == null || id.isEmpty()) throw new IdentificacaoDoEmpregadoNaoPodeSerNulaException();

        Empregado empregado = buscar(id);

        if(!(empregado instanceof Comissionado)) throw new EmpregadoNaoComissionadoException();

        validarData(data);

        BigDecimal valorVenda;

        try{
            valorVenda = new BigDecimal(valor.replace(",", "."));
        }
        catch (Exception e) {
            throw new ValorDeveSerPositivoException();
        }

        if(valorVenda.compareTo(BigDecimal.ZERO) <= 0) throw new ValorDeveSerPositivoException();

        salvarEstado();

        Venda venda = new Venda(data, valor);
        empregado.adicionarVenda(venda);
    }

    public String getVendasRealizadas(String id, String dataInicial, String dataFinal) throws Exception{
        if(id == null || id.isEmpty()) throw new IdentificacaoDoEmpregadoNaoPodeSerNulaException();

        Empregado empregado = buscar(id);

        if(!(empregado instanceof Comissionado)) throw new EmpregadoNaoEhComissionadoException();

        LocalDate inicio;
        try{
            inicio = validarData(dataInicial);
        }
        catch (DataInvalidaException e){
            throw new DataInicialInvalidaException();
        }

        LocalDate fim;
        try{
            fim = validarData(dataFinal);
        }
        catch (DataInvalidaException e){
            throw new DataFinalInvalidaException();
        }

        if(inicio.isAfter(fim)) throw new DataInicialNaoPodeSerPosteriorAaDataFinalException();

        BigDecimal total = BigDecimal.ZERO;

        for(int j = 0; j < empregado.getListaVendas().size(); j++){
            Venda venda = empregado.getListaVendas().get(j);

            LocalDate dataVenda = validarData(venda.getData());

            if(!dataVenda.isBefore(inicio) && dataVenda.isBefore(fim)){
                BigDecimal valor = new BigDecimal(venda.getValor().replace(",", "."));

                total = total.add(valor);
            }
        }
        return formatarDinheiro(total);
    }

    private String formatarDinheiro(BigDecimal valor){
        valor = valor.setScale(2, BigDecimal.ROUND_HALF_UP);

        String resultado = valor.toString();

        resultado = resultado.replace(".", ",");

        return resultado;
    }

    public void lancaTaxaServico(String membro, String data, String valor) throws Exception {
        if(membro == null || membro.isEmpty()) throw new IdentificacaoDoMembroNaoPodeSerNulaException();

        Empregado empregado = null;

        for(int j = 0; j < listaEmpregados.size(); j++){
            Empregado atual = listaEmpregados.get(j);

            if(atual.getIdSindicato() != null && atual.getIdSindicato().equals(membro)){
                empregado = atual;
                break;
            }
        }

        if(empregado == null) throw new MembroNaoExisteException();

        validarData(data);

        BigDecimal valorTaxa;

        try{
            valorTaxa = new BigDecimal(valor.replace(",", "."));
        }
        catch (Exception e){
            throw new ValorDeveSerPositivoException();
        }

        if(valorTaxa.compareTo(BigDecimal.ZERO) <= 0) throw new ValorDeveSerPositivoException();

        salvarEstado();

        TaxaServico taxaServico = new TaxaServico(data, valor);

        empregado.adicionarTaxaServico(taxaServico);
    }

    public String getTaxasServico(String id, String dataInicial, String dataFinal) throws Exception{
        if(id == null || id.isEmpty()) throw new IdentificacaoDoEmpregadoNaoPodeSerNulaException();

        Empregado empregado = buscar(id);

        if(!(empregado.getSindicalizado().equals("true"))) throw new EmpregadoNaoEhSindicalizadoException();

        LocalDate inicio;
        try{
            inicio = validarData(dataInicial);
        }
        catch (DataInvalidaException e){
            throw new DataInicialInvalidaException();
        }

        LocalDate fim;
        try{
            fim = validarData(dataFinal);
        }
        catch (DataInvalidaException e){
            throw new DataFinalInvalidaException();
        }

        if(inicio.isAfter(fim)) throw new DataInicialNaoPodeSerPosteriorAaDataFinalException();

        BigDecimal total = BigDecimal.ZERO;

        for(int j = 0; j < empregado.getListaTaxaServico().size(); j++){
            TaxaServico taxaServico = empregado.getListaTaxaServico().get(j);

            LocalDate dataTaxaServico = validarData(taxaServico.getData());

            if(!dataTaxaServico.isBefore(inicio) && dataTaxaServico.isBefore(fim)){
                BigDecimal valorTaxa = new BigDecimal(taxaServico.getValor().replace(",", "."));

                total = total.add(valorTaxa);
            }
        }
        return formatarDinheiro(total);
    }

    public String getHorasNormaisTrabalhadas(String id, String dataInicial, String dataFinal) throws Exception{
        if(id == null || id.isEmpty()) throw new IdentificacaoDoEmpregadoNaoPodeSerNulaException();

        Empregado empregado = buscar(id);

        if(!(empregado instanceof Horista)) throw new EmpregadoNaoEhHoristaException();

        LocalDate inicio;
        try{
            inicio = validarData(dataInicial);
        }
        catch (DataInvalidaException e){
            throw new DataInicialInvalidaException();
        }

        LocalDate fim;
        try{
            fim = validarData(dataFinal);
        }
        catch (DataInvalidaException e){
            throw new DataFinalInvalidaException();
        }

        if(inicio.isAfter(fim)) throw new DataInicialNaoPodeSerPosteriorAaDataFinalException();

        BigDecimal total = BigDecimal.ZERO;

        for(int j = 0; j < empregado.getListaCartoes().size(); j++){
            CartaoDePonto cartaoDePonto = empregado.getListaCartoes().get(j);

            LocalDate dataCartao = validarData(cartaoDePonto.getData());

            if(!dataCartao.isBefore(inicio) && dataCartao.isBefore(fim)){
                BigDecimal horas = new BigDecimal(cartaoDePonto.getHoras().replace(",", "."));

                if(horas.compareTo(new BigDecimal("8")) <= 0) total = total.add(horas);
                else{
                    total = total.add(new BigDecimal("8"));
                }
            }
        }
        return formatarHoras(total);
    }

    public String getHorasExtrasTrabalhadas(String id, String dataInicial, String dataFinal) throws Exception{
        if(id == null || id.isEmpty()) throw new IdentificacaoDoEmpregadoNaoPodeSerNulaException();

        Empregado empregado = buscar(id);

        if(!(empregado instanceof Horista)) throw new EmpregadoNaoEhHoristaException();

        LocalDate inicio;
        try{
            inicio = validarData(dataInicial);
        }
        catch (DataInvalidaException e){
            throw new DataInicialInvalidaException();
        }

        LocalDate fim;
        try{
            fim = validarData(dataFinal);
        }
        catch (DataInvalidaException e){
            throw new DataFinalInvalidaException();
        }

        if(inicio.isAfter(fim)) throw new DataInicialNaoPodeSerPosteriorAaDataFinalException();

        BigDecimal total = BigDecimal.ZERO;

        for(int j = 0; j < empregado.getListaCartoes().size(); j++){
            CartaoDePonto cartaoDePonto = empregado.getListaCartoes().get(j);

            LocalDate dataCartao = validarData(cartaoDePonto.getData());

            if(!dataCartao.isBefore(inicio) && dataCartao.isBefore(fim)){
                BigDecimal horas = new BigDecimal(cartaoDePonto.getHoras().replace(",", "."));

                if(horas.compareTo(new BigDecimal("8")) > 0) {
                    BigDecimal horasExtras = horas.subtract(new BigDecimal("8"));
                    total = total.add(horasExtras);
                }
            }
        }
        return formatarHoras(total);
    }

    private String formatarHoras(BigDecimal valor) {
        valor = valor.stripTrailingZeros();

        return valor.toPlainString().replace(".", ",");
    }

    public boolean deveSerPago(Empregado empregado, String data) {
        LocalDate dataPagamento = validarData(data);

        if (empregado instanceof Horista) {
            if(dataPagamento.getDayOfWeek().getValue() != 5){
                return false;
            }

            LocalDate inicio = dataPagamento.minusDays(6);

            for(int j = 0; j < empregado.getListaCartoes().size(); j++){
                LocalDate dataCartao =
                        validarData(empregado.getListaCartoes().get(j).getData());

                if(!dataCartao.isBefore(inicio) && !dataCartao.isAfter(dataPagamento)){
                    return true;
                }
            }
            return false;
        }

        if (empregado instanceof Comissionado) {
            return devePagarComissionado(dataPagamento);
        }

        if (empregado instanceof Assalariado) {
            return devePagarAssalariado(dataPagamento);
        }
        return false;
    }

    public boolean devePagarAssalariado(LocalDate data) {
        LocalDate ultimoDia = data.withDayOfMonth(data.lengthOfMonth());

        int diaDaSemana = ultimoDia.getDayOfWeek().getValue();

        if (diaDaSemana == 6) {
            ultimoDia = ultimoDia.minusDays(1);
        }
        else if (diaDaSemana == 7) {
            ultimoDia = ultimoDia.minusDays(2);
        }

        return data.equals(ultimoDia);
    }

    private boolean devePagarComissionado(LocalDate data) {
        LocalDate primeiroPagamento = LocalDate.of(2005, 1, 14);

        if(data.isBefore(primeiroPagamento)){
            return false;
        }

        long dias = ChronoUnit.DAYS.between(primeiroPagamento, data);

        return dias % 14 == 0;
    }

    public String totalFolha(String data) throws Exception{
        LocalDate dataPagamento = validarData(data);

        BigDecimal totalFolha = BigDecimal.ZERO;

        for(int j = 0; j < listaEmpregados.size(); j++){
            Empregado empregado = listaEmpregados.get(j);

            if(deveSerPago(empregado, data)){
                BigDecimal pagamento;

                if(empregado instanceof Horista){
                    pagamento = calcularPagamentoHorista(empregado, dataPagamento);
                }
                else if(empregado instanceof Comissionado){
                    pagamento = calcularPagamentoComissionado(empregado, dataPagamento);
                }
                else{
                    pagamento = calcularPagamentoAssalariado(empregado);
                }

                totalFolha = totalFolha.add(pagamento);
            }
        }

        return formatarDinheiro(totalFolha);
    }

    private BigDecimal calcularPagamentoHorista(Empregado empregado, LocalDate dataPagamento){
        BigDecimal salarioHora = new BigDecimal(empregado.getSalario().replace(",", "."));
        BigDecimal horasNormais = BigDecimal.ZERO;
        BigDecimal horasExtras = BigDecimal.ZERO;

        LocalDate inicio = dataPagamento.minusDays(6);

        for(int j = 0; j < empregado.getListaCartoes().size(); j++){
            CartaoDePonto cartao = empregado.getListaCartoes().get(j);
            LocalDate dataCartao = validarData(cartao.getData());

            if(!dataCartao.isBefore(inicio) && !dataCartao.isAfter(dataPagamento)){
                BigDecimal horas = new BigDecimal(cartao.getHoras().replace(",", "."));
                if(horas.compareTo(new BigDecimal("8")) <= 0){
                    horasNormais = horasNormais.add(horas);
                }
                else{
                    horasNormais = horasNormais.add(new BigDecimal("8"));

                    BigDecimal extras = horas.subtract(new BigDecimal("8"));

                    horasExtras = horasExtras.add(extras);
                }
            }
        }

        BigDecimal pagamentoNormal = horasNormais.multiply(salarioHora);
        BigDecimal pagamentoExtra = horasExtras.multiply(salarioHora).multiply(new BigDecimal("1.5"));

        return pagamentoNormal.add(pagamentoExtra);
    }

    private BigDecimal calcularPagamentoAssalariado(Empregado empregado){
        return new BigDecimal(empregado.getSalario().replace(",", "."));
    }

    private BigDecimal calcularPagamentoComissionado(Empregado empregado, LocalDate dataPagamento){
        Comissionado comissionado = (Comissionado) empregado;

        BigDecimal salario = new BigDecimal(comissionado.getSalario().replace(",", "."));
        BigDecimal percentualComissao = new BigDecimal(comissionado.getComissao().replace(",", "."));
        BigDecimal salarioDuasSemanas = salario.multiply(new BigDecimal("12")).divide(new BigDecimal("26"), 10, BigDecimal.ROUND_DOWN);

        salarioDuasSemanas = salarioDuasSemanas.setScale(2, BigDecimal.ROUND_DOWN);

        LocalDate inicio = dataPagamento.minusDays(13);

        BigDecimal totalVendas = BigDecimal.ZERO;

        for(int j = 0; j < comissionado.getListaVendas().size(); j++){
            Venda venda = comissionado.getListaVendas().get(j);

            LocalDate dataVenda = validarData(venda.getData());

            if(!dataVenda.isBefore(inicio) && !dataVenda.isAfter(dataPagamento)){
                BigDecimal valorVenda = new BigDecimal(venda.getValor().replace(",", "."));

                totalVendas = totalVendas.add(valorVenda);
            }
        }

        BigDecimal comissao = totalVendas.multiply(percentualComissao).setScale(2, BigDecimal.ROUND_DOWN);

        return salarioDuasSemanas.add(comissao);
    }

    private BigDecimal aplicarDescontosSindicais(Empregado empregado, BigDecimal pagamento,
                                                 LocalDate dataPagamento){
        if(!empregado.getSindicalizado().equals("true")){
            return pagamento;
        }

        LocalDate inicio;

        if(empregado instanceof Horista){
            inicio = dataPagamento.minusDays(6);
        }
        else if(empregado instanceof Comissionado){
            inicio = dataPagamento.minusDays(13);
        }
        else{
            inicio = dataPagamento.withDayOfMonth(1);
        }

        if(empregado.getTaxaSindical() != null){
            BigDecimal taxaDiaria = new BigDecimal(empregado.getTaxaSindical().replace(",", "."));

            long quantidadeDias = ChronoUnit.DAYS.between(inicio, dataPagamento) + 1;

            BigDecimal taxaSindical = taxaDiaria.multiply(BigDecimal.valueOf(quantidadeDias));

            pagamento = pagamento.subtract(taxaSindical);
        }
        return pagamento;
    }

    private BigDecimal calcularDescontos(Empregado empregado, LocalDate dataPagamento){
        BigDecimal descontos = BigDecimal.ZERO;

        if(empregado.getSindicalizado().equals("true") && empregado.getTaxaSindical() != null){

            LocalDate inicio;

            if(empregado instanceof Horista){
                inicio = dataPagamento.minusDays(6);
            }
            else if(empregado instanceof Comissionado){
                inicio = dataPagamento.minusDays(13);
            }
            else{
                inicio = dataPagamento.withDayOfMonth(1);
            }

            long dias = ChronoUnit.DAYS.between(inicio, dataPagamento) + 1;

            BigDecimal taxa = new BigDecimal(empregado.getTaxaSindical().replace(",", "."));

            descontos = descontos.add(taxa.multiply(BigDecimal.valueOf(dias)));
        }

        LocalDate inicioPeriodo;

        if(empregado instanceof Horista){
            inicioPeriodo = dataPagamento.minusDays(6);
        }
        else if(empregado instanceof Comissionado){
            inicioPeriodo = dataPagamento.minusDays(13);
        }
        else{
            inicioPeriodo = dataPagamento.withDayOfMonth(1);
        }

        for(int j = 0; j < empregado.getListaTaxaServico().size(); j++){
            TaxaServico taxaServico = empregado.getListaTaxaServico().get(j);

            LocalDate dataTaxa = validarData(taxaServico.getData());

            if(!dataTaxa.isBefore(inicioPeriodo) && dataTaxa.isBefore(dataPagamento)){
                BigDecimal valor = new BigDecimal(taxaServico.getValor().replace(",", "."));

                descontos = descontos.add(valor);
            }
        }

        return descontos;
    }

    private String formatarMetodoPagamento(Empregado empregado){
        if(empregado.getMetodoPagamento().equals("emMaos")){
            return "Em maos";
        }

        if(empregado.getMetodoPagamento().equals("correios")){
            return "Correios, " + empregado.getEndereco();
        }

        if(empregado.getMetodoPagamento().equals("banco")){
            return "Banco do Brasil, Ag. " + empregado.getAgencia() + " CC " + empregado.getContaCorrente();
        }

        return "";
    }

    public void rodaFolha(String data, String saida) throws Exception  {
        LocalDate dataPagamento = validarData(data);

        salvarEstado();

        List<Empregado> ordenados = new ArrayList<>(listaEmpregados);
        ordenados.sort((a, b) -> a.getNome().compareTo(b.getNome()));

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(saida))) {
            writer.write("FOLHA DE PAGAMENTO DO DIA " + dataPagamento);
            writer.newLine();

            writer.write("====================================");
            writer.newLine();
            writer.newLine();

            writer.write("===============================================================================================================================");
            writer.newLine();
            writer.write("===================== HORISTAS ================================================================================================");
            writer.newLine();
            writer.write("===============================================================================================================================");
            writer.newLine();

            writer.write("Nome                                 Horas Extra Salario Bruto Descontos Salario Liquido Metodo");
            writer.newLine();

            writer.write("==================================== ===== ===== ============= ========= =============== ======================================");
            writer.newLine();

            int totalHorasNormais = 0;
            int totalHorasExtras = 0;
            BigDecimal totalBrutoHoristas = BigDecimal.ZERO;
            BigDecimal totalDescontosHoristas = BigDecimal.ZERO;
            BigDecimal totalLiquidoHoristas = BigDecimal.ZERO;

            if (dataPagamento.getDayOfWeek().getValue() == 5) {
                for (int j = 0; j < ordenados.size(); j++) {
                    Empregado empregado = ordenados.get(j);

                    if (empregado instanceof Horista) {
                        int horasNormais = 0;
                        int horasExtras = 0;

                        LocalDate inicio = dataPagamento.minusDays(6);
                        BigDecimal salarioHora = new BigDecimal(empregado.getSalario().replace(",", "."));

                        for (int k = 0; k < empregado.getListaCartoes().size(); k++) {
                            CartaoDePonto cartao = empregado.getListaCartoes().get(k);
                            LocalDate dataCartao = validarData(cartao.getData());

                            if (!dataCartao.isBefore(inicio) && !dataCartao.isAfter(dataPagamento)) {
                                BigDecimal horas = new BigDecimal(cartao.getHoras().replace(",",
                                        "."));
                                if (horas.compareTo(new BigDecimal("8")) <= 0) {
                                    horasNormais += horas.intValue();
                                } else {
                                    horasNormais += 8;
                                    horasExtras += horas.subtract(new BigDecimal("8")).intValue();
                                }
                            }
                        }

                        BigDecimal pagamentoNormal = salarioHora.multiply(BigDecimal.valueOf(horasNormais));
                        BigDecimal pagamentoExtra = salarioHora.multiply(BigDecimal.valueOf(horasExtras))
                                .multiply(new BigDecimal("1.5"));
                        BigDecimal bruto = pagamentoNormal.add(pagamentoExtra);

                        BigDecimal taxasServicoPeriodo = BigDecimal.ZERO;
                        for (int k = 0; k < empregado.getListaTaxaServico().size(); k++) {
                            TaxaServico ts = empregado.getListaTaxaServico().get(k);
                            LocalDate dataTaxa = validarData(ts.getData());
                            if (!dataTaxa.isBefore(inicio) && dataTaxa.isBefore(dataPagamento)) {
                                taxasServicoPeriodo = taxasServicoPeriodo.add(new BigDecimal(ts.getValor()
                                        .replace(",", ".")));
                            }
                        }

                        BigDecimal devidoSemana = BigDecimal.ZERO;
                        if (empregado.getSindicalizado().equals("true") && empregado.getTaxaSindical() != null) {
                            BigDecimal taxaDiaria = new BigDecimal(empregado.getTaxaSindical().replace(","
                                    , "."));
                            devidoSemana = taxaDiaria.multiply(BigDecimal.valueOf(7));
                        }

                        BigDecimal dividaAnterior = empregado.getDescontosPendentes() == null ? BigDecimal.ZERO :
                                empregado.getDescontosPendentes();
                        BigDecimal totalDevido = devidoSemana.add(dividaAnterior).add(taxasServicoPeriodo);

                        BigDecimal descontos;
                        BigDecimal liquido;

                        if (bruto.compareTo(totalDevido) >= 0) {
                            descontos = totalDevido;
                            liquido = bruto.subtract(descontos);
                            empregado.setDescontosPendentes(BigDecimal.ZERO);
                        } else {
                            descontos = bruto;
                            liquido = BigDecimal.ZERO;
                            empregado.setDescontosPendentes(totalDevido.subtract(bruto));
                        }

                        totalHorasNormais += horasNormais;
                        totalHorasExtras += horasExtras;
                        totalBrutoHoristas = totalBrutoHoristas.add(bruto);
                        totalDescontosHoristas = totalDescontosHoristas.add(descontos);
                        totalLiquidoHoristas = totalLiquidoHoristas.add(liquido);

                        String metodo = formatarMetodoPagamento(empregado);

                        writer.write(String.format(Locale.US, "%-36s %5d %5d %13s %9s %15s %-38s",
                                empregado.getNome(), horasNormais, horasExtras,
                                formatarDinheiro(bruto), formatarDinheiro(descontos), formatarDinheiro(liquido),
                                metodo));
                        writer.newLine();
                    }
                }
            }

            writer.newLine();

            writer.write(String.format(Locale.US, "TOTAL HORISTAS %27d %5d %13s %9s %15s", totalHorasNormais
                    , totalHorasExtras, formatarDinheiro(totalBrutoHoristas), formatarDinheiro(totalDescontosHoristas), formatarDinheiro(totalLiquidoHoristas)));
            writer.newLine();
            writer.newLine();

            writer.write("===============================================================================================================================");
            writer.newLine();
            writer.write("===================== ASSALARIADOS ============================================================================================");
            writer.newLine();
            writer.write("===============================================================================================================================");
            writer.newLine();

            writer.write("Nome                                             Salario Bruto Descontos Salario Liquido Metodo");
            writer.newLine();

            writer.write("================================================ ============= ========= =============== ======================================");
            writer.newLine();

            BigDecimal totalBrutoAssalariados = BigDecimal.ZERO;
            BigDecimal totalDescontosAssalariados = BigDecimal.ZERO;
            BigDecimal totalLiquidoAssalariados = BigDecimal.ZERO;

            for(int j = 0; j < ordenados.size(); j++){
                Empregado empregado = ordenados.get(j);

                if(empregado instanceof Assalariado && deveSerPago(empregado, data)){
                    BigDecimal bruto = calcularPagamentoAssalariado(empregado);
                    BigDecimal descontos = calcularDescontos(empregado, dataPagamento);
                    BigDecimal liquido = bruto.subtract(descontos);

                    totalBrutoAssalariados = totalBrutoAssalariados.add(bruto);
                    totalDescontosAssalariados = totalDescontosAssalariados.add(descontos);
                    totalLiquidoAssalariados = totalLiquidoAssalariados.add(liquido);

                    writer.write(String.format(Locale.US, "%-48s %13s %9s %15s %-38s", empregado.getNome(),
                            formatarDinheiro(bruto), formatarDinheiro(descontos), formatarDinheiro(liquido), formatarMetodoPagamento(empregado)));

                    writer.newLine();
                }
            }

            writer.newLine();

            writer.write(String.format(Locale.US, "TOTAL ASSALARIADOS %43s %9s %15s",
                    formatarDinheiro(totalBrutoAssalariados), formatarDinheiro(totalDescontosAssalariados),
                    formatarDinheiro(totalLiquidoAssalariados)));            writer.newLine();
            writer.newLine();

            writer.write("===============================================================================================================================");
            writer.newLine();
            writer.write("===================== COMISSIONADOS ===========================================================================================");
            writer.newLine();
            writer.write("===============================================================================================================================");
            writer.newLine();

            writer.write("Nome                  Fixo     Vendas   Comissao Salario Bruto Descontos Salario Liquido Metodo");
            writer.newLine();

            writer.write("===================== ======== ======== ======== ============= ========= =============== ======================================");
            writer.newLine();

            BigDecimal totalFixo = BigDecimal.ZERO;
            BigDecimal totalVendas = BigDecimal.ZERO;
            BigDecimal totalComissao = BigDecimal.ZERO;
            BigDecimal totalBrutoComissionados = BigDecimal.ZERO;
            BigDecimal totalDescontosComissionados = BigDecimal.ZERO;
            BigDecimal totalLiquidoComissionados = BigDecimal.ZERO;

            for(int j = 0; j < ordenados.size(); j++){
                Empregado empregado = ordenados.get(j);

                if(empregado instanceof Comissionado && deveSerPago(empregado, data)){
                    Comissionado comissionado = (Comissionado) empregado;

                    BigDecimal fixo = new BigDecimal(empregado.getSalario().replace(",", "."))
                            .multiply(new BigDecimal("12")).divide(new BigDecimal("26"), 10,
                                    BigDecimal.ROUND_DOWN);

                    fixo = fixo.setScale(2, BigDecimal.ROUND_DOWN);

                    LocalDate inicio = dataPagamento.minusDays(13);

                    BigDecimal vendas = BigDecimal.ZERO;

                    for(int k = 0; k < empregado.getListaVendas().size(); k++){
                        Venda venda = empregado.getListaVendas().get(k);
                        LocalDate dataVenda = validarData(venda.getData());

                        if(!dataVenda.isBefore(inicio) && !dataVenda.isAfter(dataPagamento)){
                            vendas = vendas.add(new BigDecimal(venda.getValor().replace(",", ".")));
                        }
                    }

                    BigDecimal percentual = new BigDecimal(comissionado.getComissao().replace(",",
                            "."));
                    BigDecimal comissao = vendas.multiply(percentual).setScale(2, BigDecimal.ROUND_DOWN);
                    BigDecimal bruto = fixo.add(comissao);
                    BigDecimal descontos = calcularDescontos(empregado, dataPagamento);
                    BigDecimal liquido = bruto.subtract(descontos);

                    totalFixo = totalFixo.add(fixo);
                    totalVendas = totalVendas.add(vendas);
                    totalComissao = totalComissao.add(comissao);
                    totalBrutoComissionados = totalBrutoComissionados.add(bruto);
                    totalDescontosComissionados = totalDescontosComissionados.add(descontos);
                    totalLiquidoComissionados = totalLiquidoComissionados.add(liquido);

                    writer.write(String.format(Locale.US, "%-21s %8s %8s %8s %13s %9s %15s %-38s",
                            empregado.getNome(), formatarDinheiro(fixo), formatarDinheiro(vendas),
                            formatarDinheiro(comissao), formatarDinheiro(bruto), formatarDinheiro(descontos),
                            formatarDinheiro(liquido), formatarMetodoPagamento(empregado)));

                    writer.newLine();
                }
            }

            writer.newLine();

            writer.write(String.format(Locale.US, "TOTAL COMISSIONADOS %10s %8s %8s %13s %9s %15s",
                    formatarDinheiro(totalFixo), formatarDinheiro(totalVendas), formatarDinheiro(totalComissao),
                    formatarDinheiro(totalBrutoComissionados), formatarDinheiro(totalDescontosComissionados),
                    formatarDinheiro(totalLiquidoComissionados)));

            writer.newLine();
            writer.newLine();

            BigDecimal totalFolha = totalBrutoHoristas.add(totalBrutoAssalariados).add(totalBrutoComissionados);

            writer.write("TOTAL FOLHA: " + formatarDinheiro(totalFolha));

            writer.newLine();
        }
    }

    public void undo() throws Exception {
        if (pilhaUndo.empty()) throw new NaoHaComandoADesfazerException();

        EstadoBancoDados estadoAtual = new EstadoBancoDados(listaEmpregados, contador);

        pilhaRedo.push(estadoAtual);

        EstadoBancoDados estadoAnterior = pilhaUndo.pop();

        listaEmpregados.clear();
        listaEmpregados.addAll(estadoAnterior.getEmpregados());

        contador = estadoAnterior.getContador();
    }

    public void redo() throws Exception {
        if (pilhaRedo.empty()) throw new NaoHaComandoARefazerException();

        EstadoBancoDados estadoAtual = new EstadoBancoDados(listaEmpregados, contador);

        pilhaUndo.push(estadoAtual);

        EstadoBancoDados proximoEstado = pilhaRedo.pop();

        listaEmpregados.clear();
        listaEmpregados.addAll(proximoEstado.getEmpregados());

        contador = proximoEstado.getContador();
    }

    public int getNumeroDeEmpregados(){
        return listaEmpregados.size();
    }
}
