package com.example.registropruebacliente.Controller;

import com.example.registropruebacliente.ML.LoginRequest;
import com.example.registropruebacliente.ML.Result;
import com.example.registropruebacliente.ML.Usuario;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

@Controller
@RequestMapping("/usuario")
public class UsuarioController {

    @Autowired
    private RestTemplate restTemplate;

    public static final String baseUrl = "http://localhost:8080";

//    @GetMapping("/")
//    public String Registrar() {
//        return "redirect:/registro";
//    }
    @GetMapping("/login")
    public String mostrarLogin(HttpSession session, Model model) {
        Usuario usuarioSesion = (Usuario) session.getAttribute("usuario");
        if (usuarioSesion != null) {
            return "redirect:/usuario/lista";
        }

        model.addAttribute("loginRequest", new LoginRequest());
        return "Login";
    }

    @PostMapping("/login")
    public String procesarLogin(@ModelAttribute LoginRequest loginRequest, HttpSession session, Model model) {
        try {
            String url = baseUrl + "/api/usuario/login";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<LoginRequest> request = new HttpEntity<>(loginRequest, headers);

            ResponseEntity<Result> response = restTemplate.postForEntity(url, request, Result.class);
            Result result = response.getBody();

            if (result != null && result.correct) {
                Map<String, Object> userData = (Map<String, Object>) result.data;

                Usuario usuario = new Usuario();
                usuario.setIdUsuario((Integer) userData.get("idUsuario"));
                usuario.setUsername((String) userData.get("username"));
                usuario.setNombre((String) userData.get("nombre"));
                usuario.setApellidoPaterno((String) userData.get("apellidoPaterno"));
                usuario.setApellidoMaterno((String) userData.get("apellidoMaterno"));
                usuario.setCorreo((String) userData.get("correo"));
                usuario.setTelefono((String) userData.get("telefono"));
                usuario.setIsVerified((Integer) userData.get("isVerified"));

                session.setAttribute("usuario", usuario);


                return "redirect:/usuario/lista";
            } else {
                model.addAttribute("error", result != null ? result.errorMessage : "Error desconocido");
                model.addAttribute("loginRequest", loginRequest);
                return "Login";
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            model.addAttribute("error", "Error al conectar con el servidor: " + ex.getMessage());
            model.addAttribute("loginRequest", loginRequest);
            return "Login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/usuario/login";
    }

    @GetMapping("/lista")
    public String ListaUsuarios(Model model) {
        try {
            String url = baseUrl + "/api/usuario/lista";

            ResponseEntity<List<Usuario>> response = restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<Usuario>>() {
            });

            List<Usuario> usuarios = response.getBody();

            model.addAttribute("usuarios", usuarios != null ? usuarios : new ArrayList<>());
            model.addAttribute("titulo", "Lista de Usuarios");
            return "MostrarUsuarios";

        } catch (Exception ex) {
            ex.printStackTrace();
            model.addAttribute("error", "Error al mostrar usuarios" + ex.getLocalizedMessage());
            model.addAttribute("usuarios", new ArrayList<>());
            model.addAttribute("titulo", "Lista de Usuarios");
            return "MostrarUsuarios";
        }
    }

    @GetMapping("/registro")
    public String RegistrarForm(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("titulo", "Registro de Usuario");

        return "RegistrarUsuario";
    }

    @PostMapping("/registro")
    public String RegistrarUsuario(@ModelAttribute Usuario usuario, Model model) {
        try {
            String url = baseUrl + "/api/usuario/registrar";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Usuario> request = new HttpEntity<>(usuario, headers);

            ResponseEntity<Result> response = restTemplate.postForEntity(url, request, Result.class);
            Result result = response.getBody();
            if (result != null && result.correct) {
                model.addAttribute("mensaje", result.object);
                model.addAttribute("tipo", "success");
                model.addAttribute("correo", usuario.getCorreo());
                model.addAttribute("titulo", "Registro Exitoso");
                return "RegistroExitoso";
            } else {
                model.addAttribute("error", result != null ? result.errorMessage : "Error");
                model.addAttribute("usuario", usuario);
                model.addAttribute("titulo", "Registro de Usuario");
                return "RegistrarUsuario";
            }
        } catch (Exception ex) {
            model.addAttribute("error", "Error al registrar" + ex.getLocalizedMessage());
            model.addAttribute("usuario", usuario);
            model.addAttribute("titulo", "Registro de Usuario");
            return "RegistrarUsuario";
        }
    }

}
