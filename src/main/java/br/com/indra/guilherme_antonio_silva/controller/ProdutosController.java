package br.com.indra.guilherme_antonio_silva.controller;

import br.com.indra.guilherme_antonio_silva.model.Produtos;
import br.com.indra.guilherme_antonio_silva.service.ProdutosService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Produtos", description = "Endpoints para gerenciamento de produtos")
@RequestMapping("/produtos")
public class ProdutosController {

    private final ProdutosService produtosService;

    //C

    /**
     * Recomendação de desenvolvimento, ampliar responses(responseEntity)
     * possíveis além do ok.
     */
    @Operation(description = "Endpoint para criar um novo produto",
            summary = "Criação de produto")
    @PostMapping("/cria")
    public ResponseEntity<Produtos> criarProduto(@RequestBody Produtos produto){
        return ResponseEntity.ok(produtosService.criarProduto(produto));
    }

    /**
     * GET
     * localhost:9090/produtos
     * @return
     */
    @GetMapping
    public ResponseEntity<List<Produtos>> getAll(){

        return ResponseEntity.ok(produtosService.listarProdutos());
    }

    /**
     * GET
     * localhost:9090/produtos/1
     * @return
     */
    @GetMapping("/{id}")
    public ResponseEntity<Produtos> getById(@PathVariable Long id){
        return ResponseEntity.ok(produtosService.buscarProdutoPorId(id));
    }

    //U
    @PutMapping("/atualiza")
    public ResponseEntity<Produtos> atualizarProduto(@RequestParam Long id,
                                                     @RequestBody Produtos produto){
        return ResponseEntity.ok(produtosService.atualizarProdutoPorId(id, produto));
    }

    @PatchMapping("/atualiza-preco/{id}")
    public ResponseEntity<Produtos> atualizarProdutoParcial(@PathVariable Long id,
                                                            @RequestParam BigDecimal preco) {
        return ResponseEntity.ok(produtosService.atualizarPrecoProduto(id, preco));
    }

    //Mudar para delete lógico
    @DeleteMapping("/deleta/{id}")
    public ResponseEntity<Void> deletarProduto(@PathVariable Long id) {
        produtosService.removerProdutoPorId(id);
        return ResponseEntity.noContent().build();
    }
}
