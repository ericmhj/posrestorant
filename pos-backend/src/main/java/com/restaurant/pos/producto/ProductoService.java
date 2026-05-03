package com.restaurant.pos.producto;

import com.restaurant.pos.common.BusinessException;
import com.restaurant.pos.common.PaginationParams;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class ProductoService {

    private static final Logger LOG = Logger.getLogger(ProductoService.class);

    @Inject
    ImageStorageService imageStorageService;

    public List<ProductoDTO> findAll(PaginationParams pagination, boolean soloActivos) {
        List<Producto> productos = soloActivos
                ? Producto.findActivos(pagination.getPage(), pagination.getSize())
                : Producto.findAll(io.quarkus.panache.common.Sort.by("nombre"))
                          .page(pagination.getPage(), pagination.getSize())
                          .list();
        return productos.stream().map(ProductoDTO::from).collect(Collectors.toList());
    }

    public ProductoDTO findById(UUID id) {
        return ProductoDTO.from(findOrThrow(id));
    }

    @Transactional
    public ProductoDTO create(CreateProductoRequest request) {
        Categoria categoria = Categoria.findByIdOptional(request.categoriaId)
                .map(c -> (Categoria) c)
                .orElseThrow(() -> new BusinessException(404,
                        "Categoría no encontrada: " + request.categoriaId));

        Producto producto = new Producto();
        producto.nombre = request.nombre;
        producto.descripcion = request.descripcion;
        producto.precio = request.precio;
        producto.categoria = categoria;
        producto.estacion = request.estacion;
        producto.activo = true;
        producto.persist();

        LOG.infof("Producto created: nombre=%s estacion=%s", producto.nombre, producto.estacion);
        return ProductoDTO.from(producto);
    }

    @Transactional
    public ProductoDTO update(UUID id, UpdateProductoRequest request) {
        Producto producto = findOrThrow(id);

        if (request.nombre != null) producto.nombre = request.nombre;
        if (request.descripcion != null) producto.descripcion = request.descripcion;
        if (request.precio != null) producto.precio = request.precio; // only affects new orders
        if (request.estacion != null) producto.estacion = request.estacion;
        if (request.categoriaId != null) {
            Categoria cat = Categoria.findByIdOptional(request.categoriaId)
                    .map(c -> (Categoria) c)
                    .orElseThrow(() -> new BusinessException(404,
                            "Categoría no encontrada: " + request.categoriaId));
            producto.categoria = cat;
        }

        LOG.infof("Producto updated: id=%s", id);
        return ProductoDTO.from(producto);
    }

    @Transactional
    public void deactivate(UUID id) {
        Producto producto = findOrThrow(id);
        producto.activo = false;
        LOG.infof("Producto deactivated: id=%s nombre=%s", id, producto.nombre);
    }

    @Transactional
    public ProductoDTO uploadImagen(UUID id, InputStream imageStream,
                                     String contentType, long fileSize,
                                     String originalFilename) {
        Producto producto = findOrThrow(id);

        // Delete old image if exists
        if (producto.imagenUrl != null) {
            imageStorageService.delete(producto.imagenUrl);
        }

        String url = imageStorageService.save(imageStream, originalFilename, contentType, fileSize);
        producto.imagenUrl = url;

        LOG.infof("Image uploaded for producto: id=%s url=%s", id, url);
        return ProductoDTO.from(producto);
    }

    @Transactional
    public ProductoDTO deleteImagen(UUID id) {
        Producto producto = findOrThrow(id);

        if (producto.imagenUrl != null) {
            imageStorageService.delete(producto.imagenUrl);
            producto.imagenUrl = null;
        }

        LOG.infof("Image deleted for producto: id=%s", id);
        return ProductoDTO.from(producto);
    }

    private Producto findOrThrow(UUID id) {
        return Producto.findByIdOptional(id)
                .map(p -> (Producto) p)
                .orElseThrow(() -> new BusinessException(404, "Producto no encontrado: " + id));
    }
}
