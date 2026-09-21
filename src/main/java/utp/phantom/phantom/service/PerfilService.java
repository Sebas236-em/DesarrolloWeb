package utp.phantom.phantom.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import utp.phantom.phantom.model.Perfil;
import utp.phantom.phantom.model.Usuario;
import utp.phantom.phantom.repository.PerfilRepository;
import utp.phantom.phantom.repository.UsuarioRepository;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class PerfilService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PerfilRepository perfilRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    public Usuario obtenerUsuarioPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + email));
    }


    public Perfil obtenerOCrearPerfil(Usuario usuario) {
        return perfilRepository.findByUsuario(usuario).orElseGet(() -> {
            Perfil nuevo = new Perfil();
            nuevo.setUsuario(usuario);
            nuevo.setBiografia("");
            nuevo.setAvatarUrl("");
            return perfilRepository.save(nuevo);
        });
    }

    public void actualizarAvatar(Usuario usuario, MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new RuntimeException("Debes seleccionar una imagen");
        }

        Perfil perfil = obtenerOCrearPerfil(usuario);

        try {
            String extension = "";
            String nombreOriginal = archivo.getOriginalFilename();
            if (nombreOriginal != null && nombreOriginal.contains(".")) {
                extension = nombreOriginal.substring(nombreOriginal.lastIndexOf("."));
            }

            String nombreArchivo = "avatar_" + usuario.getId() + "_" + System.currentTimeMillis() + extension;

            Path carpetaDestino = Paths.get("uploads/avatars");
            Files.createDirectories(carpetaDestino);

            Path rutaCompleta = carpetaDestino.resolve(nombreArchivo);
            Files.copy(archivo.getInputStream(), rutaCompleta, StandardCopyOption.REPLACE_EXISTING);

            perfil.setAvatarUrl("/uploads/avatars/" + nombreArchivo);
            perfilRepository.save(perfil);

        } catch (IOException e) {
            throw new RuntimeException("Error al guardar la imagen: " + e.getMessage());
        }
    }

    public void eliminarAvatar(Usuario usuario) {
        Perfil perfil = obtenerOCrearPerfil(usuario);

        if (perfil.getAvatarUrl() != null && !perfil.getAvatarUrl().isEmpty()) {
            try {
                Path rutaArchivo = Paths.get(perfil.getAvatarUrl().replaceFirst("^/", ""));
                Files.deleteIfExists(rutaArchivo);
            } catch (IOException e) {
                // si falla el borrado físico no es crítico, seguimos igual
            }
        }

        perfil.setAvatarUrl("");
        perfilRepository.save(perfil);
    }


    @Transactional
    public Usuario actualizarDatosPersonales(String email, String nombre,
                                             String direccion, String telefono,
                                             String biografia) {
        Usuario usuario = obtenerUsuarioPorEmail(email);
        usuario.setNombre(nombre);
        usuario.setDireccion(direccion);
        usuario.setNumeroTelefono(telefono);
        usuarioRepository.save(usuario);

        Perfil perfil = obtenerOCrearPerfil(usuario);
        perfil.setBiografia(biografia);
        perfilRepository.save(perfil);

        return usuario;
    }


    @Transactional
    public String cambiarPassword(String email, String passwordActual,
                                  String passwordNuevo, String passwordConfirm) {
        Usuario usuario = obtenerUsuarioPorEmail(email);

        if (!passwordEncoder.matches(passwordActual, usuario.getPassword())) {
            return "La contraseña actual es incorrecta";
        }
        if (!passwordNuevo.equals(passwordConfirm)) {
            return "Las contraseñas nuevas no coinciden";
        }
        if (passwordNuevo.length() < 8) {
            return "La nueva contraseña debe tener al menos 8 caracteres";
        }

        usuario.setPassword(passwordEncoder.encode(passwordNuevo));
        usuarioRepository.save(usuario);
        return null;
    }
}
