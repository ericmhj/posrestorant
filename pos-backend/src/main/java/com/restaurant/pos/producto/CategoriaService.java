package com.restaurant.pos.producto;

import com.restaurant.pos.common.BusinessException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class CategoriaService {

    private static final Logger LOG = Logger.getLogger(CategoriaService.class);

    public List<CategoriaDTO> findAll() {
        return Categoria.findAllOrdered().stream()
                .map(CategoriaDTO::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public CategoriaDTO create(CreateCategoriaRequest request) {
        Categoria cat = new Categoria();
        cat.nombre = request.nombre;
        cat.descripcion = request.descripcion;
        cat.persist();
        LOG.infof("Categoria created: nombre=%s", cat.nombre);
        return CategoriaDTO.from(cat);
    }

    @Transactional
    public CategoriaDTO update(UUID id, CreateCategoriaRequest request) {
        Categoria cat = findOrThrow(id);
        if (request.nombre != null) cat.nombre = request.nombre;
        if (request.descripcion != null) cat.descripcion = request.descripcion;
        return CategoriaDTO.from(cat);
    }

    @Transactional
    public void delete(UUID id) {
        Categoria cat = findOrThrow(id);
        long count = Categoria.countProductosByCategoriaId(id);
        if (count > 0) {
            throw new BusinessException(409,
                    "No se puede eliminar la categoría porque tiene " + count + " producto(s) asociado(s)");
        }
        cat.delete();
        LOG.infof("Categoria deleted: id=%s nombre=%s", id, cat.nombre);
    }

    private Categoria findOrThrow(UUID id) {
        return Categoria.findByIdOptional(id)
                .map(c -> (Categoria) c)
                .orElseThrow(() -> new BusinessException(404, "Categoría no encontrada: " + id));
    }
}
