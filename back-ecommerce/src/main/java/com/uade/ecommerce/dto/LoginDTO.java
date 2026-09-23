package com.uade.ecommerce.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginDTO {

    @NotBlank(message = "El mail es obligatorio")
    @Email(message = "El formato del mail es inválido")
    private String mail;

    @NotBlank(message = "La contraseña es obligatoria")
    private String contrasenia;
}
