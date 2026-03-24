package br.com.indra.guilherme_antonio_silva.service;

import br.com.indra.guilherme_antonio_silva.model.Categoria;
import br.com.indra.guilherme_antonio_silva.repository.CategoriaRepository;
import br.com.indra.guilherme_antonio_silva.dto.CategoriaRequestDTO;
import br.com.indra.guilherme_antonio_silva.dto.CategoriaResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> listarTodas() {
        return categoriaRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional
    public CategoriaResponseDTO criar(CategoriaRequestDTO dto) {
        validarNomeObrigatorio(dto.getNome());
        validarDuplicidade(dto.getNome(), dto.getCategoriaPaiId(), null);

        Categoria categoria = new Categoria();
        categoria.setNome(dto.getNome());

        if (dto.getCategoriaPaiId() != null) {
            Categoria pai = categoriaRepository.findById(dto.getCategoriaPaiId())
                    .orElseThrow(() -> new IllegalArgumentException("Categoria pai não encontrada"));
            categoria.setCategoriaPai(pai);
        }

        Categoria salva = categoriaRepository.save(categoria);
        return toResponseDTO(salva);
    }

    @Transactional
    public CategoriaResponseDTO atualizar(Long id, CategoriaRequestDTO dto) {
        validarNomeObrigatorio(dto.getNome());

        Categoria existente = categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada"));

        Long novoPaiId = dto.getCategoriaPaiId();

        if (novoPaiId != null && novoPaiId.equals(id)) {
            throw new IllegalArgumentException("Categoria não pode ser pai de si mesma");
        }

        validarDuplicidade(dto.getNome(), novoPaiId, id);

        existente.setNome(dto.getNome());

        if (novoPaiId != null) {
            Categoria pai = categoriaRepository.findById(novoPaiId)
                    .orElseThrow(() -> new IllegalArgumentException("Categoria pai não encontrada"));
            existente.setCategoriaPai(pai);
        } else {
            existente.setCategoriaPai(null);
        }

        Categoria salva = categoriaRepository.save(existente);
        return toResponseDTO(salva);
    }

    @Transactional
    public void deletar(Long id) {
        Categoria existente = categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada"));

        if (!existente.getProdutos().isEmpty()) {
            throw new IllegalStateException("Não é possível excluir categoria com produtos associados");
        }

        categoriaRepository.delete(existente);
    }

    private void validarNomeObrigatorio(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome da categoria é obrigatório");
        }
    }

    private void validarDuplicidade(String nome, Long categoriaPaiId, Long idAtual) {
        if (categoriaPaiId == null) {
            categoriaRepository.findByNomeAndCategoriaPaiIsNull(nome)
                    .ifPresent(existente -> {
                        if (idAtual == null || !existente.getId().equals(idAtual)) {
                            throw new IllegalStateException("Já existe categoria raiz com esse nome");
                        }
                    });
        } else {
            categoriaRepository.findByNomeAndCategoriaPaiId(nome, categoriaPaiId)
                    .ifPresent(existente -> {
                        if (idAtual == null || !existente.getId().equals(idAtual)) {
                            throw new IllegalStateException("Já existe categoria com esse nome nesse nível");
                        }
                    });
        }
    }

    private CategoriaResponseDTO toResponseDTO(Categoria categoria) {
        Long paiId = categoria.getCategoriaPai() != null ? categoria.getCategoriaPai().getId() : null;
        return CategoriaResponseDTO.builder()
                .id(categoria.getId())
                .nome(categoria.getNome())
                .categoriaPaiId(paiId)
                .build();
    }
}

