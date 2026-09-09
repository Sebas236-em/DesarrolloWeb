package utp.phantom.phantom.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import utp.phantom.phantom.model.Perfil;
import utp.phantom.phantom.model.Usuario;
import utp.phantom.phantom.service.PerfilService;

@Controller
@RequestMapping("/perfil")
public class PerfilController {

    @Autowired
    private PerfilService perfilService;

    private Usuario getUsuarioAutenticado(HttpSession session) {
        return (Usuario) session.getAttribute("usuarioActual");
    }

    private void agregarAtributosComunes(Model model, Usuario usuario) {
        model.addAttribute("usuarioNombre", usuario.getNombre().split(" ")[0]);
    }

    @GetMapping
    public String verPerfil(Model model, HttpSession session) {
        Usuario usuario = getUsuarioAutenticado(session);
        if (usuario == null) return "redirect:/login";

        Perfil perfil = perfilService.obtenerOCrearPerfil(usuario);

        model.addAttribute("usuario", usuario);
        model.addAttribute("perfil", perfil);
        agregarAtributosComunes(model, usuario);

        return "perfil";
    }

    @PostMapping("/actualizar")
    public String actualizarDatos(
            @RequestParam String nombre,
            @RequestParam(required = false) String direccion,
            @RequestParam(required = false) String telefono,
            @RequestParam(required = false) String biografia,
            HttpSession session) {

        Usuario usuario = getUsuarioAutenticado(session);
        if (usuario == null) return "redirect:/login";

        Usuario actualizado = perfilService.actualizarDatosPersonales(
                usuario.getEmail(), nombre, direccion, telefono, biografia);
        session.setAttribute("usuarioActual", actualizado);

        return "redirect:/perfil?actualizado=true";
    }

    @PostMapping("/cambiar-password")
    public String cambiarPassword(
            @RequestParam String passwordActual,
            @RequestParam String passwordNuevo,
            @RequestParam String passwordConfirm,
            HttpSession session) {

        Usuario usuario = getUsuarioAutenticado(session);
        if (usuario == null) return "redirect:/login";

        String error = perfilService.cambiarPassword(usuario.getEmail(), passwordActual, passwordNuevo, passwordConfirm);

        if (error != null) {
            return "redirect:/perfil?errorPassword=" + error;
        }

        return "redirect:/perfil?passwordCambiado=true";
    }
}
