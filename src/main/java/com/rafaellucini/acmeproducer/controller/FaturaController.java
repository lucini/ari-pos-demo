package com.rafaellucini.acmeproducer.controller;


import com.rafaellucini.acmeproducer.domain.Cliente;
import com.rafaellucini.acmeproducer.domain.Fatura;
import com.rafaellucini.acmeproducer.domain.Instalacao;
import com.rafaellucini.acmeproducer.exception.RecursoNotFoundException;
import com.rafaellucini.acmeproducer.repository.ClienteRepository;
import com.rafaellucini.acmeproducer.repository.FaturaRepository;
import com.rafaellucini.acmeproducer.repository.InstalacaoRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@Api(value = "Acme AP Fatura Service", produces = MediaType.APPLICATION_JSON_VALUE)
public class FaturaController {

	@Autowired
	private FaturaRepository faturaRepository;

	@Autowired
	private InstalacaoRepository instalacaoRepository;

	@Autowired
	private ClienteRepository clienteRepository;

	@ApiOperation(value = "Mostra a lista de faturas")
	// Controle de versão explicito na URI
	@GetMapping("v1/faturas")
	public List<Fatura> getAllFaturas() {

		ArrayList<Fatura> listaFaturas = new ArrayList<Fatura>();
		try {
			listaFaturas = (ArrayList<Fatura>) faturaRepository.findAll();
		} catch (Exception e) {
			// TODO: handle exception
			throw new RecursoNotFoundException("Erro ao recuperar faturas");
		}
		

		return listaFaturas;
	}

	@ApiOperation(value = "Consulta uma fatura pelo código")
	@GetMapping("v1/faturas/{codigo}")
	public Optional<Fatura> getFatura(@PathVariable String codigo) {

		Optional<Fatura> fatura = null;
		
		try {
			fatura = faturaRepository.findByCodigo(codigo);
			if (fatura.get() == null)
				throw new RecursoNotFoundException ("codigo de fatura inválido - " + codigo);
		} catch (Exception e) {
			// TODO: handle exception
			throw new RecursoNotFoundException ("codigo de fatura inválido - " + codigo);
		}
		
		
		return fatura;
	}

	@ApiOperation(value = "Consulta as faturas pelo CPF do cliente")
	@GetMapping("v1/faturas/cpf/{cpf}")
	public List<Fatura> getFaturasPorCPF(@PathVariable String cpf) {
		
		Optional<Cliente> cliente = null;
		List<Instalacao> listaInstalacao;
		
		try {
			cliente = clienteRepository.findByCpf(cpf);
			if (cliente.get() == null)
				throw new RecursoNotFoundException ("CPF - " + cpf);
			
			listaInstalacao = instalacaoRepository.findByCliente(cliente.get());
		} catch (Exception e) {
			// TODO: handle exception
			throw new RecursoNotFoundException ("CPF inválido - " + cpf);
		}
		

		List<Fatura> listaFaturasCliente = new ArrayList<Fatura>();

		listaInstalacao
				.forEach(item -> listaFaturasCliente.addAll(item.getListaFatura()));

		return listaFaturasCliente;
	}

	@ApiOperation(value = "Gerar uma nova fatura")
	@PostMapping("v1/faturas")
	@ResponseStatus(HttpStatus.CREATED)
	public ResponseEntity<Object> gerarFatura(@RequestBody Fatura fatura) {
		
		Optional<Instalacao> instalacaoRecuperada;
		URI location = null;
		
		try {
			
			instalacaoRecuperada = instalacaoRepository.findByCodigo(fatura.getInstalacao().getCodigo());
			if (instalacaoRecuperada.get() == null)
				throw new RecursoNotFoundException ("codigo instalacao - " + fatura.getInstalacao().getCodigo());
			
			fatura.setInstalacao(instalacaoRecuperada.get());
			
			Fatura faturaCriada = faturaRepository.save(fatura);
			location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
					.buildAndExpand(faturaCriada.getId()).toUri();
			
		} catch (Exception e) {
			// TODO: handle exception
			throw new RecursoNotFoundException ("Erro ao gerar fatura para a instalacao - " + fatura.getInstalacao().getCodigo());
		}
	
				
		
		return ResponseEntity.created(location).build();
	}

}
