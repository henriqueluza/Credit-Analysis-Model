package com.creditanalysis.creditapi.domain.port;

import com.creditanalysis.creditapi.domain.model.Usuario;

public interface GeradorToken {

    TokenAcesso gerar(Usuario usuario);
}
