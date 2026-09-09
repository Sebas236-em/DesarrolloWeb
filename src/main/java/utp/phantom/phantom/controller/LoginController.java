package utp.phantom.phantom.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import utp.phantom.phantom.model.Usuario;
import utp.phantom.phantom.repository.UsuarioRepository;

import java.util.Optional;

@Controller
public class LoginController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String error) {
        if (error != null) {
            return "redirect:/?loginError=true";
        }
        return "redirect:/";
    }

    @PostMapping("/login")
    public String procesarLogin(@RequestParam String username,
                                @RequestParam String password,
                                HttpSession session) {

        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(username);

        if (usuarioOpt.isEmpty() || !passwordEncoder.matches(password, usuarioOpt.get().getPassword())) {
            return "redirect:/?loginError=true";
        }

        session.setAttribute("usuarioActual", usuarioOpt.get());
        return "redirect:/";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
