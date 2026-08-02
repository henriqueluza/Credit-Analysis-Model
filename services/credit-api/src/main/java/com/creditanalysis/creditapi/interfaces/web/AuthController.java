package com.creditanalysis.creditapi.interfaces.web;

import com.creditanalysis.creditapi.application.AutenticarUsuarioUseCase;
import com.creditanalysis.creditapi.application.ComandoAutenticar;
import com.creditanalysis.creditapi.application.ComandoRegistrarUsuario;
import com.creditanalysis.creditapi.application.RegistrarUsuarioUseCase;
import com.creditanalysis.creditapi.domain.model.Usuario;
import com.creditanalysis.creditapi.domain.port.TokenAcesso;
import com.creditanalysis.creditapi.interfaces.web.dto.LoginRequest;
import com.creditanalysis.creditapi.interfaces.web.dto.RegistrarUsuarioRequest;
import com.creditanalysis.creditapi.interfaces.web.dto.TokenResponse;
import com.creditanalysis.creditapi.interfaces.web.dto.UsuarioResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final RegistrarUsuarioUseCase registrarUsuarioUseCase;
    private final AutenticarUsuarioUseCase autenticarUsuarioUseCase;

    public AuthController(RegistrarUsuarioUseCase registrarUsuarioUseCase, AutenticarUsuarioUseCase autenticarUsuarioUseCase) {
        this.registrarUsuarioUseCase = registrarUsuarioUseCase;
        this.autenticarUsuarioUseCase = autenticarUsuarioUseCase;
    }

    @PostMapping("/register")
    public ResponseEntity<UsuarioResponse> registrar(@Valid @RequestBody RegistrarUsuarioRequest request) {
        Usuario usuario = registrarUsuarioUseCase.executar(
                new ComandoRegistrarUsuario(request.nome(), request.email(), request.senha())
        );
        UsuarioResponse response = new UsuarioResponse(usuario.id(), usuario.nome(), usuario.email(), usuario.role().name());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        TokenAcesso token = autenticarUsuarioUseCase.executar(new ComandoAutenticar(request.email(), request.senha()));
        return new TokenResponse(token.token(), "Bearer", token.expiraEm());
    }
}
