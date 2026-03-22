package br.com.indra.guilherme_antonio_silva;

import br.com.indra.guilherme_antonio_silva.model.Categoria;
import br.com.indra.guilherme_antonio_silva.repository.CategoriaRepository;
import br.com.indra.guilherme_antonio_silva.service.CategoriaService;
import br.com.indra.guilherme_antonio_silva.service.dto.CategoriaRequestDTO;
import br.com.indra.guilherme_antonio_silva.service.dto.CategoriaResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CategoriaServiceTest {

    private CategoriaRepository categoriaRepository;
    private CategoriaService categoriaService;

    @BeforeEach
    void setUp() {
        categoriaRepository = mock(CategoriaRepository.class);
        categoriaService = new CategoriaService(categoriaRepository);
    }

    @Test
    @DisplayName("Deve criar categoria raiz com nome obrigatório e sem duplicidade")
    void deveCriarCategoriaRaiz() {
        CategoriaRequestDTO request = CategoriaRequestDTO.builder()
                .nome("Eletrônicos")
                .build();

        when(categoriaRepository.findByNomeAndCategoriaPaiIsNull("Eletrônicos"))
                .thenReturn(Optional.empty());

        Categoria categoriaSalva = new Categoria();
        categoriaSalva.setId(1L);
        categoriaSalva.setNome("Eletrônicos");

        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoriaSalva);

        CategoriaResponseDTO response = categoriaService.criar(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Eletrônicos", response.getNome());
        assertNull(response.getCategoriaPaiId());

        ArgumentCaptor<Categoria> captor = ArgumentCaptor.forClass(Categoria.class);
        verify(categoriaRepository).save(captor.capture());
        assertEquals("Eletrônicos", captor.getValue().getNome());
        assertNull(captor.getValue().getCategoriaPai());
    }

    @Test
    @DisplayName("Não deve permitir criar categoria raiz com nome duplicado")
    void naoDeveCriarCategoriaRaizDuplicada() {
        CategoriaRequestDTO request = CategoriaRequestDTO.builder()
                .nome("Eletrônicos")
                .build();

        Categoria existente = new Categoria();
        existente.setId(1L);
        existente.setNome("Eletrônicos");

        when(categoriaRepository.findByNomeAndCategoriaPaiIsNull("Eletrônicos"))
                .thenReturn(Optional.of(existente));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> categoriaService.criar(request));

        assertTrue(ex.getMessage().toLowerCase().contains("categoria raiz"));
        verify(categoriaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Não deve permitir atualizar categoria sendo pai de si mesma")
    void naoDevePermitirCategoriaPaiDeSiMesma() {
        CategoriaRequestDTO request = CategoriaRequestDTO.builder()
                .nome("Eletrônicos")
                .categoriaPaiId(1L)
                .build();

        Categoria existente = new Categoria();
        existente.setId(1L);
        existente.setNome("Eletrônicos");

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(existente));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> categoriaService.atualizar(1L, request));

        assertTrue(ex.getMessage().toLowerCase().contains("pai de si mesma"));
    }
}

