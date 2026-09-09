package utp.phantom.phantom.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import utp.phantom.phantom.model.Usuario;

@Controller
public class HomeController {

    @Value("${google.maps.api-key}")
    private String mapsApiKey;

    private void agregarUsuarioAutenticado(Model model, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioActual");
        if (usuario != null) {
            model.addAttribute("usuarioNombre", usuario.getNombre().split(" ")[0]);
        }
    }

    @GetMapping("/")
    public String index(@RequestParam(required = false) String loginError,
                        Model model, HttpSession session) {
        if (loginError != null) {
            model.addAttribute("loginError", true);
        }
        agregarUsuarioAutenticado(model, session);
        return "index";
    }

    @GetMapping("/nosotros")
    public String nosotros(Model model, HttpSession session) {
        agregarUsuarioAutenticado(model, session);
        model.addAttribute("mapsApiKey", mapsApiKey);
        return "nosotros";
    }

    @GetMapping("/mision")
    public String mision(Model model, HttpSession session) {
        agregarUsuarioAutenticado(model, session);
        return "mision";
    }
}
